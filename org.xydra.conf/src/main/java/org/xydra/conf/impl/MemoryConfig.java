package org.xydra.conf.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.Setting;
import org.xydra.conf.ConfBuilder;
import org.xydra.conf.ConfigException;
import org.xydra.conf.IConfig;
import org.xydra.conf.IResolver;
import org.xydra.index.impl.MapSetIndex;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


@RunsInGWT(true)
public class MemoryConfig implements IConfig {
    
    // TODO need to check if this one runs in GWT
    private static class ClassResolver<T> implements IResolver<T> {
        
        private Class<? extends T> clazz;
        
        /**
         * @param clazz @NeverNull
         */
        public ClassResolver(Class<? extends T> clazz) {
            this.clazz = clazz;
        }
        
        @Override
        public boolean canResolve() {
            return true;
        }
        
        @Override
        public T resolve() {
            return MemoryConfig_GwtEmul.newInstance(this.clazz);
        }
        
    }
    
    private static class InstanceResolver<T> implements IResolver<T> {
        
        private T instance;
        
        /**
         * @param instance @CanBeNull
         */
        public InstanceResolver(T instance) {
            this.instance = instance;
        }
        
        @Override
        public boolean canResolve() {
            return this.instance != null;
        }
        
        @Override
        public T resolve() {
            return this.instance;
        }
        
    }
    
    private static Logger log;
    
    @Setting("Compile-time flag, should be false for high performance")
    private static boolean traceOrigins = true;
    
    /**
     * @param className @NeverNull
     * @return
     * @throws RuntimeException when class could not be loaded or has wrong type
     */
    private static <T> IResolver<T> createResolverFromClassName(String className) {
        assert className != null;
        
        // try dynamic class loading
        Class<?> clazz = MemoryConfig_GwtEmul.classForName(className);
        if(clazz == null) {
            throw new RuntimeException("Class '" + className + "' could not be loaded");
        }
        Object instance = MemoryConfig_GwtEmul.newInstance(clazz);
        try {
            @SuppressWarnings("unchecked")
            T t = (T)instance;
            return new InstanceResolver<T>(t);
        } catch(ClassCastException e) {
            throw new RuntimeException("Defined class '" + clazz.getName()
                    + "' does not implement required type", e);
        }
        
    }
    
    public static String[] decodeList(String s) {
        return s.split("[|]");
    }
    
