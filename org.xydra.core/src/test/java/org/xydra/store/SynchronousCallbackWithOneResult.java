package org.xydra.store;

import org.xydra.core.StoreException;

/**
 * A simple {@link Callback} implementation that supports waiting for results
 *
 * @author kaidel
 * @param <T> callback response type
 */
public class SynchronousCallbackWithOneResult<T> implements Callback<T> {
	public static int FAILURE = 3;
	public static int SUCCESS = 2;

	public static int TIMEOUT = 1;
	public static int UNKNOWN_ERROR = 0;

	protected T effect;
	protected Throwable exception;
	protected boolean failure = false;
	protected boolean success = false;

	public T getEffect() {
		return this.effect;
	}

	public Throwable getException() {
		return this.exception;
	}

	@Override
	public synchronized void onFailure(final Throwable exception) {
		this.failure = true;
		this.exception = exception;
		notifyAll();
	}

	@Override
	public synchronized void onSuccess(final T object) {
		this.success = true;
		this.effect = object;
		notifyAll();
	}

	/**
	 * @param timeout
	 * @return {@link #TIMEOUT}, {@link #SUCCESS}, {@link #FAILURE} or
	 *         {@link #UNKNOWN_ERROR}
	 */
	public synchronized int waitOnCallback(final long timeout) {

		if(!this.success && !this.failure) {
			try {
				wait(timeout);
			} catch(final InterruptedException ie) {
				ie.printStackTrace();
				return UNKNOWN_ERROR;
			}
		}

		if(!this.success && !this.failure) {
			return TIMEOUT;
		}

		if(this.success) {
			return SUCCESS;
		}

		if(this.failure) {
			return FAILURE;
		}

		return UNKNOWN_ERROR;
	}

	public void waitOnCallbackAndThrowExceptionForProblems(final int waitTimeout) {
		final int result = waitOnCallback(waitTimeout);
		if(result == TIMEOUT) {
			throw new StoreException("Timeout waiting for callback to be called");
		}
		if(result == UNKNOWN_ERROR) {
			throw new StoreException("Unknown error waiting for callback to be called");
		}
	}

}
