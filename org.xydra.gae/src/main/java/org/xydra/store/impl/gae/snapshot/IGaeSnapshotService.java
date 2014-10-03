package org.xydra.store.impl.gae.snapshot;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.model.XChangeLog;

public interface IGaeSnapshotService {

	/**
	 * @param requestedRevNr
	 *            of the returned snapshot. This method assumes that this
	 *            revision responds to a command that didn't fail (or is -1).
	 *            The most common value (the "current" revision) satisfies that
	 *            criteria.
	 * @param precise
	 *            True if we need the snapshot for exactly the requested
	 *            revision number or false if the snapshot of a higher revision
	 *            number can also be returned.
	 * @return an {@link XReadableModel} by applying all events in the
	 *         {@link XChangeLog} or null if the model was not present at the
	 *         requested revisionNumber
	 */
	XRevWritableModel getModelSnapshot(long requestedRevNr, boolean precise);

	/**
	 * @param modelRevisionNumber
	 *            Revision of model the returned snapshot should belong to. The
	 *            revision number of the object may be lower if the model
	 *            revision was caused by changes to other parts of the
	 *            containing model.
	 * @param precise
	 *            True if we need the snapshot for exactly the requested
	 *            revision number or false if the snapshot of a higher revision
	 *            number can also be returned.
	 * @param objectId
	 * @return an {@link XReadableObject} by applying all events in the
	 *         {@link XChangeLog} or null if the model was not present at the
	 *         requested revisionNumber
	 */
	XRevWritableObject getObjectSnapshot(long modelRevisionNumber, boolean precise, XId objectId);

	/**
	 * @param modelRevisionNumber
	 *            Revision of model the returned snapshot should belong to. The
	 *            revision number of the field may be lower if the model
	 *            revision was caused by changes to other parts of the
	 *            containing object or model.
	 * @param precise
	 *            True if we need the snapshot for exactly the requested
	 *            revision number or false if the snapshot of a higher revision
	 *            number can also be returned.
	 * @param objectId
	 * @param fieldId
	 * @return an {@link XWritableField} by applying all events in the
	 *         {@link XChangeLog} or null if the model was not present at the
	 *         requested revisionNumber
	 */
	XRevWritableField getFieldSnapshot(long modelRevisionNumber, boolean precise, XId objectId,
			XId fieldId);

	/**
	 * Get a snapshot that contains at least those parts specified.
	 * 
	 * @param modelRevisionNumber
	 *            of the returned snapshot. This method assumes that this
	 *            revision responds to a command that didn't fail (or is -1).
	 *            The most common value (the "current" revision) satisfies that
	 *            criteria.
	 * @param parts
	 *            ..
	 * @return While this return is an {@link XRevWritableModel} so that it's
	 *         parts can be added into other {@link XRevWritableModel}, it must
	 *         not be modified.
	 */
	XRevWritableModel getPartialSnapshot(long modelRevisionNumber, Iterable<XAddress> parts);

	XAddress getModelAddress();

	XWritableModel getTentativeModelSnapshot(long currentRevNr);
}
