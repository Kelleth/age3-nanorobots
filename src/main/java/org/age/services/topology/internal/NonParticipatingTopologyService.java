/*
 * Created: 2014-08-21
 */

package org.age.services.topology.internal;

import org.age.services.discovery.DiscoveryService;
import org.age.services.identity.NodeDescriptor;
import org.age.services.topology.TopologyMessage;
import org.age.services.topology.TopologyService;

import com.google.common.eventbus.EventBus;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Named("non-participating")
public class NonParticipatingTopologyService implements TopologyService {

	public static final String CONFIG_MAP_NAME = "topology/config";

	public static final String CHANNEL_NAME = "topology/channel";

	private static final Logger log = LoggerFactory.getLogger(NonParticipatingTopologyService.class);

	@Inject @MonotonicNonNull private HazelcastInstance hazelcastInstance;

	@Inject private @MonotonicNonNull DiscoveryService discoveryService;

	@Inject @MonotonicNonNull private EventBus eventBus;

	@MonotonicNonNull private IMap<String, Object> runtimeConfig;

	@MonotonicNonNull private ITopic<TopologyMessage> topic;

	@PostConstruct public void construct() {
		log.debug("Constructing NonParticipatingTopologyService.");
		// Obtain dependencies
		runtimeConfig = hazelcastInstance.getMap(CONFIG_MAP_NAME);
		topic = hazelcastInstance.getTopic(CHANNEL_NAME);

		topic.addMessageListener(new DistributedMessageListener());
		eventBus.register(this);
	}

	@Override @NonNull public Set<String> neighbours() {
		throw new UnsupportedOperationException("Neighbourhood not available for this implementation.");
	}

	@Override @NonNull public Optional<String> masterId() {
		return Optional.ofNullable((String)runtimeConfig.get(ConfigKeys.MASTER));
	}

	@Override public boolean hasTopology() {
		return false;
	}

	@Override @NonNull public Optional<DirectedGraph<String, DefaultEdge>> topologyGraph() {
		return Optional.ofNullable((DirectedGraph<String, DefaultEdge>)runtimeConfig.get(ConfigKeys.TOPOLOGY_GRAPH));
	}

	@Override @NonNull public Optional<String> topologyType() {
		return Optional.ofNullable((String)runtimeConfig.get(ConfigKeys.TOPOLOGY_TYPE));
	}

	@NonNull protected Set<@NonNull NodeDescriptor> getComputeNodes() {
		return discoveryService.membersMatching("type = 'compute'");
	}

	private static class ConfigKeys {
		public static final String MASTER = "master";

		public static final String TOPOLOGY_GRAPH = "topologyGraph";

		public static final String TOPOLOGY_TYPE = "topologyType";
	}

	private class DistributedMessageListener implements MessageListener<TopologyMessage> {
		@Override public void onMessage(final Message<TopologyMessage> message) {
			log.debug("Distributed event: {}", message);
			final TopologyMessage topologyMessage = message.getMessageObject();
			switch (topologyMessage.getType()) {
				case TOPOLOGY_SELECTED:

					break;
			}
		}
	}
}
