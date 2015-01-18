/*
 * Copyright (C) 2014 Intelligent Information Systems Group.
 *
 * This file is part of AgE.
 *
 * AgE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AgE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AgE.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Created: 2014-08-21.
 */

package org.age.services.topology.internal;

import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.age.services.topology.TopologyMessage.Type.MASTER_ELECTED;
import static org.age.services.topology.TopologyMessage.Type.TOPOLOGY_SELECTED;

import org.age.services.discovery.DiscoveryEvent;
import org.age.services.discovery.DiscoveryService;
import org.age.services.identity.NodeDescriptor;
import org.age.services.identity.NodeIdentityService;
import org.age.services.lifecycle.NodeDestroyedEvent;
import org.age.services.topology.TopologyMessage;
import org.age.services.topology.TopologyService;
import org.age.services.topology.processors.TopologyProcessor;
import org.age.util.fsm.FSM;
import org.age.util.fsm.StateMachineService;
import org.age.util.fsm.StateMachineServiceBuilder;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Named("default")
public final class DefaultTopologyService implements SmartLifecycle, TopologyService {

	public static final String CONFIG_MAP_NAME = "topology/config";

	public static final String CHANNEL_NAME = "topology/channel";

	/**
	 * States of the topology service.
	 */
	private enum State {
		OFFLINE,
		STARTING,
		MASTER_ELECTED_MASTER,
		MASTER_ELECTED_SLAVE,
		// Only in this state we can communicate
		WITH_TOPOLOGY,
		FAILED,
		TERMINATED
	}

	/**
	 * Events that can occur in the service.
	 */
	private enum Event {
		START,
		STARTED,
		MEMBERSHIP_CHANGED,
		TOPOLOGY_TYPE_CHANGED,
		TOPOLOGY_CONFIGURED,
		ERROR,
		STOP
	}

	private static final Logger log = LoggerFactory.getLogger(DefaultTopologyService.class);

	private final ListeningScheduledExecutorService executorService = listeningDecorator(
			newSingleThreadScheduledExecutor());

	@Inject @MonotonicNonNull private HazelcastInstance hazelcastInstance;

	@Inject private @MonotonicNonNull DiscoveryService discoveryService;

	@Inject @MonotonicNonNull private NodeIdentityService identityService;

	@Inject @MonotonicNonNull private EventBus eventBus;

	@Inject @MonotonicNonNull private List<TopologyProcessor> topologyProcessors;

	@MonotonicNonNull private TopologyProcessor currentTopologyProcessor;

	@MonotonicNonNull private IMap<String, Object> runtimeConfig;

	@MonotonicNonNull private ITopic<TopologyMessage> topic;

	@MonotonicNonNull private ICountDownLatch latch;

	@MonotonicNonNull private StateMachineService<State, Event> service;

	private boolean master;

	@Nullable private String listenerKey;

	@Nullable private DirectedGraph<String, DefaultEdge> cachedTopology;

	@PostConstruct private void construct() {
		log.debug("Constructing DefaultTopologyService.");
		//@formatter:off
		service = StateMachineServiceBuilder
			.withStatesAndEvents(State.class, Event.class)
			.withName("topology")
			.startWith(State.OFFLINE)
			.terminateIn(State.TERMINATED, State.FAILED)

			.in(State.OFFLINE)
				.on(Event.START).execute(this::internalStart).goTo(State.STARTING)
				.on(Event.MEMBERSHIP_CHANGED).goTo(State.OFFLINE) // Ignore event
				.commit()

			.in(State.STARTING)
				.on(Event.STARTED).execute(this::electMaster).goTo(State.MASTER_ELECTED_MASTER, State.MASTER_ELECTED_SLAVE)
				.on(Event.MEMBERSHIP_CHANGED).goTo(State.STARTING)
				.commit()

			.in(State.MASTER_ELECTED_MASTER)
				.on(Event.MEMBERSHIP_CHANGED).execute(this::electMaster).goTo(State.MASTER_ELECTED_MASTER, State.MASTER_ELECTED_SLAVE)
				.on(Event.TOPOLOGY_TYPE_CHANGED).execute(this::topologyChanged).goTo(State.WITH_TOPOLOGY)
				.commit()

			.in(State.MASTER_ELECTED_SLAVE)
				.on(Event.MEMBERSHIP_CHANGED).execute(this::electMaster).goTo(State.MASTER_ELECTED_MASTER, State.MASTER_ELECTED_SLAVE)
				.on(Event.TOPOLOGY_TYPE_CHANGED).goTo(State.MASTER_ELECTED_SLAVE)
				.on(Event.TOPOLOGY_CONFIGURED).execute(this::topologyConfigured).goTo(State.WITH_TOPOLOGY)
				.commit()

			.in(State.WITH_TOPOLOGY)
				.on(Event.MEMBERSHIP_CHANGED).execute(this::electMaster).goTo(State.MASTER_ELECTED_MASTER, State.MASTER_ELECTED_SLAVE)
				.on(Event.TOPOLOGY_TYPE_CHANGED).execute(this::topologyChanged).goTo(State.WITH_TOPOLOGY)
				.on(Event.TOPOLOGY_CONFIGURED).execute(this::topologyConfigured).goTo(State.WITH_TOPOLOGY)
				.commit()

			.inAnyState()
				.on(Event.STOP).execute(this::internalStop).goTo(State.TERMINATED)
				.on(Event.ERROR).execute(this::handleError).goTo(State.FAILED)
				.commit()

			.ifFailed()
				.fireAndCall(Event.ERROR, new ExceptionHandler())

			.withEventBus(eventBus)
			.build();
		//@formatter:on

		// Obtain dependencies
		runtimeConfig = hazelcastInstance.getMap(CONFIG_MAP_NAME);
		topic = hazelcastInstance.getTopic(CHANNEL_NAME);
	}

