package org.xydra.index;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XIDSortedSetValue;
import org.xydra.base.value.XStringSetValue;
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
	
	public void index(XAddress objectAddress, XField field) {
		XValue value = field.getValue();
		
		switch(value.getType()) {
		case Address:
			indexAddressValue(objectAddress, (XAddress)value);
			break;
		case AddressList:
			indexAddressListValue(objectAddress, (XAddressListValue)value);
			break;
		case AddressSet:
			indexAddressSetValue(objectAddress, (XAddressSetValue)value);
			break;
		case AddressSortedSet:
			// TODO Probably the same as indexing XAddressSetValue?
			indexAddressSortedSetValue(objectAddress, (XAddressSortedSetValue)value);
			break;
		case Boolean:
			indexSingleSimpleTypeValue(objectAddress, value);
			break;
		case BooleanList:
			indexSimpleTypeListValue(objectAddress, value);
			break;
		case ByteList:
			indexSimpleTypeListValue(objectAddress, value);
			break;
		case Double:
			indexSingleSimpleTypeValue(objectAddress, value);
			break;
		case DoubleList:
			indexSimpleTypeListValue(objectAddress, value);
			break;
		case Id:
			indexIdValue(objectAddress, (XID)value);
			break;
		case IdList:
			indexIdListValue(objectAddress, (XIDListValue)value);
			break;
		case IdSet:
			indexIdSetValue(objectAddress, (XIDSetValue)value);
			break;
		case IdSortedSet:
			// TODO Probably the same as indexing XIDSetValue?
			indexIdSortedSet(objectAddress, (XIDSortedSetValue)value);
			break;
		case Integer:
			indexSingleSimpleTypeValue(objectAddress, value);
			break;
		case IntegerList:
			indexSimpleTypeListValue(objectAddress, value);
			break;
		case Long:
			indexSingleSimpleTypeValue(objectAddress, value);
			break;
		case LongList:
			indexSimpleTypeListValue(objectAddress, value);
			break;
		case String:
			indexSingleSimpleTypeValue(objectAddress, value);
			break;
		case StringList:
			indexSimpleTypeListValue(objectAddress, value);
			break;
		case StringSet:
			indexStringSetValue(objectAddress, (XStringSetValue)value);
			break;
		}
	}
	
	public void deIndex(XObject object) {
		// TODO implement
	}
	
	public void deIndex(XField field) {
		// TODO implement
	}
	
	/*
	 * TODO move all these methods into a separate helper class and rewrite them
	 * so that they return the String which will be used for indexing
	 */
	private void indexStringSetValue(XAddress objectAddress, XStringSetValue value) {
		// TODO Auto-generated method stub
		
	}
	
	private void indexIdSortedSet(XAddress objectAddress, XIDSortedSetValue value) {
		// TODO Auto-generated method stub
		
	}
	
	private void indexIdSetValue(XAddress objectAddress, XIDSetValue value) {
		// TODO Auto-generated method stub
		
	}
	
	private void indexIdListValue(XAddress objectAddress, XIDListValue value) {
		// TODO Auto-generated method stub
		
	}
	
	private void indexIdValue(XAddress objectAddress, XID value) {
		this.objectIndex.index("" + value.toString(), objectAddress);
	}
	
	private void indexSimpleTypeListValue(XAddress address, XValue value) {
		// TODO Auto-generated method stub
		
	}
	
	private void indexSingleSimpleTypeValue(XAddress address, XValue value) {
		// simple types = int, long, double, String, boolean, byte, XID
		this.objectIndex.index("" + value.toString(), address);
	}
	
	private void indexAddressSortedSetValue(XAddress address, XAddressSortedSetValue value) {
		// TODO Auto-generated method stub
		
	}
	
	private void indexAddressSetValue(XAddress address, XAddressSetValue value) {
		// TODO Auto-generated method stub
		
	}
	
	private void indexAddressListValue(XAddress address, XAddressListValue value) {
		// TODO Auto-generated method stub
		
	}
	
	private void indexAddressValue(XAddress adress, XAddress value) {
		// TODO Auto-generated method stub
		
	}
}
