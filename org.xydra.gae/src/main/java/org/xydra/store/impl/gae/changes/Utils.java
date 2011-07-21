package org.xydra.store.impl.gae.changes;

import java.util.HashSet;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;


/**
 * Constains some static algorithms
 * 
 * @author dscharrer
 * @author xamde
 */
public class Utils {
	
	private static final String PROP_KEY = "__key__";
	
	/**
	 * Asks the datastore via a query for all children of the given adress by
	 * using a clever query. The idea works like this: For finding all children
	 * of a key 'foo' we query for all items in the range
	 * 'foo'+lowest_possible_key until 'foo'+highest_possible_key.
	 * 
	 * Implementation note: Using a High Redundancy data store on AppEngine,
	 * this method may return and out-dated children list.
	 * 
	 * @param address a repository, model or object address. Fields have no
	 *            children.
	 * @return the XIDs of all children
	 */
	@GaeOperation(datastoreRead = true)
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
}
