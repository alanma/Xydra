package org.xydra.base.rmof.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XSessionModel;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.XCompareUtils;


/**
 * A simple data container for {@link XWritableModel}.
 * 
 * Minimal memory footprint, can be used as data transfer object.
 * 
 * @author voelkel
 */
public class SimpleModel implements Serializable, XRevWritableModel, XSessionModel {
	
	private static final long serialVersionUID = 5593443685935758227L;
	
	// not final for GWT serialisation
	private XAddress address;
	// not final for GWT serialisation
	private Map<XID,XRevWritableObject> objects;
	private long revisionNumber;
	private boolean modelExists = true;
	
	public boolean modelExists() {
		return this.modelExists;
	}
	
	// TODO is this used?
	public void setModelExists(boolean modelExists) {
		this.modelExists = modelExists;
	}
	
	/* Just for GWT */
	protected SimpleModel() {
	}
	
	public SimpleModel(XAddress address) {
		this(address, XCommand.NEW);
	}
	
	public SimpleModel(XAddress address, long revisionNumber) {
		assert address.getAddressedType() == XType.XMODEL : address;
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.objects = new HashMap<XID,XRevWritableObject>(2);
	}
	
	public SimpleModel(XAddress address, long revisionNumber, Map<XID,XRevWritableObject> objects) {
		super();
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.objects = objects;
	}
	
	@Override
	public void addObject(XRevWritableObject object) {
		this.objects.put(object.getId(), object);
	}
	
	@Override
	public XRevWritableObject createObject(XID objectId) {
		XRevWritableObject object = this.objects.get(objectId);
		if(object != null) {
			return object;
		}
		XRevWritableObject newObject = new SimpleObject(XX.resolveObject(this.address, objectId));
		this.objects.put(objectId, newObject);
		return newObject;
	}
	
	@Override
	public XAddress getAddress() {
		return this.address;
	}
	
	@Override
	public XID getId() {
		return this.address.getModel();
	}
	
	@Override
	public XRevWritableObject getObject(XID objectId) {
		return this.objects.get(objectId);
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	@Override
	public boolean hasObject(XID objectId) {
		return this.objects.containsKey(objectId);
	}
	
	@Override
	public boolean isEmpty() {
		return this.objects.isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.objects.keySet().iterator();
	}
	
	@Override
	public boolean removeObject(XID objectId) {
		XRevWritableObject oldObject = this.objects.remove(objectId);
		return oldObject != null;
	}
	
	@Override
	public void setRevisionNumber(long rev) {
		this.revisionNumber = rev;
	}
	
	@Override
	public XType getType() {
		return XType.XMODEL;
	}
	
	@Override
	public int hashCode() {
		return (int)(this.getAddress().hashCode() + this.getRevisionNumber());
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XReadableModel
		        && XCompareUtils.equalState(this, (XReadableModel)other);
	}
	
	@Override
	public String toString() {
		return this.address + " [" + this.revisionNumber + "], " + this.objects.size() + " objects";
	}
	
	/**
	 * @param model A model to copy.
	 * @return A copy of the model. Both model share the same objects and fields
	 *         but not the same object list or revision number.
	 */
	public static XRevWritableModel shallowCopy(XRevWritableModel model) {
		
		if(model == null) {
			return null;
		}
		
		SimpleModel result = new SimpleModel(model.getAddress());
		
		if(model instanceof SimpleModel) {
			result.objects.putAll(((SimpleModel)model).objects);
		} else {
			for(XID xid : model) {
				result.addObject(model.getObject(xid));
			}
		}
		
		return result;
	}
	
	@Override
	public XSessionModel loadObject(XID objectId) {
		/* A simpleModel neither can not needs to load anything after creation */
		return this;
	}
	
	@Override
	public XSessionModel loadAllObjects() {
		/* A simpleModel neither can not needs to load anything after creation */
		return this;
	}
	
}
