package org.xydra.store.access.impl.memory;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.store.access.XAuthorisationEvent;
import org.xydra.store.access.XAccessRightValue;


/**
 * Memory implementation of {@link XAuthorisationEvent}
 * 
 * @author dscharrer
 * 
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
public class MemoryAccessEvent implements XAuthorisationEvent {
	
	private final XID access;
	private final XID actor;
	private final XAccessRightValue newAccess;
	private final XAccessRightValue oldAccess;
	private final XAddress resource;
	private final ChangeType type;
	
	public MemoryAccessEvent(ChangeType type, XID actor, XAddress resource, XID access,
	        XAccessRightValue oldAccess, XAccessRightValue newAccess) {
		if(type != ChangeType.ADD && type != ChangeType.CHANGE && type != ChangeType.REMOVE)
			throw new IllegalArgumentException("invalid type for access events: " + type);
		this.type = type;
		this.actor = actor;
		this.resource = resource;
		this.access = access;
		this.oldAccess = oldAccess;
		this.newAccess = newAccess;
	}
	
	public XID getAccessType() {
		return this.access;
	}
	
	public XID getActor() {
		return this.actor;
	}
	
	public ChangeType getChangeType() {
		return this.type;
	}
	
	public XAccessRightValue getNewAccessValue() {
		return this.newAccess;
	}
	
	public XAccessRightValue getOldAccessValue() {
		return this.oldAccess;
	}
	
	public XAddress getResource() {
		return this.resource;
	}
	
	@Override
	public String toString() {
		String prefix;
		switch(this.type) {
		case ADD:
			prefix = "add " + this.newAccess;
			break;
		case CHANGE:
			prefix = "change " + this.oldAccess + " to " + this.newAccess;
			break;
		case REMOVE:
			prefix = "remove " + this.oldAccess;
			break;
		default:
			throw new AssertionError("unexpected type for access events: " + this.type);
		}
		return prefix + " (" + this.actor + ", " + this.resource + ", " + this.access + ")";
	}
	
}
