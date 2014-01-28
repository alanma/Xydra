package org.xydra.index.impl;

public class DebugUtils {
    
    /**
     * @param object @CanBeNull
     * @param maxLen
     * @return a String with max len 'maxLen'
     */
    public static String toLimitedString(Object object, int maxLen) {
        if(object == null)
            return "-null-";
        String s = object.toString();
        if(s.length() > maxLen) {
            return s.substring(0, maxLen - 3) + "...";
        } else {
            return s;
        }
    }
    
}
