package org.xydra.core.access.impl.memory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.XX;
import org.xydra.core.access.XA;
import org.xydra.core.access.XAccessDefinition;
import org.xydra.core.access.XAccessEvent;
import org.xydra.core.access.XAccessListener;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XAccessValue;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.change.ChangeType;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.index.IMapMapMapIndex;
import org.xydra.index.XI;
import org.xydra.index.impl.FastTripleMap;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyKeyEntryTuple;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;


/**
 * Implementation of {@link XAccessManager}.
 * 
 * IMPROVE using standard java monitor for now, reader-writer lock may be more
 * appropriate
 * 
 * @author dscharrer
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
public class MemoryAccessManager extends AbstractAccessManager {
	
	private static final long serialVersionUID = -1731169839295825690L;
	
	private final XGroupDatabase groups;
	// map of access -> resource -> actor
	private final IMapMapMapIndex<XID,XAddress,XID,Boolean> rights;
	private final Set<XAccessListener> listeners;
	
	public MemoryAccessManager(XGroupDatabase groups) {
		this.groups = groups;
		this.rights = new FastTripleMap<XID,XAddress,XID,Boolean>();
		this.listeners = new HashSet<XAccessListener>();
	}
	
	synchronized public XAccessValue getAccessDefinition(XID actor, XAddress resource, XID access)
	        throws IllegalArgumentException {
		Boolean b = this.rights.lookup(access, resource, actor);
		return toAccessValue(b);
	}
	
	private XAccessValue toAccessValue(Boolean b) {
		if(b == null) {
			return XAccessValue.UNDEFINED;
		} else if(b) {
			return XAccessValue.ALLOWED;
		} else {
			return XAccessValue.DENIED;
		}
	}
	
	synchronized public Pair<Set<XID>,Set<XID>> getActorsWithPermission(XAddress resource,
	        XID access) {
		
		Set<XID> allowed = new HashSet<XID>();
		Set<XID> denied = new HashSet<XID>();
		
		Iterator<KeyKeyKeyEntryTuple<XID,XAddress,XID,Boolean>> it = this.rights.tupleIterator(
		        new EqualsConstraint<XID>(access), new EqualsConstraint<XAddress>(resource),
		        new Wildcard<XID>());
		while(it.hasNext()) {
			KeyKeyKeyEntryTuple<XID,XAddress,XID,Boolean> tuple = it.next();
			
			assert tuple.getEntry() != null : "got null entry";
			assert XI.equals(access, tuple.getKey1()) : "got wrong access type from query";
			assert XI.equals(resource, tuple.getKey2()) : "got wrong actor from query";
			
			XID actor = tuple.getKey3();
			if(tuple.getEntry().booleanValue())
				allowed.add(actor);
			else if(!XI.equals(actor, XA.GROUP_ALL))
				denied.add(actor);
		}
		
		if(!isAccessDefined(XA.GROUP_ALL, resource, access)) {
			
			XAddress parent = resource.getParent();
			if(parent != null) {
				Pair<Set<XID>,Set<XID>> pair = getActorsWithPermission(parent, access);
				
				/*
				 * all actors that are denied access to the parent and are not
				 * explicitly allowed access to this resource will be denied
				 */
				for(XID deny : pair.getSecond()) {
					boolean overwritten = false;
					for(XID g : allowed) {
						if((deny == null ? g == null : deny.equals(g))
						        || this.groups.hasGroup(deny, g)) {
							overwritten = true;
							break;
						}
					}
					if(!overwritten)
						denied.add(deny);
				}
				
				/*
				 * add all actors/groups that are allowed access to the parent,
				 * even if they are explicitly denied access to this resource as
				 * denying access for a group is not allowed and we cannot
				 * differentiate between groups and actors here
				 */
				allowed.addAll(pair.getFirst());
				
			}
			
		}
		
		return new Pair<Set<XID>,Set<XID>>(allowed, denied);
	}
	
	synchronized public Set<XID> getPermissions(XID actor, XAddress resource) {
		
		Set<XID> res = new HashSet<XID>();
		
		// iterator over defined access types
		Iterator<XID> it = this.rights.key1Iterator();
		while(it.hasNext()) {
			XID access = it.next();
			if(hasAccess(actor, resource, access).isAllowed())
				res.add(access);
		}
		// IMPROVE can this be done more efficiently?
		
		return res;
	}
	
	synchronized public XAccessValue hasAccess(XID actor, XAddress resource, XID access) {
		
		// check if access is defined for this resource
		XAccessValue def = accessForResource(actor, resource, access);
		if(def.isDefined()) {
			return def;
		}
		
		// check if access is reset for this group
		XAccessValue reset = getAccessDefinition(XA.GROUP_ALL, resource, access);
		if(reset.isDenied()) {
			return reset;
		}
		
		// check the parent resource
		XAddress parent = resource.getParent();
		assert parent != resource && !resource.equals(parent);
		if(parent != null) {
			return hasAccess(actor, parent, access);
		}
		
		return XAccessValue.UNDEFINED;
	}
	
	/**
	 * Get the access defined for the actor on this resource or the access
	 * allowed for any of the actor's groups.
	 */
	private XAccessValue accessForResource(XID actor, XAddress resource, XID access) {
		
		// check if access is specifically granted or denied for this actor
		XAccessValue def = getAccessDefinition(actor, resource, access);
		if(def.isDefined()) {
			return def;
		}
		
		// check if access is granted for any of the actor's groups
		Iterator<KeyKeyKeyEntryTuple<XID,XAddress,XID,Boolean>> it = this.rights.tupleIterator(
		        new EqualsConstraint<XID>(access), new EqualsConstraint<XAddress>(resource),
		        new Wildcard<XID>());
		while(it.hasNext()) {
			KeyKeyKeyEntryTuple<XID,XAddress,XID,Boolean> tuple = it.next();
			
			assert tuple.getEntry() != null : "got null entry";
			assert XI.equals(access, tuple.getKey1()) : "got wrong access id from query";
			assert XI.equals(resource, tuple.getKey2()) : "got wrong resource from query";
			
			boolean allowed = tuple.getEntry();
			XID group = tuple.getKey3();
			
			if(allowed && this.groups.hasGroup(actor, group)) {
				return XAccessValue.ALLOWED;
			}
		}
		
		// nothing defined
		return XAccessValue.UNDEFINED;
		
	}
	
	synchronized public XAccessValue hasAccessToSubtree(XID actor, XAddress rootResource, XID access) {
		
		// check if the actor has access to the root resource
		XAccessValue def = hasAccess(actor, rootResource, access);
		if(def.isDenied()) {
			return def;
		}
		
		// check if access is denied for any resource
		Iterator<KeyKeyKeyEntryTuple<XID,XAddress,XID,Boolean>> it = this.rights.tupleIterator(
		        new EqualsConstraint<XID>(access), new Wildcard<XAddress>(),
		        new EqualsConstraint<XID>(actor));
		while(it.hasNext()) {
			
			KeyKeyKeyEntryTuple<XID,XAddress,XID,Boolean> tuple = it.next();
			
			assert tuple.getEntry() != null : "got null entry";
			assert XI.equals(access, tuple.getKey1()) : "got wrong access type from query";
			assert XI.equals(actor, tuple.getKey3()) : "got wrong actor from query";
			
			boolean allowed = tuple.getEntry();
			
			if(!allowed) {
				
				XAddress resource = tuple.getKey2();
				if(XX.equalsOrContains(rootResource, resource)) {
					// denied for at least one resource
					return XAccessValue.DENIED;
				}
				
			}
			
		}
		
		// check if access has been reset for a resource and not granted again
		it = this.rights.tupleIterator(new EqualsConstraint<XID>(access), new Wildcard<XAddress>(),
		        new EqualsConstraint<XID>(XA.GROUP_ALL));
		while(it.hasNext()) {
			
			KeyKeyKeyEntryTuple<XID,XAddress,XID,Boolean> tuple = it.next();
			
			assert tuple.getEntry() != null : "got null entry";
			assert XI.equals(access, tuple.getKey1()) : "got wrong access type from query";
			assert XI.equals(XA.GROUP_ALL, tuple.getKey3()) : "got wrong actor from query";
			
			boolean allowed = tuple.getEntry();
			if(allowed) {
				continue;
			}
			
			XAddress resource = tuple.getKey2();
			if(!XX.contains(rootResource, resource)) {
				continue;
			}
			
			// access reset for this resource
			XAccessValue localAccess = accessForResource(actor, resource, access);
			if(!localAccess.isAllowed()) {
				// access reset and not granted again for at least one resource
				return XAccessValue.DENIED;
			}
			
		}
		
		return def;
	}
	
	public XAccessValue hasAccessToSubresource(XID actor, XAddress rootResource, XID access) {
		
		// check if the actor has access to the root resource
		XAccessValue def = hasAccess(actor, rootResource, access);
		if(def.isAllowed()) {
			return def;
		}
		
		// check if access is granted for any subresource
		Iterator<KeyKeyKeyEntryTuple<XID,XAddress,XID,Boolean>> it = this.rights.tupleIterator(
		        new EqualsConstraint<XID>(access), new Wildcard<XAddress>(), new Wildcard<XID>());
		while(it.hasNext()) {
			
			KeyKeyKeyEntryTuple<XID,XAddress,XID,Boolean> tuple = it.next();
			
			assert tuple.getEntry() != null : "got null entry";
			assert XI.equals(access, tuple.getKey1()) : "got wrong access type from query";
			
			boolean allowed = tuple.getEntry();
			if(!allowed) {
				continue;
			}
			
			XAddress resource = tuple.getKey2();
			if(!XX.contains(rootResource, resource)) {
				continue;
			}
			
			// check that the actor is not denied access here
			if(getAccessDefinition(actor, resource, access).isDenied()) {
				continue;
			}
			
			XID group = tuple.getKey3();
			if(XI.equals(actor, group) || this.groups.hasGroup(actor, group)) {
				return XAccessValue.ALLOWED;
			}
			
		}
		
		return def;
	}
	
	synchronized public boolean isAccessDefined(XID actor, XAddress resource, XID access) {
		return this.rights.containsKey(new EqualsConstraint<XID>(access),
		        new EqualsConstraint<XAddress>(resource), new EqualsConstraint<XID>(actor));
	}
	
	synchronized public void resetAccess(XID actor, XAddress resource, XID access) {
		XAccessValue old = getAccessDefinition(actor, resource, access);
		if(!old.isDefined()) {
			// no right defined => nothing to remove
			return;
		}
		this.rights.deIndex(access, resource, actor);
		dispatchEvent(new MemoryAccessEvent(ChangeType.REMOVE, actor, resource, access, old,
		        XAccessValue.UNDEFINED));
	}
	
	synchronized public void setAccess(XID actor, XAddress resource, XID access, boolean allowed) {
		XAccessValue old = getAccessDefinition(actor, resource, access);
		XAccessValue na = toAccessValue(allowed);
		if(old == na) {
			// right already defined => nothing to change
			return;
		}
		this.rights.index(access, resource, actor, allowed);
		if(!old.isDefined()) {
			dispatchEvent(new MemoryAccessEvent(ChangeType.ADD, actor, resource, access, old, na));
		} else {
			dispatchEvent(new MemoryAccessEvent(ChangeType.CHANGE, actor, resource, access, old, na));
		}
	}
	
	@Override
	synchronized public String toString() {
		return this.rights.toString();
	}
	
	synchronized public Iterator<XAccessDefinition> getDefinitions() {
		
		return new AbstractTransformingIterator<KeyKeyKeyEntryTuple<XID,XAddress,XID,Boolean>,XAccessDefinition>(
		        this.rights.tupleIterator(new Wildcard<XID>(), new Wildcard<XAddress>(),
		                new Wildcard<XID>())) {
			
			@Override
			public XAccessDefinition transform(KeyKeyKeyEntryTuple<XID,XAddress,XID,Boolean> in) {
				return new MemoryAccessDefinition(in.getKey1(), in.getKey2(), in.getKey3(), in
				        .getEntry());
			}
			
		};
	}
	
	private void dispatchEvent(XAccessEvent event) {
		for(XAccessListener listener : this.listeners) {
			listener.onAccessEvent(event);
		}
	}
	
	synchronized public void addListener(XAccessListener listener) {
		this.listeners.add(listener);
	}
	
	synchronized public void removeListener(XAccessListener listener) {
		this.listeners.remove(listener);
	}
	
}
