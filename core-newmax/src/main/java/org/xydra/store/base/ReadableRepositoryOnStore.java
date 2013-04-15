package org.xydra.store.base;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.core.StoreException;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.Callback;
import org.xydra.store.XydraStore;


/**
 * An {@link XReadableRepository} which pulls state <em>once</em> lazily via a
 * snapshot from a local {@link XydraStore}.
 * 
 * TODO GWT doesn't have a Thread class
 * 
 * @author voelkel
 */
@RunsInGWT(false)
public class ReadableRepositoryOnStore implements XReadableRepository, Serializable {
	
	private static final long serialVersionUID = -5943088597508682530L;
	protected XAddress address;
	protected XReadableRepository baseRepository;
	protected Credentials credentials;
	protected Set<XId> modelIds = null;
	private XId repositoryId;
	
	protected XydraStore store;
	
	/**
	 * @param credentials active credentials which are used to authenticate and
	 *            authorise to the XydraStore.
	 * @param store must be in the same VM and may not be accessed over a
	 *            network.
	 */
	public ReadableRepositoryOnStore(Credentials credentials, XydraStore store) {
		this.credentials = credentials;
		this.store = store;
		this.address = X.getIDProvider().fromComponents(getRepositoryId(store), null, null, null);
		XyAssert.xyAssert(this.address.getAddressedType() == XType.XREPOSITORY);
	}
	
	@Override
	public XAddress getAddress() {
		return this.address;
	}
	
	@Override
	public XId getId() {
		return this.address.getRepository();
	}
	
	@Override
	public XReadableModel getModel(XId id) {
		ReadableModelOnStore model = new ReadableModelOnStore(this.credentials, this.store,
		        XX.resolveModel(getAddress(), id));
		if(model.baseModel == null) {
			return null;
		}
		return model;
	}
	
	private synchronized XId getRepositoryId(@NeverNull XydraStore store) {
		XyAssert.xyAssert(store != null); assert store != null;
		assert store != null;
		this.repositoryId = null;
		store.getRepositoryId(this.credentials.getActorId(), this.credentials.getPasswordHash(),
		        new Callback<XId>() {
			        
			        @Override
			        public void onFailure(Throwable exception) {
				        throw new RuntimeException(exception);
			        }
			        
			        @Override
			        public void onSuccess(XId object) {
				        ReadableRepositoryOnStore.this.repositoryId = object;
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
	public boolean hasModel(XId id) {
		initModelIds();
		return this.modelIds.contains(id);
	}
	
	private void initModelIds() {
		if(this.modelIds != null) {
			return;
		}
		this.store.getModelIds(this.credentials.getActorId(), this.credentials.getPasswordHash(),
		        new Callback<Set<XId>>() {
			        
			        @Override
			        public void onFailure(Throwable exception) {
				        throw new StoreException("re-throw", exception);
			        }
			        
			        @Override
			        public void onSuccess(Set<XId> modelIds) {
				        ReadableRepositoryOnStore.this.modelIds = modelIds;
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
	public Iterator<XId> iterator() {
		initModelIds();
		return this.modelIds.iterator();
	}
	
	@Override
	public XType getType() {
		return XType.XREPOSITORY;
	}
	
}
