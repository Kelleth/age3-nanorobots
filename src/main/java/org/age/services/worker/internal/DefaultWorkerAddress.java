/*
 * Created: 2014-11-04
 * $Id$
 */

package org.age.services.worker.internal;

import static com.google.common.base.MoreObjects.toStringHelper;

import org.age.compute.api.WorkerAddress;

import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import org.checkerframework.checker.igj.qual.Immutable;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Default implementation of a compute-level address.
 */
@Immutable
@ThreadSafe
public final class DefaultWorkerAddress implements WorkerAddress {

	private static final long serialVersionUID = 1526560233585684436L;

	private final UUID uuid = UUID.randomUUID();

	private final String encodedUUID = uuid.toString();

	@Override public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DefaultWorkerAddress)) {
			return false;
		}
		final DefaultWorkerAddress other = (DefaultWorkerAddress)obj;
		return Objects.equals(uuid, other.uuid);
	}

	@Override public int hashCode() {
		return Objects.hashCode(uuid);
	}

	@Override public String toString() {
		return toStringHelper(this).addValue(encodedUUID).toString();
	}
}
