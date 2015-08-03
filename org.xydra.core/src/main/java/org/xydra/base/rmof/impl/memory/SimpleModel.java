package org.xydra.base.rmof.impl.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.annotations.NeverNull;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XSessionModel;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.sharedutils.XyAssert;

/**
 * A simple data container for {@link XWritableModel}/XRevWritableModel.
 *
 * Minimal memory footprint, can be used as data transfer object.
 *
 * @author xamde
 */
public class SimpleModel extends SimpleEntity implements XRevWritableModel, XSessionModel,
		XExistsRevWritableModel {

	private static final long serialVersionUID = 5593443685935758227L;

	// not final for GWT serialisation
	private XAddress address;

	// not final for GWT serialisation
	private Map<XId, XRevWritableObject> objects;

	private long revisionNumber;

	/* Just for GWT */
	protected SimpleModel() {
	}

	public SimpleModel(final XAddress address) {
		this(address, XCommand.NEW);
	}

	public SimpleModel(final XAddress address, final long revisionNumber) {
		assert address != null;
		assert address.getAddressedType() == XType.XMODEL : address;
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.objects = new HashMap<XId, XRevWritableObject>(2);
	}

	public SimpleModel(final XAddress address, final long revisionNumber, final Map<XId, XRevWritableObject> objects) {
		super();
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.objects = objects;
	}

	@Override
	public void addObject(@NeverNull final XRevWritableObject object) {
		XyAssert.xyAssert(object != null);
		assert object != null;
		this.objects.put(object.getId(), object);
	}

	@Override
	public XRevWritableObject createObject(@NeverNull final XId objectId) {
		final XRevWritableObject object = this.objects.get(objectId);
		if (object != null) {
			return object;
		}
		final XRevWritableObject newObject = new SimpleObject(Base.resolveObject(this.address, objectId));
		this.objects.put(objectId, newObject);
		return newObject;
	}

	@Override
	public XAddress getAddress() {
		return this.address;
	}

	@Override
	public XId getId() {
		return this.address.getModel();
	}

	@Override
	public XRevWritableObject getObject(@NeverNull final XId objectId) {
		return this.objects.get(objectId);
	}

	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}

	@Override
	public boolean hasObject(@NeverNull final XId objectId) {
		return this.objects.containsKey(objectId);
	}

	@Override
	public boolean isEmpty() {
		return this.objects.isEmpty();
	}

	@Override
	public Iterator<XId> iterator() {
		return this.objects.keySet().iterator();
	}

	@Override
	public boolean removeObject(@NeverNull final XId objectId) {
		final XRevWritableObject oldObject = this.objects.remove(objectId);
		return oldObject != null;
	}

	@Override
	public void setRevisionNumber(final long rev) {
		this.revisionNumber = rev;
	}

	@Override
	public XType getType() {
		return XType.XMODEL;
	}

	@Override
	public String toString() {
		return this.address + " [" + this.revisionNumber + "], " + this.objects.size() + " objects";
	}

	/**
	 * @param model A model to copy.
	 * @return A copy of the model. Both model share the same objects and fields
	 *         but not the same object list or revision number.
	 */
	public static XRevWritableModel shallowCopy(final XRevWritableModel model) {
		if (model == null) {
			return null;
		}

		final SimpleModel result = new SimpleModel(model.getAddress());
		if (model instanceof SimpleModel) {
			result.objects.putAll(((SimpleModel) model).objects);
		} else {
			for (final XId xid : model) {
				result.addObject(model.getObject(xid));
			}
		}
		return result;
	}

	@Override
	public XSessionModel loadObject(final XId objectId) {
		/* A simpleModel neither can not needs to load anything after creation */
		return this;
	}

	@Override
	public XSessionModel loadAllObjects() {
		/* A SimpleModel neither can nor needs to load anything after creation */
		return this;
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof XReadableModel
				&& XCompareUtils.equalState(this, (XReadableModel) other);
	}

}
