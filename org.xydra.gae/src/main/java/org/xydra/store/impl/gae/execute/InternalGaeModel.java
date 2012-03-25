/**
 * 
 */
package org.xydra.store.impl.gae.execute;

import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.model.XModel;
import org.xydra.store.impl.gae.AsyncDatastore;
import org.xydra.store.impl.gae.SyncDatastore;
import org.xydra.store.impl.gae.changes.GaeLocks;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.changes.KeyStructure;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * Internal helper class used by {@link IGaeChangesService} to access the
 * current model state.
 * 
 * @author dscharrer
 * 
 */
class InternalGaeModel extends InternalGaeContainerXEntity<InternalGaeObject> implements
        XReadableModel {
	
	private InternalGaeModel(IGaeChangesService changesService, XAddress modelAddr, long modelRev,
	        GaeLocks locks) {
		super(changesService, modelAddr, modelRev, locks);
		assert modelAddr.getAddressedType() == XType.XMODEL;
	}
	
	@Override
	public XID getId() {
		return getAddress().getModel();
	}
	
	@Override
	public XReadableObject getObject(XID objectId) {
		return getChild(objectId);
	}
	
	@Override
	public boolean hasObject(XID objectId) {
		return hasChild(objectId);
	}
	
	@Override
	protected InternalGaeObject loadChild(XAddress childAddr, Entity childEntity) {
		return new InternalGaeObject(getChangesService(), childAddr, childEntity, getLocks());
	}
	
	@Override
	protected XAddress resolveChild(XAddress addr, XID childId) {
		return XX.resolveObject(addr, childId);
	}
	
	/**
	 * Get a read-only interface to an {@link XModel} in the GAE datastore.
	 * 
	 * @param changesService The changes service that manages the model to load.
	 * @param modelRev The model revision to to be returned by
	 *            {@link #getRevisionNumber()}.
	 * @param locks The locks held by the current process. These are used to
	 *            assert that we have enough locks when reading fields or
	 *            objects as well as to determine if we can calculate object
	 *            revisions or if we have to return
	 *            {@link XEvent#RevisionNotAvailable} instead.
	 * @return A {@link XModel} interface or null if the model doesn't exist.
	 */
	static InternalGaeModel get(IGaeChangesService changesService, long modelRev, GaeLocks locks) {
		
		assert locks.canRead(changesService.getModelAddress());
		Entity e = SyncDatastore.getEntity(KeyStructure.createEntityKey(changesService
		        .getModelAddress()));
		if(e == null) {
			return null;
		}
		
		XAddress modelAddr = changesService.getModelAddress();
		
		return new InternalGaeModel(changesService, modelAddr, modelRev, locks);
	}
	
	/**
	 * Create an {@link XModel} in the GAE datastore.
	 * 
	 * It is up to the caller to acquire enough locks: The whole {@link XModel}
	 * needs to be locked while adding it.
	 * 
	 * @param modelAddr The address of the model to add.
	 * @param locks The locks held by the current process. These are used to
	 *            assert that we are actually allowed to create the
	 *            {@link XModel}.
	 */
	static Future<Key> createModel(XAddress modelAddr, GaeLocks locks) {
		assert locks.canWrite(modelAddr);
		assert modelAddr.getAddressedType() == XType.XMODEL;
		Entity e = new Entity(KeyStructure.createEntityKey(modelAddr));
		return AsyncDatastore.putEntity(e);
	}
	
	@Override
	public XType getType() {
		return XType.XMODEL;
	}
	
}
