package org.xydra.conf;

import java.util.Set;

import org.xydra.conf.impl.ConfigTool;


/**
 * seeAlso {@link ConfigTool}
 * 
 * @author xamde
 * 
 */
public interface IConfig {
    
    /**
     * Allows a class to register a required key
     * 
     * @param key
     * @param caller
     */
    void addRequiredSetting(String key, Class<?> caller);
    
    /**
     * @param key
     * @return the current value for the key
     */
    String getString(String key);
    
    /**
     * @param key
     * @return ...
     */
    String getDocumentation(String key);
    
    /**
     * @return all keys that have values, including those with default values
     */
    Iterable<String> getAvailableKeys();
    
    /**
     * @return all keys that are explicitly required
     */
    Set<String> getRequiredKeys();
    
    /**
     * @return just those keys that have explicitly defined values
     */
    Iterable<String> getExplicitlyDefinedKeys();
    
    /**
     * @return true if all required keys are defined
     */
    boolean isComplete();
    
    /**
     * Set the value of key
     * 
     * @param key
     * @param value
     */
    void setString(String key, String value);
    
    /**
     * Revert to a default if defined. Undefined otherwise.
     * 
     * @param key
     */
    void revertToDefault(String key);
    
    /**
     * @return ...
     */
    Set<String> getMissingRequiredKeys();
    
    /**
     * @param key
     * @param value
     */
    void setDefaultString(String key, String value);
    
    /**
     * @param key
     * @param documentation
     */
    void setTheDoc(String key, String documentation);
    
    /**
     * @param key
     * @param l
     */
    void setLong(String key, long l);
    
    /**
     * @param key
     * @param b
     */
    void setBoolean(String key, boolean b);
    
    /**
     * @param key
     * @return the current value for the key
     */
    Long getLong(String key);
    
    /**
     * @param key
     * @return the current value for the key
     */
    Boolean getBoolean(String key);
    
}
