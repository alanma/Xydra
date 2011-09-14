package org.xydra.store.base;

import org.xydra.base.change.XCommand;
import org.xydra.store.BatchedResult;
import org.xydra.store.StoreException;
import org.xydra.store.WaitingCallback;
import org.xydra.store.XydraStore;


/**
 * Utility for executing commands synchronously.
 * 
 * @author voelkel
 * 
 */
public class ExecuteCommandsUtils {
	
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
		WaitingCallback<BatchedResult<Long>[]> callback = new WaitingCallback<BatchedResult<Long>[]>();
		
		store.executeCommands(credentials.getActorId(), credentials.getPasswordHash(),
		        new XCommand[] { command }, callback);
		
		if(callback.getException() != null) {
			throw new StoreException("re-throw", callback.getException());
		}
		
		BatchedResult<Long>[] res = callback.getResult();
		
		assert res.length == 1;
		assert res[0] != null;
		
		if(res[0].getException() != null) {
			throw new StoreException("re-throw", res[0].getException());
		}
		
		assert res[0].getResult() != null;
		
		return res[0].getResult();
		
	}
}
