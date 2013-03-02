package org.xydra.conf.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.xydra.annotations.RunsInGWT;
import org.xydra.conf.IConfig;
import org.xydra.index.impl.MapSetIndex;


@RunsInGWT(true)
public class MemoryConfig implements IConfig {
    
    private HashMap<String,String> docs = new HashMap<String,String>();
    
    private TreeMap<String,String> defaults = new TreeMap<String,String>();
    
    private TreeMap<String,String> explicit = new TreeMap<String,String>();
    
    private MapSetIndex<String,Class<?>> required = MapSetIndex.createWithFastEntrySets();
    
    @Override
    public String getString(String key) {
        String value = this.explicit.get(key);
        if(value == null) {
            value = this.defaults.get(key);
        }
        return value;
    }
    
    @Override
    public Iterable<String> getAvailableKeys() {
        Set<String> keys = new HashSet<String>();
        keys.addAll(this.explicit.keySet());
        keys.addAll(this.defaults.keySet());
        return keys;
    }
    
    @Override
    public Iterable<String> getExplicitlyDefinedKeys() {
        return this.explicit.keySet();
    }
    
    @Override
    public void setString(String key, String value) {
        this.explicit.put(key, value);
    }
    
    @Override
    public void setDefaultString(String key, String value) {
        this.defaults.put(key, value);
    }
    
    @Override
    public void revertToDefault(String key) {
        this.explicit.remove(key);
    }
    
    @Override
    public void addRequiredSetting(String key, Class<?> caller) {
        this.required.index(key, caller);
    }
    
    @Override
    public Set<String> getRequiredKeys() {
        return this.required.keySet();
    }
    
    @Override
    public Set<String> getMissingRequiredKeys() {
        Set<String> open = new HashSet<String>(this.required.keySet());
        open.removeAll(this.explicit.keySet());
        open.removeAll(this.defaults.keySet());
        return open;
    }
    
    @Override
    public boolean isComplete() {
        return getMissingRequiredKeys().isEmpty();
    }
    
    @Override
    public void setTheDoc(String key, String documentation) {
        this.docs.put(key, documentation);
    }
    
    @Override
    public void setLong(String key, long l) {
        setString(key, "" + l);
    }
    
    @Override
    public void setBoolean(String key, boolean b) {
        setString(key, "" + b);
    }
    
    @Override
    public Long getLong(String key) {
        String s = getString(key);
        if(s == null)
            return null;
        return Long.parseLong(s);
    }
    
    @Override
    public Boolean getBoolean(String key) {
        String s = getString(key);
        if(s == null)
            return null;
        return Boolean.parseBoolean(s);
    }
    
    @Override
    public String getDocumentation(String key) {
        return this.docs.get(key);
    }
}
