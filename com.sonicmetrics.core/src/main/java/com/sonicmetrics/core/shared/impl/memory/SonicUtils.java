package com.sonicmetrics.core.shared.impl.memory;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;


public class SonicUtils {
    
    /**
     * @param category @NeverNull
     * @param action @CanBeNull
     * @param label @CanBeNull
     * @return a dot string, i.e. at least a 'somecategory', maybe longer in the
     *         format 'somecatgory.someaction', or even
     *         'somecatgory.someaction.somelabel'
     */
    @NeverNull
    public static String toDotString(@NeverNull String category, @CanBeNull String action,
            @CanBeNull String label) {
        assert category != null;
        StringBuilder b = new StringBuilder();
        b.append(category.toLowerCase());
        if(isDefined(action)) {
            b.append(".").append(action.toLowerCase());
            if(isDefined(label)) {
                b.append(".").append(label.toLowerCase());
            }
        }
        return b.toString();
    }
    
    /**
     * @param category @CanBeNull
     * @param action @CanBeNull
     * @param label @CanBeNull
     * @return a dot string, i.e. at least a '*' or 'somecategory', maybe longer
     *         in the format 'somecatgory.someaction', or even
     *         'somecatgory.someaction.somelabel'
     */
    @NeverNull
    public static String toDoStringWithWildcards(@CanBeNull String category,
            @CanBeNull String action, @CanBeNull String label) {
        return toDotString(category == null ? "*" : category, action, label);
    }
    
    public static boolean isDefined(String s) {
        return s != null && !s.equals("") && !s.equals("*");
    }
    
    public static boolean bothNullOrEqual(Object a, Object b) {
        if(a == null) {
            return b == null;
        } else {
            if(b == null)
                return false;
            else
                return a.equals(b);
        }
    }
    
    /**
     * @param a
     * @param b
     * @return true if constraint a is more general than b (or at most as
     *         specific)
     */
    public static boolean moreGeneralThanOrEqualTo(String a, String b) {
        if(a == null) {
            // a is unconstrained
            return true;
        } else {
            return a.equals(b);
        }
    }
    
    public static int hashCode(String s) {
        return s == null ? 0 : s.hashCode();
    }
    
}