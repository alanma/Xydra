package org.xydra.log.impl.jul;

import java.util.Collection;

import org.xydra.annotations.ThreadSafe;
import org.xydra.log.ILogListener;
import org.xydra.log.ILoggerFactorySPI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerWithListeners;
import org.xydra.log.ThreadSafeLoggerWithListeners;


@ThreadSafe
public class JulLoggerFactory implements ILoggerFactorySPI {
    
    @Override
    public Logger getLogger(String name, Collection<ILogListener> logListeners) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        if(logListeners == null || logListeners.isEmpty()) {
            return new JulLogger(logger);
        } else {
            return new LoggerWithListeners(JulLogger.createWithListeners(logger), logListeners);
        }
    }
    
    @Override
    public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        return new JulLogger(logger, fullyQualifiedNameOfDelegatingLoggerClass);
    }
    
    @Override
    public Logger getThreadSafeLogger(String name, Collection<ILogListener> logListeners) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        if(logListeners == null || logListeners.isEmpty()) {
            return new JulLogger(logger);
        } else {
            return new ThreadSafeLoggerWithListeners(JulLogger.createWithListeners(logger),
                    logListeners);
        }
    }
    
    @Override
    public Logger getThreadSafeWrappedLogger(String name,
            String fullyQualifiedNameOfDelegatingLoggerClass) {
        // getWrappedLogger already returns a thread-safe logger
        return this.getWrappedLogger(name, fullyQualifiedNameOfDelegatingLoggerClass);
    }
    
}