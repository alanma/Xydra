package org.xydra.store.impl.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xydra.sharedutils.GServiceLoader;


/**
 * Singleton.
 * 
 * @author xamde
 */
public class DebugFormatter {
    
    public static final String LINE_END = "  <br/>\n";
    
    public static final int MAX_VALUE_STR_LEN = 40;
    
    private static List<IDebugFormatter> debugFormatters;
    
    public static synchronized String format(Object object) {
        // handle nulls
        if(object == null)
            return "-null-";
        
        // look in registered handlers
        ensureHandlersAreLoaded();
        for(IDebugFormatter df : debugFormatters) {
            String s = df.format(object);
            if(s != null) {
                return s;
            }
        }
        
        throw new IllegalStateException("Could not format object of type '"
                + object.getClass().getName() + "'");
    }
    
    private static void ensureHandlersAreLoaded() {
        if(debugFormatters == null) {
            debugFormatters = new ArrayList<IDebugFormatter>();
            
            Collection<IDebugFormatter> set = GServiceLoader
                    .getAllImplementations(IDebugFormatter.class);
            debugFormatters.addAll(set);
            debugFormatters.add(new GenericDebugFormatter());
        }
    }
    
    public static String formatString(String s) {
        return formatString(s, MAX_VALUE_STR_LEN, false);
    }
    
    public static String formatString(String s, int maxLen, boolean encodeAngleBrackets) {
        String res;
        if(s.length() <= maxLen) {
            res = s;
        } else {
            res = s.substring(0, Math.min(maxLen, s.length())) + " ...[truncated]";
        }
        if(encodeAngleBrackets) {
            // fit better in html places AND in emails, too
            return res.replace("<", "-[").replace(">", "]-");
        } else {
            return res;
        }
    }
    
}
