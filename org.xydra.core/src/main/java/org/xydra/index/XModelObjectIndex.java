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
	private XValueIndexer indexer;
	
	public XModelObjectIndex(XModel model) {
		// TODO which kind of factory is best suited?
		IMapSetIndex<String,XAddress> objectIndex = new MapSetIndex<String,XAddress>(
		        new FastEntrySetFactory<XAddress>());
		this.indexer = new SimpleValueIndexer(objectIndex);
		
		this.index(model);
	}
	
	public XModelObjectIndex(XModel model, XValueIndexer indexer) {
		this.indexer = indexer;
		
		this.index(model);
		
	}
	
	// should only be executed once (document this!)
	private void index(XModel model) {
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
		this.indexer.indexValue(objectAddress, value);
	}
	
	public void deIndex(XAddress objectAddress, XField field) {
		XValue value = field.getValue();
		this.indexer.deIndexValue(objectAddress, value);
	}
	
}
