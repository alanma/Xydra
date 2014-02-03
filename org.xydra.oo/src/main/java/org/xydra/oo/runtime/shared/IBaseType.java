package org.xydra.oo.runtime.shared;

public interface IBaseType {
    
    String getCanonicalName();
    
    String getPackageName();
    
    String getSimpleName();
    
    boolean isArray();
    
    String getRequiredImport();
    
}
