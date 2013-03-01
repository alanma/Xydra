package org.xydra.store.impl.gae.changes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.SyncDatastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;


/**
 * Contains some static algorithms
 * 
 * @author dscharrer
 * @author xamde
 */
public class Utils {
	
	private static final Logger log = LoggerFactory.getLogger(Utils.class);
	
	public static final String PROP_KEY = "__key__";
	
	/**
	 * Asks the datastore via a query for all children of the given address by
	 * using a clever query. The idea works like this: For finding all children
	 * of a key 'foo' we query for all items in the range
	 * 'foo'+lowest_possible_key until 'foo'+highest_possible_key.
	 * 
	 * TODO Implementation note: Using a High Redundancy data store on
	 * AppEngine, this method may return and out-dated children list.
	 * 
	 * @param address a repository, model or object address. Fields have no
	 *            children.
	 * @return the XIds of all children
	 */
	@GaeOperation(datastoreRead = true)
	public static Set<XId> findChildren(XAddress address) {
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
		
		Set<XId> childIds = new HashSet<XId>();
		
		Query q = new Query(kind);
		q.setFilter(
		
		CompositeFilterOperator.and(
		
		new Query.FilterPredicate(PROP_KEY, FilterOperator.GREATER_THAN, first),
		
		new Query.FilterPredicate(PROP_KEY, FilterOperator.LESS_THAN, last)
		
		));
		q.setKeysOnly();
		
		for(Entity e : SyncDatastore.prepareQuery(q).asIterable()) {
			XAddress childAddr = KeyStructure.toAddress(e.getKey());
			assert address.equals(childAddr.getParent());
			childIds.add(getEntityId(childAddr));
		}
		log.debug(DebugFormatter.dataGet(SyncDatastore.DATASTORE_NAME,
		        "query-children:" + address.toURI(), DebugFormatter.format(childIds), Timing.Now));
		return childIds;
	}
	
	private static XId getEntityId(XAddress address) {
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
	
	public static Iterator<XAddress> findModelAdresses() {
		/*
		 * lookup XCHANGE entities by query: SELECT __key__ FROM XCHANGE WHERE
		 * __key__ < KEY('XCHANGE', '1')
		 */
		Query q = new Query(KeyStructure.KIND_XCHANGE);
		q.setFilter(
		
		new Query.FilterPredicate(PROP_KEY, FilterOperator.LESS_THAN, KeyFactory.createKey(
		        KeyStructure.KIND_XCHANGE, "1"))
		
		);
		q.setKeysOnly();
		
		final Iterator<Entity> it = SyncDatastore.prepareQuery(q).asIterable().iterator();
		
		return new TransformingIterator<Entity,XAddress>(it,
		        new TransformingIterator.Transformer<Entity,XAddress>() {
			        
			        @Override
			        public XAddress transform(Entity in) {
				        return KeyStructure.getAddressFromChangeKey(in.getKey());
			        }
		        });
	}
}
