package org.xydra.store.impl.gae.changes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.store.impl.utils.DebugFormatter;
import org.xydra.xgae.XGae;
import org.xydra.xgae.annotations.XGaeOperation;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SPreparedQuery;
import org.xydra.xgae.util.XGaeDebugHelper;
import org.xydra.xgae.util.XGaeDebugHelper.Timing;

/**
 * Contains some static algorithms
 *
 * @author dscharrer
 * @author xamde
 */
public class Utils {

	private static final Logger log = LoggerFactory.getLogger(Utils.class);

	/**
	 * Asks the datastore via a query for all children of the given address by
	 * using a clever query. The idea works like this: For finding all children
	 * of a key 'foo' we query for all items in the range
	 * 'foo'+lowest_possible_key until 'foo'+highest_possible_key.
	 *
	 * TODO Implementation note: Using a High Redundancy data store on
	 * AppEngine, this method may return and out-dated children list.
	 *
	 * @param address
	 *            a repository, model or object address. Fields have no
	 *            children.
	 * @return the XIds of all children
	 */
	@XGaeOperation(datastoreRead = true)
	public static Set<XId> findChildren(final XAddress address) {
		assert address.getRepository() != null;
		assert address.getField() == null;

		final StringBuffer uri = new StringBuffer();
		uri.append('/');
		uri.append(address.getRepository().toString());
		uri.append('/');
		if (address.getModel() != null) {
			uri.append(address.getModel().toString());
			uri.append('/');
			if (address.getObject() != null) {
				uri.append(address.getObject().toString());
				uri.append('/');
			}
		}

		final Set<XId> childIds = new HashSet<XId>();

		final String kind = address.getAddressedType().getChildType().toString();
		final String low = uri.toString();
		final String high = low + "\uFFFF";
		final SPreparedQuery preparedQuery = XGae.get().datastore().sync()
				.prepareRangeQuery(kind, true, low, high);

		for (final SEntity e : preparedQuery.asIterable()) {
			final XAddress childAddr = KeyStructure.toAddress(e.getKey());
			assert address.equals(childAddr.getParent());
			childIds.add(getEntityId(childAddr));
		}
		log.debug(XGaeDebugHelper.dataGet(XGae.get().datastore().sync().getDatastoreName(),
				"query-children:" + address.toURI(), DebugFormatter.format(childIds), Timing.Now));
		return childIds;
	}

	private static XId getEntityId(final XAddress address) {
		if (address.getField() != null) {
			return address.getField();
		}
		if (address.getObject() != null) {
			return address.getObject();
		}
		if (address.getModel() != null) {
			return address.getModel();
		}
		return address.getRepository();
	}

	public static Iterator<XAddress> findModelAdresses() {
		/*
		 * lookup XCHANGE entities by query: SELECT __key__ FROM XCHANGE WHERE
		 * __key__ < KEY('XCHANGE', '1')
		 */

		final SPreparedQuery preparedQuery = XGae.get().datastore().sync()
				.prepareRangeQuery(KeyStructure.KIND_XCHANGE, true, null, "1");
		final Iterator<SEntity> it = preparedQuery.asIterable().iterator();

		return new TransformingIterator<SEntity, XAddress>(it,
				new ITransformer<SEntity, XAddress>() {

					@Override
					public XAddress transform(final SEntity in) {
						return KeyStructure.getAddressFromChangeKey(in.getKey());
					}
				});
	}
}
