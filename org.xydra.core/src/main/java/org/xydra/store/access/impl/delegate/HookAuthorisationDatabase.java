package org.xydra.store.access.impl.delegate;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
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
	
	private XAuthorisationDatabaseWitListeners authorisationDb;
	
	public HookAuthorisationDatabase(XAuthorisationDatabaseWitListeners authorisationDatabase) {
		this.authorisationDb = authorisationDatabase;
	}
	
	@Override
    public void addListener(XAccessListener listener) {
		this.authorisationDb.addListener(listener);
	}
	
	protected void beforeRead() {
	}
	
	protected void beforeWrite() {
	}
	
	@Override
    public XAccessRightValue getAccessDefinition(XID actor, XAddress resource, XID access)
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
    public boolean isAccessDefined(XID actor, XAddress resource, XID access) {
		beforeRead();
		return this.authorisationDb.isAccessDefined(actor, resource, access);
	}
	
	@Override
    public void removeListener(XAccessListener listener) {
		this.authorisationDb.removeListener(listener);
	}
	
	@Override
    public void resetAccess(XID actor, XAddress resource, XID access) {
		beforeWrite();
		this.authorisationDb.resetAccess(actor, resource, access);
	}
	
	@Override
    public void setAccess(XID actor, XAddress resource, XID access, boolean allowed) {
		beforeWrite();
		this.authorisationDb.setAccess(actor, resource, access, allowed);
	}
	
}
