/**
 * 
 */
package org.xydra.store.impl.gae.changes;

import java.util.Set;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.store.impl.gae.GaeUtils;
import org.xydra.store.impl.gae.changes.GaeEvents.AsyncValue;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * Internal helper class used by {@link IGaeChangesService} to access the
 * current field state.
 * 
 * @author dscharrer
 * 
 */
class InternalGaeField extends InternalGaeXEntity implements XReadableField {
	
	private static final String PROP_TRANSINDEX = "transindex";
	
	private final IGaeChangesService gcs;
	private final XAddress fieldAddr;
	private final long fieldRev;
	private final int transindex;
	private AsyncValue value;
	
	/**
	 * Construct a read-only interface to an {@link XFieldEvent} in the GAE
	 * datastore.
	 * 
	 * {@link InternalGaeField}s are not constructed directly by
	 * {@link IGaeChangesService} but through
	 * {@link InternalGaeObject#getField(XID)}.
	 * 
	 */
	protected InternalGaeField(IGaeChangesService gcs, XAddress fieldAddr, Entity fieldEntity) {
		this.gcs = gcs;
		assert fieldAddr.getAddressedType() == XType.XFIELD;
		this.fieldAddr = fieldAddr;
		this.fieldRev = getFieldRev(fieldEntity);
		this.transindex = getTransIndex(fieldEntity);
	}
	
	@Override
    public long getRevisionNumber() {
		return this.fieldRev;
	}
	
	@Override
    public XValue getValue() {
		if(this.value == null) {
			// IMPROVE maybe get this when the field is fetched?
			this.value = this.gcs.getValue(this.fieldRev, this.transindex);
		}
		return this.value.get();
	}
	
	@Override
    public boolean isEmpty() {
		return this.transindex == GaeEvents.TRANSINDEX_NONE;
	}
	
	@Override
    public XAddress getAddress() {
		return this.fieldAddr;
	}
	
	@Override
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
	        GaeLocks locks) {
		assert locks.canWrite(fieldAddr);
		assert fieldAddr.getAddressedType() == XType.XFIELD;
		Entity e = new Entity(KeyStructure.createEntityKey(fieldAddr));
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
	protected static Future<Key> set(XAddress fieldAddr, long fieldRev, GaeLocks locks) {
		return set(fieldAddr, fieldRev, GaeEvents.TRANSINDEX_NONE, locks);
	}
	
	private static int getTransIndex(Entity fieldEntity) {
		Number transindex = (Number)fieldEntity.getProperty(PROP_TRANSINDEX);
		return transindex.intValue();
	}
	
	private static long getFieldRev(Entity fieldEntity) {
		long fieldRev = (Long)fieldEntity.getProperty(PROP_REVISION);
		return fieldRev;
	}
	
	@Override
	public XType getType() {
		return XType.XFIELD;
	}
	
}
