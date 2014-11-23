/*
 * Created by nnidyu on 22.11.14.
 */
package org.age.services.identity;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Set;

/**
 * Wraps cached node identity information in a single, serializable object.
 */
@Immutable
public interface NodeDescriptor extends Serializable {

	/**
	 * Returns the ID of the node described in this descriptor.
	 */
	@NonNull String id();

	/**
	 * Returns the type of the node described in this descriptor.
	 */
	@NonNull NodeType type();

	/**
	 * Returns the set of services running on the node described by this descriptor.
	 */
	@NonNull Set<@NonNull String> services();

}
