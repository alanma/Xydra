/**
 * 
 */
package org.xydra.server.impl.newgae.changes;

import java.util.Set;

import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.core.value.XValue;
import org.xydra.server.impl.newgae.GaeUtils;

import com.google.appengine.api.datastore.Entity;


/**
 * Internal helper class used by {@link GaeChangesService} to access the current
 * field state.
 * 
 * @author dscharrer
 * 
 */
class InternalGaeField extends InternalGaeXEntity implements XBaseField {
	
	private static final String PROP_TRANSINDEX = "transindex";
	// Value for PROP_TRANSINDEX if there hasn't been any XFieldEvent yet
	private static final int TRANSINDEX_NONE = -1;
	
	private final GaeChangesService changesService;
	private final XAddress fieldAddr;
	private final long fieldRev;
	private final int transindex;
	private XFieldEvent valueEvent;
	
	private InternalGaeField(GaeChangesService changesService, XAddress fieldAddr, long fieldRev,
	        int transindex) {
		this.changesService = changesService;
		assert fieldAddr.getAddressedType() == XType.XFIELD;
		this.fieldAddr = fieldAddr;
		this.fieldRev = fieldRev;
		this.transindex = transindex;
	}
	
	public long getRevisionNumber() {
		return this.fieldRev;
	}
	
	public XValue getValue() {
		if(this.transindex == TRANSINDEX_NONE) {
			return null;
		}
		if(this.valueEvent == null) {
			XAtomicEvent event = this.changesService.getAtomicEvent(this.fieldRev, this.transindex);
			if(!(event instanceof XFieldEvent)) {
				throw new RuntimeException("field refers to an event that is not an XFieldEvent: "
				        + event);
			}
			this.valueEvent = (XFieldEvent)event;
		}
		assert this.valueEvent != null;
		return this.valueEvent.getNewValue();
	}
	
	public boolean isEmpty() {
		return getValue() == null;
	}
	
	public XAddress getAddress() {
		return this.fieldAddr;
	}
	
	public XID getID() {
		return this.fieldAddr.getField();
	}
	
	protected static void set(XAddress fieldAddr, long fieldRev, int transindex, Set<XAddress> locks) {
		assert GaeChangesService.canWrite(fieldAddr, locks);
		assert fieldAddr.getAddressedType() == XType.XFIELD;
		Entity e = new Entity(KeyStructure.createCombinedKey(fieldAddr));
		e.setProperty(PROP_PARENT, fieldAddr.getParent().toURI());
		e.setUnindexedProperty(PROP_REVISION, fieldRev);
		e.setUnindexedProperty(PROP_TRANSINDEX, transindex);
		GaeUtils.putEntity(e);
	}
	
	protected static void set(XAddress fieldAddr, long fieldRev, Set<XAddress> locks) {
		set(fieldAddr, fieldRev, TRANSINDEX_NONE, locks);
	}
	
	protected static InternalGaeField get(GaeChangesService changesService, XAddress fieldAddr,
	        Entity fieldEntity) {
		
		long fieldRev = (Long)fieldEntity.getProperty(PROP_REVISION);
		Number transindex = (Number)fieldEntity.getProperty(PROP_TRANSINDEX);
		
		return new InternalGaeField(changesService, fieldAddr, fieldRev, transindex.intValue());
	}
	
}
