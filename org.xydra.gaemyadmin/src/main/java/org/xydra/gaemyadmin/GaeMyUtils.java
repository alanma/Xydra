package org.xydra.gaemyadmin;

import java.util.LinkedList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

public class GaeMyUtils {

	/**
	 * @return a list of all gae 'kinds' found in the datastore. They are
	 *         retrieved via the gae internal stats, represented as
	 *         __Stat_Kind__ which might be out of date for a while.
	 */
	public static List<String> getAllKinds() {
		final List<String> kinds = new LinkedList<String>();
		final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		final Iterable<Entity> statKinds = datastore.prepare(new Query("__Stat_Kind__")).asIterable();
		for (final Entity statKind : statKinds) {
			final String kind = statKind.getProperty("kind_name").toString();
			kinds.add(kind);
		}
		return kinds;
	}

	/**
	 * IMPROVE add pagination
	 *
	 * @param kind
	 *            of entity to export
	 * @return an {@link Iterable} over all Entity of the given kind
	 */
	public static Iterable<Entity> getEntitiesOfKind(final String kind) {
		final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		return datastore.prepare(new Query(kind)).asIterable();
	}

}
