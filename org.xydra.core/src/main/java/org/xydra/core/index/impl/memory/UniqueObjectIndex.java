package org.xydra.core.index.impl.memory;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.index.IUniqueObjectIndex;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XValue;


/**
 * Index any number of objects by a given fieldID. The value of the field is
 * converted to an internal XID key which is used as the field-ID in another
 * {@link XObject}.
 * 
 * @author voelkel
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
public class UniqueObjectIndex extends AbstractObjectIndex implements IUniqueObjectIndex {
	
	public UniqueObjectIndex(XID fieldID, XObject indexObject) {
		super(fieldID, indexObject);
	}
	
	public XID index(XObject xo) {
		if(xo == null) {
			throw new IllegalArgumentException("Object may not be null");
		}
		XField field = xo.getField(this.fieldID);
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
		XField indexField = this.indexObject.createField(this.actor, xid);
		XIDValue indexValue = (XIDValue)indexField.getValue();
		XID previous;
		if(indexValue == null) {
			previous = null;
		} else {
			previous = indexValue.contents();
		}
		indexField.setValue(this.actor, X.getValueFactory().createIDValue(value));
		return previous;
	}
	
	public XID deindex(XObject xo) {
		XField field = xo.getField(this.fieldID);
		XValue keyValue = field.getValue();
		return deindex(keyValue, xo.getID());
	}
	
	public XID deindex(XValue key, XID value) {
		XID xid = valueToXID(key);
		XField indexField = this.indexObject.createField(this.actor, xid);
		if(indexField == null) {
			return null;
		}
		XIDValue indexValue = (XIDValue)indexField.getValue();
		assert indexValue != null;
		XID previous = indexValue.contents();
		this.indexObject.removeField(this.actor, xid);
		return previous;
	}
	
	public XID lookupID(XValue indexKey) {
		XID key = valueToXID(indexKey);
		XField indexField = this.indexObject.getField(key);
		if(indexField == null) {
			return null;
		}
		XIDValue indexValue = (XIDValue)indexField.getValue();
		assert indexValue != null;
		return indexValue.contents();
	}
	
	public XObject lookup(XModel model, XValue indexKey) {
		XID id = lookupID(indexKey);
		XObject object = model.getObject(id);
		return object;
	}
	
}
