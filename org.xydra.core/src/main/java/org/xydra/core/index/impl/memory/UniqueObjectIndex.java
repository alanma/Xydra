package org.xydra.core.index.impl.memory;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.index.IUniqueObjectIndex;
import org.xydra.core.model.XObject;
import org.xydra.sharedutils.XyAssert;


/**
 * Index any number of objects by a given fieldId. The value of the field is
 * converted to an internal XId key which is used as the field-ID in another
 * {@link XObject}.
 *
 * @author xamde
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public class UniqueObjectIndex extends AbstractObjectIndex implements IUniqueObjectIndex {

	public UniqueObjectIndex(final XId fieldId, final XWritableObject indexObject) {
		super(fieldId, indexObject);
	}

	@Override
    public void clear() {
		for(final XId fieldId : this.indexObject) {
			this.indexObject.removeField(fieldId);
		}
	}

	@Override
    public boolean contains(final XValue indexKey) {
		final XId key = valueToXId(indexKey);
		final XWritableField indexField = this.indexObject.getField(key);
		if(indexField == null) {
			return false;
		}
		return true;
	}

	@Override
    public XId deindex(final XReadableObject xo) {
		final XReadableField field = xo.getField(this.fieldId);
		if(field == null) {
			return null;
		}
		final XValue keyValue = field.getValue();
		return deindex(keyValue);
	}

	@Override
    public XId deindex(final XValue key) {
		final XId xid = valueToXId(key);
		final XWritableField indexField = this.indexObject.getField(xid);
		if(indexField == null) {
			// nothing to do to deindex
			return null;
		}
		final XId indexValue = (XId)indexField.getValue();
		assert indexValue != null : "IndexField " + indexField.getId()
		        + " has a null value. Key = " + key;
		final XId previous = indexValue;
		this.indexObject.removeField(xid);
		return previous;
	}

	@Override
    public XId index(final XReadableObject xo) {
		if(xo == null) {
			throw new IllegalArgumentException("Object may not be null");
		}
		final XReadableField field = xo.getField(this.fieldId);
		final XValue keyValue = field.getValue();
		return index(keyValue, xo.getId());
	}

	public XId index(final XValue key, final XId value) {
		if(key == null) {
			throw new IllegalArgumentException("key may not be null");
		}
		if(value == null) {
			throw new IllegalArgumentException("value may not be null");
		}
		final XId xid = valueToXId(key);
		final XWritableField indexField = this.indexObject.createField(xid);
		final XId indexValue = (XId)indexField.getValue();
		XId previous;
		if(indexValue == null) {
			previous = null;
		} else {
			previous = indexValue;
		}
		indexField.setValue(value);
		return previous;
	}

	@Override
    public XWritableObject lookup(final XWritableModel model, final XValue indexKey) {
		final XId id = lookupID(indexKey);
		final XWritableObject object = model.getObject(id);
		return object;
	}

	@Override
    public XId lookupID(final XValue indexKey) {
		final XId key = valueToXId(indexKey);
		final XWritableField indexField = this.indexObject.getField(key);
		if(indexField == null) {
			return null;
		}
		final XId indexValue = (XId)indexField.getValue();
		XyAssert.xyAssert(indexValue != null); assert indexValue != null;
		return indexValue;
	}

}
