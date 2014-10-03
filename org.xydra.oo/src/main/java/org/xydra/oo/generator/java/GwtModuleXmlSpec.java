package org.xydra.oo.generator.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xydra.annotations.CanBeNull;


public class GwtModuleXmlSpec {
    
    public GwtModuleXmlSpec(String packageName, String moduleName, String rename_to) {
        this.packageName = packageName;
        this.moduleName = moduleName;
    }
    
    @CanBeNull
    private String rename_to;
    
    private String packageName;
    
    public String moduleName;
    
    public Set<GenerateWith> generateWith = new HashSet<GenerateWith>();
    
    public List<String> inherits = new ArrayList<String>();
    
    public class GenerateWith {
        
        @SuppressWarnings("hiding")
		public Class<?> generateWith;
        
        public String whenTypeAssignable;
        
        @Override
		public int hashCode() {
            return this.whenTypeAssignable.hashCode();
        }
        
        @Override
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
    
    @Override
	public String toString() {
        StringBuilder b = new StringBuilder();
        
        b.append("<module");
        if(this.rename_to != null) {
            b.append(" rename-to=\"" + this.rename_to + "\"");
        }
        b.append(">\n");
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
