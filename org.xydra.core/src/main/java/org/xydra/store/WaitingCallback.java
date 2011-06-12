/**
 * 
 */
package org.xydra.store;

import org.xydra.annotations.RunsInGWT;


@RunsInGWT(false)
public class WaitingCallback<T> implements Callback<T> {
	
	private boolean done = false;
	private Throwable exception;
	private T result;
	
	@Override
	public synchronized void onFailure(Throwable exception) {
		assert !this.done;
		this.exception = exception;
		this.done = true;
		notifyAll();
	}
	
	@Override
	public synchronized void onSuccess(T result) {
		assert !this.done;
		this.result = result;
		this.done = true;
		notifyAll();
	}
	
	public synchronized T getResult() {
		while(!this.done) {
			try {
				wait();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		return this.result;
	}
	
	public synchronized Throwable getException() {
		while(!this.done) {
			try {
				wait();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		return this.exception;
	}
	
}