	@Override public boolean isAutoStartup() {
		return identityService.isCompute();
	}

	@Override public void stop(final Runnable callback) {
		stop();
		callback.run();
	}

	@Override public void start() {
		service.fire(Event.START);
	}

	@Override public void stop() {
		service.fire(Event.STOP);
	}

	@Override public boolean isRunning() {
		return service.isRunning();
	}

	@Override public int getPhase() {
		return 0;
	}

	private void internalStart(@NonNull final FSM<State, Event> fsm) {
		log.debug("Topology service starting.");
		log.debug("Known topologies: {}.", topologyProcessors);
		assert !topologyProcessors.isEmpty() : "No topology processors.";
		assert identityService.isCompute() : "This implementation is only for compute nodes.";

		topic.addMessageListener(new DistributedMessageListener());
		eventBus.register(this);
		
		log.info("Topology service started.");
		service.fire(Event.STARTED);
	}

	private void internalStop(@NonNull final FSM<State, Event> fsm) {
		log.debug("Topology service stopping.");
		shutdownAndAwaitTermination(executorService, 10, TimeUnit.SECONDS);
		log.info("Topology service stopped.");
	}

	private void handleError(@NonNull final FSM<State, Event> fsm) {

	}

	// Topology methods

	/**
	 * Simple master selection. We rely on Hazelcast, so we just need to perform local, deterministic selection.
	 *
	 * In this case we selects the node with the largest nodeId.
	 */
	private void electMaster(@NonNull final FSM<State, Event> fsm) {
		log.debug("Locally selecting master.");

		final Set<@NonNull NodeDescriptor> computeNodes = computeNodes();
		final Optional<NodeDescriptor> maxIdentity = computeNodes.parallelStream()
		                                                       .max(Comparator.comparing(NodeDescriptor::id));
		log.debug("Max identity is {}.", maxIdentity);

		assert maxIdentity.isPresent();

		if (identityService.nodeId().equals(maxIdentity.get().id())) {
			log.debug("I am master.");
			master = true;
			runtimeConfig.put(ConfigKeys.MASTER, identityService.nodeId());

			// Select initial topology type if this is the first election
			if (!runtimeConfig.containsKey(ConfigKeys.TOPOLOGY_TYPE)) {
				log.debug("Seems to be the first election. Selecting topology.");
				final Optional<TopologyProcessor> selectedProcessor = topologyProcessors.parallelStream()
				                                                                        .max(Comparator.comparing(
						                                                                        TopologyProcessor::priority));
				assert selectedProcessor.isPresent();

				currentTopologyProcessor = selectedProcessor.get();
				log.debug("Selected initial topology: {}.", currentTopologyProcessor);
				runtimeConfig.put(ConfigKeys.TOPOLOGY_TYPE, currentTopologyProcessor.name());
			}
			listenerKey = runtimeConfig.addEntryListener(new TopologyTypeChangeListener(), ConfigKeys.TOPOLOGY_TYPE,
			                                             true);

			service.fire(Event.TOPOLOGY_TYPE_CHANGED);
			fsm.goTo(State.MASTER_ELECTED_MASTER);
			topic.publish(TopologyMessage.createWithoutPayload(MASTER_ELECTED));

		} else {
			log.debug("I am slave.");
			master = false;
			if (listenerKey != null) {
				runtimeConfig.removeEntryListener(listenerKey);
			}

			fsm.goTo(State.MASTER_ELECTED_SLAVE);
		}
	}

