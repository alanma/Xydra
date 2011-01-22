package org.xydra.core.access.impl.memory;

import java.util.HashSet;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.index.query.Pair;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAccessListener;
import org.xydra.store.access.XAccessRightDefinition;
import org.xydra.store.access.XAccessRightValue;
import org.xydra.store.access.XAuthorisationDatabaseWitListeners;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.AbstractAuthorisationManager;


/**
 * A combination of two access managers where an inner access manager is
 * responsible for a subtree and an outer access manager is responsible for
 * everything else.
 * 
 * @author dscharrer
 * @deprecated Seems not be used anywhere. Delete or un-deprecate. ~max
 */
@Deprecated
public class CompositeAccessManager extends AbstractAuthorisationManager implements
        XAuthorisationDatabaseWitListeners {
	
	private static final long serialVersionUID = 2576314343471791859L;
	
	private final XAuthorisationManager inner;
	private final XAddress mountPoint;
	private final XAuthorisationManager outer;
	
	public CompositeAccessManager(XAddress mountPoint, XAuthorisationManager outer,
	        XAuthorisationManager inner) {
		this.mountPoint = mountPoint;
		this.outer = outer;
		this.inner = inner;
	}
	
	public void addListener(XAccessListener listener) {
		this.outer.getAuthorisationDatabase().addListener(listener);
		this.inner.getAuthorisationDatabase().addListener(listener);
	}
	
	public XAccessRightValue getAccessDefinition(XID actor, XAddress resource, XID access)
	        throws IllegalArgumentException {
		if(this.mountPoint.equalsOrContains(resource)) {
			return this.inner.getAuthorisationDatabase().getAccessDefinition(actor, resource,
			        access);
		} else {
			return this.outer.getAuthorisationDatabase().getAccessDefinition(actor, resource,
			        access);
		}
	}
	
	public Pair<Set<XID>,Set<XID>> getActorsWithPermission(XAddress resource, XID access) {
		if(this.mountPoint.equalsOrContains(resource)) {
			Pair<Set<XID>,Set<XID>> i = this.inner.getActorsWithPermission(resource, access);
			Pair<Set<XID>,Set<XID>> o = this.inner.getActorsWithPermission(resource, access);
			o.getFirst().removeAll(i.getSecond());
			o.getSecond().removeAll(i.getFirst());
			if(!i.getSecond().contains(XA.GROUP_ALL) && !i.getFirst().contains(XA.GROUP_ALL)) {
				i.getFirst().addAll(o.getFirst());
				i.getSecond().addAll(o.getSecond());
			}
			return i;
		}
		return this.outer.getActorsWithPermission(resource, access);
	}
	
	@Override
	public XAuthorisationDatabaseWitListeners getAuthorisationDatabase() {
		return this;
	}
	
	public Set<XAccessRightDefinition> getDefinitions() {
		Set<XAccessRightDefinition> definitions = new HashSet<XAccessRightDefinition>();
		definitions.addAll(this.outer.getAuthorisationDatabase().getDefinitions());
		definitions.addAll(this.inner.getAuthorisationDatabase().getDefinitions());
		return definitions;
	}
	
	@Override
	public XGroupDatabaseWithListeners getGroupDatabase() {
		return this.outer.getGroupDatabase();
	}
	
	public Pair<Set<XID>,Set<XID>> getPermissions(XID actor, XAddress resource) {
		if(this.mountPoint.equalsOrContains(resource)) {
			Pair<Set<XID>,Set<XID>> i = this.inner.getPermissions(actor, resource);
			Pair<Set<XID>,Set<XID>> o = this.inner.getPermissions(actor, resource);
			o.getFirst().removeAll(i.getSecond());
			o.getSecond().removeAll(i.getFirst());
			i.getFirst().addAll(o.getFirst());
			i.getSecond().addAll(o.getSecond());
			return i;
		}
		return this.outer.getPermissions(actor, resource);
	}
	
	public XAccessRightValue hasAccess(XID actor, XAddress resource, XID access) {
		if(this.mountPoint.equalsOrContains(resource)) {
			XAccessRightValue allowed = this.inner.hasAccess(actor, resource, access);
			if(allowed.isDefined()) {
				return allowed;
			}
		}
		return this.outer.hasAccess(actor, resource, access);
	}
	
	public XAccessRightValue hasAccessToSubresource(XID actor, XAddress rootResource, XID access) {
		
		if(this.mountPoint.equalsOrContains(rootResource)) {
			
			XAccessRightValue allowed = this.inner.hasAccessToSubresource(actor, rootResource,
			        access);
			
			if(allowed.isDefined()) {
				return allowed;
			}
			
			return hasAccess(actor, rootResource, access);
			
		} else if(rootResource.contains(this.mountPoint)) {
			
			XAccessRightValue allowed = this.inner.hasAccessToSubresource(actor, rootResource,
			        access);
			
			if(allowed.isAllowed()) {
				return allowed;
			}
			
		}
		
		return this.outer.hasAccessToSubresource(actor, rootResource, access);
	}
	
	public XAccessRightValue hasAccessToSubtree(XID actor, XAddress rootResource, XID access) {
		
		if(this.mountPoint.equalsOrContains(rootResource)) {
			
			XAccessRightValue allowed = this.inner.hasAccessToSubtree(actor, rootResource, access);
			
			if(allowed.isDefined()) {
				return allowed;
			}
			return this.outer.hasAccess(actor, rootResource, access);
			
		} else if(rootResource.contains(this.mountPoint)) {
			
			XAccessRightValue allowed = this.inner.hasAccessToSubtree(actor, rootResource, access);
			
			if(allowed.isDenied()) {
				return allowed;
			}
			
		}
		
		return this.outer.hasAccessToSubtree(actor, rootResource, access);
	}
	
	public boolean isAccessDefined(XID actor, XAddress resource, XID access) {
		if(this.mountPoint.equalsOrContains(resource)) {
			return this.inner.getAuthorisationDatabase().isAccessDefined(actor, resource, access);
		} else {
			return this.outer.getAuthorisationDatabase().isAccessDefined(actor, resource, access);
		}
	}
	
	public void removeListener(XAccessListener listener) {
		this.outer.getAuthorisationDatabase().removeListener(listener);
		this.inner.getAuthorisationDatabase().removeListener(listener);
	}
	
	public void resetAccess(XID actor, XAddress resource, XID access) {
		if(this.mountPoint.equalsOrContains(resource)) {
			this.inner.getAuthorisationDatabase().resetAccess(actor, resource, access);
		} else {
			this.outer.getAuthorisationDatabase().resetAccess(actor, resource, access);
		}
	}
	
	public void setAccess(XID actor, XAddress resource, XID access, boolean allowed) {
		if(this.mountPoint.equalsOrContains(resource)) {
			this.inner.getAuthorisationDatabase().setAccess(actor, resource, access, allowed);
		} else {
			this.outer.getAuthorisationDatabase().setAccess(actor, resource, access, allowed);
		}
	}
	
}
