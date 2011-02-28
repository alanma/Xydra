/**
 * 
 */
package org.xydra.store.impl.gae.changes;

import java.util.Set;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.store.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * Internal helper class used by {@link GaeChangesService} to access the current
 * field state.
 * 
 * @author dscharrer
 * 
 */
class InternalGaeField extends InternalGaeXEntity implements XReadableField {
	
	private static final String PROP_TRANSINDEX = "transindex";
	// Value for PROP_TRANSINDEX if there hasn't been any XFieldEvent yet
	private static final int TRANSINDEX_NONE = -1;
	
	private final XAddress modelAddr;
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
	protected InternalGaeField(XAddress modelAddr, XAddress fieldAddr, Entity fieldEntity) {
		this.modelAddr = modelAddr;
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
			XAtomicEvent event = GaeEventService.getAtomicEvent(this.modelAddr, this.fieldRev,
			        this.transindex).get();
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
		return this.transindex == TRANSINDEX_NONE;
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
	protected static Future<Key> set(XAddress fieldAddr, long fieldRev, int transindex,
	        Set<XAddress> locks) {
		assert GaeChangesService.canWrite(fieldAddr, locks);
		assert fieldAddr.getAddressedType() == XType.XFIELD;
		Entity e = new Entity(KeyStructure.createEntityKey(fieldAddr));
		e.setProperty(PROP_PARENT, fieldAddr.getParent().toURI());
		e.setUnindexedProperty(PROP_REVISION, fieldRev);
		e.setUnindexedProperty(PROP_TRANSINDEX, transindex);
		return GaeUtils.putEntityAsync(e);
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
	protected static Future<Key> set(XAddress fieldAddr, long fieldRev, Set<XAddress> locks) {
		return set(fieldAddr, fieldRev, TRANSINDEX_NONE, locks);
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
