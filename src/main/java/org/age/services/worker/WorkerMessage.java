/*
 * Created: 2014-10-07
 * $Id$
 */

package org.age.services.worker;

import java.io.Serializable;
import java.util.Optional;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Message exchanged between {@link WorkerService}s.
 */
@Immutable
public class WorkerMessage implements Serializable {

	public enum Type {
		LOAD_CLASS
	}

	private final Type type;

	private final Serializable payload;

	public WorkerMessage(@NonNull final Type messageType, final Serializable payload) {
		this.type = messageType;
		this.payload = payload;
	}

	@NonNull
	public Type getType() {
		return type;
	}

	@NonNull
	public Optional<Serializable> getPayload() {
		return Optional.ofNullable(payload);
	}

}
