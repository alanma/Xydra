package org.xydra.oo.generator.codespec.impl;

import org.xydra.annotations.NeverNull;


public class NamedElement {
    
    @NeverNull
    private String name;
    
    NamedElement(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
}
