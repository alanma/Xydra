/**
 * 
 */
package org.xydra.store.impl.gae.changes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.core.XX;
import org.xydra.core.change.XEvent;
import org.xydra.store.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;


/**
 * Internal helper class used by {@link GaeChangesService} to access the current
 * model state.
 * 
 * @author dscharrer
 * 
 * @param <C> type of child entities
 */
abstract class InternalGaeContainerXEntity<C> extends InternalGaeXEntity {
	
	private final GaeChangesService changesService;
	private final Map<XID,C> cachedChildren = new HashMap<XID,C>();
	private final XAddress addr;
	private Set<XID> cachedIds;
	private final Set<XID> cachedMisses = new HashSet<XID>();
	private final Set<XAddress> locks;
	private final long rev;
	
	protected InternalGaeContainerXEntity(GaeChangesService changesService, XAddress addr,
	        long rev, Set<XAddress> locks) {
		assert rev >= 0
		        || (rev == XEvent.RevisionNotAvailable && addr.getAddressedType() == XType.XOBJECT);
		this.changesService = changesService;
		assert addr.getAddressedType() == XType.XMODEL || addr.getAddressedType() == XType.XOBJECT;
		assert GaeChangesService.canRead(addr, locks);
		this.addr = addr;
		this.locks = locks;
		this.rev = rev;
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
		
		Entity e = GaeUtils.getEntity(KeyStructure.createEntityKey(childAddr));
		if(e == null) {
			this.cachedMisses.add(fieldId);
			return null;
		}
		
		assert this.addr.equals(XX.toAddress((String)e.getProperty(PROP_PARENT)));
		
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
			
			assert GaeChangesService.canWrite(this.addr, this.locks);
			
			this.cachedIds = findChildren(this.addr);
		}
		return this.cachedIds.iterator();
	}
	
	protected Set<XAddress> getLocks() {
		return this.locks;
	}
	
	protected GaeChangesService getChangesService() {
		return this.changesService;
	}
	
	public long getRevisionNumber() {
		return this.rev;
	}
	
}
