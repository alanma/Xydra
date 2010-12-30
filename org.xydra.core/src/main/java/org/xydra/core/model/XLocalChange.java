package org.xydra.core.model;

import org.xydra.core.change.XCommand;


/**
 * TODO document when stable
 * 
 * @author dscharrer
 * 
 */
public interface XLocalChange {
	
	XCommand getCommand();
	
	XID getActor();
	
	String getPasswordHash();
	
	boolean isApplied();
	
	long getRemoteRevision();
	
	void setRemoteResult(long result);
	
}
