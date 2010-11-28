package org.xydra.store.base;

import org.xydra.core.change.XCommand;
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
		store.executeCommands(credentials.actorId, credentials.passwordHash,
		        new XCommand[] { command }, new Callback<long[]>() {
			        
			        @Override
			        public void onFailure(Throwable exception) {
				        throw new StoreException("re-throw", exception);
			        }
			        
			        @Override
			        public void onSuccess(long[] object) {
				        assert object.length == 1;
				        WritableUtils.result = object[0];
			        }
		        });
		return result;
	}
	
}
