package org.xydra.store.base;

import org.xydra.base.change.XCommand;
import org.xydra.core.StoreException;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.BatchedResult;
import org.xydra.store.WaitingCallback;
import org.xydra.store.XydraStore;


/**
 * Utility for executing commands synchronously.
 *
 * @author xamde
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
	public static long executeCommand(final Credentials credentials, final XydraStore store, final XCommand command) {
		final WaitingCallback<BatchedResult<Long>[]> callback = new WaitingCallback<BatchedResult<Long>[]>();

		store.executeCommands(credentials.getActorId(), credentials.getPasswordHash(),
		        new XCommand[] { command }, callback);

		if(callback.getException() != null) {
			throw new StoreException("re-throw", callback.getException());
		}

		final BatchedResult<Long>[] res = callback.getResult();

		XyAssert.xyAssert(res.length == 1);
		XyAssert.xyAssert(res[0] != null); assert res[0] != null;

		if(res[0].getException() != null) {
			throw new StoreException("re-throw", res[0].getException());
		}

		XyAssert.xyAssert(res[0].getResult() != null); assert res[0].getResult() != null;

		return res[0].getResult();

	}
}
