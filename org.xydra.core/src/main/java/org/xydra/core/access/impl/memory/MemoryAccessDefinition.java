package org.xydra.core.access.impl.memory;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.store.access.XAccessDefinition;


public class MemoryAccessDefinition implements XAccessDefinition {
	
	private final XID access;
	private final XAddress resource;
	private final XID actor;
	private final boolean allowed;
	
	public MemoryAccessDefinition(XID access, XAddress resource, XID actor, boolean allowed) {
		this.access = access;
		this.resource = resource;
		this.actor = actor;
		this.allowed = allowed;
	}
	
	public XID getAccess() {
		return this.access;
	}
	
	public XID getActor() {
		return this.actor;
	}
	
	public XAddress getResource() {
		return this.resource;
	}
	
	public boolean isAllowed() {
		return this.allowed;
	}
	
	@Override
	public String toString() {
		return (this.allowed ? "ALLOW" : "DENY") + " (" + this.actor + ", " + this.resource + ", "
		        + this.access + ")";
	}
	
}
