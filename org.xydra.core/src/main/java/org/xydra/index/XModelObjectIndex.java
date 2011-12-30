package org.xydra.index;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.index.impl.FastEntrySetFactory;
import org.xydra.index.impl.MapSetIndex;


// TODO implement listeners which can be registered on XModels to keep the
// contents of the index up to date

public class XModelObjectIndex {
	private XAddress address;
	private IMapSetIndex<String,XAddress> objectIndex;
	
	public XModelObjectIndex(XModel model) {
		// TODO which kind of factory is best suited?
		this.objectIndex = new MapSetIndex<String,XAddress>(new FastEntrySetFactory<XAddress>());
		this.index(model);
	}
	
	public void index(XModel model) {
		if(this.address != null) {
			this.address = model.getAddress();
		} else {
			throw new RuntimeException("Model already set");
		}
		
		for(XID objectId : model) {
			XObject object = model.getObject(objectId);
			
			index(object);
		}
	}
	
	private void index(XObject object) {
		XAddress objectAddress = object.getAddress();
		for(XID fieldId : object) {
			/*
			 * TODO also check revision numbers of the fields? Maybe thats
			 * slower than just going through their values?
			 */
			XField field = object.getField(fieldId);
			index(objectAddress, field);
		}
	}
	
	public void index(XField field) {
		XAddress fieldAddress = field.getAddress();
		XAddress objectAddress = XX.resolveObject(fieldAddress.getRepository(),
		        fieldAddress.getModel(), fieldAddress.getObject());
		
		index(objectAddress, field);
	}
	
	public void deIndex(XObject object) {
		// TODO implement
	}
	
	public void deIndex(XField field) {
		// TODO implement
	}
	
	public void index(XAddress objectAddress, XField field) {
		XValue value = field.getValue();
		XValueIndexer.indexValue(this.objectIndex, objectAddress, value);
	}
	
}
