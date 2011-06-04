package org.xydra.store.impl.gae.changes;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XEntity;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.store.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.sun.org.apache.xpath.internal.objects.XObject;


/**
 * Internal helper class used by {@link GaeChangesService} to access the current
 * model state.
 * 
 * @author dscharrer
 * 
 */
abstract public class InternalGaeXEntity implements XEntity {
	
	protected static final String PROP_KEY = "__key__";
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
		return GaeUtils.deleteEntityAsync(KeyStructure.createEntityKey(modelOrObjectOrFieldAddr));
	}
	
	public static Set<XID> findChildren(XAddress address) {
		
		assert address.getRepository() != null;
		assert address.getField() == null;
		StringBuffer uri = new StringBuffer();
		uri.append('/');
		uri.append(address.getRepository().toString());
		uri.append('/');
		if(address.getModel() != null) {
			uri.append(address.getModel().toString());
			uri.append('/');
			if(address.getObject() != null) {
				uri.append(address.getObject().toString());
				uri.append('/');
			}
		}
		
		String kind = address.getAddressedType().getChildType().toString();
		
		Key first = KeyFactory.createKey(kind, uri.toString());
		Key last = KeyFactory.createKey(kind, first.getName() + "\uFFFF");
		
		Set<XID> childIds = new HashSet<XID>();
		
		Query q = new Query(kind).addFilter(PROP_KEY, FilterOperator.GREATER_THAN, first)
		        .addFilter(PROP_KEY, FilterOperator.LESS_THAN, last).setKeysOnly();
		for(Entity e : GaeUtils.prepareQuery(q).asIterable()) {
			XAddress childAddr = KeyStructure.toAddress(e.getKey());
			assert address.equals(childAddr.getParent());
			childIds.add(getEntityId(childAddr));
		}
		return childIds;
	}
	
	private static XID getEntityId(XAddress address) {
		if(address.getField() != null) {
			return address.getField();
		}
		if(address.getObject() != null) {
			return address.getObject();
		}
		if(address.getModel() != null) {
			return address.getModel();
		}
		return address.getRepository();
	}
	
	public static boolean exists(XAddress address) {
		Key key = KeyStructure.createEntityKey(address);
		Entity entity = GaeUtils.getEntity(key);
		return (entity != null);
	}
	
}
