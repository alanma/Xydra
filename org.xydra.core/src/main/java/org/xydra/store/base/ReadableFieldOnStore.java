package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetWithAddressRequest;
import org.xydra.store.StoreException;
import org.xydra.store.XydraStore;


/**
 * At creation time the {@link XydraStore} is consulted once to fetch a fresh
 * snapshot.
 * 
 * Internally, the {@link XReadableField} retrieved as a snapshot is used as a
 * delegatee.
 * 
 * TODO what is the point of wrapping a snapshot like this instead of just using
 * the snapshot directly and storing the load function somewhere else (in a
 * SynchronousStore wrapper / helper class? Same applies for
 * {@link ReadableObjectOnStore} and {@link ReadableModelOnStore}
 * 
 * @author voelkel
 */
public class ReadableFieldOnStore implements XReadableField, Serializable {
	
	private static final long serialVersionUID = -374853720614210669L;
	protected XAddress address;
	protected XReadableField baseField;
	protected Credentials credentials;
	protected XydraStore store;
	
	/**
	 * @param credentials The credentials used for accessing the store.
	 * @param store The store to load from. must be in the same VM and may not
	 *            be accessed over a network.
	 * @param address The address of the field to load.
	 */
	public ReadableFieldOnStore(Credentials credentials, XydraStore store, XAddress address) {
		this.store = store;
		this.address = address;
		this.credentials = credentials;
		load();
	}
	
	public ReadableFieldOnStore(Credentials credentials, XydraStore store, XAddress address,
	        XReadableField baseField) {
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
	public XID getId() {
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
		this.store
		        .getObjectSnapshots(
		                this.credentials.getActorId(),
		                this.credentials.getPasswordHash(),
		                new GetWithAddressRequest[] { new GetWithAddressRequest(this.address
		                        .getParent()) }, new Callback<BatchedResult<XReadableObject>[]>() {
			                
			                @Override
			                public void onFailure(Throwable error) {
				                throw new StoreException("", error);
			                }
			                
			                @Override
			                public void onSuccess(BatchedResult<XReadableObject>[] object) {
				                XyAssert.xyAssert(object.length == 1);
				                /*
								 * TODO better error handling if getResult is
								 * null because getException has an
								 * AccessException
								 */
				                ReadableFieldOnStore.this.baseField = object[0].getResult()
				                        .getField(getId());
			                }
		                });
	}
	
	@Override
	public XType getType() {
		return XType.XFIELD;
	}
	
}
