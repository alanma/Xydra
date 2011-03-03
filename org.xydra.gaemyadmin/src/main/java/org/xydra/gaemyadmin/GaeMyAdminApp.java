package org.xydra.gaemyadmin;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.csv.impl.memory.CsvTable;
import org.xydra.csv.impl.memory.Row;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.StrictErrorHandler;
import com.google.apphosting.api.ApiProxy;


/**
 * Goal: A web application that offers admin capabilities beyond the ones
 * already provided by Google.
 * 
 * Status: First proof of concept for a web app that can read the stats entites.
 * 
 * Next steps: Compute a zip file with all data an send it to Amazon S3 on
 * request or periodically.
 * 
 * @author voelkel
 * 
 */
public class GaeMyAdminApp {
	
	public static void restless(Restless restless, String prefix) {
		restless.addMethod("/stats", "GET", GaeMyAdminApp.class, "stats", true,

		new RestlessParameter("resultFormat", "text")

		);
		restless.addMethod("/backup", "GET", GaeMyAdminApp.class, "backup", true);
		restless.addMethod("/deleteAll", "GET", GaeMyAdminApp.class, "deleteAll", true,
		        new RestlessParameter("sure", "no"));
	}
	
	private DatastoreService datastore;
	
	public GaeMyAdminApp() {
		// Get a handle on the datastore itself
		this.datastore = DatastoreServiceFactory.getDatastoreService();
	}
	
	public String backup() {
		return "not yet implemented";
	}
	
	/**
	 * Delete all data in the data store
	 * 
	 * @throws IOException from underlying http streams or datastore
	 */
	public void deleteAll(HttpServletResponse res, String sure) throws IOException {
		res.setContentType("text/plain");
		res.setCharacterEncoding("utf-8");
		Writer w = res.getWriter();
		
		// Get a handle on the datastore itself
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<String> kinds = getAllKinds();
		for(String kind : kinds) {
			w.write("Kind '" + kind + "'. Counting ... ");
			Query q = new Query(kind).setKeysOnly();
			PreparedQuery pq = datastore.prepare(q);
			int count = pq.countEntities(FetchOptions.Builder.withDefaults());
			w.write(count + "\n");
		}
		
		if(sure.equalsIgnoreCase("yes")) {
			for(String kind : kinds) {
				w.write("Deleting kind " + kind + ". Getting keys ... ");
				List<Key> keys = new LinkedList<Key>();
				Query q = new Query(kind).setKeysOnly();
				PreparedQuery pq = datastore.prepare(q);
				for(Entity entity : pq.asIterable()) {
					keys.add(entity.getKey());
				}
				w.write("Bulk delete ... ");
				datastore.delete(keys);
				w.write("Deleted all '" + kind + "'.\n");
			}
		} else {
			w.write("Ok, did not delete anything. If you are really sure, add '?sure=yes' to this url.");
		}
		
		w.flush();
		w.close();
	}
	
	public List<String> getAllKinds() {
		List<String> kinds = new LinkedList<String>();
		
		// Get a handle on the datastore itself
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Iterable<Entity> statKinds = datastore.prepare(new Query("__Stat_Kind__")).asIterable();
		for(Entity statKind : statKinds) {
			String kind = statKind.getProperty("kind_name").toString();
			kinds.add(kind);
		}
		return kinds;
	}
	
	/**
	 * TODO add pagination
	 * 
	 * @param kind of entity to export
	 * @return an {@link Iterable} over all Entity of the given kind
	 */
	public Iterable<Entity> getEntitiesOfKind(String kind) {
		return this.datastore.prepare(new Query(kind)).asIterable();
	}
	
