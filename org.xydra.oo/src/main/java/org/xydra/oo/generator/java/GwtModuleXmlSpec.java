package org.xydra.oo.generator.java;

import java.util.ArrayList;
import java.util.List;


public class GwtModuleXmlSpec {
    
    public String moduleName;
    
    public List<GenerateWith> generateWith = new ArrayList<>();
    
    public class GenerateWith {
        
        public Class<?> generateWith;
        
        public String whenTypeAssignable;
        
        public String toString(String indent) {
            StringBuilder b = new StringBuilder();
            
            b.append(indent);
            b.append("<generate-with class=\"");
            b.append(this.generateWith.getCanonicalName());
            b.append("\">\n");
            
            b.append(indent);
            b.append("    <when-type-assignable class=\"");
            b.append(this.whenTypeAssignable);
            b.append("\">\n");
            
            b.append(indent);
            b.append("</generate-with>\n");
            
            return b.toString();
        }
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        
        b.append("<module>\n");
        
        for(GenerateWith g : this.generateWith) {
            b.append(g.toString("    "));
        }
        
        b.append("<!-- Default warning for non-static, final fields enabled -->\n"
                + " <set-property name=\"gwt.suppressNonStaticFinalFieldWarnings\" value=\"false\" />");
        
        b.append("</module>");
        
        return b.toString();
    }
    
}
