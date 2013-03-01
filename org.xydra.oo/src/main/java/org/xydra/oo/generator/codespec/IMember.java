package org.xydra.oo.generator.codespec;

import java.util.Set;


public interface IMember extends Comparable<IMember> {
    
    /**
     * @return name of member
     */
    String getName();
    
    /**
     * @return JavaDoc comment
     */
    String getComment();
    
    /**
     * For debugging
     */
    void dump();
    
    /**
     * @return fully qualified class names
     */
    Set<String> getRequiredImports();
    
    /**
     * @return debug info
     */
    String getGeneratedFrom();
    
}