	public void stats(HttpServletRequest req, String resultFormat, HttpServletResponse res)
	        throws IOException {
		long start = System.currentTimeMillis();
		
		res.getWriter().write("=== Version 2010-11-10 $Revision$\n");
		
		DatastoreServiceConfig defaultConfig = DatastoreServiceConfig.Builder.withDefaults();
		res.getWriter().write(
		        "Default datastore config\n"

		        + "* deadline: " + defaultConfig.getDeadline() + "\n"

		        + "* implicitTransactionManagementPolicy: "
		                + defaultConfig.getImplicitTransactionManagementPolicy().name() + "\n"

		                + "* readPolicy: " + defaultConfig.getReadPolicy().getConsistency() + "\n"

		);
		
		// put dummy entity to test datastore health
		Key key = KeyFactory.createKey("dummy", "dummyEntity");
		Entity dummyEntity = new Entity(key);
		try {
			this.datastore.put(dummyEntity);
			res.getWriter().write("Datastore health: Fully functional and writeable\n");
		} catch(ApiProxy.CapabilityDisabledException e) {
			res.getWriter().write("Datastore health: /!\\ READ-ONLY MODE /!\\ \n");
		}
		
		MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
		ms.setErrorHandler(new StrictErrorHandler());
		
		try {
			ms.put("dummy", "dummy");
			res.getWriter().write("Memcache health:  Fully functional and writeable\n");
		} catch(com.google.appengine.api.memcache.MemcacheServiceException e) {
			// Memcache is down, degrade gracefully
			res.getWriter().write(
			        "Memcache health:  /!\\ READ-ONLY MODE /!\\ Will return no hits\n");
		}
		
		Collection<Transaction> txns = this.datastore.getActiveTransactions();
		res.getWriter().write("Active transactions: " + txns.size() + "\n");
		
		FetchOptions defaultFetchOptions = FetchOptions.Builder.withDefaults();
		res.getWriter().write("Default fetchOptions\n"

		+ "* chunkSize: " + defaultFetchOptions.getChunkSize() + "\n"

		+ "* limit: " + defaultFetchOptions.getLimit() + "\n"

		+ "* offset: " + defaultFetchOptions.getOffset() + "\n"

		+ "* prefetchSize: " + defaultFetchOptions.getPrefetchSize() + "\n"

		);
		
		// Each statistic entity has the following properties:
		// * count, the number of items considered by the statistic (a long
		// integer)
		// * bytes, the total size of the items for this statistic (a long
		// integer)
		// * timestamp, the time of the most recent update to the statistic (a
		// date-time value)
		
		// __Stat_Kind__
		// Entities of a kind; one stat entity for each kind of entity stored.
		// props: kind_name, the name of the kind represented (a string)
		
		if(resultFormat.equals("text")) {
			res.getWriter().write("All kinds of entities\n");
			for(String kind : getAllKinds()) {
				res.getWriter().write("* Kind: " + kind + "\n");
				
				for(Entity e : getEntitiesOfKind(kind)) {
					// handle entity
					res.getWriter().write(
					        "** appid:" + e.getAppId() + " namespace:" + e.getNamespace()
					                + " props:" + e.getProperties().keySet() + "\n");
					
					// handle properties
					Map<String,Object> props = e.getProperties();
					for(Map.Entry<String,Object> me : props.entrySet()) {
						res.getWriter().write("*** " + me.getKey() + " = " + me.getValue() + "\n");
					}
				}
			}
		} else {
			CsvTable csv = new CsvTable();
			res.getWriter().write("Fetching kind names...\n");
			for(String kind : getAllKinds()) {
				res.getWriter().write("Processing all elements of kind: " + kind + "\n");
				for(Entity e : getEntitiesOfKind(kind)) {
					// handle entity
					Row row = csv.getOrCreateRow(e.getKey().toString(), true);
					row.setValue("appid", e.getAppId(), true);
					row.setValue("namespace", e.getNamespace(), true);
					row.setValue("kind", e.getKind(), true);
					if(e.getParent() != null) {
						row.setValue("parentkey", e.getParent().toString(), true);
					}
					
					// handle properties
					Map<String,Object> props = e.getProperties();
					for(Map.Entry<String,Object> me : props.entrySet()) {
						Object o = me.getValue();
						String value = null;
						if(o == null) {
							// keep null
						} else if(o instanceof Text) {
							value = ((Text)o).getValue();
						} else {
							value = o.toString();
						}
						row.setValue("prop-" + me.getKey(), value, true);
						if(o != null) {
							row.setValue("prop-" + me.getKey() + "-type", o.getClass().getName(),
							        true);
						}
					}
				}
			}
			
			res.getWriter().write("--------- >8 ----- CSV \n");
			csv.writeTo(res.getWriter());
			res.getWriter().write("--------- >8 ----- CSV \n");
		}
		
		// __Stat_Kind_IsRootEntity__
		// Entities of a kind that are entity group root entities (have no
		// ancestor
		// parent); one stat entity for each kind of entity stored.
		// Additional properties: kind_name, the name of the kind represented (a
		// string)
		
		// __Stat_Kind_NotRootEntity__
		// Entities of a kind that are not entity group root entities (have an
		// ancestor parent); one stat entity for each kind of entity stored.
		// Additional properties: kind_name, the name of the kind represented (a
		// string)
		
		// __Stat_PropertyType__
		// Properties of a value type across all entities; one stat entity per
		// value
		// type.
		// Additional properties: property_type, the name of the value type (a
		// string)
		
		// __Stat_PropertyType_Kind__
		// Properties of a value type across entities of a given kind; one stat
		// entity per combination of property type and kind.
		// Additional properties:
		// * property_type, the name of the value type (a string)
		// * kind_name, the name of the kind represented (a string)
		
		// __Stat_PropertyName_Kind__
		// Properties with a given name across entities of a given kind; one
		// stat
		// entity per combination of unique property name and kind.
		// Additional properties:
		// * property_name, the name of the property (a string)
		// * kind_name, the name of the kind represented (a string)
		
		// __Stat_PropertyType_PropertyName_Kind__
		// Properties with a given name and of a given value type across
		// entities of
		// a given kind; one stat entity per combination of property name, value
		// type and kind that exists in the datastore.
		// Additional properties:
		// * property_type, the name of the value type (a string)
		// * property_name, the name of the property (a string)
		// * kind_name, the name of the kind represented (a string)
		
		// Some statistics refer to datastore property value types by name, as
		// strings. These names are as follows:
		//
		// * "Blob"
		// * "Boolean"
		// * "ByteString"
		// * "Category"
		// * "Date/Time"
		// * "Email"
		// * "Float"
		// * "GeoPt"
		// * "Integer"
		// * "Key"
		// * "Link"
		// * "NULL"
		// * "PhoneNumber"
		// * "PostalAddress"
		// * "Rating"
		// * "String"
		// * "Text"
		// * "User"
		
		Entity statTotal = this.datastore.prepare(new Query("__Stat_Total__")).asSingleEntity();
		if(statTotal != null) {
			Long totalBytes = (Long)statTotal.getProperty("bytes");
			Long totalEntities = (Long)statTotal.getProperty("count");
			res.getWriter()
			        .write("Datastore contains " + totalBytes + " bytes in " + totalEntities
			                + " entities");
		} else {
			res.getWriter()
			        .write("No entity named '__Stat_Total__' found. Works maybe only in production. Stats are computed only once a day.");
		}
		
		long stop = System.currentTimeMillis();
		res.getWriter().write("Done processing in " + ((stop - start) / 1000) + " seconds");
		
		res.setContentType("text/plain");
		res.setCharacterEncoding("utf-8");
		res.getWriter().flush();
		res.getWriter().close();
	}
}
