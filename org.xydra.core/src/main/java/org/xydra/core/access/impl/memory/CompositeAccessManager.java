package org.xydra.core.access.impl.memory;

import java.util.Iterator;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.access.XAccessDefinition;
import org.xydra.core.access.XAccessListener;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XAccessValue;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.index.iterator.BagUnionIterator;
import org.xydra.index.query.Pair;


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
	
	private final XAccessManager outer;
	private final XAccessManager inner;
	private final XAddress mountPoint;
	
	public CompositeAccessManager(XAddress mountPoint, XAccessManager outer, XAccessManager inner) {
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
		if(XX.equalsOrContains(this.mountPoint, resource)) {
			return this.inner.getAccessDefinition(actor, resource, access);
		} else {
			return this.outer.getAccessDefinition(actor, resource, access);
		}
	}
	
	public Pair<Set<XID>,Set<XID>> getActorsWithPermission(XAddress resource, XID access) {
		// TODO implement me
		throw new UnsupportedOperationException();
	}
	
	public Iterator<XAccessDefinition> getDefinitions() {
		return new BagUnionIterator<XAccessDefinition>(this.outer.getDefinitions(), this.inner
		        .getDefinitions());
	}
	
	public Set<XID> getPermissions(XID actor, XAddress resource) {
		// TODO implement me
		throw new UnsupportedOperationException();
	}
	
	public XAccessValue hasAccess(XID actor, XAddress resource, XID access) {
		if(XX.equalsOrContains(this.mountPoint, resource)) {
			XAccessValue allowed = this.inner.hasAccess(actor, resource, access);
			if(allowed.isDefined()) {
				return allowed;
			}
		}
		return this.outer.hasAccess(actor, resource, access);
	}
	
	public XAccessValue hasAccessToSubtree(XID actor, XAddress rootResource, XID access) {
		
		if(XX.equalsOrContains(this.mountPoint, rootResource)) {
			
			XAccessValue allowed = this.inner.hasAccessToSubtree(actor, rootResource, access);
			
			if(allowed.isDefined()) {
				return allowed;
			}
			return this.outer.hasAccess(actor, rootResource, access);
			
		} else if(XX.contains(rootResource, this.mountPoint)) {
			
			XAccessValue allowed = this.inner.hasAccessToSubtree(actor, rootResource, access);
			
			if(allowed.isDenied()) {
				return allowed;
			}
			
		}
		
		return this.outer.hasAccessToSubtree(actor, rootResource, access);
	}
	
	public XAccessValue hasAccessToSubresource(XID actor, XAddress rootResource, XID access) {
		
		if(XX.equalsOrContains(this.mountPoint, rootResource)) {
			
			XAccessValue allowed = this.inner.hasAccessToSubresource(actor, rootResource, access);
			
			if(allowed.isDefined()) {
				return allowed;
			}
			
			return hasAccess(actor, rootResource, access);
			
		} else if(XX.contains(rootResource, this.mountPoint)) {
			
			XAccessValue allowed = this.inner.hasAccessToSubresource(actor, rootResource, access);
			
			if(allowed.isAllowed()) {
				return allowed;
			}
			
		}
		
		return this.outer.hasAccessToSubresource(actor, rootResource, access);
	}
	
	public boolean isAccessDefined(XID actor, XAddress resource, XID access) {
		if(XX.equalsOrContains(this.mountPoint, resource)) {
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
		if(XX.equalsOrContains(this.mountPoint, resource)) {
			this.inner.resetAccess(actor, resource, access);
		} else {
			this.outer.resetAccess(actor, resource, access);
		}
	}
	
	public void setAccess(XID actor, XAddress resource, XID access, boolean allowed) {
		if(XX.equalsOrContains(this.mountPoint, resource)) {
			this.inner.setAccess(actor, resource, access, allowed);
		} else {
			this.outer.setAccess(actor, resource, access, allowed);
		}
	}
	
}
