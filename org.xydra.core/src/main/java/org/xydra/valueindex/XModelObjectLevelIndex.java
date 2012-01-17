package org.xydra.valueindex;

import java.util.HashSet;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.impl.FastEntrySetFactory;
import org.xydra.index.impl.MapSetIndex;


public class XModelObjectLevelIndex {
	private XValueIndexer indexer;
	
	public XModelObjectLevelIndex(XModel model) {
		// TODO which kind of factory is best suited?
		IMapSetIndex<String,AddressValueCounterTriple> objectIndex = new MapSetIndex<String,AddressValueCounterTriple>(
		        new FastEntrySetFactory<AddressValueCounterTriple>());
		this.indexer = new SimpleValueIndexer(objectIndex);
		
		this.index(model);
	}
	
	public XModelObjectLevelIndex(XModel model, XValueIndexer indexer) {
		this.indexer = indexer;
		
		this.index(model);
		
	}
	
	// should only be executed once (document this!) - executing more than once
	// is dangerous
	private void index(XModel model) {
		for(XID objectId : model) {
			XObject object = model.getObject(objectId);
			
			index(object);
		}
	}
	
	/*
	 * TODO Document why this is private and not public (indexing a new object
	 * without any fields makes no sense and there is no way to add a new object
	 * with fields - it would actually be dangerous to make this public, like
	 * with index(XModel))
	 */
	private void index(XObject object) {
		XAddress objectAddress = object.getAddress();
		for(XID fieldId : object) {
			XField field = object.getField(fieldId);
			index(objectAddress, field);
		}
	}
	
	public void updateIndex(XReadableObject oldObject, XReadableObject newObject) {
		XAddress address = oldObject.getAddress();
		
		if(!address.equals(newObject.getAddress())) {
			throw new RuntimeException("oldObject and newObject do not have the same address.");
		}
		
		if(newObject.getRevisionNumber() > oldObject.getRevisionNumber()) {
			// objects changed
			
			HashSet<XID> intersection = new HashSet<XID>();
			
			for(XID id : oldObject) {
				XReadableField oldField = oldObject.getField(id);
				
				if(newObject.hasField(id)) {
					intersection.add(id);
					
					XReadableField newField = newObject.getField(id);
					
					updateIndexWithoutCheck(address, oldField, newField);
				} else {
					// field was completely removed
					deIndex(address, oldField);
				}
			}
			
			for(XID id : newObject) {
				if(!intersection.contains(id)) {
					// field is new
					index(address, newObject.getField(id));
				}
			}
		}
	}
	
	public void index(XReadableField field) {
		XAddress fieldAddress = field.getAddress();
		XAddress objectAddress = XX.resolveObject(fieldAddress.getRepository(),
		        fieldAddress.getModel(), fieldAddress.getObject());
		
		index(objectAddress, field);
	}
	
	public void updateIndex(XAddress objectAddress, XReadableField oldField, XReadableField newField) {
		XAddress address = oldField.getAddress();
		
		if(objectAddress.getAddressedType() != XType.XOBJECT) {
			throw new RuntimeException("objectAddress is no valid Object-XAddress, but an "
			        + objectAddress.getAddressedType() + "-Address.");
		}
		
		if(!address.equals(newField.getAddress())) {
			throw new RuntimeException("oldField and newField do not have the same address.");
		}
	}
	
	// TODO find better name
	private void updateIndexWithoutCheck(XAddress objectAddress, XReadableField oldField,
	        XReadableField newField) {
		if(newField.getRevisionNumber() > oldField.getRevisionNumber()) {
			// value of field was changed
			deIndex(objectAddress, oldField);
			index(objectAddress, newField);
		}
	}
	
	public void deIndex(XReadableObject object) {
		XAddress objectAddress = object.getAddress();
		for(XID fieldId : object) {
			XReadableField field = object.getField(fieldId);
			deIndex(objectAddress, field);
		}
	}
	
	public void deIndexObjectByAddress(XAddress objectAddress) {
		if(objectAddress.getAddressedType() != XType.XOBJECT) {
			throw new RuntimeException("objectAddress is no valid Object-XAddress, but an "
			        + objectAddress.getAddressedType() + "-Address.");
		}
		
		// TODO implement
		// this.indexer.deIndex(objectAddress);
	}
	
	public void deIndex(XReadableField field) {
		XAddress fieldAddress = field.getAddress();
		XAddress objectAddress = XX.resolveObject(fieldAddress.getRepository(),
		        fieldAddress.getModel(), fieldAddress.getObject());
		
		deIndex(objectAddress, field);
	}
	
	public void index(XAddress objectAddress, XReadableField field) {
		if(objectAddress.getAddressedType() != XType.XOBJECT) {
			throw new RuntimeException("objectAddress is no valid Object-XAddress, but an "
			        + objectAddress.getAddressedType() + "-Address.");
		}
		
		XValue value = field.getValue();
		this.indexer.indexValue(objectAddress, value);
	}
	
	public void updateIndex(XAddress objectAddress, XValue oldValue, XValue newValue) {
		if(objectAddress.getAddressedType() != XType.XOBJECT) {
			throw new RuntimeException("objectAddress is no valid Object-XAddress, but an "
			        + objectAddress.getAddressedType() + "-Address.");
		}
		
		if(!oldValue.equals(newValue)) {
			this.indexer.deIndexValue(objectAddress, oldValue);
			this.indexer.indexValue(objectAddress, newValue);
		}
	}
	
	public void deIndex(XAddress objectAddress, XReadableField field) {
		XValue value = field.getValue();
		this.indexer.deIndexValue(objectAddress, value);
	}
	
	public List<XAddress> search(String key) {
		// TODO implement as "1 Token Search"
		
		return null;
	}
}
