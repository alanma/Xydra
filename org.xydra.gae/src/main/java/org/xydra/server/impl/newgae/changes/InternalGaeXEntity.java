package org.xydra.server.impl.newgae.changes;

import java.util.Set;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XType;
import org.xydra.server.impl.newgae.GaeUtils;

import com.sun.org.apache.xpath.internal.objects.XObject;


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
	protected static void remove(XAddress modelOrObjectOrFieldAddr, Set<XAddress> locks) {
		assert GaeChangesService.canWrite(modelOrObjectOrFieldAddr, locks);
		assert modelOrObjectOrFieldAddr.getAddressedType() == XType.XMODEL
		        || modelOrObjectOrFieldAddr.getAddressedType() == XType.XOBJECT
		        || modelOrObjectOrFieldAddr.getAddressedType() == XType.XFIELD;
		GaeUtils.deleteEntity(KeyStructure.createCombinedKey(modelOrObjectOrFieldAddr));
	}
	
}
