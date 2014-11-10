/*
 * Created: 2014-08-22
 * $Id$
 */

package org.age.services.identity;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.requireNonNull;

import org.age.services.worker.WorkerService;

import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

@Named
public class NodeIdentityService {

	private static final Logger log = LoggerFactory.getLogger(NodeIdentityService.class);

	private final UUID nodeId = UUID.randomUUID();

	private final String encodedNodeId = nodeId.toString();

	@NonNull private NodeType nodeType = NodeType.UNKNOWN;

	@Inject @MonotonicNonNull private ApplicationContext applicationContext;

	@PostConstruct private void construct() {
		log.debug("Constructing identity service.");
		try {
			applicationContext.getBean(WorkerService.class);
			nodeType = NodeType.COMPUTE;
		} catch (final NoSuchBeanDefinitionException ignored) {
			nodeType = NodeType.SATELLITE;
		}
		log.info("Node type: {}.", nodeType);
		log.info("Node id: {}.", nodeId);
	}

	@NonNull
	public String nodeId() {
		return nodeId.toString();
	}

	@NonNull
	public NodeType nodeType() {
		return nodeType;
	}

	@NonNull
	public NodeIdentity nodeIdentity() {
		return new NodeIdentity(encodedNodeId, nodeType, services());
	}

	@NonNull
	public Set<String> services() {
		return newHashSet();
	}

	public boolean isCompute() {
		return is(NodeType.COMPUTE);
	}

	public boolean is(@NonNull final NodeType type) {
		return nodeType == requireNonNull(type);
	}
}
