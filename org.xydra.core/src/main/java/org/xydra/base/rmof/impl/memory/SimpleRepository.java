package org.xydra.base.rmof.impl.memory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XRevWritableRepository;
import org.xydra.base.rmof.XWritableRepository;


/**
 * A simple data container for {@link XWritableRepository}.
 * 
 * Minimal memory footprint, can be used as data transfer object.
 * 
 * @author voelkel
 */
public class SimpleRepository implements XRevWritableRepository, Serializable {
	
	private static final long serialVersionUID = 5593443685935758227L;
	
	private final XAddress address;
	private final Set<XID> modelIds;
	
	public SimpleRepository(XAddress address, Set<XID> modelIds) {
		assert address.getAddressedType() == XType.XREPOSITORY;
		this.address = address;
		this.modelIds = modelIds;
	}
	
	@Override
	public SimpleModel createModel(XID modelId) {
		this.modelIds.add(modelId);
		return getModel(modelId);
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
	public SimpleModel getModel(XID modelId) {
		// FIXME this returns different instances for each call
		if(hasModel(modelId)) {
			return new SimpleModel(XX.resolveModel(this.getAddress(), modelId), XCommand.NEW, null);
		} else {
			return null;
		}
	}
	
	public Set<XID> getModelIds() {
		return this.modelIds;
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
	
	@Override
	public boolean removeModel(XID modelId) {
		return this.modelIds.remove(modelId);
	}
	
}
