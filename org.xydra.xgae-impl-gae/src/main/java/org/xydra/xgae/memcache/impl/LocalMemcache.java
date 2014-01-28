package org.xydra.xgae.memcache.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.xgae.memcache.api.IMemCache;


/**
 * To fix an issue with the Gae local test implementation (takes more and more
 * memory), this implementation can be restricted in memory use. This
 * implementation keeps 5 MB of memory free.
 * 
 * @author xamde
 * 
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class LocalMemcache implements IMemCache {
    
    private static final Logger log = LoggerFactory.getLogger(LocalMemcache.class);
    
    private static final byte[] NULL_VALUE = "null_value".getBytes();
    
    @Override
    public int size() {
        return this.internalMap.size();
    }
    
    @Override
    public boolean isEmpty() {
        return this.internalMap.isEmpty();
    }
    
    @Override
    public boolean containsKey(Object key) {
        return this.internalMap.containsKey(key);
    }
    
    @Override
    public boolean containsValue(Object value) {
        return this.internalMap.containsValue(valueToStored(value));
    }
    
    @Override
    public Object get(Object key) {
        byte[] bytes = this.internalMap.get(key);
        if(bytes == null) {
            return null;
        }
        Object result = storedToValue(bytes);
        return result;
    }
    
    @Override
    public Object put(String key, Object value) {
        controlCacheSize();
        // transform null value & clone value
        byte[] oldValue = this.internalMap.put(key, valueToStored(value));
        if(oldValue == null) {
            return null;
        }
        return storedToValue(oldValue);
    }
    
    private static final Object storedToValue(byte[] stored) {
        XyAssert.xyAssert(stored != null);
        assert stored != null;
        if(stored == NULL_VALUE) {
            return null;
        }
        // else
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(stored);
            ObjectInputStream oin = new ObjectInputStream(bis);
            Object result = oin.readObject();
            oin.close();
            return result;
        } catch(IOException e) {
            throw new RuntimeException(e);
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static final byte[] valueToStored(Object value) {
        if(value == null) {
            return NULL_VALUE;
        }
        // else
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.close();
            byte[] bytes = bos.toByteArray();
            return bytes;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static final long RESERVE_MEMORY = 5 * 1024 * 1024;
    
    private void controlCacheSize() {
        long free = Runtime.getRuntime().freeMemory();
        if(free < RESERVE_MEMORY) {
            log.warn("Free memory = " + free + ", require " + RESERVE_MEMORY + " -> Auto-clear.");
            this.clear();
            System.gc();
        }
    }
    
    @Override
    public Object remove(Object key) {
        return this.internalMap.remove(key);
    }
    
    @Override
    public void putAll(Map<? extends String,? extends Object> m) {
        // transform values implicitly
        for(Map.Entry<? extends String,? extends Object> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public void clear() {
        this.internalMap.clear();
    }
    
    @Override
    public Set<String> keySet() {
        return this.internalMap.keySet();
    }
    
    @Override
    public Collection<Object> values() {
        // transform null values
        List<Object> result = new LinkedList<Object>();
        for(byte[] o : this.internalMap.values()) {
            result.add(storedToValue(o));
        }
        return result;
    }
    
    @Override
    public Set<java.util.Map.Entry<String,Object>> entrySet() {
        // transform null values
        Map<String,Object> result = new HashMap<String,Object>();
        for(Map.Entry<String,byte[]> e : this.internalMap.entrySet()) {
            result.put(e.getKey(), storedToValue(e.getValue()));
        }
        return result.entrySet();
    }
    
    @Override
    public boolean equals(Object o) {
        return this.internalMap.equals(o);
    }
    
    @Override
    public int hashCode() {
        return this.internalMap.hashCode();
    }
    
    private ConcurrentHashMap<String,byte[]> internalMap;
    
    public LocalMemcache() {
        log.info("Using LocalMemcache");
        this.internalMap = new ConcurrentHashMap<String,byte[]>();
    }
    
    @Override
    public String stats() {
        return "In-memory, items: " + size();
    }
    
    @Override
    public Map<String,Object> getAll(Collection<String> keys) {
        Map<String,Object> result = new HashMap<String,Object>();
        for(String key : keys) {
            Object value = this.get(key);
            if(value != null) {
                result.put(key, value);
            }
        }
        return result;
    }
    
    @Override
    public void putIfValueIsNull(String key, Object value) {
        this.internalMap.putIfAbsent(key, valueToStored(value));
    }
    
    public static class IdentifiableImpl implements IdentifiableValue {
        
        private Object o;
        
        public IdentifiableImpl(Object o) {
            this.o = o;
        }
        
        @Override
        public Object getValue() {
            return this.o;
        }
        
    }
    
    @Override
    public boolean putIfUntouched(@NeverNull String key, @NeverNull IdentifiableValue oldValue,
            @CanBeNull Object newValue) {
        synchronized(this.internalMap) {
            Object current = get(key);
            if(oldValue.getValue() == null) {
                if(current == null) {
                    put(key, newValue);
                    return true;
                } else {
                    return false;
                }
            } else {
                if(current.equals(oldValue.getValue())) {
                    put(key, newValue);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
    
    @Override
    public IdentifiableValue getIdentifiable(String key) {
        Object o = get(key);
        return new IdentifiableImpl(o);
    }
    
    @Override
    public Map<String,Long> incrementAll(Map<String,Long> offsets, long initialValue) {
        Map<String,Long> result = new HashMap<String,Long>();
        synchronized(this.internalMap) {
            for(java.util.Map.Entry<String,Long> entry : offsets.entrySet()) {
                String key = entry.getKey();
                byte[] valueBytes = this.internalMap.get(key);
                long newValue;
                if(valueBytes == null) {
                    newValue = initialValue;
                } else {
                    Object value = storedToValue(valueBytes);
                    XyAssert.xyAssert(value instanceof Long);
                    long oldValue = (Long)value;
                    newValue = oldValue + entry.getValue();
                }
                result.put(key, newValue);
                valueBytes = valueToStored(newValue);
                this.internalMap.put(key, valueBytes);
            }
        }
        return result;
    }
    
    @Override
    public Object putChecked(String key, Object value) throws IOException {
        return put(key, value);
    }
    
}
