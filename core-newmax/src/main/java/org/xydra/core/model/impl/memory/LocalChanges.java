package org.xydra.core.model.impl.memory;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;


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
    
    public void append(XCommand command, XEvent event) {
        LocalChange lc = new LocalChange(command, event);
        this.list.add(lc);
    }
    
    public static LocalChanges create() {
        return new LocalChanges(new ArrayList<LocalChange>());
    }
    
    public int countUnappliedLocalChanges() {
        return this.list.size();
    }
    
}
