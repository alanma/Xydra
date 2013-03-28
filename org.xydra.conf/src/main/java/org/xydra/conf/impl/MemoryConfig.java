package org.xydra.conf.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.xydra.annotations.RunsInGWT;
import org.xydra.conf.ConfBuilder;
import org.xydra.conf.ConfigException;
import org.xydra.conf.IConfig;
import org.xydra.core.model.impl.memory.UUID;
import org.xydra.index.impl.MapSetIndex;


@RunsInGWT(true)
public class MemoryConfig implements IConfig {
    
    /**
     * Compile-time flag, should be false for high performance
     */
    private static boolean traceOrigins = true;
    
    public static String[] decodeList(String s) {
        return s.split("[|]");
    }
    
    public static String encodeList(String[] strings) {
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < strings.length; i++) {
            b.append(strings[i]);
            if(i < strings.length)
                b.append("|");
        }
        return b.toString();
    }
    
    private TreeMap<String,String> defaults = new TreeMap<String,String>();
    
    private HashMap<String,String> docs = new HashMap<String,String>();
    
    private TreeMap<String,String> explicit = new TreeMap<String,String>();
    
    private final String internalId = UUID.uuid(4);
    
    private MapSetIndex<String,Class<?>> required = MapSetIndex.createWithFastEntrySets();
    
    private Set<Exception> setOrigins = new HashSet<Exception>();
    
    @Override
    public void addRequiredSetting(String key, Class<?> caller) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        this.required.index(key, caller);
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
    
    public String get(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return get(key.name());
    }
    
    @Override
    public String get(String key) {
        String value = tryToGet(key);
        if(value == null) {
            throw new ConfigException("Setting '" + key
                    + "' requested but not set and no default set either. " + idStr());
        }
        return value;
    }
    
    @Override
    public boolean getBoolean(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return getBoolean(key.name());
    }
    
    @Override
    public boolean getBoolean(String key) {
        String s = get(key);
        return Boolean.parseBoolean(s);
    }
    
    @Override
    public Iterable<String> getDefinedKeys() {
        Set<String> keys = new HashSet<String>();
        keys.addAll(this.explicit.keySet());
        keys.addAll(this.defaults.keySet());
        return keys;
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
        String s = get(key);
        return Long.parseLong(s);
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
    public String[] getStringArray(String key) {
        String s = get(key);
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
    public void revertToDefault(String key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        this.explicit.remove(key);
    }
    
    @Override
    public ConfBuilder set(Enum<?> key, String value) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return set(key.name(), value);
    }
    
    @Override
    public ConfBuilder set(String key, String value) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        this.explicit.put(key, value);
        return new ConfBuilder(this, key);
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
    public ConfBuilder setDefault(String key, String value, boolean initial) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        if(!initial && this.defaults.containsKey(key))
            throw new ConfigException("Setting '" + initial + "' had already a default value "
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
    public ConfBuilder setDefault(Enum<?> key, String value, boolean initial) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return setDefault(key.name(), value, initial);
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
    public String tryToGet(String key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        String value = this.explicit.get(key);
        if(value == null) {
            value = this.defaults.get(key);
        }
        return value;
    }
    
    @Override
    public void addRequiredSetting(Enum<?> key, Class<?> caller) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        addRequiredSetting(key.name(), caller);
    }
    
    @Override
    public String getDocumentation(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return getDocumentation(key.name());
    }
    
    @Override
    public void revertToDefault(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        revertToDefault(key.name());
    }
    
    @Override
    public String tryToGet(Enum<?> key) {
        if(key == null)
            throw new IllegalArgumentException("Key may not be null");
        return tryToGet(key.name());
    }
    
}
