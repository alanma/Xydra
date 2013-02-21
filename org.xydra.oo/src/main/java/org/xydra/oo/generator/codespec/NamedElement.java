package org.xydra.oo.generator.codespec;

import org.xydra.annotations.NeverNull;


public class NamedElement {
    
    public NamedElement(String name) {
        this.name = name;
    }
    
    @NeverNull
    private String name;
    
    public String getName() {
        return this.name;
    }
    
}
