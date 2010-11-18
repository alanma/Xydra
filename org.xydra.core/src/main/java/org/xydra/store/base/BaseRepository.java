package org.xydra.store.base;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseRepository;
import org.xydra.core.model.XID;
import org.xydra.store.Callback;
import org.xydra.store.StoreException;
import org.xydra.store.XydraStore;


/**
 * At creation time the {@link XydraStore} is consulted once to fetch a fresh
 * snapshot.
 * 
 * @author voelkel
 */
public class BaseRepository implements XBaseRepository, Serializable {
	
	private static final long serialVersionUID = -5943088597508682530L;
	protected XAddress address;
	protected Credentials credentials;
	protected Set<XID> modelIds;
	protected XBaseRepository baseRepository;
	protected XydraStore store;
	
	/**
	 * @param credentials
	 * @param store must be in the same VM and may not be accessed over a
	 *            network.
	 * @param address
	 */
	public BaseRepository(Credentials credentials, XydraStore store, XAddress address) {
		this.store = store;
		this.address = address;
		this.credentials = credentials;
		initModelIds();
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
	public XBaseModel getModel(XID id) {
		return new BaseModel(this.credentials, this.store, XX.resolveModel(getAddress(), id));
	}
	
	@Override
	public boolean hasModel(XID id) {
		return this.modelIds.contains(id);
	}
	
	private void initModelIds() {
		this.store.getModelIds(this.credentials.actorId, this.credentials.passwordHash, getID(),
		        new Callback<Set<XID>>() {
			        
			        @Override
			        public void onFailure(Throwable exception) {
				        throw new StoreException("re-throw", exception);
			        }
			        
			        @Override
			        public void onSuccess(Set<XID> modelIds) {
				        BaseRepository.this.modelIds = modelIds;
			        }
		        });
		
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
