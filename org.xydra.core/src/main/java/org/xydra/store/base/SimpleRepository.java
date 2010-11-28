package org.xydra.store.base;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableRepository;


/**
 * A simple data container for {@link XWritableRepository}.
 * 
 * @author voelkel
 */
public class SimpleRepository implements XWritableRepository, Serializable {
	
	private static final long serialVersionUID = 5593443685935758227L;
	protected XAddress address;
	protected long revisionNumber;
	protected Set<XID> modelIds;
	
	public SimpleRepository(XAddress address, long revisionNumber, Set<XID> modelIds) {
		super();
		this.address = address;
		this.revisionNumber = revisionNumber;
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
		return this.address.getField();
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
