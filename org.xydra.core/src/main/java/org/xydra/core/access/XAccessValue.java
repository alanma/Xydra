package org.xydra.core.access;

public enum XAccessValue {
	
	ALLOWED, DENIED, UNDEFINED;
	
	public boolean isAllowed() {
		return this == ALLOWED;
	}
	
	public boolean isDenied() {
		return this == DENIED;
	}
	
	public boolean isDefined() {
		return this != UNDEFINED;
	}
	
}
