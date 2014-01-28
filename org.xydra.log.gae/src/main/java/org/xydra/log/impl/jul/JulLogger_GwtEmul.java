package org.xydra.log.impl.jul;

import java.util.logging.LogRecord;

import org.xydra.annotations.RunsInGWT;
import org.xydra.log.api.Logger;


public abstract class JulLogger_GwtEmul implements Logger {
    
    @RunsInGWT(false)
    protected synchronized void setCorrectCallerClassAndMethod(LogRecord record) {
        try {
            throw new RuntimeException("trigger");
        } catch(RuntimeException e) {
            e.fillInStackTrace();
            e.getStackTrace();
            if(this.hasLogListeners()) {
                // we are wrapped one level deeper
                record.setSourceClassName(e.getStackTrace()[4].getClassName());
                record.setSourceMethodName(e.getStackTrace()[4].getMethodName());
                // e.getStackTrace()[1].getLineNumber();
                record.setMessage(record.getMessage());
            } else {
                record.setSourceClassName(e.getStackTrace()[3].getClassName());
                record.setSourceMethodName(e.getStackTrace()[3].getMethodName());
                record.setMessage(record.getMessage());
                // e.getStackTrace()[1].getLineNumber();
            }
        }
        
    }
    
    protected abstract boolean hasLogListeners();
    
}
