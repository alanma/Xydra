package org.xydra.base.id;

import java.io.Serializable;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.value.ValueType;


/**
 * An implementation of {@link XId} where an ID is represented by a String
 * value.
 * 
 * @author xamde
 */

@RunsInGWT(true)
@RequiresAppEngine(false)
public class MemoryStringID implements XId, Serializable {
    
    private static final long serialVersionUID = 3397013331330118533L;
    
    /** Impl Note: field is not final to allow GWT-Serialisation to work on it */
    protected String string;
    
    /** Required for GWT. Do not use. */
    public MemoryStringID() {
    }
    
    /**
     * No syntax checks are performed.
     * 
     * @param uriString
     */
    protected MemoryStringID(String uriString) {
        this.string = uriString.intern();
    }
    
    @Override
    public int compareTo(XId o) {
        return this.toString().compareTo(o.toString());
    }
    
    @Override
    public boolean equals(Object other) {
        if(other instanceof MemoryStringID) {
            MemoryStringID otherMemoryStringID = (MemoryStringID)other;
            if(otherMemoryStringID.string == this.string)
                return true;
            return otherMemoryStringID.string.equals(this.string);
        } else if(other instanceof XId) {
            return ((XId)other).toString().equals(this.string);
        } else {
            return false;
        }
    }
    
    @Override
    public XId getId() {
        return this;
    }
    
    @Override
    public ValueType getType() {
        return ValueType.Id;
    }
    
    @Override
    public XId getValue() {
        return this;
    }
    
    @Override
    public int hashCode() {
        return this.string.hashCode();
    }
    
    @Override
    public String toString() {
        return this.string;
    }
    
}
