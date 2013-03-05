package org.xydra.conf;

import java.util.Set;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.conf.impl.ConfigTool;


/**
 * Allows a runtime configuration management with many involved modules.
 * Fundamentally following the blackboard pattern, each component can write or
 * read config values.
 * 
 * Normal usage is best done with enum types to keep using consistent keys.
 * 
 * seeAlso {@link ConfigTool}
 * 
 * @author xamde
 * 
 */
public interface IConfig {
    
    /**
     * Allows a class to register a required key. A call to
     * {@link #isComplete()} reveals if all such required keys have been
     * satisfied.
     * 
     * @param key
     * @param caller
     */
    void addRequiredSetting(String key, Class<?> caller);
    
    /**
     * @param key @NeverNull
     * @return the current value for the key @NeverNull
     * @throws ConfigException if value is null
     */
    @NeverNull
    String get(Enum<?> key);
    
    /**
     * @param key @NeverNull
     * @return the current value for the key @NeverNull
     * @throws ConfigException if value is null
     */
    @NeverNull
    String get(String key);
    
    /**
     * @param key
     * @return the current value for the key.
     * @throws ConfigException if key is not defined
     */
    boolean getBoolean(Enum<?> key);
    
    /**
     * @param key
     * @return the current value for the key.
     * @throws ConfigException if key is not defined
     */
    boolean getBoolean(String key);
    
    /**
     * @return all keys that have values, including those with default values
     */
    Iterable<String> getDefinedKeys();
    
    /**
     * @param key
     * @return human-readable documentation for a key
     */
    String getDocumentation(String key);
    
    /**
     * @return just those keys that have explicitly defined values, i.e. not
     *         those with only a default value
     */
    Iterable<String> getExplicitlyDefinedKeys();
    
    /**
     * @param key
     * @return the current value for the key converted as a boolean.
     * @throws ConfigException if key is not defined
     */
    long getLong(Enum<?> key);
    
    /**
     * @param key
     * @return the current value for the key converted as a boolean.
     * @throws ConfigException if key is not defined
     */
    long getLong(String key);
    
    /**
     * @return the set of all keys that are required, but have no value yet.
     */
    Set<String> getMissingRequiredKeys();
    
    /**
     * @return all keys that are explicitly required. If one is missing
     *         {@link #isComplete()} returns false.
     */
    Set<String> getRequiredKeys();
    
    /**
     * @param key
     * @return null or string array
     */
    String[] getStringArray(String key);
    
    /**
     * @param key
     * @return @NeverNull
     */
    Set<String> getStringSet(String key);
    
    /**
     * @return true if all required keys (registered via
     *         {@link #addRequiredSetting(String, Class)} ) are defined
     */
    boolean isComplete();
    
    /**
     * Revert a key to its default value, if defined. Undefined behaviour
     * otherwise.
     * 
     * @param key
     */
    void revertToDefault(String key);
    
    /**
     * Set the value of key
     * 
     * @param key @NeverNull
     * @param value @NeverNull
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder set(Enum<?> key, String value);
    
    /**
     * Set the value of key
     * 
     * @param key @NeverNull
     * @param value @NeverNull
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder set(String key, String value);
    
    /**
     * Set the current value for a keys as a boolean
     * 
     * @param key
     * @param b
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setBoolean(String key, boolean b);
    
    /**
     * Set the current value for a keys as a boolean
     * 
     * @param key
     * @param b
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setBoolean(Enum<?> key, boolean b);
    
    /**
     * Set the default value for a key, which is used, if no explicit value is
     * set.
     * 
     * @param key
     * @param value
     * @param initial is true, when you set the value initially. If another
     *            default value was already set, you get an
     *            {@link ConfigException}. If you set initial to false, silent
     *            overwrite mode is active.
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setDefault(String key, String value, boolean initial);
    
    /**
     * Set the human-readable documentation for the given key
     * 
     * @param key
     * @param documentation
     * @return a {@link ConfBuilder} for a fluent API style
     */
    IConfig setDocumentation(Enum<?> key, String documentation);
    
    /**
     * Set the human-readable documentation for the given key
     * 
     * @param key
     * @param documentation
     * @return a {@link ConfBuilder} for a fluent API style
     */
    IConfig setDocumentation(String key, String documentation);
    
    /**
     * Set the current value for a keys as a long
     * 
     * @param key
     * @param l
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setLong(String key, long l);
    
    /**
     * @param key
     * @param values may not contain pipe symbol '|', may not be null
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setStrings(Enum<?> key, String ... values);
    
    /**
     * @param key
     * @param values may not contain pipe symbol '|', may not be null
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setStrings(String key, String ... values);
    
    /**
     * @param key @NeverNull
     * @return value or null
     */
    @CanBeNull
    String tryToGet(String key);
    
    ConfBuilder setLong(Enum<?> key, long value);
    
    Set<String> getStringSet(Enum<?> key);
    
}
