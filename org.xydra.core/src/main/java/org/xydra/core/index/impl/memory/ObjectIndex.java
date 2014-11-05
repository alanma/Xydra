package org.xydra.core.index.impl.memory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XIdSetValue;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
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
	
	public ObjectIndex(XId fieldId, XWritableObject indexObject) {
		super(fieldId, indexObject);
	}
	
	@Override
    public void deindex(XReadableObject xo) {
		XReadableField field = xo.getField(this.fieldId);
		XValue keyValue = field.getValue();
		deindex(keyValue, xo.getId());
	}
	
	public void deindex(XValue key, XId value) {
		XId xid = valueToXId(key);
		XWritableField indexField = this.indexObject.createField(xid);
		XValue indexValue = indexField.getValue();
		XIdSetValue indexedIds;
		XyAssert.xyAssert(indexValue != null); assert indexValue != null;
		XIdSetValue currentIndexedIds = (XIdSetValue)indexValue;
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
    public void index(XReadableObject xo) {
		XReadableField field = xo.getField(this.fieldId);
		XValue keyValue = field.getValue();
		index(keyValue, xo.getId());
	}
	
	public void index(XValue key, XId value) {
		XId xid = valueToXId(key);
		XWritableField indexField = this.indexObject.createField(xid);
		XValue indexValue = indexField.getValue();
		XIdSetValue indexedIds;
		if(indexValue == null) {
			indexedIds = X.getValueFactory().createIdSetValue(new XId[] { value });
		} else {
			XIdSetValue currentIndexedIds = (XIdSetValue)indexValue;
			indexedIds = currentIndexedIds.add(value);
		}
		indexField.setValue(indexedIds);
	}
	
	
	@Override
    public Set<XWritableObject> lookup(XWritableModel model, XValue indexKey) {
		Set<XId> ids = lookupIDs(indexKey);
		Set<XWritableObject> objects = new HashSet<XWritableObject>();
		for(XId id : ids) {
			XWritableObject object = model.getObject(id);
			objects.add(object);
		}
		return objects;
	}
	
	public Set<XId> lookupIDs(XValue indexKey) {
		XId key = valueToXId(indexKey);
		XWritableField indexField = this.indexObject.getField(key);
		if(indexField == null) {
			// nothing indexed
			return Collections.emptySet();
		}
		XValue indexValue = indexField.getValue();
		assert indexValue != null : "otherwise field should have been removed";
		XIdSetValue indexedIds = (XIdSetValue)indexValue;
		return indexedIds.toSet();
	}
	
}
