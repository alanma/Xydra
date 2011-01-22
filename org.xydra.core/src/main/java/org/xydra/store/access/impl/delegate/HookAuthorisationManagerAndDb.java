package org.xydra.store.access.impl.delegate;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
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
	
	private XAuthorisationManager authorisationManager;
	
	/**
	 * @param authorisationManager uses the XAuthorisationDatabase returned from
	 *            ii for read/write access
	 */
	public HookAuthorisationManagerAndDb(XAuthorisationManager authorisationManager) {
		this.authorisationManager = authorisationManager;
	}
	
	public void addListener(XAccessListener listener) {
		this.authorisationManager.getAuthorisationDatabase().addListener(listener);
	}
	
	protected void beforeRead() {
	}
	
	protected void beforeWrite() {
	}
	
	public XAccessRightValue getAccessDefinition(XID actor, XAddress resource, XID access)
	        throws IllegalArgumentException {
		beforeRead();
		return this.authorisationManager.getAuthorisationDatabase().getAccessDefinition(actor,
		        resource, access);
	}
	
	public Pair<Set<XID>,Set<XID>> getActorsWithPermission(XAddress resource, XID access) {
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
	
	public Set<XAccessRightDefinition> getDefinitions() {
		beforeRead();
		return this.authorisationManager.getAuthorisationDatabase().getDefinitions();
	}
	
	public XGroupDatabaseWithListeners getGroupDatabase() {
		return this.authorisationManager.getGroupDatabase();
	}
	
	public Pair<Set<XID>,Set<XID>> getPermissions(XID actor, XAddress resource) {
		beforeRead();
		return this.authorisationManager.getPermissions(actor, resource);
	}
	
	public XAccessRightValue hasAccess(XID actor, XAddress resource, XID access) {
		beforeRead();
		return this.authorisationManager.hasAccess(actor, resource, access);
	}
	
	public XAccessRightValue hasAccessToSubresource(XID actor, XAddress rootResource, XID access) {
		beforeRead();
		return this.authorisationManager.hasAccessToSubresource(actor, rootResource, access);
	}
	
	public XAccessRightValue hasAccessToSubtree(XID actor, XAddress rootResource, XID access) {
		beforeRead();
		return this.authorisationManager.hasAccessToSubtree(actor, rootResource, access);
	}
	
	public boolean isAccessDefined(XID actor, XAddress resource, XID access) {
		beforeRead();
		return this.authorisationManager.getAuthorisationDatabase().isAccessDefined(actor,
		        resource, access);
	}
	
	public void removeListener(XAccessListener listener) {
		this.authorisationManager.getAuthorisationDatabase().removeListener(listener);
	}
	
	public void resetAccess(XID actor, XAddress resource, XID access) {
		beforeWrite();
		this.authorisationManager.getAuthorisationDatabase().resetAccess(actor, resource, access);
	}
	
	public void setAccess(XID actor, XAddress resource, XID access, boolean allowed) {
		beforeWrite();
		this.authorisationManager.getAuthorisationDatabase().setAccess(actor, resource, access,
		        allowed);
	}
	
}
