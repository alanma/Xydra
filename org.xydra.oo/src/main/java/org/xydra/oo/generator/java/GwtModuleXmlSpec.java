package org.xydra.oo.generator.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GwtModuleXmlSpec {
    
    public GwtModuleXmlSpec(String packageName, String moduleName) {
        this.packageName = packageName;
        this.moduleName = moduleName;
    }
    
    private String packageName;
    
    public String moduleName;
    
    public Set<GenerateWith> generateWith = new HashSet<>();
    
    public List<String> inherits = new ArrayList<>();
    
    public class GenerateWith {
        
        public Class<?> generateWith;
        
        public String whenTypeAssignable;
        
        public int hashCode() {
            return this.whenTypeAssignable.hashCode();
        }
        
        public boolean equals(Object o) {
            return o instanceof GenerateWith
                    && ((GenerateWith)o).whenTypeAssignable.endsWith(this.whenTypeAssignable);
        }
        
        public String toString(String indent) {
            StringBuilder b = new StringBuilder();
            
            b.append(indent);
            b.append("<generate-with class=\"");
            b.append(this.generateWith.getCanonicalName());
            b.append("\">\n");
            
            b.append(indent);
            b.append("    <when-type-assignable class=\"");
            b.append(this.whenTypeAssignable);
            b.append("\" />\n");
            
            b.append(indent);
            b.append("</generate-with>\n");
            
            return b.toString();
        }
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        
        b.append("<module>\n");
        b.append("    <!-- Inherit this module as " + this.packageName + "." + this.moduleName
                + " -->\n");
        b.append("\n");
        for(GenerateWith g : this.generateWith) {
            b.append(g.toString("    "));
        }
        b.append("\n");
        for(String inherit : this.inherits) {
            b.append("    <inherits name=\"" + inherit + "\" />\n");
        }
        b.append("\n");
        b.append("    <source path=\"client\" />\n");
        b.append("    <source path=\"shared\" />\n");
        // b.append("\n");
        // b.append("<!-- Default warning for non-static, final fields enabled -->\n"
        // +
        // " <set-property name=\"gwt.suppressNonStaticFinalFieldWarnings\" value=\"false\" />");
        //
        b.append("</module>");
        
        return b.toString();
    }
}
