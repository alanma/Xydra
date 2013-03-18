package org.xydra.devtools.dot;

import java.io.IOException;
import java.io.Writer;

import org.xydra.devtools.dot.Graph.ILabelRenderer;


public class Edge {
    
    public Edge(String a, String b, int weight, String edgeLabel) {
        super();
        this.a = a;
        this.b = b;
        this.weight = weight;
        this.edgeLabel = edgeLabel;
    }
    
    private String a;
    private String b;
    private int weight;
    private String edgeLabel;
    
    public void write(Writer w, ILabelRenderer labelRenderer) throws IOException {
        String labelA = labelRenderer.render(this.a);
        String labelB = labelRenderer.render(this.b);
        
        if(labelA.equals("")) {
            labelA = "ROOT";
        }
        if(labelB.equals("")) {
            labelB = "ROOT";
        }
        
        w.write(labelA
                + " -> "
                + labelB
                +
                
                (this.weight > 1 ? " [weight=" + this.weight + "]" : "")
                
                + (this.edgeLabel == null ? "" : " [label="
                        + addLineBreaksForCamelCase(this.edgeLabel) + "]")
                
                // + "[lhead=cluster_" +
                // Project.getParentPackageName(this.b).replace(".", "_") + "]"
                
                + ";\n"
        
        );
    }
    
    private static String addLineBreaksForCamelCase(String s) {
        int len = 0;
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(Character.isUpperCase(c)) {
                if(len > 2) {
                    // add linebreak
                    b.append("<br/>");
                    len = 0;
                }
            }
            b.append(c);
            len++;
        }
        return b.toString();
    }
    
    public int hashCode() {
        return this.a.hashCode() + this.b.hashCode();
    }
    
    public boolean equals(Object o) {
        if(o instanceof Edge) {
            Edge oEdge = (Edge)o;
            return oEdge.a.equals(this.a) && oEdge.b.equals(this.b);
        }
        return false;
    }
    
    public String getSource() {
        return this.a;
    }
    
    public String getTarget() {
        return this.b;
    }
    
    public String toString() {
        return this.a + " -> " + this.b;
    }
    
    public String getEdgeLabel() {
        return this.edgeLabel;
    }
}
