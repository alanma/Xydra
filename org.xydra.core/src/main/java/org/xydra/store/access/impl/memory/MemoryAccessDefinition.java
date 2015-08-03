package org.xydra.store.access.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.store.access.XAccessRightDefinition;


/**
 * An in-memory implementation of {@link XAccessRightDefinition}.
 *
 * @author xamde
 */

public class MemoryAccessDefinition implements XAccessRightDefinition {

	private final XId access;
	private final XId actor;
	private final boolean allowed;
	private final XAddress resource;

	public MemoryAccessDefinition(final XId access, final XAddress resource, final XId actor, final boolean allowed) {
		this.access = access;
		this.resource = resource;
		this.actor = actor;
		this.allowed = allowed;
	}

	@Override
    public XId getAccess() {
		return this.access;
	}

	@Override
    public XId getActor() {
		return this.actor;
	}

	@Override
    public XAddress getResource() {
		return this.resource;
	}

	@Override
    public boolean isAllowed() {
		return this.allowed;
	}

	@Override
	public String toString() {
		return (this.allowed ? "ALLOW" : "DENY") + " (" + this.actor + ", " + this.resource + ", "
		        + this.access + ")";
	}

}
