package org.xydra.store.base;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.StoreException;
import org.xydra.store.XydraStore;


/**
 * An {@link XBaseModel} which pulls state <em>once</em> lazily via a snapshot
 * from a local {@link XydraStore}.
 * 
 * @author voelkel
 */
public class BaseModel implements XBaseModel, Serializable {
	
	private static final Logger log = LoggerFactory.getLogger(BaseModel.class);
	
	private static final long serialVersionUID = 2086217765670621565L;
	
	protected XAddress address;
	protected Credentials credentials;
	protected XBaseModel baseModel;
	protected XydraStore store;
	
	/**
	 * @param credentials
	 * @param store must be in the same VM and may not be accessed over a
	 *            network.
	 * @param address
	 */
	public BaseModel(Credentials credentials, XydraStore store, XAddress address) {
		this.store = store;
		this.address = address;
		this.credentials = credentials;
		load();
	}
	
	@Override
	public XAddress getAddress() {
		return this.address;
	}
	
	protected synchronized void load() {
		LoadingCallback callback = new LoadingCallback();
		this.store.getModelSnapshots(this.credentials.getActorId(),
		        this.credentials.getPasswordHash(), new XAddress[] { this.address }, callback);
		while(!callback.done) {
			try {
				callback.wait();
			} catch(InterruptedException e) {
				log.debug("Could not wait", e);
			}
		}
	}
	
	private final class LoadingCallback implements Callback<BatchedResult<XBaseModel>[]> {
		public boolean done = false;
		
		@Override
		public synchronized void onFailure(Throwable error) {
			this.done = true;
			// TODO is the notify necessary?
			notify();
			throw new StoreException("", error);
		}
		
		@Override
		public synchronized void onSuccess(BatchedResult<XBaseModel>[] model) {
			assert model.length == 1;
			this.done = true;
			/*
			 * TODO better error handling if getResult is null because
			 * getException has an AccessException
			 */
			BaseModel.this.baseModel = model[0].getResult();
			notify();
		}
	}
	
	@Override
	public XID getID() {
		return this.address.getField();
	}
	
	public XBaseObject getObject(XID objectId) {
		if(this.baseModel == null) {
			return null;
		}
		
		return this.baseModel.getObject(objectId);
	}
	
	@Override
	public long getRevisionNumber() {
		return this.baseModel.getRevisionNumber();
	}
	
	public boolean hasObject(XID objectId) {
		return this.baseModel.hasObject(objectId);
	}
	
	public boolean isEmpty() {
		return this.baseModel.isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.baseModel.iterator();
	}
	
}
