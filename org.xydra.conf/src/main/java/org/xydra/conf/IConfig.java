package org.xydra.conf;

import java.util.Map;
import java.util.Set;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;


/**
 * Allows a runtime configuration management with many involved modules.
 * 
 * Fundamentally simply a singleton, following the blackboard pattern: each
 * component can write or read config values.
 * 
 * Normal usage is best done with enum types to keep using consistent keys.
 * 
 * seeAlso {@link org.xydra.conf.impl.ConfigTool}
 * 
 * @author xamde
 * 
 */
@RunsInGWT(true)
public interface IConfig {
    
    /**
     * Allows a class to register a required key. A call to
     * {@link #isComplete()} reveals if all such required keys have been
     * satisfied.
     * 
     * @param key @NeverNull
     * @param caller
     */
    void addRequiredSetting(Enum<?> key, Class<?> caller);
    
    /**
     * Allows a class to register a required key. A call to
     * {@link #isComplete()} reveals if all such required keys have been
     * satisfied.
     * 
     * @param key @NeverNull
     * @param caller
     */
    void addRequiredSetting(String key, Class<?> caller);
    
    /**
     * @param key @NeverNull
     * @throws ConfigException if given key is not defined
     */
    void assertDefined(Enum<?> key);
    
    /**
     * @param key @NeverNull
     * @throws ConfigException if given key is not defined
     */
    void assertDefined(String key);
    
    /**
     * @param key @NeverNull
     * @return the current value for the key @NeverNull
     * @throws ConfigException if value is null
     */
    @NeverNull
    Object get(Enum<?> key);
    
    /**
     * @param key @NeverNull
     * @return the current value for the key @NeverNull
     * @throws ConfigException if value is null
     */
    @NeverNull
    Object get(String key);
    
    /**
     * @param key @NeverNull
     * @return the current value for the key.
     * @throws ConfigException if key is not defined
     */
    boolean getBoolean(Enum<?> key);
    
    /**
     * @param key @NeverNull
     * @return the current value for the key.
     * @throws ConfigException if key is not defined
     */
    boolean getBoolean(String key);
    
    /**
     * @return all keys that have values, including those with default values
     */
    Iterable<String> getDefinedKeys();
    
    /**
     * @param key @NeverNull
     * @return human-readable documentation for a key
     */
    String getDocumentation(Enum<?> key);
    
    /**
     * @param key @NeverNull
     * @return human-readable documentation for a key
     */
    String getDocumentation(String key);
    
    /**
     * @return just those keys that have explicitly defined values, i.e. not
     *         those with only a default value
     */
    Iterable<String> getExplicitlyDefinedKeys();
    
    /**
     * @return an id string that helps distinguish several config instances at
     *         runtime from each other. Mostly to fix instantiation bugs.
     */
    public String getInternalId();
    
    /**
     * @param key @NeverNull
     * @return the current value for the key converted as a boolean.
     * @throws ConfigException if key is not defined
     */
    long getLong(Enum<?> key);
    
    /**
     * @param key @NeverNull
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
     * @param key @NeverNull
     * @return the defined value for a key as a string array or null, if key not
     *         set
     */
    String[] getStringArray(String key);
    
    /**
     * @param key @NeverNull
     * @return the value of this key as a Set of Strings, or an empty set, if
     *         key not set @NeverNull
     */
    Set<String> getStringSet(Enum<?> key);
    
    /**
     * @param key @NeverNull
     * @return the value of this key as a Set of Strings, or an empty set, if
     *         key not set @NeverNull
     */
    Set<String> getStringSet(String key);
    
    /**
     * @param key @NeverNull
     * @return the value of this key as a String @NeverNull
     * @throws ConfigException if value is null
     */
    String getString(Enum<?> key);
    
    /**
     * @param key @NeverNull
     * @return the value of this key as a String @NeverNull
     * @throws ConfigException if value is null
     */
    String getString(String key);
    
    /**
     * @return true if all required keys (registered via
     *         {@link #addRequiredSetting(String, Class)} ) are defined
     */
    boolean isComplete();
    
