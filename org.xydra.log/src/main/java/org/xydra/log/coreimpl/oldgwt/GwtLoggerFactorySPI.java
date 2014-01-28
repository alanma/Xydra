package org.xydra.log.coreimpl.oldgwt;

import java.util.Collection;

import org.xydra.annotations.ThreadSafe;
import org.xydra.log.api.ILogListener;
import org.xydra.log.api.Logger;
import org.xydra.log.coreimpl.util.LoggerWithListeners;
import org.xydra.log.coreimpl.util.ThreadSafeLoggerWithListeners;
import org.xydra.log.spi.ILoggerFactorySPI;


/**
 * Delegate to an GWT 2.1 logger
 * 
 * @author xamde
 */
@ThreadSafe
@Deprecated
public class GwtLoggerFactorySPI implements ILoggerFactorySPI {
    
    @Override
    public Logger getLogger(String name, Collection<ILogListener> logListeners) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        Logger gwtLogger = new GwtLogger(logger);
        if(logListeners != null) {
            return new LoggerWithListeners(gwtLogger, logListeners);
        } else {
            return gwtLogger;
        }
    }
    
    @Override
    public Logger getThreadSafeLogger(String name, Collection<ILogListener> logListeners) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        Logger gwtLogger = new GwtLogger(logger);
        if(logListeners != null) {
            return new ThreadSafeLoggerWithListeners(gwtLogger, logListeners);
        } else {
            return gwtLogger;
        }
    }
    
    @Override
    public Logger getThreadSafeWrappedLogger(String name,
            String fullyQualifiedNameOfDelegatingLoggerClass) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
        throw new UnsupportedOperationException();
    }
}
