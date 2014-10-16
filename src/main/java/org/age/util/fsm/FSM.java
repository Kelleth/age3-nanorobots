/*
 * Created: 2014-09-19
 * $Id$
 */

package org.age.util.fsm;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface FSM<S extends Enum<S>, E extends Enum<E>> {

	void goTo(@NonNull final S nextState);

}
