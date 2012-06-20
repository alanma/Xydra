package org.xydra.gae.datalogger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.model.impl.memory.UUID;
import org.xydra.index.impl.IteratorUtils;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.index.iterator.TransformingIterator.Transformer;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.store.impl.gae.AsyncDatastore;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.SyncDatastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.apphosting.api.ApiProxy.CapabilityDisabledException;


/**
 * Logs data in the GAE backend. Extracts key-value pairs from DATA?key=value
 * syntax.
 * 
 * @author xamde
 * 
 */
public class DataLogger {
	
	private static final DataRecordEntryHandler ENTRYHANDLER = new DataRecordEntryHandler();
	
	private static final String KIND_DATARECORD = "DATA_RECORD";
	private static final Logger log = LoggerFactory.getLogger(DataLogger.class);
	private static final Transformer<Entity,DataRecord> TRANSFORMER = new Transformer<Entity,DataRecord>() {
		@Override
		public DataRecord transform(Entity in) {
			return ENTRYHANDLER.fromEntity(in);
		}
	};
	
	/**
	 * @param key property name to be prefix-filtered
	 * @param valuePrefix start of prefix search
	 * @return the query further constrained with prefixKey ~ shouldStartWith
	 *         prefixValue
	 */
	@SuppressWarnings("unused")
	private static Query addKeyPrefixFilter(Query query, String key, String valuePrefix) {
		return query.addFilter(key, FilterOperator.GREATER_THAN_OR_EQUAL, valuePrefix).addFilter(
		        key, FilterOperator.LESS_THAN_OR_EQUAL,
		        valuePrefix + GaePersistence.LAST_UNICODE_CHAR);
	}
	
	private static Query addKeyValueFilter(Query query, String key, String value) {
		return query.addFilter(key, FilterOperator.EQUAL, value);
	}
	
	public static DataRecord createDataRecord(Map<String,String> map) {
		DataRecord dr = new DataRecord(System.currentTimeMillis(), map);
		return dr;
	}
	
	/**
	 * @param start inclusive
	 * @param end inclusive
	 * @return a prepared query that returns all records in the given time range
	 */
	@SuppressWarnings("unused")
	private static Query createIntervalQuery_property(long start, long end) {
		Query query = new Query(KIND_DATARECORD).addSort(DataRecord.CREATION_DATE)
		        .addFilter(DataRecord.CREATION_DATE, FilterOperator.GREATER_THAN_OR_EQUAL, start)
		        .addFilter(DataRecord.CREATION_DATE, FilterOperator.LESS_THAN_OR_EQUAL, end);
		return query;
	}
	
	/**
	 * @param start inclusive
	 * @param end inclusive
	 * @return a prepared query that returns all records in the given time range
	 */
	private static Query createIntervalQuery_key(long start, long end) {
		Query query = new Query(KIND_DATARECORD).addSort(DataRecord.KEY);
		
		/* constrain only if thats a benefit */
		if(start > 0 && end < Long.MAX_VALUE) {
			query = query.addFilter(DataRecord.KEY, FilterOperator.GREATER_THAN_OR_EQUAL,
			        KeyFactory.createKey(KIND_DATARECORD, "" + start)).addFilter(
			        DataRecord.KEY,
			        FilterOperator.LESS_THAN_OR_EQUAL,
			        KeyFactory.createKey(KIND_DATARECORD, "" + end
			                + GaePersistence.LAST_UNICODE_CHAR));
		}
		
		return query;
	}
	
	private static Query createKeyQuery(String key) {
		Query query = new Query(KIND_DATARECORD).addFilter(DataRecord.KEY, FilterOperator.EQUAL,
		        KeyFactory.createKey(KIND_DATARECORD, "" + key));
		return query;
	}
	
	/**
	 * Delete all records matching the filter criteria
	 * 
	 * @param start first matching timestamp
	 * @param end last matching timestamp
	 * @param filter if defined, these are restricting the query
	 */
	public static void deleteRecords(long start, long end, Pair<String,String> ... filter) {
		Iterator<Entity> it = toExecutableQuery(toGaeQuery(start, end, filter)).asIterable()
		        .iterator();
		Iterator<Key> keyIt = new TransformingIterator<Entity,Key>(it,
		
		new Transformer<Entity,Key>() {
			@Override
			public Key transform(Entity in) {
				return in.getKey();
			}
		});
		
		HashSet<Key> keys = IteratorUtils.addAll(keyIt, new HashSet<Key>());
		
		try {
			SyncDatastore.deleteEntities(keys);
		} catch(CapabilityDisabledException err) {
			log.warn("Could not delete anything. ", err);
		}
	}
	
