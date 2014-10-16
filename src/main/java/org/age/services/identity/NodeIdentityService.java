/*
 * Created: 2014-08-22
 * $Id$
 */

package org.age.services.identity;

import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.age.services.worker.WorkerService;

import static com.google.common.collect.Sets.newHashSet;

@Named
public class NodeIdentityService implements ApplicationContextAware {

	private final UUID nodeId = UUID.randomUUID();

	@NonNull private NodeType nodeType = NodeType.UNKNOWN;

	@MonotonicNonNull private ApplicationContext applicationContext;

	@PostConstruct
	void construct() {
		try {
			applicationContext.getBean(WorkerService.class);
			nodeType = NodeType.COMPUTE;
		} catch (final NoSuchBeanDefinitionException ignored) {
			nodeType = NodeType.SATELLITE;
		}
	}

	@Override
	public void setApplicationContext(@NonNull final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@NonNull
	public String getNodeId() {
		return nodeId.toString();
	}

	@NonNull
	public NodeType getNodeType() {
		return nodeType;
	}

	@NonNull
	public NodeIdentity getNodeIdentity() {
		return new NodeIdentity(nodeId.toString(), nodeType, getServices());
	}

	@NonNull
	public Set<String> getServices() {
		return newHashSet();
	}

	public boolean isCompute() {
		return is(NodeType.COMPUTE);
	}

	public boolean is(@NonNull NodeType nodeType) {
		return nodeType.equals(requireNonNull(nodeType));
	}
}
