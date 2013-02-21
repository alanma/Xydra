package org.xydra.store;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;


/**
 * A class used for callback from X*Service APIs. This is a a simpler form of
 * Java Future. This one is simpler to implement and also works in GWT.
 * 
 * @author dscharrer
 * 
 * @param <T> Type of the object returned on success.
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public interface Callback<T> {
	
	/**
	 * Indicates that the operation failed and no changes were made.
	 * 
	 * This method should not be called after {@link #onSuccess(Object)} has
	 * been called.
	 * 
	 * @param exception An object describing the exception that occurred.
	 */
	void onFailure(Throwable exception);
	
	/**
	 * Indicates that the operation succeeded without problems.
	 * 
	 * After a {@link #onFailure(Throwable)} has been called, this method may
	 * not be called.
	 * 
	 * @param object The result of the operation, if any.
	 */
	void onSuccess(T object);
	
}