    /**
     * Revert a key to its default value, if defined. Undefined behaviour
     * otherwise.
     * 
     * @param key @NeverNull
     */
    void revertToDefault(Enum<?> key);
    
    /**
     * Revert a key to its default value, if defined. Undefined behaviour
     * otherwise.
     * 
     * @param key @NeverNull
     */
    void revertToDefault(String key);
    
    /**
     * Set the value of key
     * 
     * @param key @NeverNull
     * @param value @NeverNull
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder set(Enum<?> key, Object value);
    
    /**
     * Set the value of key
     * 
     * @param key @NeverNull
     * @param value @NeverNull
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder set(String key, Object value);
    
    /**
     * Set the current value for a keys as a boolean
     * 
     * @param key @NeverNull
     * @param b
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setBoolean(Enum<?> key, boolean b);
    
    /**
     * Set the current value for a keys as a boolean
     * 
     * @param key @NeverNull
     * @param b
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setBoolean(String key, boolean b);
    
    /**
     * Set the default value for a key, which is used, if no explicit value is
     * set.
     * 
     * @param key @NeverNull
     * @param value
     * @param initial is true, when you set the value initially. If another
     *            default value was already set, you get an
     *            {@link ConfigException}. If you set initial to false, silent
     *            overwrite mode is active.
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setDefault(Enum<?> key, Object value, boolean initial);
    
    /**
     * Set the default value for a key, which is used, if no explicit value is
     * set.
     * 
     * @param key @NeverNull
     * @param value
     * @param initial is true, when you set the value initially. If another
     *            default value was already set, you get an
     *            {@link ConfigException}. If you set initial to false, silent
     *            overwrite mode is active.
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setDefault(String key, Object value, boolean initial);
    
    /**
     * Set the human-readable documentation for the given key
     * 
     * @param key @NeverNull
     * @param documentation
     * @return a {@link ConfBuilder} for a fluent API style
     */
    IConfig setDocumentation(Enum<?> key, String documentation);
    
    /**
     * Set the human-readable documentation for the given key
     * 
     * @param key @NeverNull
     * @param documentation
     * @return a {@link ConfBuilder} for a fluent API style
     */
    IConfig setDocumentation(String key, String documentation);
    
    /**
     * Set the desired instance type for the given key
     * 
     * @param key @NeverNull
     * @param type @NeverNull The expected type of a config key. Currently a
     *            purely informative setting, might be used stronger in later
     *            IConfig versions.
     * @return a {@link ConfBuilder} for a fluent API style
     */
    IConfig setType(Enum<?> key, Class<?> type);
    
    /**
     * Set the desired instance type for the given key
     * 
     * @param key @NeverNull
     * @param type @NeverNull The expected type of a config key. Currently a
     *            purely informative setting, might be used stronger in later
     *            IConfig versions.
     * @return a {@link ConfBuilder} for a fluent API style
     */
    IConfig setType(String key, Class<?> type);
    
    /**
     * Set the current value for a keys as a long
     * 
     * @param key @NeverNull
     * @param l
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setLong(Enum<?> key, long l);
    
    /**
     * Set the current value for a keys as a long
     * 
     * @param key @NeverNull
     * @param l
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setLong(String key, long l);
    
    /**
     * @param key @NeverNull
     * @param values may not contain pipe symbol '|', may not be null
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setStrings(Enum<?> key, String ... values);
    
    /**
     * @param key @NeverNull
     * @param values may not contain pipe symbol '|', may not be null
     * @return a {@link ConfBuilder} for a fluent API style
     */
    ConfBuilder setStrings(String key, String ... values);
    
    /**
     * @param key @NeverNull
     * @return value or @CanBeNull
     */
    @CanBeNull
    Object tryToGet(Enum<?> key);
    
    /**
     * Throws no exceptions if a value if missing
     * 
     * @param key @NeverNull
     * @return value or @CanBeNull
     */
    @CanBeNull
    Object tryToGet(String key);
    
