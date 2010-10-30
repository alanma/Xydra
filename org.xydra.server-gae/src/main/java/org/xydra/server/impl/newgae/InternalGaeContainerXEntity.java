/**
 * 
 */
package org.xydra.server.impl.newgae;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.server.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;


/**
 * Internal helper class used by {@link GaeChangesService} to access the current
 * model state.
 * 
 * @author dscharrer
 * 
 * @param <C>
 */
abstract class InternalGaeContainerXEntity<C> extends InternalGaeXEntity {
	
	private final GaeChangesService changesService;
	private final Map<XID,C> cachedChildren = new HashMap<XID,C>();
	private final XAddress addr;
	private Set<XID> cachedIds;
	private final Set<XID> cachedMisses = new HashSet<XID>();
	private final Set<XAddress> locks;
	
	protected InternalGaeContainerXEntity(GaeChangesService changesService, XAddress addr,
	        Set<XAddress> locks) {
		this.changesService = changesService;
		assert addr.getAddressedType() != XType.XFIELD;
		assert GaeChangesService.canRead(addr, locks);
		this.addr = addr;
		this.locks = locks;
	}
	
	public boolean isEmpty() {
		return !iterator().hasNext();
	}
	
	public XAddress getAddress() {
		return this.addr;
	}
	
	protected abstract XAddress resolveChild(XAddress addr, XID childId);
	
	protected abstract C loadChild(XAddress childAddr, Entity childEntity);
	
	public C getChild(XID fieldId) {
		
		// don't look in this.cachedIds, as this might contain outdated
		// information due to being based on GAE queries
		if(this.cachedMisses.contains(fieldId)) {
			return null;
		}
		
		C gf = this.cachedChildren.get(fieldId);
		if(gf != null) {
			return gf;
		}
		
		XAddress childAddr = resolveChild(this.addr, fieldId);
		assert GaeChangesService.canRead(childAddr, this.locks);
		
		Entity e = GaeUtils.getEntity(KeyStructure.createCombinedKey(childAddr));
		if(e == null) {
			this.cachedMisses.add(fieldId);
			return null;
		}
		
		gf = loadChild(childAddr, e);
		this.cachedChildren.put(fieldId, gf);
		return gf;
	}
	
	public boolean hasChild(XID fieldId) {
		return this.cachedIds != null ? this.cachedIds.contains(fieldId)
		        : getChild(fieldId) != null;
	}
	
	public Iterator<XID> iterator() {
		if(this.cachedIds == null) {
			this.cachedIds = new HashSet<XID>();
			Query q = new Query(this.addr.getAddressedType().getChildType().toString()).addFilter(
			        PROP_PARENT, FilterOperator.EQUAL, this.addr.toURI()).setKeysOnly();
			for(Entity e : GaeUtils.prepareQuery(q).asIterable()) {
				XAddress childAddr = KeyStructure.toAddress(e.getKey());
				this.cachedIds.add(getChildId(childAddr));
			}
		}
		return this.cachedIds.iterator();
	}
	
	abstract protected XID getChildId(XAddress childAddr);
	
	protected Set<XAddress> getLocks() {
		return this.locks;
	}
	
	protected GaeChangesService getChangesService() {
		return this.changesService;
	}
	
}
