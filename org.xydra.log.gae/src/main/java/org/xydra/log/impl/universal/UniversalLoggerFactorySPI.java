package org.xydra.log.impl.universal;

import java.util.Collection;

import org.xydra.annotations.ThreadSafe;
import org.xydra.conf.IConfig;
import org.xydra.conf.annotations.RequireConf;
import org.xydra.env.Env;
import org.xydra.log.ILogListener;
import org.xydra.log.ILoggerFactorySPI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.impl.jul.JulLoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;


/**
 * In development mode log4j is used. In production, j.u.l. is used.
 * 
 * @author voelkel
 */
@ThreadSafe
public class UniversalLoggerFactorySPI implements ILoggerFactorySPI {
    
    private ILoggerFactorySPI factory = null;
    
    /**
     * Create and register appropriate factory.
     */
    @RequireConf(value = { ConfParamsUniversalLog.GAE_IN_PRODUCTION,
            ConfParamsUniversalLog.GWT_IN_PRODUCTION })
    public UniversalLoggerFactorySPI() {
        if(this.factory == null) {
            IConfig conf = Env.get().conf();
            boolean gwtInProduction = conf.getBoolean(ConfParamsUniversalLog.GWT_IN_PRODUCTION);
            boolean gaeInProduction = conf.getBoolean(ConfParamsUniversalLog.GAE_IN_PRODUCTION);
            
            if(gwtInProduction || gaeInProduction) {
                this.factory = new JulLoggerFactory();
                getLogger(LoggerFactory.ROOT_LOGGER_NAME, null).info("Using j.u.l. logger");
            } else {
                this.factory = new Log4jLoggerFactory();
                getLogger(LoggerFactory.ROOT_LOGGER_NAME, null).info("Using Log4j logger");
            }
            LoggerFactory.setLoggerFactorySPI(this.factory);
        }
    }
    
    @Override
    public Logger getLogger(String name, Collection<ILogListener> logListener) {
        return this.factory.getLogger(name, logListener);
    }
    
    @Override
    public Logger getWrappedLogger(String name, String fullyQualifiedNameOfDelegatingLoggerClass) {
        return this.factory.getWrappedLogger(name, fullyQualifiedNameOfDelegatingLoggerClass);
    }
    
    @Override
    public Logger getThreadSafeLogger(String name, Collection<ILogListener> logListeners) {
        return this.factory.getThreadSafeLogger(name, logListeners);
    }
    
    @Override
    public Logger getThreadSafeWrappedLogger(String name,
            String fullyQualifiedNameOfDelegatingLoggerClass) {
        return this.factory.getThreadSafeWrappedLogger(name,
                fullyQualifiedNameOfDelegatingLoggerClass);
    }
    
    /**
     * Configures default values of {@link ConfParamsUniversalLog} and sets
     * logger factory
     * 
     * @param inGWT if running as compiled JavaScript
     * @param onGAE if running on App Engine
     */
    public static void activate(boolean inGWT, boolean onGAE) {
        IConfig conf = Env.get().conf();
        new ConfParamsUniversalLog().configure(conf);
        conf.setBoolean(ConfParamsUniversalLog.GWT_IN_PRODUCTION, inGWT);
        conf.setBoolean(ConfParamsUniversalLog.GAE_IN_PRODUCTION, onGAE);
        LoggerFactory.setLoggerFactorySPI(new UniversalLoggerFactorySPI());
    }
    
}
