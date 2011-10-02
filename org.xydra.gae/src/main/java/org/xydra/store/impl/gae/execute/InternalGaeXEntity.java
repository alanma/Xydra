package org.xydra.store.impl.gae.execute;

import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.rmof.XEntity;
import org.xydra.core.model.XField;
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
abstract public class InternalGaeXEntity implements XEntity {
	
	protected static final String PROP_REVISION = "revision";
	
	/**
	 * Remove an {@link XModel}, {@link XObject} or {@link XField} from the GAE
	 * datastore.
	 * 
	 * It is up to the caller to acquire enough locks to cover the whole entity
	 * being removed. To remove an entity, the whole entity itself will need to
	 * be locked.
	 * 
	 * @param modelOrObjectOrFieldAddr The address of the entity to remove.
	 * @param locks The locks held by the current process. These are used to
	 *            assert that we are actually allowed to remove the entity.
	 */
	protected static Future<Void> remove(XAddress modelOrObjectOrFieldAddr, GaeLocks locks) {
		assert locks.canRemove(modelOrObjectOrFieldAddr);
		assert modelOrObjectOrFieldAddr.getAddressedType() == XType.XMODEL
		        || modelOrObjectOrFieldAddr.getAddressedType() == XType.XOBJECT
		        || modelOrObjectOrFieldAddr.getAddressedType() == XType.XFIELD;
		return AsyncDatastore.deleteEntity(KeyStructure.createEntityKey(modelOrObjectOrFieldAddr));
	}
	
	public static boolean exists(XAddress address) {
		Key key = KeyStructure.createEntityKey(address);
		Entity entity = SyncDatastore.getEntity(key);
		return (entity != null);
	}
	
}
