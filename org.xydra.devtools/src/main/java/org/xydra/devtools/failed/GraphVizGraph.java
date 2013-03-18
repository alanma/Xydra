package org.xydra.devtools.failed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class GraphVizGraph {
    
    private OutputStreamWriter w;
    private FileOutputStream fos;
    private int edges = 0;
    
    public GraphVizGraph(String name, File f) throws FileNotFoundException, IOException {
        this.fos = new FileOutputStream(f);
        this.w = new OutputStreamWriter(this.fos, "utf-8");
        
        this.w.write("digraph " + name + " {\n");
        this.w.write("size=\"" + "10,10" + "\"\n");
    }
    
    public void close() throws IOException {
        this.w.write("}");
        this.w.close();
        this.fos.close();
    }
    
    public void writeEdge(String a, String b, int weight, String groupId) throws IOException {
        String labelA = a;
        String labelB = b;
        
        if(labelA.startsWith("org.xydra")) {
            labelA = labelA.substring("org.xydra.".length());
        }
        if(labelB.startsWith("org.xydra")) {
            labelB = labelB.substring("org.xydra.".length());
        }
        
        this.w.write("\"" + labelA + "\"" + " -> " + "\"" + labelB + "\" [weight=" + weight + "]"
                + (groupId == null ? "" :
                
                "[group=" + groupId.replace(".", "_") + "]"
                
                ));
        this.edges++;
    }
    
    public int size() {
        return this.edges;
    }
    
}
