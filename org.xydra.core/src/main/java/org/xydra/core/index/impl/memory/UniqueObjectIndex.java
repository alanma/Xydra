package org.xydra.core.index.impl.memory;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.index.IUniqueObjectIndex;
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
public class UniqueObjectIndex extends AbstractObjectIndex implements IUniqueObjectIndex {
	
	public UniqueObjectIndex(XID fieldId, XWritableObject indexObject) {
		super(fieldId, indexObject);
	}
	
	public void clear() {
		for(XID fieldId : this.indexObject) {
			this.indexObject.removeField(fieldId);
		}
	}
	
	public boolean contains(XValue indexKey) {
		XID key = valueToXID(indexKey);
		XWritableField indexField = this.indexObject.getField(key);
		if(indexField == null) {
			return false;
		}
		return true;
	}
	
	public XID deindex(XReadableObject xo) {
		XReadableField field = xo.getField(this.fieldId);
		if(field == null) {
			return null;
		}
		XValue keyValue = field.getValue();
		return deindex(keyValue);
	}
	
	public XID deindex(XValue key) {
		XID xid = valueToXID(key);
		XWritableField indexField = this.indexObject.getField(xid);
		if(indexField == null) {
			// nothing to do to deindex
			return null;
		}
		XID indexValue = (XID)indexField.getValue();
		assert indexValue != null : "IndexField " + indexField.getID()
		        + " has a null value. Key = " + key;
		XID previous = indexValue;
		this.indexObject.removeField(xid);
		return previous;
	}
	
	public XID index(XReadableObject xo) {
		if(xo == null) {
			throw new IllegalArgumentException("Object may not be null");
		}
		XReadableField field = xo.getField(this.fieldId);
		XValue keyValue = field.getValue();
		return index(keyValue, xo.getID());
	}
	
	public XID index(XValue key, XID value) {
		if(key == null) {
			throw new IllegalArgumentException("key may not be null");
		}
		if(value == null) {
			throw new IllegalArgumentException("value may not be null");
		}
		XID xid = valueToXID(key);
		XWritableField indexField = this.indexObject.createField(xid);
		XID indexValue = (XID)indexField.getValue();
		XID previous;
		if(indexValue == null) {
			previous = null;
		} else {
			previous = indexValue;
		}
		indexField.setValue(value);
		return previous;
	}
	
	public XWritableObject lookup(XWritableModel model, XValue indexKey) {
		XID id = lookupID(indexKey);
		XWritableObject object = model.getObject(id);
		return object;
	}
	
	public XID lookupID(XValue indexKey) {
		XID key = valueToXID(indexKey);
		XWritableField indexField = this.indexObject.getField(key);
		if(indexField == null) {
			return null;
		}
		XID indexValue = (XID)indexField.getValue();
		assert indexValue != null;
		return indexValue;
	}
	
}
