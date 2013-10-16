package org.xydra.store.impl.gae;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import org.xydra.annotations.RunsInGWT;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeModelRevision;
import org.xydra.store.impl.gae.changes.KeyStructure;
import org.xydra.store.impl.utils.XydraDebugFormatter;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * GAE-specific parts of {@link DebugFormatter}. Separating them allows for
 * re-use in GWT.
 * 
 * @author xamde
 * 
 */
// Runs in GWT via a super-source
@RunsInGWT(false)
public class GaeDebugFormatter {
    
    public static boolean canHandle(Object o) {
        return o instanceof Key
        
        || o instanceof Entity
        
        || o instanceof com.google.appengine.api.memcache.MemcacheService.IdentifiableValue
        
        || o instanceof GaeModelRevision
        
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
                String aKey = a.getKey();
                Object aValue = a.getValue();
                buf.append("<br />"
                        + aKey
                        + ": "
                        + XydraDebugFormatter.formatString(
                                aValue == null ? "null" : aValue.toString(), 300, true) + "; ");
            }
            return "Entity={" + buf.toString() + " }";
        } else if(value instanceof GaeChange) {
            GaeChange c = (GaeChange)value;
            return "GaeChange {" + XydraDebugFormatter.formatString(c.toString(), 1000, false)
                    + "}";
        } else if(value instanceof GaeModelRevision) {
            GaeModelRevision g = (GaeModelRevision)value;
            return "GaeModelRevision {"
                    + XydraDebugFormatter.formatString(g.toString(), 140, false) + "}";
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
