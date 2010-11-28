package org.xydra.core.access.impl.memory;

import java.util.HashSet;
import java.util.Set;

import org.xydra.core.access.XAccessListener;
import org.xydra.core.access.XAccessManagerWithListeners;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.index.query.Pair;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAccessDefinition;
import org.xydra.store.access.XAccessValue;


/**
 * A combination of two access managers where an inner access manager is
 * responsible for a subtree and an outer access manager is responsible for
 * everything else.
 * 
 * @author dscharrer
 * 
 */
public class CompositeAccessManager extends AbstractAccessManager {
	
	private static final long serialVersionUID = 2576314343471791859L;
	
	private final XAccessManagerWithListeners outer;
	private final XAccessManagerWithListeners inner;
	private final XAddress mountPoint;
	
	public CompositeAccessManager(XAddress mountPoint, XAccessManagerWithListeners outer,
	        XAccessManagerWithListeners inner) {
		this.mountPoint = mountPoint;
		this.outer = outer;
		this.inner = inner;
	}
	
	public void addListener(XAccessListener listener) {
		this.outer.addListener(listener);
		this.inner.addListener(listener);
	}
	
	public XAccessValue getAccessDefinition(XID actor, XAddress resource, XID access)
	        throws IllegalArgumentException {
		if(this.mountPoint.equalsOrContains(resource)) {
			return this.inner.getAccessDefinition(actor, resource, access);
		} else {
			return this.outer.getAccessDefinition(actor, resource, access);
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
	
	public Set<XAccessDefinition> getDefinitions() {
		Set<XAccessDefinition> definitions = new HashSet<XAccessDefinition>();
		definitions.addAll(this.outer.getDefinitions());
		definitions.addAll(this.inner.getDefinitions());
		return definitions;
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
	
	public XAccessValue hasAccess(XID actor, XAddress resource, XID access) {
		if(this.mountPoint.equalsOrContains(resource)) {
			XAccessValue allowed = this.inner.hasAccess(actor, resource, access);
			if(allowed.isDefined()) {
				return allowed;
			}
		}
		return this.outer.hasAccess(actor, resource, access);
	}
	
	public XAccessValue hasAccessToSubtree(XID actor, XAddress rootResource, XID access) {
		
		if(this.mountPoint.equalsOrContains(rootResource)) {
			
			XAccessValue allowed = this.inner.hasAccessToSubtree(actor, rootResource, access);
			
			if(allowed.isDefined()) {
				return allowed;
			}
			return this.outer.hasAccess(actor, rootResource, access);
			
		} else if(rootResource.contains(this.mountPoint)) {
			
			XAccessValue allowed = this.inner.hasAccessToSubtree(actor, rootResource, access);
			
			if(allowed.isDenied()) {
				return allowed;
			}
			
		}
		
		return this.outer.hasAccessToSubtree(actor, rootResource, access);
	}
	
	public XAccessValue hasAccessToSubresource(XID actor, XAddress rootResource, XID access) {
		
		if(this.mountPoint.equalsOrContains(rootResource)) {
			
			XAccessValue allowed = this.inner.hasAccessToSubresource(actor, rootResource, access);
			
			if(allowed.isDefined()) {
				return allowed;
			}
			
			return hasAccess(actor, rootResource, access);
			
		} else if(rootResource.contains(this.mountPoint)) {
			
			XAccessValue allowed = this.inner.hasAccessToSubresource(actor, rootResource, access);
			
			if(allowed.isAllowed()) {
				return allowed;
			}
			
		}
		
		return this.outer.hasAccessToSubresource(actor, rootResource, access);
	}
	
	public boolean isAccessDefined(XID actor, XAddress resource, XID access) {
		if(this.mountPoint.equalsOrContains(resource)) {
			return this.inner.isAccessDefined(actor, resource, access);
		} else {
			return this.outer.isAccessDefined(actor, resource, access);
		}
	}
	
	public void removeListener(XAccessListener listener) {
		this.outer.removeListener(listener);
		this.inner.removeListener(listener);
	}
	
	public void resetAccess(XID actor, XAddress resource, XID access) {
		if(this.mountPoint.equalsOrContains(resource)) {
			this.inner.resetAccess(actor, resource, access);
		} else {
			this.outer.resetAccess(actor, resource, access);
		}
	}
	
	public void setAccess(XID actor, XAddress resource, XID access, boolean allowed) {
		if(this.mountPoint.equalsOrContains(resource)) {
			this.inner.setAccess(actor, resource, access, allowed);
		} else {
			this.outer.setAccess(actor, resource, access, allowed);
		}
	}
	
}
