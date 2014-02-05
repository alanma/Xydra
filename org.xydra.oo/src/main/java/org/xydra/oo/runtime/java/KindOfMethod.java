package org.xydra.oo.runtime.java;

public enum KindOfMethod {
    
    Get("get"), Set("set"), Is("is"), GetCollection(null);
    
    String prefix;
    
    /**
     * @param prefix
     */
    private KindOfMethod(String prefix) {
        this.prefix = prefix;
    }
}
