package org.xydra.valueindex;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.index.query.EqualsConstraint;


/**
 * An index for {@link XModel XModels}. Indexes the contents of the
 * {@link XValue XValues} and stores them together with the {@link XAddress} of
 * the {@link XObject} containing the {@link XField} which holds the value.
 * 
 * The index entries are "String -> XAddress & XValue". An {@link XValueIndexer}
 * is needed to get the String representations used for indexing.
 * 
 * @author Kaidel
 * 
 */

/*
 * TODO Keep in mind that it will NOT be possible to iterate over all existing
 * keys in the planned implementation, so do not use keyIterator etc. on the
 * index
 */

public class XModelObjectLevelIndex {
	private XValueIndexer indexer;
	private ValueIndex index;
	
	/**
	 * Creates a new index for the given {@link XModel} using the given
	 * {@link XValueIndexer}.
	 * 
	 * @param model The {@link XModel} which will be indexed.
	 * @param indexer The {@link XValueIndexer} which is to be used to get the
	 *            Strings used for indexing.
	 */
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
	
	/*
	 * deIndex methods for event types apart from XFieldEvents are not possible,
	 * because:
	 * 
	 * XModelEvents: Only the REMOVE-Case is interesting here. We would need to
	 * remove a complete object only by it's address, which is not possible,
	 * because we do not have the possibility to iterate over all keys (to look
	 * up the entry which contain the object address) nor do we have the
	 * possibility to look up the values which were stored in the XObject.
	 * 
	 * XObjectEvents: Only the REMOVE-Case is interesting here. We would need to
	 * remove a complete field only by it's address. The argumentation why this
	 * is not possible is analogous to the argumentation for XModelEvents.
	 * 
	 * XTransactionEvents: Typically contain both XModel- and XObjectEvents,
	 * which cannot be handled. Transaction consisting only of XFieldEvents also
	 * cannot be handled, since the Transaction contains no information over the
	 * removed values etc., which is needed for deindexing.
	 */

	public void updateIndex(XFieldEvent event, XValue oldValue) {
		/*
		 * TODO is it a good idea to put the removed value as a parameter?
		 */

		XAddress objectAddress = XX.resolveObject(event.getRepositoryId(), event.getModelId(),
		        event.getObjectId());
		
		switch(event.getChangeType()) {
		case ADD:
			XValue addedValue = event.getNewValue();
			this.indexer.indexValue(objectAddress, addedValue);
			break;
		case REMOVE:
			this.indexer.deIndexValue(objectAddress, oldValue);
			break;
		case CHANGE:
			this.indexer.deIndexValue(objectAddress, oldValue);
			addedValue = event.getNewValue();
			this.indexer.indexValue(objectAddress, addedValue);
			break;
		case TRANSACTION: // TRANSACTION is not possible for XFieldEvents
			break;
		}
	}
	
	public void index(XReadableField field) {
		XAddress fieldAddress = field.getAddress();
		XAddress objectAddress = XX.resolveObject(fieldAddress.getRepository(),
		        fieldAddress.getModel(), fieldAddress.getObject());
		
		index(objectAddress, field);
	}
	
	public void updateIndex(XReadableField oldField, XReadableField newField) {
		XAddress oldAddress = oldField.getAddress();
		XAddress newAddress = newField.getAddress();
		
		if(!oldAddress.equals(newAddress)) {
			throw new RuntimeException(
			        "oldField and newField do not have the same address and therefore aren't different versions of the same field.");
		}
		
		XAddress objectAddress = XX.resolveObject(oldAddress.getRepository(),
		        oldAddress.getModel(), oldAddress.getObject());
		
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
