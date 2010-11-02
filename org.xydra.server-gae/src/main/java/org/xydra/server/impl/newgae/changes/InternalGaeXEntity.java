package org.xydra.server.impl.newgae.changes;

import java.util.Set;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XType;
import org.xydra.server.impl.newgae.GaeUtils;


/**
 * Internal helper class used by {@link GaeChangesService} to access the current
 * model state.
 * 
 * @author dscharrer
 * 
 */
public class InternalGaeXEntity {
	
	protected static final String PROP_PARENT = "parent";
	protected static final String PROP_REVISION = "revision";
	
	protected static void remove(XAddress modelOrObjectOrFieldAddr, Set<XAddress> locks) {
		assert GaeChangesService.canWrite(modelOrObjectOrFieldAddr, locks);
		assert modelOrObjectOrFieldAddr.getAddressedType() == XType.XMODEL
		        || modelOrObjectOrFieldAddr.getAddressedType() == XType.XOBJECT
		        || modelOrObjectOrFieldAddr.getAddressedType() == XType.XFIELD;
		GaeUtils.deleteEntity(KeyStructure.createCombinedKey(modelOrObjectOrFieldAddr));
	}
	
}
