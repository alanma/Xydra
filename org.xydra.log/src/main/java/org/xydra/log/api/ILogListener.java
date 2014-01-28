package org.xydra.log.api;

/**
 * Listens to all log events of a certain {@link Logger}. This is similar to the
 * concept of log handlers/appenders in other frameworks.
 * 
 * @author voelkel
 */
public interface ILogListener {
    
    /**
     * @param log the {@link Logger} sending the log message
     * @param msg the log message
     */
    void debug(Logger log, String msg);
    
    /**
     * @param log the {@link Logger} sending the log message
     * @param msg the log message
     * @param t @NeverNull a thrown exception to be logged
     */
    void debug(Logger log, String msg, Throwable t);
    
    /**
     * @param log the {@link Logger} sending the log message
     * @param msg the log message
     */
    void error(Logger log, String msg);
    
    /**
     * @param log the {@link Logger} sending the log message
     * @param msg the log message
     * @param t @NeverNull a thrown exception to be logged
     */
    void error(Logger log, String msg, Throwable t);
    
    /**
     * @param log the {@link Logger} sending the log message
     * @param msg the log message
     */
    void info(Logger log, String msg);
    
    /**
     * @param log the {@link Logger} sending the log message
     * @param msg the log message
     * @param t @NeverNull a thrown exception to be logged
     */
    void info(Logger log, String msg, Throwable t);
    
    /**
     * @param log the {@link Logger} sending the log message
     * @param msg the log message
     */
    void trace(Logger log, String msg);
    
    /**
     * @param log the {@link Logger} sending the log message
     * @param msg the log message
     * @param t @NeverNull a thrown exception to be logged
     */
    void trace(Logger log, String msg, Throwable t);
    
    /**
     * @param log the {@link Logger} sending the log message
     * @param msg the log message
     */
    void warn(Logger log, String msg);
    
    /**
     * @param log the {@link Logger} sending the log message
     * @param msg the log message
     * @param t @NeverNull a thrown exception to be logged
     */
    void warn(Logger log, String msg, Throwable t);
}