	/**
	 * Called when topology has changed:
	 * <ul>
	 * <li>new member</li>
	 * <li>member removal</li>
	 * <li>type changed</li>
	 * </ul>
	 *
	 * Executed only on master.
	 */
	private void topologyChanged(final FSM<State, Event> stateEventFSM) {
		assert master;
		log.debug("Topology initialization.");

		final String processorName = topologyType().get();
		log.debug("Processor name: {}.", processorName);

		final Optional<TopologyProcessor> topologyProcessor = getTopologyProcessorWithName(processorName);
		assert topologyProcessor.isPresent();
		currentTopologyProcessor = topologyProcessor.get();

		final Set<NodeDescriptor> computeNodes = computeNodes();
		cachedTopology = currentTopologyProcessor.createGraphFrom(computeNodes);
		log.debug("Topology: {}.", cachedTopology);
		runtimeConfig.put(ConfigKeys.TOPOLOGY_GRAPH, cachedTopology);
		topic.publish(TopologyMessage.createWithoutPayload(TOPOLOGY_SELECTED));
	}

	/**
	 * Called on all nodes when the topology has been configured by master.
	 */
	private void topologyConfigured(final FSM<State, Event> stateEventFSM) {
		assert !master || (currentTopologyProcessor != null) : "Current topology processor null for master";
		assert runtimeConfig.get(ConfigKeys.TOPOLOGY_GRAPH) != null : "No topology graph in config";

		log.debug("Topology has been configured. Caching the graph.");
		cachedTopology = getCurrentTopologyGraph();
	}

	@NonNull private Optional<TopologyProcessor> getTopologyProcessorWithName(@NonNull final String processorName) {
		assert processorName != null;
		return topologyProcessors.parallelStream()
		                         .filter(processor -> processor.name().equals(processorName))
		                         .findFirst();
	}

	@Nullable private DirectedGraph<String, DefaultEdge> getCurrentTopologyGraph() {
		return (DirectedGraph<String, DefaultEdge>)runtimeConfig.get(ConfigKeys.TOPOLOGY_GRAPH);
	}


	@Override @NonNull public Optional<String> masterId() {
		return Optional.ofNullable((String)runtimeConfig.get(ConfigKeys.MASTER));
	}

	@Override public boolean isLocalNodeMaster() {
		final Optional<String> masterId = masterId();
		return masterId.isPresent() && masterId.get().equals(identityService.nodeId());
	}

	@Override public boolean hasTopology() {
		return service.isInState(State.WITH_TOPOLOGY);
	}

	@Override @NonNull public Optional<DirectedGraph<String, DefaultEdge>> topologyGraph() {
		return Optional.ofNullable(cachedTopology);
	}

	@Override @NonNull public Optional<String> topologyType() {
		return Optional.ofNullable((String)runtimeConfig.get(ConfigKeys.TOPOLOGY_TYPE));
	}

	@Override @NonNull public Set<String> neighbours() {
		if (!hasTopology()) {
			throw new IllegalStateException("Topology not ready.");
		}

		final DirectedGraph<String, DefaultEdge> graph = getCurrentTopologyGraph();
		final Set<DefaultEdge> outEdges = graph.outgoingEdgesOf(identityService.nodeId());
		return outEdges.stream().map(graph::getEdgeTarget).collect(Collectors.toSet());
	}

	@Subscribe public void membershipChange(final DiscoveryEvent event) {
		log.debug("Membership change: {}.", event);
		service.fire(Event.MEMBERSHIP_CHANGED);
	}

	@NonNull protected Set<@NonNull NodeDescriptor> computeNodes() {
		return discoveryService.membersMatching("type = 'compute'");
	}

	private static class ConfigKeys {
		public static final String MASTER = "master";

		public static final String TOPOLOGY_GRAPH = "topologyGraph";

		public static final String TOPOLOGY_TYPE = "topologyType";
	}

	@Subscribe public void handleNodeDestroyedEvent(final @NonNull NodeDestroyedEvent event) {
		log.debug("Got event: {}.", event);
		service.fire(Event.STOP);
	}

	private class TopologyTypeChangeListener extends EntryAdapter<String, Object> {
		@Override public void entryUpdated(final EntryEvent<String, Object> event) {
			log.info("Topology type updated: {}.", event);
			service.fire(Event.TOPOLOGY_TYPE_CHANGED);
		}
	}

	private class DistributedMessageListener implements MessageListener<TopologyMessage> {
		@Override public void onMessage(final Message<TopologyMessage> message) {
			log.debug("Distributed event: {}", message);
			final TopologyMessage topologyMessage = message.getMessageObject();
			switch (topologyMessage.getType()) {
				case TOPOLOGY_SELECTED:
					service.fire(Event.TOPOLOGY_CONFIGURED);
					break;
			}
		}
	}

	private class ExceptionHandler implements Consumer<Throwable> {

		@Override public void accept(final Throwable throwable) {
			log.error("Exception", throwable);
		}
	}
}
