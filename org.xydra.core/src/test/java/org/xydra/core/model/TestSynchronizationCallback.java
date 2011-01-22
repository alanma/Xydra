/**
 * 
 */
package org.xydra.core.model;

import org.xydra.core.model.sync.XSynchronizationCallback;


/**
 * {@link XSynchronizationCallback} implementation that allows test to wait
 * until the callback is invoked.
 * 
 * @author dscharrer
 * 
 */
class TestSynchronizationCallback implements XSynchronizationCallback {
	
	private Throwable commandError;
	private boolean committed = false;
	private Throwable eventsError;
	private Throwable requestError;
	private boolean success = false;
	
	synchronized private void commit() {
		assert !this.committed : "double fail/apply detected";
		this.committed = true;
		notifyAll();
	}
	
	public Throwable getCommandError() {
		waitForResult();
		return this.commandError;
	}
	
	public Throwable getEventsError() {
		waitForResult();
		return this.eventsError;
	}
	
	public Throwable getRequestError() {
		waitForResult();
		return this.requestError;
	}
	
	public boolean isSuccess() {
		waitForResult();
		return this.success;
	}
	
	@Override
	public void onCommandErrror(Throwable t) {
		this.commandError = t;
		commit();
	}
	
	@Override
	public void onEventsError(Throwable t) {
		this.eventsError = t;
		commit();
	}
	
	@Override
	public void onRequestError(Throwable t) {
		this.requestError = t;
		commit();
	}
	
	@Override
	public void onSuccess() {
		this.success = true;
		commit();
	}
	
	synchronized public void waitForResult() {
		
		long time = System.currentTimeMillis();
		while(!this.committed) {
			
			assert System.currentTimeMillis() - time <= 1000 : "timeout waiting for callback";
			
			try {
				wait(1100);
			} catch(InterruptedException e) {
				// ignore
			}
		}
	}
	
}
