package org.xydra.store.base;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableModel;


/**
 * A simple data container.
 * 
 * @author voelkel
 */
public class SimpleModel implements XWritableModel, Serializable {
	
	private static final long serialVersionUID = 5593443685935758227L;
	protected XAddress address;
	protected long revisionNumber;
	protected Set<XID> objectIds;
	
	public SimpleModel(XAddress address, long revisionNumber, Set<XID> objectIds) {
		super();
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.objectIds = objectIds;
	}
	
	@Override
	public XAddress getAddress() {
		return this.address;
	}
	
	@Override
	public XID getID() {
		return this.address.getField();
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	@Override
	public SimpleObject createObject(XID ObjectId) {
		this.objectIds.add(ObjectId);
		return getObject(ObjectId);
	}
	
	@Override
	public boolean removeObject(XID ObjectId) {
		return this.objectIds.remove(ObjectId);
	}
	
	@Override
	public SimpleObject getObject(XID ObjectId) {
		if(hasObject(ObjectId)) {
			return new SimpleObject(XX.resolveObject(this.getAddress(), ObjectId),
			        SimpleConstants.REVISION_NUMBER_UNDEFINED, null);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean hasObject(XID ObjectId) {
		return this.objectIds.contains(ObjectId);
	}
	
	@Override
	public boolean isEmpty() {
		return this.objectIds.isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.objectIds.iterator();
	}
	
}
