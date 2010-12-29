package org.xydra.core.model;

import org.xydra.core.change.XCommand;


/**
 * An interface to notify if re-applying local {@link XCommand XCommands} failed
 * during synchronizing an {@link XModel}/{@link XObject}.
 * 
 * @author dscharrer
 * 
 */
public interface XLocalChangeCallback {
	
	/**
	 * Called when there have been remote changes that prevent this command from
	 * applying.
	 */
	void failed();
	
	/**
	 * Called when the command has successfully been synchronized and is applied
	 * remotely.
	 * 
	 * @param revision The final revision the command has received after being
	 *            applied remotely. This may be {@link XCommand#NOCHANGE} but
	 *            not {@link XCommand#FAILED}.
	 */
	void applied(long revision);
	
}
