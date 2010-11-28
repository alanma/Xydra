package org.xydra.core.access.impl.memory;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.access.XAccessEvent;
import org.xydra.core.change.ChangeType;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.store.access.XAccessValue;


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
	private final XAccessValue oldAccess;
	private final XAccessValue newAccess;
	
	public MemoryAccessEvent(ChangeType type, XID actor, XAddress resource, XID access,
	        XAccessValue oldAccess, XAccessValue newAccess) {
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
	
	public XAccessValue getNewAccessValue() {
		return this.newAccess;
	}
	
	public XAccessValue getOldAccessValue() {
		return this.oldAccess;
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
