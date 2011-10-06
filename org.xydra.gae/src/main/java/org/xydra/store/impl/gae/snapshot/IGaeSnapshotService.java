package org.xydra.store.impl.gae.snapshot;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.core.model.XChangeLog;


public interface IGaeSnapshotService {
	
	/**
	 * @param requestedRevNr of the returned snapshot
	 * @param precise True if we need the snapshot for exactly the requested
	 *            revision number or false if the snapshot of a higher revision
	 *            number can also be returned.
	 * @return an {@link XReadableModel} by applying all events in the
	 *         {@link XChangeLog} or null if the model was not present at the
	 *         requested revisionNumber
	 */
	public XRevWritableModel getModelSnapshot(long requestedRevNr, boolean precise);
	
	/**
	 * @param modelRevisionNumber Revision of model the returned snapshot should
	 *            belong to. The revision number of the object may be lower if
	 *            the model revision was caused by changes to other parts of the
	 *            containing model.
	 * @param precise True if we need the snapshot for exactly the requested
	 *            revision number or false if the snapshot of a higher revision
	 *            number can also be returned.
	 * @return an {@link XReadableObject} by applying all events in the
	 *         {@link XChangeLog} or null if the model was not present at the
	 *         requested revisionNumber
	 */
	public XRevWritableObject getObjectSnapshot(long modelRevisionNumber, boolean precise,
	        XID objectId);
	
	/**
	 * @param modelRevisionNumber Revision of model the returned snapshot should
	 *            belong to. The revision number of the field may be lower if
	 *            the model revision was caused by changes to other parts of the
	 *            containing object or model.
	 * @param precise True if we need the snapshot for exactly the requested
	 *            revision number or false if the snapshot of a higher revision
	 *            number can also be returned.
	 * @return an {@link XWritableField} by applying all events in the
	 *         {@link XChangeLog} or null if the model was not present at the
	 *         requested revisionNumber
	 */
	public XRevWritableField getFieldSnapshot(long modelRevisionNumber, boolean precise,
	        XID objectId, XID fieldId);
	
	XAddress getModelAddress();
}
