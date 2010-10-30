/**
 * 
 */
package org.xydra.server.impl.newgae;

import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.server.impl.gae.GaeUtils;

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
	
	private final long modelRev;
	
	private InternalGaeModel(GaeChangesService changesService, XAddress modelAddr, long modelRev,
	        Set<XAddress> locks) {
		super(changesService, modelAddr, locks);
		assert modelAddr.getAddressedType() == XType.XMODEL;
		this.modelRev = modelRev;
	}
	
	public long getRevisionNumber() {
		return this.modelRev;
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
		return new InternalGaeObject(getChangesService(), childAddr, getLocks());
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
	
	protected static InternalGaeModel get(GaeChangesService changesService, long modelRev,
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
	
}
