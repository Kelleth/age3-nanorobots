package org.age.util.fsm;

public final class IllegalTransitionException extends RuntimeException {

	private static final long serialVersionUID = -8827262839354852835L;

	public IllegalTransitionException(final String message) {
		super(message);
	}

	public IllegalTransitionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public IllegalTransitionException(final Throwable cause) {
		super(cause);
	}
}
