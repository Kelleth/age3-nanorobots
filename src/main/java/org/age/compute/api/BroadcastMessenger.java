/*
 * Created: 2014-10-07
 * $Id$
 */

package org.age.compute.api;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface BroadcastMessenger {

	<T extends Serializable> void send(@NonNull T message);

	<T extends Serializable> void registerListener(@NonNull MessageListener<T> listener);

	<T extends Serializable> void removeListener(@NonNull MessageListener<T> listener);
}
