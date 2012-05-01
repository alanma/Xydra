package org.xydra.base.rmof.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableRepository;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.sharedutils.XyAssert;


/**
 * A simple data container for {@link XWritableRepository}.
 * 
 * Minimal memory footprint, can be used as data transfer object.
 * 
 * @author voelkel
 */
public class SimpleRepository implements XRevWritableRepository, Serializable {
	
	private static final long serialVersionUID = 5593443685935758227L;
	
	// not final for GWT serialisation
	private XAddress address;
	// not final for GWT serialisation
	private Map<XID,XRevWritableModel> models = new HashMap<XID,XRevWritableModel>();
	
	/* Just for GWT */
	protected SimpleRepository() {
	}
	
	public SimpleRepository(XAddress address) {
		XyAssert.xyAssert(address.getAddressedType() == XType.XREPOSITORY);
		this.address = address;
	}
	
	@Override
	public XRevWritableModel createModel(XID modelId) {
		XRevWritableModel model = this.models.get(modelId);
		if(model != null) {
			return model;
		}
		SimpleModel newModel = new SimpleModel(XX.resolveModel(this.address, modelId));
		this.models.put(modelId, newModel);
		return newModel;
	}
	
	@Override
	public XAddress getAddress() {
		return this.address;
	}
	
	@Override
	public XID getId() {
		return this.address.getRepository();
	}
	
	@Override
	public XRevWritableModel getModel(XID modelId) {
		return this.models.get(modelId);
	}
	
	@Override
	public boolean hasModel(XID modelId) {
		return this.models.containsKey(modelId);
	}
	
	@Override
	public boolean isEmpty() {
		return this.models.isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.models.keySet().iterator();
	}
	
	@Override
	public boolean removeModel(XID modelId) {
		if(this.models.remove(modelId) != null) {
			return true;
		}
		return false;
	}
	
	@Override
	public void addModel(XRevWritableModel model) {
		this.models.put(model.getId(), model);
	}
	
	@Override
	public XType getType() {
		return XType.XREPOSITORY;
	}
	
}
