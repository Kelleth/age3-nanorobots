/*
 * Created: 2014-08-22
 */

package org.age.services.identity.internal;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import org.age.services.identity.NodeIdentityService;
import org.age.services.identity.NodeType;
import org.age.services.worker.WorkerService;

import com.google.common.collect.ImmutableSet;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class DefaultNodeIdentityService implements NodeIdentityService {

	private static final Logger log = LoggerFactory.getLogger(DefaultNodeIdentityService.class);

	// XXX: We can force all services to implement one interface.
	// We are defining by a class name in order to not depend on them in compile time.
	private static final Set<String> SERVICES_NAMES = ImmutableSet.of("org.age.services.discovery.DiscoveryService",
	                                                                  "org.age.services.lifecycle.NodeLifecycleService",
	                                                                  "org.age.services.topology.TopologyService",
	                                                                  "org.age.services.worker.WorkerService",
	                                                                  "org.age.services.identity.NodeIdentityService");

	private final UUID nodeId = UUID.randomUUID();

	private final String encodedNodeId = nodeId.toString();

	private @NonNull NodeType nodeType = NodeType.UNKNOWN;

	@Inject private @MonotonicNonNull ApplicationContext applicationContext;

	@PostConstruct private void construct() {
		log.debug("Constructing identity service.");
		try {
			applicationContext.getBean(WorkerService.class);
			nodeType = NodeType.COMPUTE;
		} catch (final NoSuchBeanDefinitionException ignored) {
			nodeType = NodeType.SATELLITE;
		}
		log.info("Node type: {}.", nodeType);
		log.info("Node id: {}.", encodedNodeId);
	}

	@Override public @NonNull String nodeId() {
		return encodedNodeId;
	}

	@Override public @NonNull NodeType nodeType() {
		return nodeType;
	}

	@Override public @NonNull NodeDescriptor descriptor() {
		return new NodeDescriptor(encodedNodeId, nodeType, services());
	}

	@Override public @NonNull Set<@NonNull String> services() {
		// FIXME: Does not work with multiple implementations
		return SERVICES_NAMES.parallelStream().filter(service -> {
			try {
				final Class<?> aClass = Class.forName(service);
				applicationContext.getBean(aClass);
				return true;
			} catch (final ClassNotFoundException | NoSuchBeanDefinitionException e) {
				log.debug("No service {}.", service, e);
				return false;
			}
		}).collect(toSet());
	}

	@Override public boolean isCompute() {
		return is(NodeType.COMPUTE);
	}

	@Override public boolean is(@NonNull final NodeType type) {
		return nodeType == requireNonNull(type);
	}
}
