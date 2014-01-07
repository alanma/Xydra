package org.xydra.log;

import java.util.Collection;

import org.xydra.annotations.ThreadSafe;


@ThreadSafe
public class DefaultLoggerFactorySPI implements ILoggerFactorySPI {
    
    @Override
    public Logger getLogger(String name, Collection<ILogListener> logListener) {
        return new DefaultLogger(name, logListener);
    }
    
    @Override
    public Logger getThreadSafeLogger(String name, Collection<ILogListener> logListener) {
        // TODO implement logListeners
        return new ThreadSafeDefaultLogger(name);
    }
    
    @Override
    public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Logger getThreadSafeWrappedLogger(String name,
            String fullyQualifiedNameOfDelegatingLoggerClass) {
        throw new UnsupportedOperationException();
    }
    
}
