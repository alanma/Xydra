package org.xydra.core.index.impl.memory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XIdSetValue;
import org.xydra.base.value.XValue;
import org.xydra.core.index.IObjectIndex;
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
public class ObjectIndex extends AbstractObjectIndex implements IObjectIndex {

	public ObjectIndex(final XId fieldId, final XWritableObject indexObject) {
		super(fieldId, indexObject);
	}

	@Override
    public void deindex(final XReadableObject xo) {
		final XReadableField field = xo.getField(this.fieldId);
		final XValue keyValue = field.getValue();
		deindex(keyValue, xo.getId());
	}

	public void deindex(final XValue key, final XId value) {
		final XId xid = valueToXId(key);
		final XWritableField indexField = this.indexObject.createField(xid);
		final XValue indexValue = indexField.getValue();
		XIdSetValue indexedIds;
		XyAssert.xyAssert(indexValue != null); assert indexValue != null;
		final XIdSetValue currentIndexedIds = (XIdSetValue)indexValue;
		indexedIds = currentIndexedIds.remove(value);
		if(indexedIds.size() == 0) {
			// remove empty field
			this.indexObject.removeField(xid);
		} else {
			// set remaining entries
			indexField.setValue(indexedIds);
		}
	}

	@Override
    public void index(final XReadableObject xo) {
		final XReadableField field = xo.getField(this.fieldId);
		final XValue keyValue = field.getValue();
		index(keyValue, xo.getId());
	}

	public void index(final XValue key, final XId value) {
		final XId xid = valueToXId(key);
		final XWritableField indexField = this.indexObject.createField(xid);
		final XValue indexValue = indexField.getValue();
		XIdSetValue indexedIds;
		if(indexValue == null) {
			indexedIds = BaseRuntime.getValueFactory().createIdSetValue(new XId[] { value });
		} else {
			final XIdSetValue currentIndexedIds = (XIdSetValue)indexValue;
			indexedIds = currentIndexedIds.add(value);
		}
		indexField.setValue(indexedIds);
	}


	@Override
    public Set<XWritableObject> lookup(final XWritableModel model, final XValue indexKey) {
		final Set<XId> ids = lookupIDs(indexKey);
		final Set<XWritableObject> objects = new HashSet<XWritableObject>();
		for(final XId id : ids) {
			final XWritableObject object = model.getObject(id);
			objects.add(object);
		}
		return objects;
	}

	public Set<XId> lookupIDs(final XValue indexKey) {
		final XId key = valueToXId(indexKey);
		final XWritableField indexField = this.indexObject.getField(key);
		if(indexField == null) {
			// nothing indexed
			return Collections.emptySet();
		}
		final XValue indexValue = indexField.getValue();
		assert indexValue != null : "otherwise field should have been removed";
		final XIdSetValue indexedIds = (XIdSetValue)indexValue;
		return indexedIds.toSet();
	}

}
