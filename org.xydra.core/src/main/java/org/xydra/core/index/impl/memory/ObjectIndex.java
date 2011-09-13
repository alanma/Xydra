package org.xydra.core.index.impl.memory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XValue;
import org.xydra.core.index.IObjectIndex;
import org.xydra.core.model.XObject;


/**
 * Index any number of objects by a given fieldId. The value of the field is
 * converted to an internal XID key which is used as the field-ID in another
 * {@link XObject}.
 * 
 * @author voelkel
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public class ObjectIndex extends AbstractObjectIndex implements IObjectIndex {
	
	public ObjectIndex(XID fieldId, XWritableObject indexObject) {
		super(fieldId, indexObject);
	}
	
	@Override
    public void deindex(XReadableObject xo) {
		XReadableField field = xo.getField(this.fieldId);
		XValue keyValue = field.getValue();
		deindex(keyValue, xo.getID());
	}
	
	public void deindex(XValue key, XID value) {
		XID xid = valueToXID(key);
		XWritableField indexField = this.indexObject.createField(xid);
		XValue indexValue = indexField.getValue();
		XIDSetValue indexedIds;
		assert indexValue != null;
		XIDSetValue currentIndexedIds = (XIDSetValue)indexValue;
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
		index(keyValue, xo.getID());
	}
	
	public void index(XValue key, XID value) {
		XID xid = valueToXID(key);
		XWritableField indexField = this.indexObject.createField(xid);
		XValue indexValue = indexField.getValue();
		XIDSetValue indexedIds;
		if(indexValue == null) {
			indexedIds = X.getValueFactory().createIDSetValue(new XID[] { value });
		} else {
			XIDSetValue currentIndexedIds = (XIDSetValue)indexValue;
			indexedIds = currentIndexedIds.add(value);
		}
		indexField.setValue(indexedIds);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.xydra.core.ext.index.IObjectIndex#lookup(org.xydra.core.model.XModel,
	 * org.xydra.core.value.XValue)
	 */
	@Override
    public Set<XWritableObject> lookup(XWritableModel model, XValue indexKey) {
		Set<XID> ids = lookupIDs(indexKey);
		Set<XWritableObject> objects = new HashSet<XWritableObject>();
		for(XID id : ids) {
			XWritableObject object = model.getObject(id);
			objects.add(object);
		}
		return objects;
	}
	
	public Set<XID> lookupIDs(XValue indexKey) {
		XID key = valueToXID(indexKey);
		XWritableField indexField = this.indexObject.getField(key);
		if(indexField == null) {
			// nothing indexed
			return Collections.emptySet();
		}
		XValue indexValue = indexField.getValue();
		assert indexValue != null : "otherwise field should have been removed";
		XIDSetValue indexedIds = (XIDSetValue)indexValue;
		return indexedIds.toSet();
	}
	
}
