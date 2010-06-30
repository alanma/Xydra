package org.xydra.client;

/**
 * A class used for callback from X*Service APIs.
 * 
 * @author dscharrer
 * 
 * @param <T> Type of the object returned on success.
 */
public interface Callback<T> {
	
	void onSuccess(T object);
	
	void onFailure(Throwable error);
	
}
