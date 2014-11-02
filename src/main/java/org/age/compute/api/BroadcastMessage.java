/*
 * Created: 2014-10-23
 * $Id$
 */

package org.age.compute.api;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.igj.qual.Immutable;

@Immutable
public class BroadcastMessage implements Serializable {

	private static final long serialVersionUID = 6454910639600959920L;

	private final Serializable payload;

	public BroadcastMessage(final Serializable payload) {
		this.payload = requireNonNull(payload);
	}

	public Serializable getPayload() {
		return payload;
	}
}
