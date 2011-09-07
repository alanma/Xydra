package org.xydra.store.base;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.store.BatchedResult;
import org.xydra.store.StoreException;
import org.xydra.store.WaitingCallback;
import org.xydra.store.XydraStore;


/**
 * An {@link XReadableModel} which pulls state <em>once</em> lazily via a
 * snapshot from a local {@link XydraStore}.
 * 
 * TODO {@link WaitingCallback} doesn't work with GWT
 * 
 * @author voelkel
 */
@RunsInGWT(false)
public class ReadableModelOnStore implements XReadableModel, Serializable {
	
	private static final long serialVersionUID = 2086217765670621565L;
	protected XAddress address;
	protected XReadableModel baseModel;
	protected Credentials credentials;
	
	protected XydraStore store;
	
	/**
	 * @param credentials The credentials used for accessing the store.
	 * @param store The store to load from. must be in the same VM and may not
	 *            be accessed over a network.
	 * @param address The address of the model to load.
	 */
	public ReadableModelOnStore(Credentials credentials, XydraStore store, XAddress address) {
		this.store = store;
		this.address = address;
		this.credentials = credentials;
		load();
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
    public XReadableObject getObject(XID objectId) {
		if(this.baseModel == null) {
			return null;
		}
		
		return this.baseModel.getObject(objectId);
	}
	
	@Override
	public long getRevisionNumber() {
		return this.baseModel.getRevisionNumber();
	}
	
	@Override
    public boolean hasObject(XID objectId) {
		return this.baseModel.hasObject(objectId);
	}
	
	@Override
    public boolean isEmpty() {
		return this.baseModel.isEmpty();
	}
	
	@Override
    public Iterator<XID> iterator() {
		return this.baseModel.iterator();
	}
	
	protected synchronized void load() {
		
		WaitingCallback<BatchedResult<XReadableModel>[]> callback = new WaitingCallback<BatchedResult<XReadableModel>[]>();
		this.store.getModelSnapshots(this.credentials.getActorId(), this.credentials
		        .getPasswordHash(), new XAddress[] { this.address }, callback);
		
		if(callback.getException() != null) {
			throw new StoreException("re-throw", callback.getException());
		}
		
		BatchedResult<XReadableModel>[] res = callback.getResult();
		
		assert res.length == 1;
		assert res[0] != null;
		
		if(res[0].getException() != null) {
			throw new StoreException("re-throw", res[0].getException());
		}
		
		assert res[0].getResult() != null;
		
		this.baseModel = res[0].getResult();
	}
	
	@Override
	public XType getType() {
		return XType.XMODEL;
	}
}
