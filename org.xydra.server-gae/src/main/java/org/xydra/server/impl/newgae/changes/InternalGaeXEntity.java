package org.xydra.server.impl.newgae.changes;

import org.xydra.core.model.XAddress;
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
public class InternalGaeXEntity {
	
	protected static final String PROP_PARENT = "parent";
	
	protected static void remove(XAddress modelOrObjectOrFieldAddr) {
		assert modelOrObjectOrFieldAddr.getAddressedType() == XType.XMODEL
		        || modelOrObjectOrFieldAddr.getAddressedType() == XType.XOBJECT
		        || modelOrObjectOrFieldAddr.getAddressedType() == XType.XFIELD;
		GaeUtils.deleteEntity(KeyStructure.createCombinedKey(modelOrObjectOrFieldAddr));
	}
	
	public static void createContainer(XAddress modelOrObjectAddr) {
		assert modelOrObjectAddr.getAddressedType() == XType.XMODEL
		        || modelOrObjectAddr.getAddressedType() == XType.XOBJECT;
		Entity e = new Entity(KeyStructure.createCombinedKey(modelOrObjectAddr));
		e.setProperty(PROP_PARENT, modelOrObjectAddr.getParent().toURI());
		GaeUtils.putEntity(e);
	}
	
}
