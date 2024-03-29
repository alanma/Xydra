package org.xydra.store.access.impl.memory;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.store.access.XAccessRightValue;
import org.xydra.store.access.XAuthorisationEvent;


/**
 * Memory implementation of {@link XAuthorisationEvent}
 *
 * @author dscharrer
 *
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public class MemoryAccessEvent implements XAuthorisationEvent {

	private final XId access;
	private final XId actor;
	private final XAccessRightValue newAccess;
	private final XAccessRightValue oldAccess;
	private final XAddress resource;
	private final ChangeType type;

	public MemoryAccessEvent(final ChangeType type, final XId actor, final XAddress resource, final XId access,
	        final XAccessRightValue oldAccess, final XAccessRightValue newAccess) {
		if(type != ChangeType.ADD && type != ChangeType.CHANGE && type != ChangeType.REMOVE) {
			throw new IllegalArgumentException("invalid type for access events: " + type);
		}
		this.type = type;
		this.actor = actor;
		this.resource = resource;
		this.access = access;
		this.oldAccess = oldAccess;
		this.newAccess = newAccess;
	}

	@Override
    public XId getAccessType() {
		return this.access;
	}

	@Override
    public XId getActor() {
		return this.actor;
	}

	@Override
    public ChangeType getChangeType() {
		return this.type;
	}

	@Override
    public XAccessRightValue getNewAccessValue() {
		return this.newAccess;
	}

	@Override
    public XAccessRightValue getOldAccessValue() {
		return this.oldAccess;
	}

	@Override
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
