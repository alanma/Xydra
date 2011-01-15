package org.xydra.store.base;

import org.xydra.core.change.XCommand;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.StoreException;
import org.xydra.store.XydraStore;


/**
 * Utility for executing commands synchronously.
 * 
 * @author voelkel
 * 
 */
public class WritableUtils {
	
	private static final Logger log = LoggerFactory.getLogger(WritableUtils.class);
	
	private static long result;
	
	/**
	 * Execute a single command and return synchronously the result
	 * 
	 * @param credentials
	 * @param store
	 * @param command
	 * @return
	 */
	public static synchronized long executeCommand(Credentials credentials, XydraStore store,
	        XCommand command) {
		WaitingCallback callback = new WaitingCallback();
		store.executeCommands(credentials.getActorId(), credentials.getPasswordHash(),
		        new XCommand[] { command }, callback);
		while(!callback.done) {
			try {
				// TODO add a suitable timeout (which should be larger than the
				// XydraStore timeout)
				callback.wait();
			} catch(InterruptedException e) {
				log.debug("Could not wait", e);
			}
		}
		
		return result;
	}
	
	private static class WaitingCallback implements Callback<BatchedResult<Long>[]> {
		
		public boolean done = false;
		
		@Override
		public void onFailure(Throwable exception) {
			// thread communication
			this.done = true;
			// wake up waiting threads implicitly via exception
			throw new StoreException("re-throw", exception);
		}
		
		@Override
		public synchronized void onSuccess(BatchedResult<Long>[] object) {
			assert object.length == 1;
			/*
			 * TODO better error handling if getResult is null because
			 * getException has an AccessException
			 */
			WritableUtils.result = object[0].getResult();
			// thread communication
			this.done = true;
			this.notifyAll();
		}
		
	}
	
}
