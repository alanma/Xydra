package com.sonicmetrics.core.shared.query;

import java.util.Collection;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;


/**
 * Mandatory: subject, category, action, source
 * 
 * @author xamde
 */
public interface ISonicFilter {
    
    /**
     * @return the list of key-value-constraints, can be empty
     */
    @NeverNull
    Collection<KeyValueConstraint> getKeyValueConstraints();
    
    /**
     * @return null = unconstrained
     */
    @CanBeNull
    String getSubject();
    
    /**
     * @return null = unconstrained
     */
    @CanBeNull
    String getCategory();
    
    /**
     * @return null = unconstrained
     */
    @CanBeNull
    String getAction();
    
    /**
     * @return null = unconstrained
     */
    @CanBeNull
    String getLabel();
    
    /**
     * @return null = unconstrained
     */
    @CanBeNull
    String getSource();
    
    @NeverNull
    String getDotString();
    
    // TODO why no uniqueId here?
    
}
