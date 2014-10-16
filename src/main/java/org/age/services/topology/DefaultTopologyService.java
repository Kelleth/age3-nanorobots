/*
 * Created: 2014-08-21
 * $Id$
 */

package org.age.services.topology;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptySet;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import org.age.services.discovery.DiscoveryEvent;
import org.age.services.discovery.HazelcastDiscoveryService;
import org.age.services.identity.NodeIdentity;
import org.age.services.identity.NodeIdentityService;
import org.age.util.fsm.FSM;
import org.age.util.fsm.StateMachineService;
import org.age.util.fsm.StateMachineServiceBuilder;

import static org.age.services.topology.TopologyMessage.Type.TOPOLOGY_SELECTED;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;

@Named
public class DefaultTopologyService implements SmartLifecycle {

	/**
	 * States of the topology service.
	 */
	public enum State {
		OFFLINE,
		STARTING,
		MASTER_ELECTED_MASTER,
		MASTER_ELECTED_SLAVE,
		WITH_TOPOLOGY,
		// Only in this state we can communicate
		FAILED,
		TERMINATED
	}
	/**
	 * Events that can occur in the service.
	 */
	public enum Event {
		START,
		MEMBERSHIP_CHANGED,
		TOPOLOGY_TYPE_CHANGED,
		TOPOLOGY_SELECTED,
		ERROR,
		STOP
	}
	private static final Logger log = LoggerFactory.getLogger(DefaultTopologyService.class);
	private final ListeningScheduledExecutorService executorService = listeningDecorator(
			newSingleThreadScheduledExecutor());
	private StateMachineService<State, Event> service;
	@Inject private HazelcastInstance hazelcastInstance;
	@Inject private HazelcastDiscoveryService discoveryService;
	@Inject private NodeIdentityService identityService;
	@Inject private EventBus eventBus;
	@Inject private List<TopologyProcessor> topologyProcessors;
	private ListenableFuture<?> future;
	@MonotonicNonNull private TopologyProcessor currentTopologyProcessor;
	@MonotonicNonNull private IMap<String, Object> runtimeConfig;
	@MonotonicNonNull private ITopic<TopologyMessage> topic;
	@MonotonicNonNull private ICountDownLatch latch;
	private boolean master;
	@Nullable private String listenerKey;
	@Nullable private DirectedGraph<String, DefaultEdge> cachedTopology;

