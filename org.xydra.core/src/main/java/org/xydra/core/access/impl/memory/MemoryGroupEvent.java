package org.xydra.core.access.impl.memory;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.access.XAccessEvent;
import org.xydra.core.access.XGroupEvent;
import org.xydra.core.change.ChangeType;
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
public class MemoryGroupEvent implements XGroupEvent {
	
	private final XID actor;
	private final XID group;
	private final ChangeType type;
	
	public MemoryGroupEvent(ChangeType type, XID actor, XID group) {
		if(type != ChangeType.ADD && type != ChangeType.REMOVE)
			throw new IllegalArgumentException("invalid type for group events: " + type);
		this.type = type;
		this.actor = actor;
		this.group = group;
	}
	
	public XID getActor() {
		return this.actor;
	}
	
	public XID getGroup() {
		return this.group;
	}
	
	public ChangeType getChangeType() {
		return this.type;
	}
	
	@Override
	public String toString() {
		switch(this.type) {
		case ADD:
			return "add " + this.actor + " to " + this.group;
		case REMOVE:
			return "remove " + this.actor + " from " + this.group;
		default:
			throw new AssertionError("unexpected type for group events: " + this.type);
		}
	}
	
}
