package org.xydra.log.api;

import org.xydra.annotations.ThreadSafe;


/**
 * Interface for Xydra loggers. Named 'Logger' to be compatible to java util
 * logging or Log4j, you only need to change the imports.
 * 
 * @author voelkel
 */
@ThreadSafe
/*
 * The current code in this class is thread safe. Changing this class might
 * change that and may make implementations of this abstract class, which are
 * currently thread-safe, thread-unsafe.
 */
public interface Logger {
    
    /**
     * The classic set of warning levels. Generalised from log4j, slf4j, and
     * java util logging.
     * 
     * @author voelkel
     */
    public static enum Level {
        
        /* FINE in j.u.l. */
        Debug(1),
        
        /* SEVERE in j.u.l. */
        Error(4),
        
        /* INFO in j.u.l. */
        Info(2),
        
        /* FINEST in j.u.l. */
        Trace(0),
        
        /* WARNING in j.u.l. */
        Warn(3);
        
        /**
         * @param levelName upper, lower or mixed case
         * @return the Level
         * @throws IllegalArgumentException
         */
        public static Level fromString(String levelName) throws IllegalArgumentException {
            for(Level level : values()) {
                if(levelName.equalsIgnoreCase(level.name()))
                    return level;
            }
            throw new IllegalArgumentException("Could not parse '" + levelName + "'");
        }
        
        private int num;
        
        Level(int num) {
            this.num = num;
        }
        
        public int getNumericLevel() {
            return this.num;
        }
        
        public boolean isAsImportantOrEvenMoreImportantThan(Level other) {
            return this.num >= other.num;
        }
        
    }
    
    public static final String ROOT_LOGGER_NAME = "ROOT";
    
    void debug(String msg);
    
    void debug(String msg, Throwable t);
    
    void error(String msg);
    
    void error(String msg, Throwable t);
    
    void info(String msg);
    
    void info(String msg, Throwable t);
    
    boolean isDebugEnabled();
    
    boolean isErrorEnabled();
    
    boolean isInfoEnabled();
    
    boolean isTraceEnabled();
    
    boolean isWarnEnabled();
    
    void trace(String msg);
    
    void trace(String msg, Throwable t);
    
    void warn(String msg);
    
    void warn(String msg, Throwable t);
    
    /**
     * OPTIONAL OPERATION: Not all implementations support this
     * 
     * @param level
     */
    void setLevel(Level level);
}
