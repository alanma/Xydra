package org.xydra.core.model.impl.memory;

import java.util.List;


/**
 * TODO maybe merge with XChangeLog
 * 
 * @author xamde
 */
public class LocalChanges {
    
    private List<LocalChange> list;
    private boolean locked;
    
    public LocalChanges(List<LocalChange> list) {
        this.list = list;
    }
    
    public void lock() {
        this.locked = true;
    }
    
    public void unlock() {
        this.locked = false;
    }
    
    public boolean isLocked() {
        return this.locked;
    }
    
    public List<LocalChange> getList() {
        return this.list;
    }
    
    public void clear() {
        this.list.clear();
    }
    
}
