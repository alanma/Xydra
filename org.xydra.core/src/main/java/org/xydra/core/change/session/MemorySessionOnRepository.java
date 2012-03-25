package org.xydra.core.change.session;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.index.impl.IteratorUtils;
import org.xydra.store.GetWithAddressRequest;


/**
 * Creates only read-only session models.
 * 
 * @author xamde
 * 
 */
public class MemorySessionOnRepository implements ISessionPersistence {
	
	private XWritableRepository repo;
	
	public MemorySessionOnRepository(XWritableRepository repo) {
		this.repo = repo;
	}
	
	@Override
	public long createModel(XID modelId) {
		if(this.repo.hasModel(modelId)) {
			return XCommand.NOCHANGE;
		} else {
			XWritableModel model = this.repo.createModel(modelId);
			long rev = model.getRevisionNumber();
			return rev > 0 ? rev : 1;
		}
	}
	
	@Override
	public XID getRepositoryId() {
		return this.repo.getId();
	}
	
	@Override
	public long applyChangesAsTxn(SessionCachedModel changedModel, XID actorId) {
		XWritableModel writableModel = this.repo.getModel(changedModel.getId());
		changedModel.commitTo(writableModel);
		return 1;
	}
	
	@Override
	public long removeModel(XID modelId) {
		return this.repo.removeModel(modelId) ? 1 : XCommand.NOCHANGE;
	}
	
	public Collection<XReadableModel> getModelSnapshots(XID ... modelIds) {
		List<XReadableModel> result = new LinkedList<XReadableModel>();
		if(modelIds != null) {
			for(XID modelId : modelIds) {
				XWritableModel model = this.repo.getModel(modelId);
				if(model != null) {
					result.add(model);
				}
			}
		}
		return result;
	}
	
	public Collection<XReadableObject> getObjectSnapshots(XAddress ... objectAddresses) {
		List<XReadableObject> result = new LinkedList<XReadableObject>();
		if(objectAddresses != null) {
			for(XAddress objectAddress : objectAddresses) {
				XWritableModel model = this.repo.getModel(objectAddress.getModel());
				if(model != null) {
					XWritableObject object = model.getObject(objectAddress.getObject());
					result.add(object);
				}
			}
		}
		return result;
	}
	
	@Override
	public XReadableModel getModelSnapshot(GetWithAddressRequest modelRequest) {
		assert modelRequest != null;
		return this.repo.getModel(modelRequest.address.getModel());
	}
	
	@Override
	public XReadableObject getObjectSnapshot(GetWithAddressRequest objectAddressRequest) {
		assert objectAddressRequest != null;
		XWritableModel model = this.repo.getModel(objectAddressRequest.address.getModel());
		if(model == null) {
			return null;
		}
		return model.getObject(objectAddressRequest.address.getObject());
	}
	
	@Override
	public void deleteAllData() {
		Collection<XID> modelIds = IteratorUtils.addAll(this.repo.iterator(), new HashSet<XID>());
		for(XID modelId : modelIds) {
			this.repo.removeModel(modelId);
		}
	}
	
}
