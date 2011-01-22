package org.xydra.core.index.impl.memory;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.base.XID;
import org.xydra.base.value.XValue;
import org.xydra.core.index.IUniqueObjectIndex;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * Index any number of objects by a given fieldId. The value of the field is
 * converted to an internal XID key which is used as the field-ID in another
 * {@link XObject}.
 * 
 * @author voelkel
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
public class UniqueObjectIndex extends AbstractObjectIndex implements IUniqueObjectIndex {
	
	public UniqueObjectIndex(XID fieldId, XObject indexObject) {
		super(fieldId, indexObject);
	}
	
	public void clear() {
		for(XID fieldId : this.indexObject) {
			this.indexObject.removeField(fieldId);
		}
	}
	
	public boolean contains(XValue indexKey) {
		XID key = valueToXID(indexKey);
		XField indexField = this.indexObject.getField(key);
		if(indexField == null) {
			return false;
		}
		return true;
	}
	
	public XID deindex(XObject xo) {
		XField field = xo.getField(this.fieldId);
		if(field == null) {
			return null;
		}
		XValue keyValue = field.getValue();
		return deindex(keyValue);
	}
	
	public XID deindex(XValue key) {
		XID xid = valueToXID(key);
		XField indexField = this.indexObject.getField(xid);
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
	
	public XID index(XObject xo) {
		if(xo == null) {
			throw new IllegalArgumentException("Object may not be null");
		}
		XField field = xo.getField(this.fieldId);
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
		XField indexField = this.indexObject.createField(xid);
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
	
	public XObject lookup(XModel model, XValue indexKey) {
		XID id = lookupID(indexKey);
		XObject object = model.getObject(id);
		return object;
	}
	
	public XID lookupID(XValue indexKey) {
		XID key = valueToXID(indexKey);
		XField indexField = this.indexObject.getField(key);
		if(indexField == null) {
			return null;
		}
		XID indexValue = (XID)indexField.getValue();
		assert indexValue != null;
		return indexValue;
	}
	
}
