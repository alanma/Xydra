/**
 * 
 */
package org.xydra.server.impl.newgae.changes;

import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XType;
import org.xydra.server.impl.newgae.GaeUtils;

import com.google.appengine.api.datastore.Entity;


/**
 * Internal helper class used by {@link GaeChangesService} to access the current
 * model state.
 * 
 * @author dscharrer
 * 
 */
public class InternalGaeModel extends InternalGaeContainerXEntity<InternalGaeObject> implements
        XBaseModel {
	
	private InternalGaeModel(GaeChangesService changesService, XAddress modelAddr, long modelRev,
	        Set<XAddress> locks) {
		super(changesService, modelAddr, modelRev, locks);
		assert modelAddr.getAddressedType() == XType.XMODEL;
	}
	
	public XID getID() {
		return getAddress().getModel();
	}
	
	public XBaseObject getObject(XID objectId) {
		return getChild(objectId);
	}
	
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
	
	@Override
	protected XID getChildId(XAddress childAddr) {
		assert childAddr.getAddressedType() == XType.XOBJECT;
		return childAddr.getObject();
	}
	
	/**
	 * Get a read-only interface to an {@link XModel} in the GAE datastore.
	 * 
	 * @param changesService The changes service that manages the model to load.
	 * @param rev The model revision to to be returned by
	 *            {@link #getRevisionNumber()}.
	 * @param locks The locks held by the current process. These are used to
	 *            assert that we have enough locks when reading fields or
	 *            objects as well as to determine if we can calculate object
	 *            revisions or if we have to return
	 *            {@link XEvent#RevisionNotAvailable} instead.
	 * @return A {@link XModel} interface or null if the model doesn't exist.
	 */
	public static InternalGaeModel get(GaeChangesService changesService, long modelRev,
	        Set<XAddress> locks) {
		
		assert GaeChangesService.canRead(changesService.getBaseAddress(), locks);
		Entity e = GaeUtils.getEntity(KeyStructure.createCombinedKey(changesService
		        .getBaseAddress()));
		if(e == null) {
			return null;
		}
		
		return new InternalGaeModel(changesService, changesService.getBaseAddress(), modelRev,
		        locks);
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
	public static void createModel(XAddress modelAddr, Set<XAddress> locks) {
		assert GaeChangesService.canWrite(modelAddr, locks);
		assert modelAddr.getAddressedType() == XType.XMODEL;
		Entity e = new Entity(KeyStructure.createCombinedKey(modelAddr));
		e.setProperty(PROP_PARENT, modelAddr.getParent().toURI());
		GaeUtils.putEntity(e);
	}
	
}
