package org.xydra.store.impl.delegate;

import org.xydra.base.XID;
import org.xydra.store.Callback;


class DelegationUtils {
	
	public static void assertNonNull(Object o) {
		if(o == null) {
			throw new IllegalArgumentException("null parameter not permitted");
		}
	}
	
	/**
	 * @param actorId may be null
	 * @param passwordHash may be null
	 * @throws IllegalArgumentException if a parameter is null.
	 */
	public static void assertNonNullActorAndPassword(XID actorId, String passwordHash) {
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
	public static void assertNonNullCallback(Callback<?> callback) {
		if(callback == null) {
			throw new IllegalArgumentException(
			        "callback for side-effect free methods must not be null");
		}
	}
	
}
