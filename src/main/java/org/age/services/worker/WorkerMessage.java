/*
 * Created: 2014-10-07
 * $Id$
 */

package org.age.services.worker;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.collect.ImmutableSet;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Message exchanged between {@link WorkerService}s.
 */
@Immutable
public class WorkerMessage implements Serializable {

	public enum Type {
		LOAD_CLASS(true),
		COMPUTATION_MESSAGE(true);

		private final boolean payloadRequired;

		Type(final boolean payloadRequired) {
			this.payloadRequired = payloadRequired;
		}

		public boolean isPayloadRequired() {
			return payloadRequired;
		}
	}

	private static final long serialVersionUID = -6353101926420379298L;

	private final Type type;

	private final ImmutableSet<String> recipients;

	private final Serializable payload;

	WorkerMessage(@NonNull final Type type, @NonNull final Set<String> recipients,
	              @Nullable final Serializable payload) {
		this.type = requireNonNull(type);
		this.recipients = ImmutableSet.copyOf(requireNonNull(recipients));
		this.payload = payload;
	}

	@NonNull public static WorkerMessage createBroadcastWithoutPayload(@NonNull final Type type) {
		checkArgument(!type.isPayloadRequired(), "Message type require payload.");
		return new WorkerMessage(type, emptySet(), null);
	}

	@NonNull public static WorkerMessage createWithoutPayload(@NonNull final Type type,
	                                                          @NonNull final Set<String> recipients) {
		checkArgument(!type.isPayloadRequired(), "Message type require payload.");
		return new WorkerMessage(type, recipients, null);
	}

	@NonNull public static WorkerMessage createBroadcastWithPayload(@NonNull final Type type,
	                                                                @NonNull final Serializable payload) {
		return new WorkerMessage(type, emptySet(), requireNonNull(payload));
	}

	@NonNull public static WorkerMessage createWithPayload(@NonNull final Type type,
	                                                       @NonNull final Set<String> recipients,
	                                                       @NonNull final Serializable payload) {
		return new WorkerMessage(type, recipients, requireNonNull(payload));
	}

	@NonNull public Type getType() {
		return type;
	}

	public boolean hasType(@NonNull final Type typeToCheck) {
		return type == requireNonNull(typeToCheck);
	}

	@NonNull public Optional<@Nullable Serializable> getPayload() {
		return Optional.ofNullable(payload);
	}

	@NonNull protected Set<String> getRecipients() {
		return recipients;
	}

	public boolean isRecipient(@NonNull final String id) {
		return recipients.isEmpty() || recipients.contains(requireNonNull(id));
	}

	public boolean isBroadcast() {
		return recipients.isEmpty();
	}

	@Override public String toString() {
		return toStringHelper(this).add("type", type).add("recipients", recipients).addValue(payload).toString();
	}
}
