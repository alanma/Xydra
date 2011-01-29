package org.xydra.store.base;

import org.xydra.base.change.XCommand;
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
public class ExecuteCommandsUtils {
	
	private static class WaitingCallback implements Callback<BatchedResult<Long>[]> {
		
		protected boolean done = false;
		protected long result;
		
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
			assert object[0] != null;
			/*
			 * TODO better error handling if getResult is null because
			 * getException has an AccessException
			 */
			this.result = object[0].getResult();
			// thread communication
			this.done = true;
			this.notifyAll();
		}
		
	}
	
	private static final Logger log = LoggerFactory.getLogger(ExecuteCommandsUtils.class);
	
	/**
	 * Execute a single command and return synchronously the result
	 * 
	 * @param credentials The credentials used for executing the command.
	 * @param store The store to execute the command on.
	 * @param command The command to execute.
	 * @return the result of executing the command. See {@link XCommand} for
	 *         values.
	 */
	public static long executeCommand(Credentials credentials, XydraStore store, XCommand command) {
		WaitingCallback callback = new WaitingCallback();
		synchronized(callback) {
			store.executeCommands(credentials.getActorId(), credentials.getPasswordHash(),
			        new XCommand[] { command }, callback);
			while(!callback.done) {
				try {
					// TODO add a suitable timeout (which should be larger than
					// the XydraStore timeout)
					callback.wait();
				} catch(InterruptedException e) {
					log.debug("Could not wait", e);
				}
			}
			
			return callback.result;
		}
	}
	
}
