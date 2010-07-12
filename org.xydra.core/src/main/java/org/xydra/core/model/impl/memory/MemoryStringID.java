package org.xydra.core.model.impl.memory;

import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.model.XID;


/**
 * An implementation of {@link XID} where an ID is represented by a String
 * value.
 * 
 * @author voelkel
 */

@RunsInGWT
@RunsInJava
public class MemoryStringID implements XID {
	
	private static final long serialVersionUID = 3397013331330118533L;
	
	private final String string;
	
	protected MemoryStringID(String uriString) {
		this.string = uriString;
	}
	
	public String toURI() {
		return this.string;
	}
	
	@Override
	public int hashCode() {
		return this.string.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof MemoryStringID) {
			return ((MemoryStringID)other).string.equals(this.string);
		} else if(other instanceof XID) {
			return ((XID)other).toURI().equals(this.string);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return toURI();
	}
	
}
