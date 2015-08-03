package org.xydra.store.impl.gae.ng;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.core.XCopyUtils;
import org.xydra.sharedutils.XyAssert;

/**
 * An object snapshot that reflects a tentative version. It can be stored in the
 * GAE datastore.
 *
 * Basically an {@link XRevWritableObject} + {@link #exists()}
 *
 * @author xamde
 */
public class TentativeObjectState implements Serializable, XRevWritableObject {

	private static final long serialVersionUID = -1351865686747885441L;

	private long modelRevision;

	@NeverNull
	private XRevWritableObject object;

	private boolean objectExists;

	@Override
	public boolean exists() {
		return this.objectExists;
	}

	@Override
	public String toString() {
		return getAddress() + " [" + (this.objectExists ? "exists" : "-na-") + "]"
				+ " [model:" + this.modelRevision + "] " + this.object;
	}

	/**
	 * @param object
	 *            is snapshotted
	 * @param objectExists
	 * @param modelRev
	 */
	public TentativeObjectState(@NeverNull final XReadableObject object, final boolean objectExists,
			final long modelRev) {
		XyAssert.xyAssert(object != null);
		this.object = XCopyUtils.createSnapshot(object);
		this.objectExists = objectExists;
		this.modelRevision = modelRev;
	}

	public XRevWritableObject asRevWritableObject() {
		return this.object;
	}

	@Override
	public XAddress getAddress() {
		return this.object.getAddress();
	}

	@Override
	public XRevWritableField getField(final XId fieldId) {
		return this.object.getField(fieldId);
	}

	@Override
	public XId getId() {
		return getAddress().getObject();
	}

	public long getModelRevision() {
		return this.modelRevision;
	}

	@Override
	public long getRevisionNumber() {
		return this.object.getRevisionNumber();
	}

	@Override
	public XType getType() {
		return XType.XOBJECT;
	}

	@Override
	public boolean hasField(final XId fieldId) {
		return this.object.hasField(fieldId);
	}

	@Override
	public boolean isEmpty() {
		return this.object.isEmpty();
	}

	@Override
	public Iterator<XId> iterator() {
		return this.object.iterator();
	}

	public void setModelRev(final long modelRev) {
		this.modelRevision = modelRev;
	}

	public TentativeObjectState copy() {
		return new TentativeObjectState(XCopyUtils.createSnapshot(this.object), this.objectExists,
				this.modelRevision);
	}

	@Override
	public boolean removeField(final XId fieldId) {
		return this.object.removeField(fieldId);
	}

	@Override
	public void addField(final XRevWritableField field) {
		this.object.addField(field);
	}

	@Override
	public XRevWritableField createField(final XId fieldId) {
		return this.object.createField(fieldId);
	}

	@Override
	public void setRevisionNumber(final long rev) {
		this.object.setRevisionNumber(rev);
	}

	public void setObjectExists(final boolean objectExists) {
		this.objectExists = objectExists;
	}

	public void setObjectState(final XReadableObject object) {
		this.object = XCopyUtils.createSnapshot(object);
	}

}
