package org.xydra.store.access.impl.memory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.core.XX;
import org.xydra.index.IMapMapMapIndex;
import org.xydra.index.XI;
import org.xydra.index.impl.FastTripleMap;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyKeyEntryTuple;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAccessListener;
import org.xydra.store.access.XAccessRightDefinition;
import org.xydra.store.access.XAccessRightValue;
import org.xydra.store.access.XAuthorisationDatabaseWitListeners;
import org.xydra.store.access.XAuthorisationEvent;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabase;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.AbstractAuthorisationManager;


/**
 * Implementation of {@link XAuthorisationManager}.
 * 
 * IMPROVE using standard java monitor for now, reader-writer lock may be more
 * appropriate
 * 
 * @author dscharrer
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public class MemoryAuthorisationManager extends AbstractAuthorisationManager implements
        XAuthorisationManager, XAuthorisationDatabaseWitListeners {
	
	public static final long serialVersionUID = -1731169839295825690L;
	
	private final XGroupDatabaseWithListeners groups;
	private final Set<XAccessListener> listeners;
	// map of access -> resource -> actor
	private final IMapMapMapIndex<XId,XAddress,XId,Boolean> rights;
	
	public MemoryAuthorisationManager(XGroupDatabaseWithListeners groups) {
		this.groups = groups;
		this.rights = new FastTripleMap<XId,XAddress,XId,Boolean>();
		this.listeners = new HashSet<XAccessListener>();
	}
	
	/**
	 * FIXME should be private and called from constructor?
	 * 
	 * @param administratorGroupId usually
	 *            {@link XGroupDatabase#ADMINISTRATOR_GROUP_ID}
	 * @param repositoryId for which to allow everything
	 */
	public void grantGroupAllAccessToRepository(XId administratorGroupId, XId repositoryId) {
		// add built-in access rights
		this.getAuthorisationDatabase().setAccess(administratorGroupId,
		        XX.toAddress(repositoryId, null, null, null), XA.ACCESS_WRITE, true);
		this.getAuthorisationDatabase().setAccess(administratorGroupId,
		        XX.toAddress(repositoryId, null, null, null), XA.ACCESS_READ, true);
		this.getAuthorisationDatabase().setAccess(administratorGroupId,
		        XX.toAddress(repositoryId, null, null, null), XA.ACCESS_DENY, true);
		this.getAuthorisationDatabase().setAccess(administratorGroupId,
		        XX.toAddress(repositoryId, null, null, null), XA.ACCESS_ALLOW, true);
	}
	
	/**
	 * Get the access defined for the actor on this resource or the access
	 * allowed for any of the actor's groups.
	 */
	private XAccessRightValue accessForResource(XId actor, XAddress resource, XId access) {
		
		// check if access is specifically granted or denied for this actor
		XAccessRightValue def = getAccessDefinition(actor, resource, access);
		if(def.isDefined()) {
			return def;
		}
		
		// check if access is granted for any of the actor's groups
		Iterator<KeyKeyKeyEntryTuple<XId,XAddress,XId,Boolean>> it = this.rights.tupleIterator(
		        new EqualsConstraint<XId>(access), new EqualsConstraint<XAddress>(resource),
		        new Wildcard<XId>());
		while(it.hasNext()) {
			KeyKeyKeyEntryTuple<XId,XAddress,XId,Boolean> tuple = it.next();
			
			assert tuple.getEntry() != null : "got null entry";
			assert XI.equals(access, tuple.getKey1()) : "got wrong access id from query";
			assert XI.equals(resource, tuple.getKey2()) : "got wrong resource from query";
			
			boolean allowed = tuple.getEntry();
			XId group = tuple.getKey3();
			
			if(allowed && this.groups.hasGroup(actor, group)) {
				return XAccessRightValue.ALLOWED;
			}
		}
		
		// nothing defined
		return XAccessRightValue.UNDEFINED;
		
	}
	
	@Override
	synchronized public void addListener(XAccessListener listener) {
		this.listeners.add(listener);
	}
	
	private void dispatchEvent(XAuthorisationEvent event) {
		for(XAccessListener listener : this.listeners) {
			listener.onAccessEvent(event);
		}
	}
	
	@Override
	synchronized public XAccessRightValue getAccessDefinition(XId actor, XAddress resource,
	        XId access) throws IllegalArgumentException {
		Boolean b = this.rights.lookup(access, resource, actor);
		return toAccessValue(b);
	}
	
	@Override
	synchronized public Pair<Set<XId>,Set<XId>> getActorsWithPermission(XAddress resource,
	        XId access) {
		
		Set<XId> allowed = new HashSet<XId>();
		Set<XId> denied = new HashSet<XId>();
		
		Iterator<KeyKeyKeyEntryTuple<XId,XAddress,XId,Boolean>> it = this.rights.tupleIterator(
		        new EqualsConstraint<XId>(access), new EqualsConstraint<XAddress>(resource),
		        new Wildcard<XId>());
		while(it.hasNext()) {
			KeyKeyKeyEntryTuple<XId,XAddress,XId,Boolean> tuple = it.next();
			
			assert tuple.getEntry() != null : "got null entry";
			assert XI.equals(access, tuple.getKey1()) : "got wrong access type from query";
			assert XI.equals(resource, tuple.getKey2()) : "got wrong actor from query";
			
			XId actor = tuple.getKey3();
			if(tuple.getEntry().booleanValue())
				allowed.add(actor);
			else if(!XI.equals(actor, XA.GROUP_ALL))
				denied.add(actor);
		}
		
		if(!isAccessDefined(XA.GROUP_ALL, resource, access)) {
			
			XAddress parent = resource.getParent();
			if(parent != null) {
				Pair<Set<XId>,Set<XId>> pair = getActorsWithPermission(parent, access);
				
				/*
				 * all actors that are denied access to the parent and are not
				 * explicitly allowed access to this resource will be denied
				 */
				for(XId deny : pair.getSecond()) {
					boolean overwritten = false;
					for(XId g : allowed) {
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
		
		return new Pair<Set<XId>,Set<XId>>(allowed, denied);
	}
	
	@Override
	public XAuthorisationDatabaseWitListeners getAuthorisationDatabase() {
		return this;
	}
	
	@Override
	synchronized public Set<XAccessRightDefinition> getDefinitions() {
		AbstractTransformingIterator<KeyKeyKeyEntryTuple<XId,XAddress,XId,Boolean>,XAccessRightDefinition> it = new AbstractTransformingIterator<KeyKeyKeyEntryTuple<XId,XAddress,XId,Boolean>,XAccessRightDefinition>(
		        this.rights.tupleIterator(new Wildcard<XId>(), new Wildcard<XAddress>(),
		                new Wildcard<XId>())) {
			
			@Override
			public XAccessRightDefinition transform(KeyKeyKeyEntryTuple<XId,XAddress,XId,Boolean> in) {
				return new MemoryAccessDefinition(in.getKey1(), in.getKey2(), in.getKey3(),
				        in.getEntry());
			}
			
		};
		Set<XAccessRightDefinition> result = new HashSet<XAccessRightDefinition>();
		while(it.hasNext()) {
			result.add(it.next());
		}
		return result;
	}
	
	@Override
	public XGroupDatabaseWithListeners getGroupDatabase() {
		return this.groups;
	}
	
	@Override
	synchronized public Pair<Set<XId>,Set<XId>> getPermissions(XId actor, XAddress resource) {
		
		Set<XId> allowed = new HashSet<XId>();
		Set<XId> denied = new HashSet<XId>();
		
		// iterator over defined access types
		Iterator<XId> it = this.rights.key1Iterator();
		while(it.hasNext()) {
			XId access = it.next();
			XAccessRightValue v = hasAccess(actor, resource, access);
			if(v.isAllowed()) {
				allowed.add(access);
			} else if(v.isDenied()) {
				denied.add(access);
			}
		}
		// IMPROVE can this be done more efficiently?
		
		return new Pair<Set<XId>,Set<XId>>(allowed, denied);
	}
	
	@Override
	synchronized public XAccessRightValue hasAccess(XId actor, XAddress resource, XId access) {
		// check if access is defined for this resource
		XAccessRightValue def = accessForResource(actor, resource, access);
		if(def.isDefined()) {
			return def;
		}
		
		// check if access is reset for this group
		XAccessRightValue reset = getAccessDefinition(XA.GROUP_ALL, resource, access);
		if(reset.isDenied()) {
			return reset;
		}
		
		// check the parent resource
		XAddress parent = resource.getParent();
		XyAssert.xyAssert(parent != resource && !resource.equals(parent));
		if(parent != null) {
			return hasAccess(actor, parent, access);
		}
		
		return XAccessRightValue.UNDEFINED;
	}
	
	@Override
	public XAccessRightValue hasAccessToSubresource(XId actor, XAddress rootResource, XId access) {
		
		// check if the actor has access to the root resource
		XAccessRightValue def = hasAccess(actor, rootResource, access);
		if(def.isAllowed()) {
			return def;
		}
		
		// check if access is granted for any subresource
		Iterator<KeyKeyKeyEntryTuple<XId,XAddress,XId,Boolean>> it = this.rights.tupleIterator(
		        new EqualsConstraint<XId>(access), new Wildcard<XAddress>(), new Wildcard<XId>());
		while(it.hasNext()) {
			
			KeyKeyKeyEntryTuple<XId,XAddress,XId,Boolean> tuple = it.next();
			
			assert tuple.getEntry() != null : "got null entry";
			assert XI.equals(access, tuple.getKey1()) : "got wrong access type from query";
			
			boolean allowed = tuple.getEntry();
			if(!allowed) {
				continue;
			}
			
			XAddress resource = tuple.getKey2();
			if(!rootResource.contains(resource)) {
				continue;
			}
			
			// check that the actor is not denied access here
			if(getAccessDefinition(actor, resource, access).isDenied()) {
				continue;
			}
			
			XId group = tuple.getKey3();
			if(XI.equals(actor, group) || this.groups.hasGroup(actor, group)) {
				return XAccessRightValue.ALLOWED;
			}
			
		}
		
		return def;
	}
	
	@Override
	synchronized public XAccessRightValue hasAccessToSubtree(XId actor, XAddress rootResource,
	        XId access) {
		
		// check if the actor has access to the root resource
		XAccessRightValue def = hasAccess(actor, rootResource, access);
		if(def.isDenied()) {
			return def;
		}
		
		// check if access is denied for any resource
		Iterator<KeyKeyKeyEntryTuple<XId,XAddress,XId,Boolean>> it = this.rights.tupleIterator(
		        new EqualsConstraint<XId>(access), new Wildcard<XAddress>(),
		        new EqualsConstraint<XId>(actor));
		while(it.hasNext()) {
			
			KeyKeyKeyEntryTuple<XId,XAddress,XId,Boolean> tuple = it.next();
			
			assert tuple.getEntry() != null : "got null entry";
			assert XI.equals(access, tuple.getKey1()) : "got wrong access type from query";
			assert XI.equals(actor, tuple.getKey3()) : "got wrong actor from query";
			
			boolean allowed = tuple.getEntry();
			
			if(!allowed) {
				
				XAddress resource = tuple.getKey2();
				if(rootResource.equalsOrContains(resource)) {
					// denied for at least one resource
					return XAccessRightValue.DENIED;
				}
				
			}
			
		}
		
		// check if access has been reset for a resource and not granted again
		it = this.rights.tupleIterator(new EqualsConstraint<XId>(access), new Wildcard<XAddress>(),
		        new EqualsConstraint<XId>(XA.GROUP_ALL));
		while(it.hasNext()) {
			
			KeyKeyKeyEntryTuple<XId,XAddress,XId,Boolean> tuple = it.next();
			
			assert tuple.getEntry() != null : "got null entry";
			assert XI.equals(access, tuple.getKey1()) : "got wrong access type from query";
			assert XI.equals(XA.GROUP_ALL, tuple.getKey3()) : "got wrong actor from query";
			
			boolean allowed = tuple.getEntry();
			if(allowed) {
				continue;
			}
			
			XAddress resource = tuple.getKey2();
			if(!rootResource.contains(resource)) {
				continue;
			}
			
			// access reset for this resource
			XAccessRightValue localAccess = accessForResource(actor, resource, access);
			if(!localAccess.isAllowed()) {
				// access reset and not granted again for at least one resource
				return XAccessRightValue.DENIED;
			}
			
		}
		
		return def;
	}
	
	@Override
	synchronized public boolean isAccessDefined(XId actor, XAddress resource, XId access) {
		return this.rights.containsKey(new EqualsConstraint<XId>(access),
		        new EqualsConstraint<XAddress>(resource), new EqualsConstraint<XId>(actor));
	}
	
	@Override
	synchronized public void removeListener(XAccessListener listener) {
		this.listeners.remove(listener);
	}
	
	@Override
	synchronized public void resetAccess(XId actor, XAddress resource, XId access) {
		XAccessRightValue old = getAccessDefinition(actor, resource, access);
		if(!old.isDefined()) {
			// no right defined => nothing to remove
			return;
		}
		this.rights.deIndex(access, resource, actor);
		dispatchEvent(new MemoryAccessEvent(ChangeType.REMOVE, actor, resource, access, old,
		        XAccessRightValue.UNDEFINED));
	}
	
	@Override
	synchronized public void setAccess(XId actor, XAddress resource, XId access, boolean allowed) {
		XAccessRightValue old = getAccessDefinition(actor, resource, access);
		XAccessRightValue na = toAccessValue(allowed);
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
	
	private static XAccessRightValue toAccessValue(Boolean b) {
		if(b == null) {
			return XAccessRightValue.UNDEFINED;
		} else if(b) {
			return XAccessRightValue.ALLOWED;
		} else {
			return XAccessRightValue.DENIED;
		}
	}
	
	@Override
	synchronized public String toString() {
		return this.rights.toString();
	}
	
}