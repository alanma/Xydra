package com.sonicmetrics.core.shared.query;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;

import com.sonicmetrics.core.shared.impl.memory.SonicUtils;


/**
 * @author xamde
 * 
 */
@RunsInGWT(true)
public class SonicQuery extends SonicFilter implements ISonicQuery {
    
    private static final long serialVersionUID = 1L;
    
    public static class Builder extends SonicFilter.AbstractBuilder<SonicQuery,Builder> {
        
        private final TimeConstraint timeConstraint;
        /** default is 100 */
        private int limit = 100;
        
        public Builder(TimeConstraint timeConstraint) {
            this.b = this;
            this.timeConstraint = timeConstraint;
        }
        
        public SonicQuery done() {
            KeyValueConstraint[] keyValueConstraints = this.keyValueConstraints
                    .toArray(new KeyValueConstraint[0]);
            
            return new SonicQuery(this.timeConstraint, this.limit, keyValueConstraints);
        }
        
        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }
        
    }
    
    @NeverNull
    private final TimeConstraint timeConstraint;
    
    private int limit;
    
    public SonicQuery(TimeConstraint timeConstraint, int limit,
            KeyValueConstraint ... keyValueConstraints) {
        super(keyValueConstraints);
        this.timeConstraint = timeConstraint;
        this.limit = limit;
    }
    
    public static SonicQuery.Builder build(TimeConstraint timeConstraint) {
        return new SonicQuery.Builder(timeConstraint);
    }
    
    public boolean isTimeConstrained() {
        return this.timeConstraint != null && this.timeConstraint.isConstraining();
    }
    
    @Override
	@NeverNull
    public TimeConstraint getTimeConstraint() {
        return this.timeConstraint;
    }
    
    @Override
    public int getLimit() {
        return this.limit;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.timeConstraint.toString());
        for(KeyValueConstraint c : this.keyValueConstraints.values()) {
            sb.append(" '" + c.getKey() + "'='" + c.getValue() + "'");
        }
        sb.append(" limit:" + this.limit);
        return sb.toString();
    }
    
    public static boolean equals(ISonicQuery a, ISonicQuery b) {
        if(!SonicFilter.equals(a, b))
            return false;
        
        // compare remaining properties
        if(!a.getTimeConstraint().equals(b.getTimeConstraint()))
            return false;
        
        if(a.getLimit() != b.getLimit())
            return false;
        
        return true;
    }
    
    @Override
	public boolean equals(Object other) {
        if(!(other instanceof ISonicQuery))
            return false;
        
        ISonicQuery o = (ISonicQuery)other;
        return equals(this, o);
    }
    
    /**
     * Note: Even if this query is more general and has a higher limit it might
     * still return less events than the other query requested, due to too many
     * events delivered in total.
     * 
     * @param other
     * @return true if this query includes the other query, i.e. this query is
     *         more general (or equal) to the other query and the other query
     *         requests no events that this query won't return.
     */
    public boolean includes(ISonicQuery other) {
        if(!super.includes(other))
            return false;
        
        if(!this.getTimeConstraint().includes(other.getTimeConstraint())) {
            return false;
        }
        
        if(this.getLimit() < other.getLimit()) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return SonicUtils.hashCode(getSubject()) + SonicUtils.hashCode(getSource())
                + SonicUtils.hashCode(getCategory()) + SonicUtils.hashCode(getAction())
                + SonicUtils.hashCode(getLabel());
    }
    
}
