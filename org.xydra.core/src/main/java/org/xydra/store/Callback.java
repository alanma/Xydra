package org.xydra.store;

/**
 * A class used for callback from X*Service APIs.
 * 
 * @author dscharrer
 * 
 * @param <T> Type of the object returned on success.
 */
public interface Callback<T> {
	
	/**
	 * Indicates that the operation succeeded without problems.
	 * 
	 * TODO require this to not be called after {@link #onFailure(Throwable)}?
	 * 
	 * @param object The result of the operation, if any.
	 */
	void onSuccess(T object);
	
	/**
	 * Indicates that the operation failed and no changes were made.
	 * 
	 * This method should not be called after {@link #onSuccess(Object)} has
	 * been called.
	 * 
	 * @param error An object describing the error that occurred.
	 */
	void onFailure(Throwable error);
	
}
