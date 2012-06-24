package org.xydra.gaemyadmin;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.csv.impl.memory.CsvTable;
import org.xydra.csv.impl.memory.Row;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.IRestlessContext;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;

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
 * Status: First proof of concept for a web app that can read the stats entites.
 * 
 * Next steps: Compute a zip file with all data an send it to Amazon S3 on
 * request or periodically.
 * 
 * @author xamde
 */
public class DatastoreAdminResource {
	
	private static final Logger log = LoggerFactory.getLogger(DatastoreAdminResource.class);
	static final String PAGE_NAME = "Datastore Admin";
	public static String URL;
	
	public static void restless(Restless restless, String prefix) {
		URL = prefix + "/datastore";
		restless.addMethod(URL, "GET", DatastoreAdminResource.class, "index", true);
		restless.addMethod(URL + "/stats", "GET", DatastoreAdminResource.class, "stats", true,
		
		new RestlessParameter("resultFormat", "text")
		
		);
		restless.addMethod(URL + "/deleteAll", "GET", DatastoreAdminResource.class, "deleteAll",
		        true, new RestlessParameter("confirm", "wrong"));
		
		restless.addMethod(URL + "/deleteKind", "GET", DatastoreAdminResource.class, "deleteKind",
		        true,
		        
		        new RestlessParameter("kind"), new RestlessParameter("confirm", "wrong")
		
		);
	}
	
	public void index(HttpServletResponse res, HttpServletRequest req) throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = AppConstants.startPage(res, PAGE_NAME, "");
		
		w.write(HtmlUtils.toOrderedList(Arrays.asList(
		        
		        HtmlUtils.link("/admin" + URL + "/stats", "Statistics"),
		        
		        HtmlUtils
		                .link("/admin" + URL + "/deleteKind?kind=",
		                        "Page to delete data of a certain kind - clicking this link just lists stats about data"),
		        
		        HtmlUtils.link("/admin" + URL + "/deleteAll",
		                "Page to delete all data - clicking this link just lists stats about data")
		
		)));
		
