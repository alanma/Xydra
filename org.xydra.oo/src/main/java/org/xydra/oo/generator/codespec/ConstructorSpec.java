package org.xydra.oo.generator.codespec;

public class ConstructorSpec extends AbstractConstructorOrMethodSpec implements IMember {
    
    public ConstructorSpec(ClassSpec classSpec, String generatedFrom) {
        super(classSpec.getName(), generatedFrom);
    }
    
    public String toString() {
        String s = "";
        s += "CONSTRUCTOR\n";
        s += "  name:" + this.getName() + "\n";
        s += "  comment:" + this.comment + "\n";
        for(FieldSpec p : this.params) {
            s += "  PARAM " + p.toString() + "\n";
        }
        for(String l : this.sourceLines) {
            s += "  CODE " + l + "\n";
        }
        return s;
    }
    
}
