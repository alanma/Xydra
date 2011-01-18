package org.xydra.core.access.impl.memory;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.core.access.XAccessListener;
import org.xydra.core.access.XAccessManager;
import org.xydra.index.query.Pair;
import org.xydra.store.NamingUtils;
import org.xydra.store.access.XAccessDatabase;
import org.xydra.store.access.XAccessDefinition;
import org.xydra.store.access.XAccessValue;
import org.xydra.store.access.impl.delegate.AccessModelWrapperOnPersistence;
import org.xydra.store.access.impl.delegate.AccountModelWrapperOnPersistence;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * Uses a {@link XydraPersistence} (via {@link AccessModelWrapperOnPersistence})
 * to persists access rights and a fast in-memory {@link MemoryAccessManager} to
 * reason on access definitions.
 * 
 * FIXME synchronize across threads via GAE MemCache (if we are on GAE)
 * 
 * @author xamde
 * 
 */
public class DelegatingAccessManager extends AbstractAccessManager implements XAccessManager {
	
	private static final long serialVersionUID = -1076084147882243484L;
	
	private XAccessDatabase accessPersistence;
	
	private MemoryAccessManager memoryAccessManager;
	
	/**
	 * @param persistence
	 * @param internalActorId
	 * @param modelId base modelId
	 */
	public DelegatingAccessManager(XydraPersistence persistence, XID internalActorId, XID modelId) {
		AccountModelWrapperOnPersistence accounts = new AccountModelWrapperOnPersistence(
		        persistence, internalActorId);
		this.memoryAccessManager = new MemoryAccessManager(accounts);
		
		XID rightsModelId = NamingUtils.getRightsModelId(modelId);
		this.accessPersistence = new AccessModelWrapperOnPersistence(persistence, internalActorId,
		        rightsModelId);
		// load
		for(XAccessDefinition def : this.accessPersistence.getDefinitions()) {
			this.memoryAccessManager.setAccess(def.getActor(), def.getResource(), def.getAccess(),
			        def.isAllowed());
		}
	}
	
	public void addListener(XAccessListener listener) {
		this.memoryAccessManager.addListener(listener);
	}
	
	public XAccessValue getAccessDefinition(XID actor, XAddress resource, XID access)
	        throws IllegalArgumentException {
		return this.memoryAccessManager.getAccessDefinition(actor, resource, access);
	}
	
	public Pair<Set<XID>,Set<XID>> getActorsWithPermission(XAddress resource, XID access) {
		return this.memoryAccessManager.getActorsWithPermission(resource, access);
	}
	
	public Set<XAccessDefinition> getDefinitions() {
		return this.memoryAccessManager.getDefinitions();
	}
	
	public Pair<Set<XID>,Set<XID>> getPermissions(XID actor, XAddress resource) {
		return this.memoryAccessManager.getPermissions(actor, resource);
	}
	
	public XAccessValue hasAccess(XID actor, XAddress resource, XID access) {
		return this.memoryAccessManager.hasAccess(actor, resource, access);
	}
	
	public XAccessValue hasAccessToSubresource(XID actor, XAddress rootResource, XID access) {
		return this.memoryAccessManager.hasAccessToSubresource(actor, rootResource, access);
	}
	
	public XAccessValue hasAccessToSubtree(XID actor, XAddress rootResource, XID access) {
		return this.memoryAccessManager.hasAccessToSubtree(actor, rootResource, access);
	}
	
	public boolean isAccessDefined(XID actor, XAddress resource, XID access) {
		return this.memoryAccessManager.isAccessDefined(actor, resource, access);
	}
	
	public void removeListener(XAccessListener listener) {
		this.memoryAccessManager.removeListener(listener);
	}
	
	// write operation
	@Override
	public void resetAccess(XID actor, XAddress resource, XID access) {
		this.memoryAccessManager.resetAccess(actor, resource, access);
		// persist
		this.accessPersistence.resetAccess(actor, resource, access);
	}
	
	// write operation
	@Override
	public void setAccess(XID actor, XAddress resource, XID access, boolean allowed) {
		this.memoryAccessManager.setAccess(actor, resource, access, allowed);
		// persist
		this.accessPersistence.setAccess(actor, resource, access, allowed);
	}
	
}
