/*
 * Created by nnidyu on 22.11.14.
 */

package org.age.services.discovery;

import org.age.services.identity.NodeDescriptor;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

/**
 * Discovery service is responsible for collecting info about other nodes in the cluster and providing them to other services.
 *
 */
public interface DiscoveryService {
	/**
	 * Returns {@link NodeDescriptor}s of cluster members matching the given criteria.
	 *
	 * @param criteria Criteria in the form of SQL query.
	 * @return A set of node descriptors.
	 *
	 * @see com.hazelcast.query.SqlPredicate
	 */
	@NonNull @Immutable Set<@NonNull NodeDescriptor> membersMatching(@NonNull String criteria);

	/**
	 * Returns {@link NodeDescriptor}s of all cluster members.
	 *
	 * @return A set of node descriptors.
	 */
	@NonNull @Immutable Set<@NonNull NodeDescriptor> allMembers();
}
