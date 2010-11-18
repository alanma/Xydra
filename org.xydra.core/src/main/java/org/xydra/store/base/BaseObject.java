package org.xydra.store.base;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseObject;
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
public class BaseObject implements XBaseObject, Serializable {
	
	private static final long serialVersionUID = -4890586955104381922L;
	protected XAddress address;
	protected Credentials credentials;
	protected XBaseObject baseObject;
	protected XydraStore store;
	
	/**
	 * @param credentials
	 * @param store must be in the same VM and may not be accessed over a
	 *            network.
	 * @param address
	 */
	public BaseObject(Credentials credentials, XydraStore store, XAddress address) {
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
		this.store.getObjectSnapshots(this.credentials.actorId, this.credentials.passwordHash,
		        new XAddress[] { this.address }, new Callback<XBaseObject[]>() {
			        
			        @Override
			        public void onFailure(Throwable error) {
				        throw new StoreException("", error);
			        }
			        
			        @Override
			        public void onSuccess(XBaseObject[] object) {
				        assert object.length == 1;
				        BaseObject.this.baseObject = object[0];
			        }
		        });
	}
	
	@Override
	public XBaseField getField(XID fieldId) {
		return this.baseObject.getField(fieldId);
	}
	
	@Override
	public XID getID() {
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
	
}
