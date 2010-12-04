package org.xydra.store.base;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.StoreException;
import org.xydra.store.XydraStore;


/**
 * At creation time the {@link XydraStore} is consulted once to fetch a fresh
 * snapshot.
 * 
 * @author voelkel
 */
public class BaseModel implements XBaseModel, Serializable {
	
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
	
	protected void load() {
		this.store.getModelSnapshots(this.credentials.actorId, this.credentials.passwordHash,
		        new XAddress[] { this.address }, new Callback<BatchedResult<XBaseModel>[]>() {
			        
			        @Override
			        public void onFailure(Throwable error) {
				        throw new StoreException("", error);
			        }
			        
			        @Override
			        public void onSuccess(BatchedResult<XBaseModel>[] model) {
				        assert model.length == 1;
				        /*
						 * TODO better error handling if getResult is null
						 * because getException has an AccessException
						 */
				        BaseModel.this.baseModel = model[0].getResult();
			        }
		        });
	}
	
	@Override
	public XID getID() {
		return this.address.getField();
	}
	
	public XBaseObject getObject(XID objectId) {
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