		AppConstants.endPage(w);
	}
	
	private static final String passwordPropertyNameInWebXml = "org.xydra.gaemyadmin.DatastoreAdminResource.password";
	
	/**
	 * Delete all data in the data store
	 * 
	 * @param context
	 * @param res
	 * @param confirmParam
	 * 
	 * @throws IOException from underlying http streams or datastore
	 */
	public void deleteAll(IRestlessContext context, HttpServletResponse res, String confirmParam)
	        throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		AdminAuthUtils.setTempAuthCookie(context, passwordPropertyNameInWebXml);
		
		Writer w = AppConstants.startPage(res, PAGE_NAME, "Delete All");
		
		// Get a handle on the datastore itself
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<String> kinds = GaeMyUtils.getAllKinds();
		for(String kind : kinds) {
			w.write("Kind '" + kind + "'. Counting ... ");
			Query q = new Query(kind).setKeysOnly();
			PreparedQuery pq = datastore.prepare(q);
			int count = pq.countEntities(FetchOptions.Builder.withDefaults());
			w.write(count + "\n");
		}
		
		String password = context.getRestless().getInitParameter(passwordPropertyNameInWebXml);
		w.write("Password is '"
		        + password
		        + "' it must match the URL param 'confirm' and the cookie. Setting cookie for 120 seconds ..."
		        + "<br/>\n");
		try {
			AdminAuthUtils.checkIfAuthorised(context, passwordPropertyNameInWebXml, confirmParam);
			for(String kind : kinds) {
				w.write("Deleting kind " + kind + ". Getting keys ... ");
				w.flush();
				List<Key> keys = new LinkedList<Key>();
				Query q = new Query(kind).setKeysOnly();
				PreparedQuery pq = datastore.prepare(q);
				for(Entity entity : pq.asIterable()) {
					keys.add(entity.getKey());
				}
				w.write("Bulk delete ... ");
				try {
					datastore.delete(keys);
				} catch(Exception e) {
					log.warn("Could not delete kind '" + kind + "'", e);
					w.write("Could not delete kind '" + kind + "'.");
					
				}
				w.write("Deleted all '" + kind + "'.\n");
			}
			w.write("Done with delete all.\n");
		} catch(Exception e) {
			w.write("Ok, did not delete anything. If you are really sure, add '?confirm=....' to this url.");
		}
		
		AppConstants.endPage(w);
	}
	
	/**
	 * Delete all data of a certain KIND in the data store
	 * 
	 * @param context
	 * @param res
	 * @param kind to be deleted
	 * @param confirmParam
	 * 
	 * @throws IOException from underlying http streams or datastore
	 */
	public void deleteKind(IRestlessContext context, HttpServletResponse res, String kind,
	        String confirmParam) throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = AppConstants.startPage(res, PAGE_NAME, "Delete Kind '" + kind + "'");
		
		AdminAuthUtils.setTempAuthCookie(context, passwordPropertyNameInWebXml);
		
		// Get a handle on the datastore itself
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		w.write("Kind '" + kind + "'. Counting ... ");
		Query q = new Query(kind).setKeysOnly();
		PreparedQuery pq = datastore.prepare(q);
		int count = pq.countEntities(FetchOptions.Builder.withDefaults());
		w.write(count + "\n");
		
		String password = context.getRestless().getInitParameter(passwordPropertyNameInWebXml);
		w.write("Password is '"
		        + password
		        + "' it must match the URL param 'confirm' and the cookie. Setting cookie for 120 seconds ..."
		        + "<br/>\n");
		try {
			AdminAuthUtils.checkIfAuthorised(context, passwordPropertyNameInWebXml, confirmParam);
			w.write("Deleting kind " + kind + ". Getting keys ... ");
			List<Key> keys = new LinkedList<Key>();
			q = new Query(kind).setKeysOnly();
			pq = datastore.prepare(q);
			for(Entity entity : pq.asIterable()) {
				keys.add(entity.getKey());
			}
			w.write("Bulk delete ... ");
			try {
				datastore.delete(keys);
			} catch(Exception e) {
				log.warn("Could not delete kind '" + kind + "'", e);
				w.write("Could not delete kind '" + kind + "'.");
				
			}
			w.write("Deleted all '" + kind + "'.\n");
		} catch(Exception e) {
			w.write("Ok, did not delete anything. If you are really sure, add '?confirm=....' to this url.");
		}
		
		AppConstants.endPage(w);
	}
	
	public void stats(HttpServletRequest req, String resultFormat, HttpServletResponse res)
	        throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = AppConstants.startPage(res, PAGE_NAME, "Stats");
		
		long start = System.currentTimeMillis();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		w.write("<code><pre>");
		
		w.write("=== Version 2010-11-10 $Revision: 8152 $\n");
		
		DatastoreServiceConfig defaultConfig = DatastoreServiceConfig.Builder.withDefaults();
		w.write("Default datastore config\n"
		
		+ "* deadline: " + defaultConfig.getDeadline() + "\n"
		
		+ "* implicitTransactionManagementPolicy: "
		        + defaultConfig.getImplicitTransactionManagementPolicy().name() + "\n"
		        
		        + "* readPolicy: " + defaultConfig.getReadPolicy().getConsistency() + "\n"
		
		);
		
		// put dummy entity to test datastore health
		Key key = KeyFactory.createKey("dummy", "dummyEntity");
		Entity dummyEntity = new Entity(key);
		try {
			datastore.put(dummyEntity);
			w.write("Datastore health: Fully functional and writeable\n");
		} catch(ApiProxy.CapabilityDisabledException e) {
			w.write("Datastore health: /!\\ READ-ONLY MODE /!\\ \n");
		}
		
		MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
		ms.setErrorHandler(new StrictErrorHandler());
		
		try {
			ms.put("dummy", "dummy");
			w.write("Memcache health:  Fully functional and writeable\n");
		} catch(com.google.appengine.api.memcache.MemcacheServiceException e) {
			// Memcache is down, degrade gracefully
			w.write("Memcache health:  /!\\ READ-ONLY MODE /!\\ Will return no hits\n");
		}
		
		Collection<Transaction> txns = datastore.getActiveTransactions();
		w.write("Active transactions: " + txns.size() + "\n");
		
		FetchOptions defaultFetchOptions = FetchOptions.Builder.withDefaults();
		w.write("Default fetchOptions\n"
		
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
			w.write("All kinds of entities\n");
			for(String kind : GaeMyUtils.getAllKinds()) {
				w.write("* Kind: " + kind + "\n");
				
				for(Entity e : GaeMyUtils.getEntitiesOfKind(kind)) {
					// handle entity
					w.write("** appid:" + e.getAppId() + " namespace:" + e.getNamespace()
					        + " props:" + e.getProperties().keySet() + "\n");
					
					// handle properties
					Map<String,Object> props = e.getProperties();
					for(Map.Entry<String,Object> me : props.entrySet()) {
						w.write("*** " + me.getKey() + " = " + me.getValue() + "\n");
					}
				}
			}
		} else {
			CsvTable csv = new CsvTable();
			w.write("Fetching kind names...\n");
			for(String kind : GaeMyUtils.getAllKinds()) {
				w.write("Processing all elements of kind: " + kind + "\n");
				for(Entity e : GaeMyUtils.getEntitiesOfKind(kind)) {
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
			
			w.write("--------- >8 ----- CSV \n");
			csv.writeTo(w);
			w.write("--------- >8 ----- CSV \n");
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
		
		Entity statTotal = datastore.prepare(new Query("__Stat_Total__")).asSingleEntity();
		if(statTotal != null) {
			Long totalBytes = (Long)statTotal.getProperty("bytes");
			Long totalEntities = (Long)statTotal.getProperty("count");
			w.write("Datastore contains " + totalBytes + " bytes in " + totalEntities + " entities");
		} else {
			w.write("No entity named '__Stat_Total__' found. Works maybe only in production. Stats are computed only once a day.");
		}
		
		long stop = System.currentTimeMillis();
		w.write("Done processing in " + ((stop - start) / 1000) + " seconds");
		
		w.write("</pre></code>");
		AppConstants.endPage(w);
	}
	
}
