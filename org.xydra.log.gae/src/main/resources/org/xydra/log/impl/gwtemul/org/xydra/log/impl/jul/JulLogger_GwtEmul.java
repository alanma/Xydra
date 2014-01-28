package org.xydra.log.impl.jul;

import java.util.logging.LogRecord;

import org.xydra.annotations.RunsInGWT;
import org.xydra.log.api.Logger;


public abstract class JulLogger_GwtEmul implements Logger {
    
    @RunsInGWT(false)
    protected synchronized void setCorrectCallerClassAndMethod(LogRecord record) {
        // not in GWT :-)
    }
    
    protected abstract boolean hasLogListeners();
    
}
