package org.xydra.log.impl.jul;

import java.util.logging.LogRecord;

import org.xydra.annotations.ThreadSafe;
import org.xydra.conf.IConfig;
import org.xydra.env.Env;
import org.xydra.log.Logger;


/**
 * A Java Utils Logging logger that additionally is configurable via Env.conf()
 * by setting the logger name to the desired log level as type
 * {@link org.xydra.log.Logger.Level}
 * 
 * <h3>Mapping:</h3>
 * 
 * <pre>
 * FINEST  = trace()
 * FINER   > debug()
 * FINE    = debug()
 * INFO    = info()
 * CONFIG  > info()
 * WARNING = warn()
 * SEVERE  = error()
 * </pre>
 * 
 * @author voelkel
 */
@ThreadSafe
public class JulLogger extends JulLogger_GwtEmul {
    
    private static JulLogger logSystem;
    
    static {
        java.util.logging.Logger julLogSystem = java.util.logging.Logger.getLogger("logSystem");
        julLogSystem.setLevel(java.util.logging.Level.INFO);
        logSystem = new JulLogger(julLogSystem);
        logSystem.isConfigured = true;
    }
    
    /**
     * java.util.logging.Logger is thread-safe.
     */
    private java.util.logging.Logger jul;
    
    private boolean hasLogListeners;
    
    private boolean isConfigured;
    
    protected boolean hasLogListeners() {
        return this.hasLogListeners;
    }
    
    public JulLogger(java.util.logging.Logger julLogger) {
        super();
        this.jul = julLogger;
    }
    
    public static JulLogger createWithListeners(java.util.logging.Logger julLogger) {
        JulLogger j = new JulLogger(julLogger);
        j.hasLogListeners = true;
        return j;
    }
    
    public JulLogger(java.util.logging.Logger logger,
            String fullyQualifiedNameOfDelegatingLoggerClass) {
        this(logger);
        // IMPROVE fix and make sure stacktrace is computed correctly
    }
    
    private LogRecord createLogRecord(java.util.logging.Level level, String msg) {
        LogRecord record = new LogRecord(level, msg);
        record.setLoggerName(this.jul.getName());
        setCorrectCallerClassAndMethod(record);
        return record;
    }
    
    private LogRecord createLogRecord(java.util.logging.Level level, String msg, Throwable t) {
        LogRecord record = new LogRecord(level, msg);
        record.setLoggerName(this.jul.getName());
        record.setThrown(t);
        setCorrectCallerClassAndMethod(record);
        return record;
    }
    
    @Override
    public void debug(String msg) {
        if(!isDebugEnabled())
            return;
        this.jul.log(createLogRecord(java.util.logging.Level.FINE, msg));
    }
    
    @Override
    public void debug(String msg, Throwable t) {
        if(!isDebugEnabled())
            return;
        this.jul.log(createLogRecord(java.util.logging.Level.FINE, msg, t));
    }
    
    @Override
    public void error(String msg) {
        if(!isErrorEnabled())
            return;
        this.jul.log(createLogRecord(java.util.logging.Level.SEVERE, msg));
    }
    
    @Override
    public void error(String msg, Throwable t) {
        if(!isErrorEnabled())
            return;
        this.jul.log(createLogRecord(java.util.logging.Level.SEVERE, msg, t));
    }
    
    @Override
    public void info(String msg) {
        if(!isInfoEnabled())
            return;
        this.jul.log(createLogRecord(java.util.logging.Level.INFO, msg));
    }
    
    @Override
    public void info(String msg, Throwable t) {
        if(!isInfoEnabled())
            return;
        this.jul.log(createLogRecord(java.util.logging.Level.INFO, msg, t));
    }
    
    @Override
    public void trace(String msg) {
        if(!isTraceEnabled())
            return;
        this.jul.log(createLogRecord(java.util.logging.Level.FINEST, msg));
    }
    
    @Override
    public void trace(String msg, Throwable t) {
        if(!isTraceEnabled())
            return;
        this.jul.log(createLogRecord(java.util.logging.Level.FINEST, msg, t));
    }
    
    @Override
    public void warn(String msg) {
        if(!isWarnEnabled())
            return;
        this.jul.log(createLogRecord(java.util.logging.Level.WARNING, msg));
    }
    
    @Override
    public void warn(String msg, Throwable t) {
        if(!isWarnEnabled())
            return;
        this.jul.log(createLogRecord(java.util.logging.Level.WARNING, msg, t));
    }
    
    private void ensureLevelInit() {
        if(this.isConfigured)
            return;
        
        IConfig conf = Env.get().conf();
        String name = this.jul.getName();
        
        Logger.Level level = conf.tryToGetAs(name, Logger.Level.class);
        while(level == null && name.contains(".")) {
            name = name.substring(0, name.lastIndexOf('.'));
            level = conf.tryToGetAs(name, Logger.Level.class);
        }
        
        if(level == null) {
            logSystem.info("No log level defined for '" + this.jul.getName()
                    + "' and parents. Keeping current level of '" + this.jul.getLevel()
                    + "', null=inherit.");
            // if in doubt, cut some off
        } else {
            java.util.logging.Level julLevel = toJulLevel(level);
            logSystem.info("Set log level of " + this.jul.getName() + "' (defined on '" + name
                    + "') to '" + level + "' = '" + julLevel + "' in jul");
            this.jul.setLevel(julLevel);
            assert this.jul.getLevel() == julLevel;
        }
        this.isConfigured = true;
    }
    
    /**
     * Mapping: see {@link JulLogger} doc
     * 
     * @param level
     * @return
     */
    private static java.util.logging.Level toJulLevel(Level level) {
        switch(level) {
        case Error:
            return java.util.logging.Level.SEVERE;
        case Warn:
            return java.util.logging.Level.WARNING;
        case Info:
            return java.util.logging.Level.INFO;
        case Debug:
            return java.util.logging.Level.FINE;
        case Trace:
            return java.util.logging.Level.FINEST;
        default:
            throw new AssertionError();
        }
    }
    
    @Override
    public boolean isDebugEnabled() {
        ensureLevelInit();
        return this.jul.isLoggable(java.util.logging.Level.FINE);
    }
    
    @Override
    public boolean isTraceEnabled() {
        ensureLevelInit();
        return this.jul.isLoggable(java.util.logging.Level.FINEST);
    }
    
    @Override
    public boolean isErrorEnabled() {
        ensureLevelInit();
        return this.jul.isLoggable(java.util.logging.Level.SEVERE);
    }
    
    @Override
    public boolean isInfoEnabled() {
        ensureLevelInit();
        return this.jul.isLoggable(java.util.logging.Level.INFO);
    }
    
    @Override
    public boolean isWarnEnabled() {
        ensureLevelInit();
        return this.jul.isLoggable(java.util.logging.Level.WARNING);
    }
    
    @Override
    public String toString() {
        return this.jul.getName();
    }
    
}
