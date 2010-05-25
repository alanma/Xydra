package org.xydra.core.access.impl.memory;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.access.XAccessEvent;
import org.xydra.core.change.ChangeType;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;



/**
 * Memory implementation of {@link XAccessEvent}
 * 
 * @author dscharrer
 * 
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
public class MemoryAccessEvent implements XAccessEvent {
	
	private final XID access;
	private final XID actor;
	private final XAddress resource;
	private final ChangeType type;
	private final boolean oldAllowed;
	private final boolean newAllowed;
	
	public MemoryAccessEvent(ChangeType type, XID actor, XAddress resource, XID access,
	        boolean oldAllowed, boolean newAllowed) {
		if(type != ChangeType.ADD && type != ChangeType.CHANGE && type != ChangeType.REMOVE)
			throw new IllegalArgumentException("invalid type for access events: " + type);
		this.type = type;
		this.actor = actor;
		this.resource = resource;
		this.access = access;
		this.oldAllowed = oldAllowed;
		this.newAllowed = newAllowed;
	}
	
	public XID getAccessType() {
		return this.access;
	}
	
	public XID getActor() {
		return this.actor;
	}
	
	public boolean getNewAllowed() {
		return this.newAllowed;
	}
	
	public boolean getOldAllowed() {
		return this.oldAllowed;
	}
	
	public XAddress getResource() {
		return this.resource;
	}
	
	public ChangeType getChangeType() {
		return this.type;
	}
	
	@Override
	public String toString() {
		String prefix;
		switch(this.type) {
		case ADD:
			prefix = "add " + (this.newAllowed ? "ALLOW" : "DENY");
			break;
		case CHANGE:
			prefix = "change " + (this.oldAllowed ? "ALLOW" : "DENY") + " to "
			        + (this.newAllowed ? "ALLOW" : "DENY");
			break;
		case REMOVE:
			prefix = "remove " + (this.oldAllowed ? "ALLOW" : "DENY");
			break;
		default:
			throw new AssertionError("unexpected type for access events: " + this.type);
		}
		return prefix + " (" + this.actor + ", " + this.resource + ", " + this.access + ")";
	}
	
}
