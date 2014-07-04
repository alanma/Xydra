package org.xydra.sharedutils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;


@RunsInGWT(true)
public class DebugUtils {
    
    public static void dumpStacktrace() {
        try {
            throw new RuntimeException("CALLER");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static <T> Set<T> toSet(Iterator<T> iterator) {
        HashSet<T> set = new HashSet<T>();
        while(iterator.hasNext()) {
            set.add(iterator.next());
        }
        return set;
    }
    
    public static String toIndent(String indent, int count) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < count; i++) {
            buf.append(indent);
        }
        return buf.toString();
    }
    
    /**
     * @param label
     * @param b
     * @return a compact string for debugging
     */
    public static String dumpFlag(String label, boolean b) {
        return (b ? "+" : "-") + label;
    }
    
    public static <T> String toString(Iterable<T> c) {
        return toString(c.iterator());
    }
    
    public static <T> String toString(Iterator<T> it) {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        while(it.hasNext()) {
            T t = it.next();
            buf.append(t.toString());
            if(it.hasNext())
                buf.append(", ");
        }
        buf.append("}");
        return buf.toString();
    }
}
