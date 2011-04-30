package org.xydra.store;

/**
 * A simple {@link Callback} implementation for tests of {@link XydraStore}
 * 
 * @author Kaidel
 */

public class SynchronousTestCallback<T> implements Callback<T> {
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
	public synchronized void onFailure(Throwable exception) {
		this.failure = true;
		this.exception = exception;
		notifyAll();
	}
	
	@Override
	public synchronized void onSuccess(T object) {
		this.success = true;
		this.effect = object;
		
		notifyAll();
	}
	
	public int waitOnCallback(long timeout) {
		if(!this.success && !this.failure) {
			try {
				Thread.sleep(timeout);
			} catch(InterruptedException ie) {
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
	
}
