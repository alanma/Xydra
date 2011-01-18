package org.xydra.store.base;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XWritableRepository;
import org.xydra.base.XType;
import org.xydra.base.XHalfWritableRepository;
import org.xydra.core.XX;


/**
 * A simple data container for {@link XHalfWritableRepository}.
 * 
 * Minimal memory footprint, can be used as data transfer object.
 * 
 * @author voelkel
 */
public class SimpleRepository implements XWritableRepository, Serializable {
	
	private static final long serialVersionUID = 5593443685935758227L;
	
	private final XAddress address;
	private final Set<XID> modelIds;
	
	public SimpleRepository(XAddress address, Set<XID> modelIds) {
		assert address.getAddressedType() == XType.XREPOSITORY;
		this.address = address;
		this.modelIds = modelIds;
	}
	
	public Set<XID> getModelIds() {
		return this.modelIds;
	}
	
	@Override
	public XAddress getAddress() {
		return this.address;
	}
	
	@Override
	public XID getID() {
		return this.address.getRepository();
	}
	
	@Override
	public SimpleModel createModel(XID modelId) {
		this.modelIds.add(modelId);
		return getModel(modelId);
	}
	
	@Override
	public boolean removeModel(XID modelId) {
		return this.modelIds.remove(modelId);
	}
	
	@Override
	public SimpleModel getModel(XID modelId) {
		// FIXME this returns different instances for each call
		if(hasModel(modelId)) {
			return new SimpleModel(XX.resolveModel(this.getAddress(), modelId),
			        SimpleConstants.REVISION_NUMBER_UNDEFINED, null);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean hasModel(XID modelId) {
		return this.modelIds.contains(modelId);
	}
	
	@Override
	public boolean isEmpty() {
		return this.modelIds.isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.modelIds.iterator();
	}
	
}
