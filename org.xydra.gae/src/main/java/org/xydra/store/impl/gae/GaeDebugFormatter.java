package org.xydra.store.impl.gae;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.KeyStructure;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * GAE-specific parts of {@link DebugFormatter}. Separating them allows for
 * re-use in GWT.
 * 
 * @author xamde
 * 
 */
public class GaeDebugFormatter {
	
	public static boolean canHandle(Object o) {
		return o instanceof Key

		|| o instanceof Entity

		|| o instanceof com.google.appengine.api.memcache.MemcacheService.IdentifiableValue

		|| o instanceof GaeChange;
	}
	
	public static String toString(Object value) {
		if(value instanceof Key) {
			return KeyStructure.toString((Key)value);
		} else if(value instanceof com.google.appengine.api.memcache.MemcacheService.IdentifiableValue) {
			return DebugFormatter
			        .format((((com.google.appengine.api.memcache.MemcacheService.IdentifiableValue)value)
			                .getValue()));
		} else if(value instanceof Entity) {
			Entity e = (Entity)value;
			if(e.equals(Memcache.NULL_ENTITY)) {
				return "NullEntity";
			}
			StringBuffer buf = new StringBuffer();
			buf.append("key:" + e.getKey() + " ");
			for(Entry<String,Object> a : e.getProperties().entrySet()) {
				buf.append(a.getKey() + ": " + DebugFormatter.formatString(a.getValue().toString())
				        + "; ");
			}
			return "Entity={" + buf.toString() + " }";
		} else if(value instanceof GaeChange) {
			GaeChange c = (GaeChange)value;
			return "GaeChange {" + DebugFormatter.formatString(c.toString(), 140) + "}";
		}
		
		throw new IllegalAccessError("Cannot handle this, check via canHandle() before");
		
	}
	
	/**
	 * @return "2011-10-12 19:05:02.617" UTC time
	 */
	public static String currentTimeInGaeFormat() {
		long ms = System.currentTimeMillis();
		Date d = new Date(ms);
		DateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
		return df.format(d);
	}
	
}
