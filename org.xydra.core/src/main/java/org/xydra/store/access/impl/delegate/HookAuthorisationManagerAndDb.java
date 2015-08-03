package org.xydra.store.access.impl.delegate;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.index.query.Pair;
import org.xydra.store.access.XAccessListener;
import org.xydra.store.access.XAccessRightDefinition;
import org.xydra.store.access.XAccessRightValue;
import org.xydra.store.access.XAuthorisationDatabaseWitListeners;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.AbstractAuthorisationManager;


/**
 * Provides hooks for subclasses. They can overwrite beforeRead() and/or
 * beforeWrite() which are invoked before calling the underlying instance.
 *
 * @author xamde
 */
public class HookAuthorisationManagerAndDb extends AbstractAuthorisationManager implements
        XAuthorisationManager, XAuthorisationDatabaseWitListeners {

	private final XAuthorisationManager authorisationManager;

	/**
	 * @param authorisationManager uses the XAuthorisationDatabase returned from
	 *            ii for read/write access
	 */
	public HookAuthorisationManagerAndDb(final XAuthorisationManager authorisationManager) {
		this.authorisationManager = authorisationManager;
	}

	@Override
    public void addListener(final XAccessListener listener) {
		this.authorisationManager.getAuthorisationDatabase().addListener(listener);
	}

	protected void beforeRead() {
	}

	protected void beforeWrite() {
	}

	@Override
    public XAccessRightValue getAccessDefinition(final XId actor, final XAddress resource, final XId access)
	        throws IllegalArgumentException {
		beforeRead();
		return this.authorisationManager.getAuthorisationDatabase().getAccessDefinition(actor,
		        resource, access);
	}

	@Override
    public Pair<Set<XId>,Set<XId>> getActorsWithPermission(final XAddress resource, final XId access) {
		beforeRead();
		return this.authorisationManager.getActorsWithPermission(resource, access);
	}

	@Override
	public XAuthorisationDatabaseWitListeners getAuthorisationDatabase() {
		return this.authorisationManager.getAuthorisationDatabase();
	}

	protected XAuthorisationManager getBaseAuthorisationManager() {
		return this.authorisationManager;
	}

	@Override
    public Set<XAccessRightDefinition> getDefinitions() {
		beforeRead();
		return this.authorisationManager.getAuthorisationDatabase().getDefinitions();
	}

	@Override
    public XGroupDatabaseWithListeners getGroupDatabase() {
		return this.authorisationManager.getGroupDatabase();
	}

	@Override
    public Pair<Set<XId>,Set<XId>> getPermissions(final XId actor, final XAddress resource) {
		beforeRead();
		return this.authorisationManager.getPermissions(actor, resource);
	}

	@Override
    public XAccessRightValue hasAccess(final XId actor, final XAddress resource, final XId access) {
		beforeRead();
		return this.authorisationManager.hasAccess(actor, resource, access);
	}

	@Override
    public XAccessRightValue hasAccessToSubresource(final XId actor, final XAddress rootResource, final XId access) {
		beforeRead();
		return this.authorisationManager.hasAccessToSubresource(actor, rootResource, access);
	}

	@Override
    public XAccessRightValue hasAccessToSubtree(final XId actor, final XAddress rootResource, final XId access) {
		beforeRead();
		return this.authorisationManager.hasAccessToSubtree(actor, rootResource, access);
	}

	@Override
    public boolean isAccessDefined(final XId actor, final XAddress resource, final XId access) {
		beforeRead();
		return this.authorisationManager.getAuthorisationDatabase().isAccessDefined(actor,
		        resource, access);
	}

	@Override
    public void removeListener(final XAccessListener listener) {
		this.authorisationManager.getAuthorisationDatabase().removeListener(listener);
	}

	@Override
    public void resetAccess(final XId actor, final XAddress resource, final XId access) {
		beforeWrite();
		this.authorisationManager.getAuthorisationDatabase().resetAccess(actor, resource, access);
	}

	@Override
    public void setAccess(final XId actor, final XAddress resource, final XId access, final boolean allowed) {
		beforeWrite();
		this.authorisationManager.getAuthorisationDatabase().setAccess(actor, resource, access,
		        allowed);
	}

}
