package org.xydra.store.access.impl.delegate;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.store.access.XAccessListener;
import org.xydra.store.access.XAccessRightDefinition;
import org.xydra.store.access.XAccessRightValue;
import org.xydra.store.access.XAuthorisationDatabaseWitListeners;


/**
 * Provides hooks for subclasses. They can overwrite beforeRead() and/or
 * beforeWrite() which are invoked before calling the underlying instance.
 *
 * @author xamde
 */
public class HookAuthorisationDatabase implements XAuthorisationDatabaseWitListeners {

	private final XAuthorisationDatabaseWitListeners authorisationDb;

	public HookAuthorisationDatabase(final XAuthorisationDatabaseWitListeners authorisationDatabase) {
		this.authorisationDb = authorisationDatabase;
	}

	@Override
    public void addListener(final XAccessListener listener) {
		this.authorisationDb.addListener(listener);
	}

	protected void beforeRead() {
	}

	protected void beforeWrite() {
	}

	@Override
    public XAccessRightValue getAccessDefinition(final XId actor, final XAddress resource, final XId access)
	        throws IllegalArgumentException {
		beforeRead();
		return this.authorisationDb.getAccessDefinition(actor, resource, access);
	}

	protected XAuthorisationDatabaseWitListeners getBaseAuthorisationDatabase() {
		return this.authorisationDb;
	}

	@Override
    public Set<XAccessRightDefinition> getDefinitions() {
		beforeRead();
		return this.authorisationDb.getDefinitions();
	}

	@Override
    public boolean isAccessDefined(final XId actor, final XAddress resource, final XId access) {
		beforeRead();
		return this.authorisationDb.isAccessDefined(actor, resource, access);
	}

	@Override
    public void removeListener(final XAccessListener listener) {
		this.authorisationDb.removeListener(listener);
	}

	@Override
    public void resetAccess(final XId actor, final XAddress resource, final XId access) {
		beforeWrite();
		this.authorisationDb.resetAccess(actor, resource, access);
	}

	@Override
    public void setAccess(final XId actor, final XAddress resource, final XId access, final boolean allowed) {
		beforeWrite();
		this.authorisationDb.setAccess(actor, resource, access, allowed);
	}

}
