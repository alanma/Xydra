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
import org.xydra.restless.utils.ServletUtils;
import org.xydra.store.impl.gae.AsyncDatastore;
import org.xydra.store.impl.gae.SyncDatastore;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;


/**
 * Logs data in the GAE backend. Extracts key-value pairs from DATA?key=value
 * syntax.
 * 
 * @author xamde
 * 
 */
public class DataLogger {
	
	private static final DataRecord.DataRecordEntryHandler ENTRYHANDLER = new DataRecord.DataRecordEntryHandler();
	private static final String KIND_DATARECORD = "DATA_RECORD";
	private static final Transformer<Entity,DataRecord> TRANSFORMER = new Transformer<Entity,DataRecord>() {
		@Override
		public DataRecord transform(Entity in) {
			return ENTRYHANDLER.fromEntity(in);
		}
	};
	
	/**
	 * @param map see {@link DataRecord#DataRecord(long, Map)} for constraints
	 * @return the created DataRecord
	 */
	public static DataRecord log(Map<String,String> map) {
		DataRecord dr = createDataRecord(map);
		log(dr);
		return dr;
	}
	
	public static void log(DataRecord dataRecord) {
		String keyStr = dataRecord.getCreationDate() + "-" + UUID.uuid(8);
		Key key = KeyFactory.createKey(KIND_DATARECORD, keyStr);
		Entity e = ENTRYHANDLER.toEntity(key, dataRecord);
		AsyncDatastore.putEntity(e);
	}
	
	public static DataRecord createDataRecord(Map<String,String> map) {
		DataRecord dr = new DataRecord(System.currentTimeMillis(), map);
		return dr;
	}
	
	/**
	 * @param start first matching timestamp
	 * @param end last matching timestamp
	 * @param filter if defined, these are restricting the query
	 * @return all {@link DataRecord} created in given time range.
	 */
	public static Iterator<DataRecord> getRecords(long start, long end,
	        Pair<String,String> ... filter) {
		Iterator<Entity> it = toGaeQuery(start, end, filter).asIterable().iterator();
		return new TransformingIterator<Entity,DataRecord>(it, TRANSFORMER);
	}
	
	private static PreparedQuery toGaeQuery(long start, long end, Pair<String,String> ... filter) {
		Query query = new Query(KIND_DATARECORD).addSort(DataRecord.CREATION_DATE)
		        .addFilter(DataRecord.CREATION_DATE, FilterOperator.GREATER_THAN_OR_EQUAL, start)
		        .addFilter(DataRecord.CREATION_DATE, FilterOperator.LESS_THAN_OR_EQUAL, end);
		if(filter != null) {
			for(Pair<String,String> p : filter) {
				if(p != null && p.getFirst() != null && !p.getFirst().equals("")) {
					query.addFilter(p.getFirst().trim(), FilterOperator.EQUAL, p.getSecond().trim());
				}
			}
		}
		return SyncDatastore.prepareQuery(query);
	}
	
	/**
	 * Delete all records matching the filter criteria
	 * 
	 * @param start first matching timestamp
	 * @param end last matching timestamp
	 * @param filter if defined, these are restricting the query
	 */
	public static void deleteRecords(long start, long end, Pair<String,String> ... filter) {
		Iterator<Entity> it = toGaeQuery(start, end, filter).asIterable().iterator();
		Iterator<Key> keyIt = new TransformingIterator<Entity,Key>(it,
		
		new Transformer<Entity,Key>() {
			@Override
			public Key transform(Entity in) {
				return in.getKey();
			}
		});
		
		HashSet<Key> keys = IteratorUtils.addAll(keyIt, new HashSet<Key>());
		SyncDatastore.deleteEntities(keys);
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
	
}