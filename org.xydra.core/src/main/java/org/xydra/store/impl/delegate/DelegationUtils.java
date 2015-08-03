package org.xydra.store.impl.delegate;

import org.xydra.base.XId;
import org.xydra.store.Callback;


class DelegationUtils {

	public static void assertNonNull(final Object o) {
		if(o == null) {
			throw new IllegalArgumentException("null parameter not permitted");
		}
	}

	/**
	 * @param actorId may be null
	 * @param passwordHash may be null
	 * @throws IllegalArgumentException if a parameter is null.
	 */
	public static void assertNonNullActorAndPassword(final XId actorId, final String passwordHash) {
		if(actorId == null) {
			throw new IllegalArgumentException("actorId may not be null");
		}
		if(passwordHash == null) {
			throw new IllegalArgumentException("passwordHash may not be null");
		}
	}

	/**
	 * @param callback may be null
	 * @throws IllegalArgumentException if a parameter is null.
	 */
	public static void assertNonNullCallback(final Callback<?> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
	}

}
