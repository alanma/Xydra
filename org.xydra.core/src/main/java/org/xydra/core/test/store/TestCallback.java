package org.xydra.core.test.store;

import org.xydra.store.Callback;

/**
 * A simple {@link Callback} implementation for tests of {@link XydraStore}
 * @author Kaidel
 */

public class TestCallback<T> implements Callback<T> {
	private T effect;
	private Throwable exception;
	
	private boolean success = false;
	private boolean failure = false;
	
	public static int UNKNOWN_ERROR = 0;
	public static int TIMEOUT = 1;
	public static int SUCCESS = 2;
	public static int FAILURE = 3;

	@Override
	public void onSuccess(T object) {
		this.success = true;
		notifyAll();
		
		this.effect = object;
	}

	@Override
	public void onFailure(Throwable exception) {
		this.failure = true;
		notifyAll();
		
		this.exception = exception;
	}
	
	public T getEffect() {
		return this.effect;
	}
	
	public Throwable getException() {
		return this.exception;
	}
	
	public int waitOnCallback(long timeout) {
		if(!success && !failure) {
			try {
				Thread.sleep(timeout);
			}
			catch(InterruptedException ie) {
				ie.printStackTrace();
				return UNKNOWN_ERROR;
			}
		}
		
		if(!success && !failure) {
			return TIMEOUT;
		}
		
		if(success) {
			return SUCCESS;
		}
		
		if(failure) {
			return FAILURE;
		}
		
		
		return UNKNOWN_ERROR;
	}

}
