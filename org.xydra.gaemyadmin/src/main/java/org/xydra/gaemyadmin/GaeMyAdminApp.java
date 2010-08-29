package org.xydra.gaemyadmin;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
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
	
	static {
		restless();
	}
	
	public static void restless() {
		GaeMyAdminApp app = new GaeMyAdminApp();
		
		Restless.addGet("/stats", app, "stats");
		Restless.addGet("/backup", app, "backup");
	}
	
	public void stats(HttpServletRequest req, HttpServletResponse res) throws IOException {
		StringBuffer msg = new StringBuffer();
		msg.append("=== Version 2010-08-29 14:27\n");
		
		DatastoreServiceConfig defaultConfig = DatastoreServiceConfig.Builder.withDefaults();
		msg.append("Default datastore config\n"

		+ "* deadline: " + defaultConfig.getDeadline() + "\n"

		+ "* implicitTransactionManagementPolicy: "
		        + defaultConfig.getImplicitTransactionManagementPolicy().name() + "\n"

		        + "* readPolicy: " + defaultConfig.getReadPolicy().getConsistency() + "\n"

		);
		
		// Get a handle on the datastore itself
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		// put dummy entity to test datastore health
		Key key = KeyFactory.createKey("dummy", "dummyEntity");
		Entity dummyEntity = new Entity(key);
		try {
			datastore.put(dummyEntity);
			msg.append("Datastore health: Fully functional and writeable\n");
		} catch(ApiProxy.CapabilityDisabledException e) {
			msg.append("Datastore health: /!\\ READ-ONLY MODE /!\\ \n");
		}
		
		MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
		ms.setErrorHandler(new StrictErrorHandler());
		
		try {
			ms.put("dummy", "dummy");
			msg.append("Memcache health: Fully functional and writeable\n");
		} catch(com.google.appengine.api.memcache.MemcacheServiceException e) {
			// Memcache is down, degrade gracefully
			msg.append("Memcache health: /!\\ READ-ONLY MODE /!\\ Will return no hits\n");
		}
		
		Collection<Transaction> txns = datastore.getActiveTransactions();
		msg.append("Active transactions: " + txns.size() + "\n");
		
		FetchOptions defaultFetchOptions = FetchOptions.Builder.withDefaults();
		msg.append("Default fetchOptions\n"

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
		
		msg.append("All kinds of entities\n");
		Iterable<Entity> statKinds = datastore.prepare(new Query("__Stat_Kind__")).asIterable();
		for(Entity statKind : statKinds) {
			String kind = statKind.getProperty("kind_name").toString();
			msg.append("* Kind: " + kind + "\n");
			for(Entity e : datastore.prepare(new Query(kind).setKeysOnly()).asIterable()) {
				msg.append("** appid:" + e.getAppId() + " namespace:" + e.getNamespace()
				        + " props:" + e.getProperties().keySet() + "\n");
				Map<String,Object> props = e.getProperties();
				for(Map.Entry<String,Object> me : props.entrySet()) {
					msg.append("*** " + me.getKey() + " = " + me.getValue() + "\n");
				}
			}
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
			msg.append("Datastore contains " + totalBytes + " bytes in " + totalEntities
			        + " entities");
		} else {
			msg
			        .append("No entity named '__Stat_Total__' found. Works maybe only in production. Stats are computed only once a day.");
		}
		System.out.println(msg);
		
		res.setContentType("text/plain");
		res.setCharacterEncoding("utf-8");
		res.getWriter().println(msg);
		res.getWriter().flush();
		res.getWriter().close();
	}
}
