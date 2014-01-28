package org.xydra.log.impl.log4j;

import java.util.Collection;

import org.xydra.annotations.ThreadSafe;
import org.xydra.log.api.ILogListener;
import org.xydra.log.api.Logger;
import org.xydra.log.spi.ILoggerFactorySPI;


@ThreadSafe
public class Log4jLoggerFactory implements ILoggerFactorySPI {
    
    @Override
    public Logger getLogger(String name, Collection<ILogListener> logListeners) {
        throw new IllegalStateException("Make sure your conf knows that GWT is in production now");
    }
    
    @Override
    public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
        throw new IllegalStateException("Make sure your conf knows that GWT is in production now");
    }
    
    @Override
    public Logger getThreadSafeLogger(String name, Collection<ILogListener> logListeners) {
        throw new IllegalStateException("Make sure your conf knows that GWT is in production now");
    }
    
    @Override
    public Logger getThreadSafeWrappedLogger(String name,
            String fullyQualifiedNameOfDelegatingLoggerClass) {
        throw new IllegalStateException("Make sure your conf knows that GWT is in production now");
    }
    
}
