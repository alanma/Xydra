package org.xydra.xgae.datastore.impl.gae;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;

/**
 * Utility to use synchronous datastore
 * 
 * @author voelkel
 */
public abstract class DatastoreImplGaeBase {

	public static final String PROP_KEY = "__key__";

	/**
	 * How-to:
	 * 
	 * <pre>
	 * Query q = new Query(kind).addFilter(PROP_KEY, FilterOperator.GREATER_THAN_OR_EQUAL, first)
	 * 		.setKeysOnly();
	 * </pre>
	 * 
	 * @param kind
	 * @param keysOnly
	 *            if true, the query is very fast and returns entities that have
	 *            only the key defined
	 * @param lowestName
	 *            inclusive, @CanBeNull to denote there is no lower bound -
	 *            requires a highName in this case
	 * @param highestName
	 *            inclusive @CanBeNull to denote there is no upper bound -
	 *            requires a lowName in this case
	 * @return
	 */
	protected Query createRangeQuery(String kind, boolean keysOnly, String lowestName,
			String highestName) {
		assert lowestName != null || highestName != null;

		Key lowestKey = KeyFactory.createKey(kind, lowestName);
		Key highestKey = KeyFactory.createKey(kind, highestName);

		Query query = new Query(kind);
		if (lowestName == null) {
			assert highestName != null;
			// create half-open interval
			query.setFilter(

			new Query.FilterPredicate(PROP_KEY, FilterOperator.LESS_THAN_OR_EQUAL, highestKey)

			);
		} else if (highestName == null) {
			assert lowestName != null;
			// create half-open interval
			query.setFilter(

			new Query.FilterPredicate(PROP_KEY, FilterOperator.GREATER_THAN_OR_EQUAL, lowestKey)

			);
		} else {
			// create closed interval
			query.setFilter(

			CompositeFilterOperator.and(

			new Query.FilterPredicate(PROP_KEY, FilterOperator.GREATER_THAN_OR_EQUAL, lowestKey),

			new Query.FilterPredicate(PROP_KEY, FilterOperator.LESS_THAN_OR_EQUAL, highestKey)

			));
		}

		if (keysOnly) {
			query.setKeysOnly();
		}
		return query;
	}

}
