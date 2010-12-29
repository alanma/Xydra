package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.value.XValue;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.StoreException;
import org.xydra.store.XydraStore;


/**
 * At creation time the {@link XydraStore} is consulted once to fetch a fresh
 * snapshot.
 * 
 * Internally, the {@link XBaseField} retrieved as a snapshot is used as a
 * delegatee.
 * 
 * TODO what is the point of wrapping a snapshot like this instead of just using
 * the snapshot directly and storing the load function somewhere else (in a
 * SynchronousStore wrapper / helper class? Same applies for {@link BaseObject}
 * and {@link BaseModel}
 * 
 * @author voelkel
 */
public class BaseField implements XBaseField, Serializable {
	
	private static final long serialVersionUID = -374853720614210669L;
	protected XAddress address;
	protected Credentials credentials;
	protected XBaseField baseField;
	protected XydraStore store;
	
	/**
	 * @param credentials
	 * @param store must be in the same VM and may not be accessed over a
	 *            network.
	 * @param address
	 */
	public BaseField(Credentials credentials, XydraStore store, XAddress address) {
		this.store = store;
		this.address = address;
		this.credentials = credentials;
		load();
	}
	
	public BaseField(Credentials credentials, XydraStore store, XAddress address,
	        XBaseField baseField) {
		this.store = store;
		this.address = address;
		this.credentials = credentials;
		this.baseField = baseField;
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
	public long getRevisionNumber() {
		return this.baseField.getRevisionNumber();
	}
	
	@Override
	public XValue getValue() {
		return this.baseField.getValue();
	}
	
	@Override
	public boolean isEmpty() {
		return this.baseField.isEmpty();
	}
	
	protected void load() {
		this.store.getObjectSnapshots(this.credentials.getActorId(), this.credentials.getPasswordHash(),
		        new XAddress[] { this.address.getParent() },
		        new Callback<BatchedResult<XBaseObject>[]>() {
			        
			        @Override
			        public void onFailure(Throwable error) {
				        throw new StoreException("", error);
			        }
			        
			        @Override
			        public void onSuccess(BatchedResult<XBaseObject>[] object) {
				        assert object.length == 1;
				        /*
						 * TODO better error handling if getResult is null
						 * because getException has an AccessException
						 */
				        BaseField.this.baseField = object[0].getResult().getField(getID());
			        }
		        });
	}
	
}
