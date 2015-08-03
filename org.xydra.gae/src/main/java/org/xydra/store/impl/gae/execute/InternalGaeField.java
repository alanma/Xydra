/**
 *
 */
package org.xydra.store.impl.gae.execute;

import java.util.Set;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.store.impl.gae.changes.GaeEvents;
import org.xydra.store.impl.gae.changes.GaeEvents.AsyncValue;
import org.xydra.store.impl.gae.changes.GaeLocks;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.changes.KeyStructure;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;

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
	 * {@link InternalGaeObject#getField(XId)}.
	 *
	 */
	protected InternalGaeField(final IGaeChangesService gcs, final XAddress fieldAddr, final SEntity fieldEntity) {
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
		if (this.value == null) {
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
	public XId getId() {
		return this.fieldAddr.getField();
	}

	/**
	 * Create or update an {@link XField} in the GAE datastore.
	 *
	 * It is up to the caller to acquire enough locks. It is sufficient to only
	 * lock the field itself.
	 *
	 * @param fieldAddr
	 *            The address of the field to add.
	 * @param fieldRev
	 *            The revision number of the current change. This will be saved
	 *            to the field entity.
	 * @param transindex
	 *            The index of the current event into the current transaction.
	 *            This will be used to find the {@link XValue} of this field.
	 *            Use {@link #set(XAddress, long, Set)} to create fields without
	 *            a value or remove the value of existing fields.
	 * @param locks
	 *            The locks held by the current process. These are used to
	 *            assert that we are actually allowed to create the entity.
	 */
	protected static Future<SKey> set(final XAddress fieldAddr, final long fieldRev, final int transindex,
			final GaeLocks locks) {
		assert locks.canWrite(fieldAddr);
		assert fieldAddr.getAddressedType() == XType.XFIELD;
		final SEntity e = XGae.get().datastore().createEntity(KeyStructure.createEntityKey(fieldAddr));
		e.setAttribute(PROP_REVISION, fieldRev);
		e.setAttribute(PROP_TRANSINDEX, transindex);

		return XGae.get().datastore().async().putEntity(e);
	}

	/**
	 * Create or update an empty {@link XField} in the GAE datastore. The
	 * created field will have no {@link XValue} and if there existed a field
	 * with a {@link XValue} before, the {@link XValue} will be removed.
	 *
	 * It is up to the caller to acquire enough locks. It is sufficient to only
	 * lock the field itself.
	 *
	 * @param fieldAddr
	 *            The address of the field to add.
	 * @param fieldRev
	 *            The revision number of the current change. This will be saved
	 *            to the field entity.
	 * @param locks
	 *            The locks held by the current process. These are used to
	 *            assert that we are actually allowed to create the entity.
	 */
	protected static Future<SKey> set(final XAddress fieldAddr, final long fieldRev, final GaeLocks locks) {
		return set(fieldAddr, fieldRev, GaeEvents.TRANSINDEX_NONE, locks);
	}

	private static int getTransIndex(final SEntity fieldEntity) {
		final Number transindex = (Number) fieldEntity.getAttribute(PROP_TRANSINDEX);
		return transindex.intValue();
	}

	private static long getFieldRev(final SEntity fieldEntity) {
		final long fieldRev = (Long) fieldEntity.getAttribute(PROP_REVISION);
		return fieldRev;
	}

	@Override
	public XType getType() {
		return XType.XFIELD;
	}

}