    /**
     * @return a shallow copy
     */
    IConfig copy();
    
    /**
     * @param key @NeverNull
     * @return the current value for the key.
     * @throws ConfigException if key is not defined
     */
    <T> IResolver<T> getResolver(Enum<?> key);
    
    /**
     * @param interfaze @NeverNull
     * @return the current value for the key.
     * @throws ConfigException if key is not defined
     */
    <T> IResolver<T> getResolver(Class<T> interfaze);
    
    /**
     * @param key @NeverNull
     * @return the current value for the key.
     * @throws ConfigException if key is not defined
     */
    <T> IResolver<T> getResolver(String key);
    
    /**
     * Set the current value for a key
     * 
     * @param key @NeverNull
     * @param resolver
     * @return a {@link ConfBuilder} for a fluent API style
     */
    <T> ConfBuilder setResolver(Enum<?> key, IResolver<T> resolver);
    
    /**
     * Set the current value for a key
     * 
     * @param key @NeverNull
     * @param resolver
     * @return a {@link ConfBuilder} for a fluent API style
     */
    <T> ConfBuilder setResolver(String key, IResolver<T> resolver);
    
    /**
     * Set the current value for a key
     * 
     * @param interfaze @NeverNull
     * @param resolver
     * @return a {@link ConfBuilder} for a fluent API style
     */
    <T> ConfBuilder setResolver(Class<T> interfaze, IResolver<T> resolver);
    
    /**
     * Convenience method
     * 
     * @param interfaze @NeverNull
     * @return @CanBeNull or the desired singleton instance
     * @throws IllegalArgumentException
     * @throws RuntimeException
     */
    <T> T resolve(Class<T> interfaze) throws IllegalArgumentException, RuntimeException;
    
    /**
     * Convenience method
     * 
     * @param interfaze @NeverNull
     * @return @CanBeNull or the desired singleton instance
     */
    <T> T tryToResolve(Class<T> interfaze);
    
    /**
     * Convenience method
     * 
     * @param key @NeverNull
     * @return @CanBeNull or the desired singleton instance
     */
    <T> T tryToResolve(String key);
    
    /**
     * Convenience method
     * 
     * @param key @NeverNull
     * @param returnType @NeverNull
     * @return the value for the given key, casted to the desired returnType
     * @throws ConfigException if value is missing
     */
    <T> T getAs(String key, Class<T> returnType) throws ConfigException;
    
    /**
     * Convenience method; Throws no exception on missing values
     * 
     * @param key @NeverNull
     * @param returnType @NeverNull
     * @return the value for the given key, casted to the desired returnType
     */
    <T> T tryToGetAs(String key, Class<T> returnType);
    
    /**
     * Convenience method
     * 
     * @param key @NeverNull
     * @param returnType @NeverNull
     * @return the value for the given key, casted to the desired returnType
     */
    <T> T getAs(Enum<?> key, Class<T> returnType);
    
    /**
     * @param key @NeverNull
     * @param value
     */
    <T> void setAs(String key, T value);
    
    /**
     * @param key @NeverNull
     * @param value
     */
    <T> void setAs(Enum<?> key, T value);
    
    /**
     * Eager instantiation. Instance has been created and is set to conf. To be
     * used via {@link #getResolver(Class)}.
     * 
     * @param interfaze @NeverNull
     * @param instance
     */
    <T> void setInstance(Class<T> interfaze, T instance);
    
    /**
     * Eager instantiation. Instance has been created and is set to conf. To be
     * used via {@link #getResolver(Class)}.
     * 
     * @param key @NeverNull
     * @param instance
     */
    <T> void setInstance(String key, T instance);
    
    /**
     * Lazy instantiation. Class needs to have a public zero-args constructor.
     * To be used via {@link #getResolver(Class)}.
     * 
     * @param interfaze @NeverNull
     * @param clazz
     */
    <T> void setClass(Class<T> interfaze, Class<? extends T> clazz);
    
    Map<String,? extends Object> asMap();
    
}
