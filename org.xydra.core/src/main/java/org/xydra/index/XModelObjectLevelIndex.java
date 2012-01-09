package org.xydra.index;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.value.XValue;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.index.impl.FastEntrySetFactory;
import org.xydra.index.impl.MapSetIndex;


// TODO implement listeners which can be registered on XModels to keep the
// contents of the index up to date

public class XModelObjectLevelIndex {
	private XValueIndexer indexer;
	
	public XModelObjectLevelIndex(XModel model) {
		// TODO which kind of factory is best suited?
		IMapSetIndex<String,XAddress> objectIndex = new MapSetIndex<String,XAddress>(
		        new FastEntrySetFactory<XAddress>());
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
	
	public void index(XField field) {
		XAddress fieldAddress = field.getAddress();
		XAddress objectAddress = XX.resolveObject(fieldAddress.getRepository(),
		        fieldAddress.getModel(), fieldAddress.getObject());
		
		index(objectAddress, field);
	}
	
	public void deIndex(XObject object) {
		XAddress objectAddress = object.getAddress();
		for(XID fieldId : object) {
			XField field = object.getField(fieldId);
			deIndex(objectAddress, field);
		}
	}
	
	public void deIndexObjectByAddress(XAddress objectAddress) {
		// this.indexer.deIndex(objectAddress);
	}
	
	public void deIndex(XField field) {
		XAddress fieldAddress = field.getAddress();
		XAddress objectAddress = XX.resolveObject(fieldAddress.getRepository(),
		        fieldAddress.getModel(), fieldAddress.getObject());
		
		deIndex(objectAddress, field);
	}
	
	public void index(XAddress objectAddress, XField field) {
		XValue value = field.getValue();
		this.indexer.indexValue(objectAddress, value);
	}
	
	/*
	 * FIXME this deindexes the value completely, although there might still be
	 * another field which has the same value - how to handle this? Idea: Maybe
	 * add a counter how often it was indexed?!
	 */
	public void deIndex(XAddress objectAddress, XField field) {
		XValue value = field.getValue();
		this.indexer.deIndexValue(objectAddress, value);
	}
	
	public class XModelObjectLevelIndexModelEventListener implements XModelEventListener {
		
		@Override
		public void onChangeEvent(XModelEvent event) {
			ChangeType type = event.getChangeType();
			
			/*
			 * Only the REMOVE-type is interesting, since adding an object adds
			 * no new values and there is no CHANGE-/TRANSACTION-type event for
			 * objects
			 */
			if(type == ChangeType.REMOVE) {
				deIndexObjectByAddress(event.getChangedEntity());
			}
		}
	}
	
	// TODO Document why no ObjectEventListener is needed
	
	public class XModelObjectLevelIndexFieldEventListener implements XFieldEventListener {
		
		@Override
		public void onChangeEvent(XFieldEvent event) {
			ChangeType type = event.getChangeType();
			XAddress fieldAdr = event.getChangedEntity();
			XAddress objectAdr = XX.resolveObject(fieldAdr.getRepository(), fieldAdr.getModel(),
			        fieldAdr.getField());
			XValue newValue = event.getNewValue();
			
			switch(type) {
			case ADD:
				XModelObjectLevelIndex.this.indexer.indexValue(objectAdr, newValue);
				break;
			case REMOVE:
				/*
				 * TODO How to handle removes? The event doesn't hold the old
				 * value... Same goes for the change-type, because it also
				 * involves a remove-op
				 */
			case CHANGE:
			case TRANSACTION:
				break;
			}
		}
	}
	
}
