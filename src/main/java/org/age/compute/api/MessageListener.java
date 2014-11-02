/*
 * Created: 2014-10-27
 * $Id$
 */

package org.age.compute.api;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface MessageListener<@NonNull T> {

	void onMessage(@NonNull T message);

}
