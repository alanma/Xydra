package org.xydra.store.base;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseRepository;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.store.Callback;
import org.xydra.store.StoreException;
import org.xydra.store.XydraStore;


/**
 * An {@link XBaseRepository} which pulls state <em>once</em> lazily via a
 * snapshot from a local {@link XydraStore}.
 * 
 * @author voelkel
 */
public class BaseRepository implements XBaseRepository, Serializable {
	
	private static final long serialVersionUID = -5943088597508682530L;
	protected XAddress address;
	protected Credentials credentials;
	protected Set<XID> modelIds = null;
	protected XBaseRepository baseRepository;
	protected XydraStore store;
	
	/**
	 * @param credentials active credentials which are used to authenticate and
	 *            authorise to the XydraStore.
	 * @param store must be in the same VM and may not be accessed over a
	 *            network.
	 */
	public BaseRepository(Credentials credentials, XydraStore store) {
		this.credentials = credentials;
		this.store = store;
		this.address = X.getIDProvider().fromComponents(getRepositoryId(store), null, null, null);
		assert this.address.getAddressedType() == XType.XREPOSITORY;
	}
	
	private XID repositoryId;
	
	private synchronized XID getRepositoryId(XydraStore store) {
		assert store != null;
		this.repositoryId = null;
		store.getRepositoryId(this.credentials.getActorId(), this.credentials.getPasswordHash(),
		        new Callback<XID>() {
			        
			        @Override
			        public void onSuccess(XID object) {
				        BaseRepository.this.repositoryId = object;
			        }
			        
			        @Override
			        public void onFailure(Throwable exception) {
				        throw new RuntimeException(exception);
			        }
		        });
		long c = 1;
		while(this.repositoryId == null && c < 1000) {
			try {
				// TODO implement smarter with wait()?
				Thread.sleep(c);
			} catch(InterruptedException e) {
			}
			c *= 2;
		}
		return this.repositoryId;
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
	public XBaseModel getModel(XID id) {
		BaseModel model = new BaseModel(this.credentials, this.store, XX.resolveModel(getAddress(),
		        id));
		if(model.baseModel == null) {
			return null;
		}
		return model;
	}
	
	@Override
	public boolean hasModel(XID id) {
		initModelIds();
		return this.modelIds.contains(id);
	}
	
	private void initModelIds() {
		if(this.modelIds != null) {
			return;
		}
		this.store.getModelIds(this.credentials.getActorId(), this.credentials.getPasswordHash(),
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
		// FIXME callback may not have been called yet
	}
	
	@Override
	public boolean isEmpty() {
		initModelIds();
		return this.modelIds.isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		initModelIds();
		return this.modelIds.iterator();
	}
	
}
