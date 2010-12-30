/**
 * 
 */
package org.xydra.core.test.model;

import org.xydra.core.change.XCommand;
import org.xydra.core.model.XLocalChangeCallback;


/**
 * {@link XLocalChangeCallback} implementation that allows test to wait until
 * the callback is invoked.
 * 
 * @author dscharrer
 * 
 */
class TestLocalChangeCallback implements XLocalChangeCallback {
	
	private boolean committed = false;
	private long result;
	
	synchronized public void failed() {
		
		assert !this.committed : "double fail/apply detected";
		
		this.committed = true;
		this.result = XCommand.FAILED;
		notifyAll();
	}
	
	synchronized public void applied(long revision) {
		
		assert !this.committed : "double fail/apply detected";
		
		assert revision >= 0 || revision == XCommand.NOCHANGE;
		
		this.committed = true;
		this.result = revision;
		notifyAll();
	}
	
	synchronized public long waitForResult() {
		
		long time = System.currentTimeMillis();
		while(!this.committed) {
			
			assert System.currentTimeMillis() - time <= 1000 : "timeout waiting for command to apply";
			
			try {
				wait(1100);
			} catch(InterruptedException e) {
				// ignore
			}
		}
		
		return this.result;
	}
	
}
