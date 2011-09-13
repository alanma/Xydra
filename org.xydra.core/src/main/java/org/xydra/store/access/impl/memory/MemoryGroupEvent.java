package org.xydra.store.access.impl.memory;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.store.access.XAuthorisationEvent;
import org.xydra.store.access.XGroupEvent;


/**
 * Memory implementation of {@link XAuthorisationEvent}
 * 
 * @author dscharrer
 * 
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
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
	
	@Override
    public XID getActor() {
		return this.actor;
	}
	
	@Override
    public ChangeType getChangeType() {
		return this.type;
	}
	
	@Override
    public XID getGroup() {
		return this.group;
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