	/**
	 * @param start first matching timestamp (=inclusive)
	 * @param end last matching timestamp (=inclusive)
	 * @param filter if defined, these are restricting the query. Null-values
	 *            are treaded as wildcard, i.e. that pair is ignored
	 * @return all {@link DataRecord} created in given time range.
	 */
	public static Iterator<DataRecord> getRecords(long start, long end,
	        Pair<String,String> ... filter) {
		Iterator<Entity> it = toExecutableQuery(toGaeQuery(start, end, filter)).asIterable()
		        .iterator();
		return new TransformingIterator<Entity,DataRecord>(it, TRANSFORMER);
	}
	
	/**
	 * @param log is ignored
	 * @param level 0 (TRACE) -- 4 (ERROR)
	 * @param msg is only processed if format = "DATA?key1=val1&key2=val2..."
	 * @param t can be null
	 */
	public static void handleLog(Logger log, int level, String msg, Throwable t) {
		if(msg != null && msg.contains("DATA?")) {
			String dataQuery = msg.substring(msg.indexOf("DATA?") + "DATA?".length());
			Map<String,String> map = ServletUtils.parseQueryString(dataQuery);
			if(t != null) {
				map.put("logExceptionClass", t.getClass().getName());
			}
			DataRecord dr = DataRecord.create() // .
			        // built-ins
			        .withParam("logLevel", "" + level) // .
			        .withParams(map) // .
			        .done();
			log(dr);
		}
	}
	
	public static void log(DataRecord dataRecord) {
		String keyStr = dataRecord.getKey();
		if(keyStr == null) {
			/* creation date is a prefix */
			keyStr = dataRecord.getCreationDate() + "-" + UUID.uuid(8);
		}
		Key key = KeyFactory.createKey(KIND_DATARECORD, keyStr);
		Entity e = ENTRYHANDLER.toEntity(key, dataRecord);
		try {
			AsyncDatastore.putEntity(e);
		} catch(CapabilityDisabledException err) {
			log.warn("Could not write " + dataRecord.toString(), err);
		}
	}
	
	/**
	 * @param map see {@link DataRecord#DataRecord(long, Map)} for constraints
	 * @return the created DataRecord
	 */
	public static DataRecord log(Map<String,String> map) {
		DataRecord dr = createDataRecord(map);
		log(dr);
		return dr;
	}
	
	private static PreparedQuery toExecutableQuery(Query query) {
		return SyncDatastore.prepareQuery(query);
	}
	
	/**
	 * @param start inclusive
	 * @param end inclusive
	 * @param filter optional, null-Filters are ignored
	 * @return a GAE query
	 */
	private static Query toGaeQuery(long start, long end, Pair<String,String> ... filter) {
		Query query = createIntervalQuery_key(start, end);
		
		if(filter != null) {
			for(Pair<String,String> p : filter) {
				if(p == null) {
					// just ignored
					continue;
				}
				if(p.getFirst() == null) {
					throw new IllegalArgumentException("A pair.first was null");
				}
				if(p.getFirst().equals("")) {
					throw new IllegalArgumentException("A pair.first was the empty string");
				}
				if(p.getSecond() == null || p.getSecond().equals("")) {
					// does not constrain the query
				} else {
					String key = p.getFirst().trim();
					String value = p.getSecond().trim();
					addKeyValueFilter(query, key, value);
				}
			}
		}
		return query;
	}
	
	public static DataRecord getRecordByKeys(String key) {
		Query query = createKeyQuery(key);
		PreparedQuery eQuery = toExecutableQuery(query);
		Iterator<Entity> it = eQuery.asIterable().iterator();
		TransformingIterator<Entity,DataRecord> transIt = new TransformingIterator<Entity,DataRecord>(
		        it, TRANSFORMER);
		return transIt.next();
	}
	
}
