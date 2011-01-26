package org.xydra.core.model.impl.memory;

import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.base.XID;


/**
 * An implementation of {@link XID} where an ID is represented by a String
 * value.
 * 
 * @author voelkel
 */

@RunsInGWT(true)
@RequiresAppEngine(false)
public class MemoryStringID implements XID {
	
	private static final long serialVersionUID = 3397013331330118533L;
	
	private final String string;
	
	/**
	 * No syntax checks are performed.
	 * 
	 * @param uriString
	 */
	protected MemoryStringID(String uriString) {
		this.string = uriString;
	}
	
	public int compareTo(XID o) {
		return this.toString().compareTo(o.toString());
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof MemoryStringID) {
			return ((MemoryStringID)other).string.equals(this.string);
		} else if(other instanceof XID) {
			return ((XID)other).toString().equals(this.string);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.string.hashCode();
	}
	
	@Override
	public String toString() {
		return this.string;
	}
	
}
