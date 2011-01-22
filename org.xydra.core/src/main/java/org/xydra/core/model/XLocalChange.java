package org.xydra.core.model;

import org.xydra.base.XID;
import org.xydra.base.change.XCommand;


/**
 * TODO document when stable
 * 
 * @author dscharrer
 * 
 */
public interface XLocalChange {
	
	XID getActor();
	
	XCommand getCommand();
	
	String getPasswordHash();
	
	long getRemoteRevision();
	
	boolean isApplied();
	
	void setRemoteResult(long result);
	
}