	@PostConstruct
	public void construct() {
		//@formatter:off
		service = StateMachineServiceBuilder
			.withStatesAndEvents(State.class, Event.class)
			.withName("topology")
			.startWith(State.OFFLINE)
			.terminateIn(State.TERMINATED)

			.in(State.OFFLINE)
				.on(Event.START).execute(this::internalStart).goTo(State.STARTING)
				.on(Event.MEMBERSHIP_CHANGED).goTo(State.OFFLINE)
				.commit()

			.in(State.STARTING)
				.on(Event.MEMBERSHIP_CHANGED).execute(this::electMaster).goTo(State.MASTER_ELECTED_MASTER, State.MASTER_ELECTED_SLAVE)
				.commit()

			.in(State.MASTER_ELECTED_MASTER)
				.on(Event.MEMBERSHIP_CHANGED).execute(this::electMaster).goTo(State.MASTER_ELECTED_MASTER, State.MASTER_ELECTED_SLAVE)
				.on(Event.TOPOLOGY_TYPE_CHANGED).execute(this::topologyChanged).goTo(State.WITH_TOPOLOGY)
				.commit()

			.in(State.MASTER_ELECTED_SLAVE)
				.on(Event.MEMBERSHIP_CHANGED).execute(this::electMaster).goTo(State.MASTER_ELECTED_MASTER, State.MASTER_ELECTED_SLAVE)
				.on(Event.TOPOLOGY_TYPE_CHANGED).goTo(State.MASTER_ELECTED_SLAVE)
				.on(Event.TOPOLOGY_SELECTED).execute(this::topologySelected).goTo(State.WITH_TOPOLOGY)
				.commit()

			.in(State.WITH_TOPOLOGY)
				.on(Event.MEMBERSHIP_CHANGED).execute(this::electMaster).goTo(State.MASTER_ELECTED_MASTER, State.MASTER_ELECTED_SLAVE)
				.on(Event.TOPOLOGY_TYPE_CHANGED).goTo(State.WITH_TOPOLOGY)
				.on(Event.TOPOLOGY_SELECTED).goTo(State.WITH_TOPOLOGY)
				.commit()

			.inAnyState()
				.on(Event.STOP).execute(this::internalStop).goTo(State.TERMINATED)
				.on(Event.ERROR).execute(this::handleError).goTo(State.FAILED)
				.commit()

			.ifFailed()
				.fire(Event.ERROR)

			.withEventBus(eventBus)
			//.notifyWithType(LifecycleStateChangedEvent.class)
			.shutdownWhenTerminated().build();
		//@formatter:on

		// Obtain dependencies
		runtimeConfig = hazelcastInstance.getMap("topology/config");
		latch = hazelcastInstance.getCountDownLatch("topology/agreement");
		topic = hazelcastInstance.getTopic("topology/channel");

		topic.addMessageListener(new DistributedMessageListener());
		eventBus.register(this);
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(final Runnable callback) {
		stop();
		callback.run();
	}

	@Override
	public void start() {
		service.fire(Event.START);
	}

	@Override
	public void stop() {
		service.fire(Event.STOP);
	}

	@Override
	public boolean isRunning() {
		return service.running();
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@NonNull
	public Set<NodeIdentity> getNeighbours() {
		if (!service.inState(State.WITH_TOPOLOGY)) {
			return emptySet();
		}

		return null;
	}

	private void internalStart(@NonNull final FSM<State, Event> fsm) {
		log.debug("Topology service starting.");

		// Select initial topology
		log.debug("Known topologies: {}.", topologyProcessors);

		log.info("Topology service started.");
	}

	private void internalStop(@NonNull final FSM<State, Event> fsm) {
		log.debug("Topology service stopping.");

		shutdownAndAwaitTermination(executorService, 10, TimeUnit.SECONDS);

		log.info("Topology service stopped.");
	}

	private void handleError(@NonNull final FSM<State, Event> fsm) {

	}

	/**
	 * Simple master selection. We rely on Hazelcast, so we just need to perform local, deterministic selection.
	 *
	 * In this case we selects the node with the largest nodeId.
	 */
	private void electMaster(@NonNull final FSM<State, Event> fsm) {
		log.debug("Locally selecting master.");

		final Set<@NonNull NodeIdentity> computeNodes = getComputeNodes();
		final Optional<NodeIdentity> maxIdentity = computeNodes.parallelStream()
		                                                       .max(Comparator.comparing(NodeIdentity::getId));
		log.debug("Max identity is {}.", maxIdentity);

		assert maxIdentity.isPresent();

		if (identityService.getNodeId().equals(maxIdentity.get().getId())) {
			log.debug("I am master.");
			master = true;
			runtimeConfig.put(ConfigKeys.MASTER, identityService.getNodeId());

			// Select initial topology type
			if (!runtimeConfig.containsKey(ConfigKeys.TOPOLOGY_TYPE)) {
				final Optional<TopologyProcessor> selectedProcessor = topologyProcessors.parallelStream()
				                                                                        .max(Comparator.comparing(
						                                                                        TopologyProcessor::getPriority));
				if (!selectedProcessor.isPresent()) {
					throw new RuntimeException("No TopologyProcessor."); // XXX: Exception type
				}
				currentTopologyProcessor = selectedProcessor.get();
				log.debug("Selected initial topology: {}.", currentTopologyProcessor);
				runtimeConfig.put(ConfigKeys.TOPOLOGY_TYPE, currentTopologyProcessor.getName());
			}
			listenerKey = runtimeConfig.addEntryListener(new TopologyTypeChangeListener(), ConfigKeys.TOPOLOGY_TYPE,
			                                             true);

			service.fire(Event.TOPOLOGY_TYPE_CHANGED);
			fsm.goTo(State.MASTER_ELECTED_MASTER);

		} else {
			log.debug("I am slave.");
			master = false;
			if (listenerKey != null) {
				runtimeConfig.removeEntryListener(listenerKey);
			}

			fsm.goTo(State.MASTER_ELECTED_SLAVE);
		}
	}

	@NonNull
	private Set<@NonNull NodeIdentity> getComputeNodes() {
		return discoveryService.getMembers("type = 'compute'");
	}

	private void topologyChanged(final FSM<State, Event> stateEventFSM) {
		assert master;

		final Set<@NonNull NodeIdentity> computeNodes = getComputeNodes();
		log.debug("Topology initialization.");
		final DirectedGraph<String, DefaultEdge> topologyGraph = currentTopologyProcessor.getGraph(computeNodes);
		log.debug("Topology: {}.", topologyGraph);
		runtimeConfig.put(ConfigKeys.TOPOLOGY, topologyGraph);
		topic.publish(new TopologyMessage(TOPOLOGY_SELECTED));
	}

	private void topologySelected(final FSM<State, Event> stateEventFSM) {
		cachedTopology = (DirectedGraph<String, DefaultEdge>)runtimeConfig.get(ConfigKeys.TOPOLOGY);
	}

	@NonNull
	public Optional<String> getMasterId() {
		return Optional.ofNullable((String)runtimeConfig.get(ConfigKeys.MASTER));
	}

	@NonNull
	public Optional<DirectedGraph<String, DefaultEdge>> getTopology() {
		return Optional.ofNullable(cachedTopology);
	}

	@NonNull
	public Optional<String> getTopologyType() {
		return Optional.ofNullable((String)runtimeConfig.get(ConfigKeys.TOPOLOGY_TYPE));
	}

	@Subscribe
	public void membershipChange(final DiscoveryEvent event) {
		log.debug("Membership change: {}.", event);
		service.fire(Event.MEMBERSHIP_CHANGED);
	}

	private static class ConfigKeys {
		public static final String MASTER = "master";
		public static final String TOPOLOGY = "topology";
		public static final String TOPOLOGY_TYPE = "topologyType";
	}

	private class TopologyTypeChangeListener extends EntryAdapter<String, Object> {
		@Override
		public void entryUpdated(final EntryEvent<String, Object> event) {
			log.info("Topology type updated: {}.", event);
			service.fire(Event.TOPOLOGY_TYPE_CHANGED);
		}
	}

	private class DistributedMessageListener implements MessageListener<TopologyMessage> {
		@Override
		public void onMessage(final Message<TopologyMessage> message) {
			log.debug("Distributed event: {}", message);
			final TopologyMessage topologyMessage = message.getMessageObject();
			switch (topologyMessage.getType()) {
				case TOPOLOGY_SELECTED:
					service.fire(Event.TOPOLOGY_SELECTED);
					break;
			}
		}
	}
}
