package org.xydra.log.impl.jul;

import java.util.logging.LogRecord;

import org.xydra.annotations.RunsInGWT;


@RunsInGWT(true)
public class Jul_GwtEmul {
    
    public static String getSourceClassName(LogRecord log) {
        String name = log.getLoggerName();
        int i = name.lastIndexOf('.');
        if(i > 0 && i + 1 < name.length()) {
            return name.substring(i + 1);
        }
        // fall-back
        return "?c";
    }
    
    public static String getSourceMethodName(LogRecord log) {
        return "?m";
    }
    
}