    public static String encodeList(String[] strings) {
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < strings.length; i++) {
            b.append(strings[i]);
            if(i + 1 < strings.length)
                b.append("|");
        }
        return b.toString();
    }
    
    // delayed log init for improved participation in boot sequences
    private static void ensureLogInit() {
        if(log == null) {
            log = LoggerFactory.getLogger(MemoryConfig.class);
        }
    }
    
    /** default values, are used if no explicit value has been defined */
    private TreeMap<String,Object> defaults = new TreeMap<String,Object>();
    
    /** human-readable */
    private HashMap<String,String> docs = new HashMap<String,String>();
    
    /** values that override the defaults */
    private TreeMap<String,Object> explicit = new TreeMap<String,Object>();
    
    /** for debugging */
    private final String internalId;
    
    private MapSetIndex<String,Class<?>> required = MapSetIndex.createWithFastEntrySets();
    
    private Set<Exception> setOrigins = new HashSet<Exception>();
    
    /** informative */
    private HashMap<String,Class<?>> types = new HashMap<String,Class<?>>();
    
    public MemoryConfig() {
        this.internalId = "" + ((int)(Math.random() * 10000d));
    }
    
    private MemoryConfig(String internalId) {
        this.internalId = internalId;
    }
    
    @Override
    public void addRequiredSetting(Enum<?> key, Class<?> caller) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        addRequiredSetting(key.name(), caller);
    }
    
    @Override
    public void addRequiredSetting(String key, Class<?> caller) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        this.required.index(key, caller);
    }
    
    @Override
    public Map<String,Object> asMap() {
        Map<String,Object> map = new TreeMap<String,Object>();
        
        for(String s : this.defaults.keySet()) {
            map.put(s, this.defaults.get(s));
        }
        for(String s : this.explicit.keySet()) {
            map.put(s, this.explicit.get(s));
        }
        
        return map;
    }
    
    @Override
    public void assertDefined(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        assertDefined(key.name());
    }
    
    @Override
    public void assertDefined(String key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        get(key);
    }
    
    @Override
    public IConfig copy() {
        MemoryConfig copy = new MemoryConfig(this.internalId + "-copy");
        copy.defaults.putAll(this.defaults);
        copy.docs = (HashMap<String,String>)this.docs.clone();
        copy.explicit.putAll(this.explicit);
        copy.required.clear();
        Iterator<KeyEntryTuple<String,Class<?>>> it = this.required.tupleIterator(
                new Wildcard<String>(), new Wildcard<Class<?>>());
        while(it.hasNext()) {
            KeyEntryTuple<String,Class<?>> keyEntryTuple = it.next();
            copy.required.index(keyEntryTuple.getKey(), keyEntryTuple.getEntry());
        }
        copy.setOrigins.clear();
        copy.setOrigins.addAll(this.setOrigins);
        return copy;
    }
    
    public Object get(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return get(key.name());
    }
    
    @Override
    public Object get(String key) {
        Object value = tryToGet(key);
        if(value == null) {
            throw new ConfigException("Config key '" + key
                    + "' requested but not defined - and no default defined either. " + idStr()
                    + " \n" + getDocumentation(key));
        }
        return value;
    }
    
    @Override
    public <T> T getAs(Enum<?> key, Class<T> clazz) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return getAs(key.name(), clazz);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAs(String key, Class<T> clazz) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        Object o = get(key);
        if(o == null)
            return null;
        return ((T)o);
    }
    
    @Override
    public boolean getBoolean(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return getBoolean(key.name());
    }
    
    @Override
    public boolean getBoolean(String key) {
        Object o = getInternal(key, Boolean.class, null);
        if(o instanceof String)
            return Boolean.parseBoolean((String)o);
        else
            return (Boolean)o;
    }
    
    @Override
    public Iterable<String> getDefinedKeys() {
        Set<String> keys = new HashSet<String>();
        keys.addAll(this.explicit.keySet());
        keys.addAll(this.defaults.keySet());
        return keys;
    }
    
    @Override
    public String getDocumentation(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return getDocumentation(key.name());
    }
    
    @Override
    public String getDocumentation(String key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return this.docs.get(key);
    }
    
    @Override
    public Iterable<String> getExplicitlyDefinedKeys() {
        return this.explicit.keySet();
    }
    
    /**
     * @param key
     * @param requestedType used only to generate better error messages
     * @return
     */
    private Object getInternal(String key, Class<?> requestedType, String callContext) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        
        Object value = tryToGet(key);
        
        if(value == null) {
            throw new ConfigException("Config key '" + key + "' requested as '"
                    + requestedType.getName()
                    + "' but not defined - and no default defined either. " + idStr() + " \n"
                    + getDocumentation(key));
        }
        return value;
    }
    
    /**
     * @return a short 4-character marker string to help identify which config
     *         is which.
     */
    public String getInternalId() {
        return this.internalId;
    }
    
    @Override
    public long getLong(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return getLong(key.name());
    }
    
    @Override
    public long getLong(String key) {
        Object o = get(key);
        if(o instanceof Integer) {
            return (long)(int)(Integer)o;
        }
        if(o instanceof Long) {
            return (Long)o;
        }
        if(o instanceof String) {
            return Long.parseLong((String)o);
        }
        throw new ConfigException("Value with key '" + key + "' not a long but '"
                + o.getClass().getName());
    }
    
    @Override
    public Set<String> getMissingRequiredKeys() {
        Set<String> open = new HashSet<String>(this.required.keySet());
        open.removeAll(this.explicit.keySet());
        open.removeAll(this.defaults.keySet());
        return open;
    }
    
    @Override
    public Set<String> getRequiredKeys() {
        return this.required.keySet();
    }
    
    @Override
    public <T> IResolver<T> getResolver(Class<T> interfaze) {
        return getResolver(interfaze.getName());
    }
    
    @Override
    public <T> IResolver<T> getResolver(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return getResolver(key.name());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> IResolver<T> getResolver(String key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        Object o = get(key);
        
        if(o instanceof String) {
            return createResolverFromClassName((String)o);
        }
        
        if(o instanceof IResolver) {
            return (IResolver<T>)o;
        }
        // default:
        throw new ConfigException("instance at key '" + key + "' could not be used. Type="
                + o.getClass().getName());
    }
    
    public String getString(Enum<?> key) {
        return getString(key.name());
    }
    
    @Override
    public String getString(String key) {
        Object o = get(key);
        if(!(o instanceof String))
            throw new ConfigException("instance at key '" + key + "' was not String but "
                    + o.getClass().getName());
        return (String)o;
    }
    
    @Override
    public String[] getStringArray(String key) {
        String s = getString(key);
        if(s == null)
            return null;
        return decodeList(s);
    }
    
    @Override
    public Set<String> getStringSet(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return getStringSet(key.name());
    }
    
    @Override
    public Set<String> getStringSet(String key) {
        String[] array = getStringArray(key);
        if(array == null)
            array = new String[0];
        return new HashSet<String>(Arrays.asList(array));
    }
    
    private String idStr() {
        return "[confId=" + getInternalId() + "]";
    }
    
    @Override
    public boolean isComplete() {
        return getMissingRequiredKeys().isEmpty();
    }
    
    @Override
    public <T> T resolve(Class<T> interfaze) {
        IResolver<T> resolver = getResolver(interfaze);
        if(resolver == null)
            return null;
        return resolver.resolve();
    }
    
    @Override
    public void revertToDefault(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        revertToDefault(key.name());
    }
    
    @Override
    public void revertToDefault(String key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        this.explicit.remove(key);
    }
    
    @Override
    public ConfBuilder set(Enum<?> key, Object value) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return set(key.name(), value);
    }
    
    @Override
    public ConfBuilder set(String key, Object value) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        ensureLogInit();
        log.trace("Setting '" + key + "' to object");
        this.explicit.put(key, value);
        return new ConfBuilder(this, key);
    }
    
    @Override
    public <T> void setAs(Enum<?> key, T value) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        setAs(key.name(), value);
    }
    
    @Override
    public <T> void setAs(String key, T value) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        set(key, value);
    }
    
    @Override
    public ConfBuilder setBoolean(Enum<?> key, boolean b) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return setBoolean(key.name(), b);
    }
    
    @Override
    public ConfBuilder setBoolean(String key, boolean b) {
        set(key, "" + b);
        return new ConfBuilder(this, key);
    }
    
    @Override
    public <T> void setClass(Class<T> interfaze, Class<? extends T> clazz) {
        if(interfaze == null)
            throw new IllegalArgumentException("Key may not be null");
        if(clazz == null)
            throw new IllegalArgumentException("Class may not be null");
        
        setResolver(interfaze, new ClassResolver<T>(clazz));
    }
    
    @Override
    public ConfBuilder setDefault(Enum<?> key, Object value, boolean initial) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return setDefault(key.name(), value, initial);
    }
    
    @Override
    public ConfBuilder setDefault(String key, Object value, boolean initial) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        if(!initial && this.defaults.containsKey(key))
            throw new ConfigException("Config key '" + initial + "' had already a default value "
                    + idStr());
        this.defaults.put(key, value);
        if(traceOrigins) {
            try {
                throw new RuntimeException("MARKER");
            } catch(RuntimeException e) {
                e.fillInStackTrace();
                this.setOrigins.add(e);
            }
        }
        return new ConfBuilder(this, key);
    }
    
    @Override
    public IConfig setDocumentation(Enum<?> key, String documentation) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return setDocumentation(key.name(), documentation);
    }
    
    @Override
    public IConfig setDocumentation(String key, String documentation) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        this.docs.put(key, documentation);
        return this;
    }
    
    @Override
    public <T> void setInstance(Class<T> interfaze, T instance) {
        if(interfaze == null)
            throw new IllegalArgumentException("Key may not be null");
        setResolver(interfaze, new InstanceResolver<T>(instance));
    }
    
    @Override
    public <T> void setInstance(String key, T instance) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        setResolver(key, new InstanceResolver<T>(instance));
    }
    
    @Override
    public ConfBuilder setLong(Enum<?> key, long value) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return setLong(key.name(), value);
    }
    
    @Override
    public ConfBuilder setLong(String key, long l) {
        set(key, "" + l);
        return new ConfBuilder(this, key);
    }
    
    @Override
    public <T> ConfBuilder setResolver(Class<T> interfaze, IResolver<T> resolver) {
        return setResolver(interfaze.getName(), resolver);
    }
    
    @Override
    public <T> ConfBuilder setResolver(Enum<?> key, IResolver<T> resolver) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return setResolver(key.name(), resolver);
    }
    
    @Override
    public <T> ConfBuilder setResolver(String key, IResolver<T> resolver) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        if(resolver == null)
            throw new IllegalArgumentException("resolver may not be null");
        set(key, resolver);
        return new ConfBuilder(this, key);
    }
    
    @Override
    public ConfBuilder setStrings(Enum<?> key, String ... values) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return setStrings(key.name(), values);
    }
    
    @Override
    public ConfBuilder setStrings(String key, String ... values) {
        assert values != null;
        String s = encodeList(values);
        set(key, s);
        return new ConfBuilder(this, key);
    }
    
    @Override
    public IConfig setType(Enum<?> key, Class<?> type) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return setType(key.name(), type);
    }
    
    @Override
    public IConfig setType(String key, Class<?> type) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        this.types.put(key, type);
        return this;
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("DEFINED\n");
        List<String> defined = new ArrayList<String>();
        for(String s : getExplicitlyDefinedKeys()) {
            defined.add(s);
        }
        Collections.sort(defined);
        for(String s : defined) {
            b.append(s + "=" + get(s) + "\n");
        }
        return b.toString();
    }
    
    @Override
    public Object tryToGet(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return tryToGet(key.name());
    }
    
    @Override
    public Object tryToGet(String key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        Object value = this.explicit.get(key);
        if(value == null) {
            value = this.defaults.get(key);
        }
        return value;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T tryToGetAs(String key, Class<T> clazz) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        Object o = tryToGet(key);
        if(o == null)
            return null;
        return ((T)o);
    }
    
    @Override
    public <T> T tryToResolve(Class<T> interfaze) {
        try {
            return resolve(interfaze);
        } catch(Exception e) {
            return null;
        }
    }
    
    @Override
    public <T> T tryToResolve(String key) {
        try {
            IResolver<T> resolver = getResolver(key);
            if(resolver == null)
                return null;
            return resolver.resolve();
        } catch(Exception e) {
            return null;
        }
    }
}
