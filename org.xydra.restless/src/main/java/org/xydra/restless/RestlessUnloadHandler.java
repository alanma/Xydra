package org.xydra.restless;

public interface RestlessUnloadHandler {

	/**
	 * Called before containing servlet is unloaded = removed from memory.
	 * 
	 * This also happens when the server is shut down.
	 */
	void onBeforeUnload();

}
