package org.xydra.oo.generator.codespec;

import java.util.Set;


public interface IMember extends Comparable<IMember> {
    
    String getName();
    
    String getComment();
    
    void dump();
    
    Set<String> getRequiredImports();
    
    String getGeneratedFrom();
    
}
