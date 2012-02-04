package org.xydra.valueindex;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.impl.FastEntrySetFactory;
import org.xydra.index.impl.MapSetIndex;
import org.xydra.index.query.EqualsConstraint;


public class XModelObjectLevelIndex {
	private XValueIndexer indexer;
	private IMapSetIndex<String,ValueIndexEntry> index;
	
	public XModelObjectLevelIndex(XModel model) {
		// TODO change it to our own implementation as soon as it's ready
		this.index = new MapSetIndex<String,ValueIndexEntry>(
		        new FastEntrySetFactory<ValueIndexEntry>());
		this.indexer = new SimpleValueIndexer(this.index);
		
		this.index(model);
	}
	
	public XModelObjectLevelIndex(XModel model, XValueIndexer indexer) {
		this.indexer = indexer;
		this.index = indexer.getIndex();
		
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
	
	public void updateIndex(XEvent event) {
		// TODO implement event-api
	}
	
	public void index(XReadableField field) {
		XAddress fieldAddress = field.getAddress();
		XAddress objectAddress = XX.resolveObject(fieldAddress.getRepository(),
		        fieldAddress.getModel(), fieldAddress.getObject());
		
		index(objectAddress, field);
	}
	
	// TODO why is there an xaddress parameter here and in the next method?
	public void updateIndex(XAddress objectAddress, XReadableField oldField, XReadableField newField) {
		XAddress address = oldField.getAddress();
		
		if(objectAddress.getAddressedType() != XType.XOBJECT) {
			throw new RuntimeException("objectAddress is no valid Object-XAddress, but an "
			        + objectAddress.getAddressedType() + "-Address.");
		}
		
		if(!address.equals(newField.getAddress())) {
			throw new RuntimeException("oldField and newField do not have the same address.");
		}
		
		updateIndexWithoutCheck(objectAddress, oldField, newField);
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
	
	public void deIndex(XAddress objectAddress) {
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
	
	public void deIndex(XAddress objectAddress, XReadableField field) {
		XValue value = field.getValue();
		this.indexer.deIndexValue(objectAddress, value);
	}
	
	public List<XAddress> search(String key) {
		// IMPROVE rather simple search algorithm at the moment...
		
		/*
		 * the index uses lower case strings only, so we need to transform the
		 * given key appropriately
		 */
		String indexKey = key.toLowerCase();
		LinkedList<XAddress> list = new LinkedList<XAddress>();
		
		EqualsConstraint<String> constraint = new EqualsConstraint<String>(indexKey);
		Iterator<ValueIndexEntry> iterator = this.index.constraintIterator(constraint);
		
		while(iterator.hasNext()) {
			list.add(iterator.next().getAddress());
		}
		
		return list;
	}
}
