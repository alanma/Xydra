/**
 * 
 */
package org.xydra.server.impl.newgae.changes;

import java.util.Set;

import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XField;
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
	
	/**
	 * Construct a read-only interface to an {@link XFieldEvent} in the GAE
	 * datastore.
	 * 
	 * {@link InternalGaeField}s are not constructed directly by
	 * {@link GaeChangesService} but through
	 * {@link InternalGaeObject#getField(XID)}.
	 * 
	 */
	protected InternalGaeField(GaeChangesService changesService, XAddress fieldAddr,
	        Entity fieldEntity) {
		this.changesService = changesService;
		assert fieldAddr.getAddressedType() == XType.XFIELD;
		this.fieldAddr = fieldAddr;
		this.fieldRev = getFieldRev(fieldEntity);
		this.transindex = getTransIndex(fieldEntity);
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
	
	/**
	 * Create or update an {@link XField} in the GAE datastore.
	 * 
	 * It is up to the caller to acquire enough locks. It is sufficient to only
	 * lock the field itself.
	 * 
	 * @param fieldAddr The address of the field to add.
	 * @param fieldRev The revision number of the current change. This will be
	 *            saved to the field entity.
	 * @param transindex The index of the current event into the current
	 *            transaction. This will be used to find the {@link XValue} of
	 *            this field. Use {@link #set(XAddress, long, Set)} to create
	 *            fields without a value or remove the value of existing fields.
	 * @param locks The locks held by the current process. These are used to
	 *            assert that we are actually allowed to create the entity.
	 */
	protected static void set(XAddress fieldAddr, long fieldRev, int transindex, Set<XAddress> locks) {
		assert GaeChangesService.canWrite(fieldAddr, locks);
		assert fieldAddr.getAddressedType() == XType.XFIELD;
		Entity e = new Entity(KeyStructure.createCombinedKey(fieldAddr));
		e.setProperty(PROP_PARENT, fieldAddr.getParent().toURI());
		e.setUnindexedProperty(PROP_REVISION, fieldRev);
		e.setUnindexedProperty(PROP_TRANSINDEX, transindex);
		GaeUtils.putEntity(e);
	}
	
	/**
	 * Create or update an empty {@link XField} in the GAE datastore. The
	 * created field will have no {@link XValue} and if there existed a field
	 * with a {@link XValue} before, the {@link XValue} will be removed.
	 * 
	 * It is up to the caller to acquire enough locks. It is sufficient to only
	 * lock the field itself.
	 * 
	 * @param fieldAddr The address of the field to add.
	 * @param fieldRev The revision number of the current change. This will be
	 *            saved to the field entity.
	 * @param locks The locks held by the current process. These are used to
	 *            assert that we are actually allowed to create the entity.
	 */
	protected static void set(XAddress fieldAddr, long fieldRev, Set<XAddress> locks) {
		set(fieldAddr, fieldRev, TRANSINDEX_NONE, locks);
	}
	
	private static int getTransIndex(Entity fieldEntity) {
		Number transindex = (Number)fieldEntity.getProperty(PROP_TRANSINDEX);
		return transindex.intValue();
	}
	
	private static long getFieldRev(Entity fieldEntity) {
		long fieldRev = (Long)fieldEntity.getProperty(PROP_REVISION);
		return fieldRev;
	}
	
}
