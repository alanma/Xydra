package org.xydra.store.base;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
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
 * @author voelkel
 */
public class ReadableObjectOnStore implements XReadableObject, Serializable {
	
	private static final long serialVersionUID = -4890586955104381922L;
	protected XAddress address;
	protected XReadableObject baseObject;
	protected Credentials credentials;
	protected XydraStore store;
	
	/**
	 * @param credentials The credentials used for accessing the store.
	 * @param store The store to read from. must be in the same VM and may not
	 *            be accessed over a network.
	 * @param address The address of the object to load.
	 */
	public ReadableObjectOnStore(Credentials credentials, XydraStore store, XAddress address) {
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
	public XReadableField getField(XID fieldId) {
		return this.baseObject.getField(fieldId);
	}
	
	@Override
	public XID getId() {
		return this.address.getField();
	}
	
	@Override
	public long getRevisionNumber() {
		return this.baseObject.getRevisionNumber();
	}
	
	@Override
	public boolean hasField(XID fieldId) {
		return this.baseObject.hasField(fieldId);
	}
	
	@Override
	public boolean isEmpty() {
		return this.baseObject.isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.baseObject.iterator();
	}
	
	protected void load() {
		this.store.getObjectSnapshots(this.credentials.getActorId(),
		        this.credentials.getPasswordHash(),
		        new GetWithAddressRequest[] { new GetWithAddressRequest(this.address) },
		        new Callback<BatchedResult<XReadableObject>[]>() {
			        
			        @Override
			        public void onFailure(Throwable error) {
				        throw new StoreException("", error);
			        }
			        
			        @Override
			        public void onSuccess(BatchedResult<XReadableObject>[] object) {
				        XyAssert.xyAssert(object.length == 1);
				        /*
						 * TODO better error handling if getResult is null
						 * because getException has an AccessException
						 */
				        ReadableObjectOnStore.this.baseObject = object[0].getResult();
			        }
		        });
	}
	
	@Override
	public XType getType() {
		return XType.XOBJECT;
	}
	
}