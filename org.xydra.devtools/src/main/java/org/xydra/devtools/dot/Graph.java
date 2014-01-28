package org.xydra.devtools.dot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Ready to be rendered as a .DOT file
 * 
 * @author xamde
 */
public class Graph implements Comparable<Graph> {
    
    public static interface ILabelRenderer {
        String render(String s);
    }
    
    private ILabelRenderer labelRenderer = new ILabelRenderer() {
        
        @Override
        public String render(String s) {
            return s;
        }
    };
    
    public void setLabelRenderer(ILabelRenderer labelRenderer) {
        this.labelRenderer = labelRenderer;
    }
    
    private String name;
    
    private Set<Edge> edges = new HashSet<Edge>();
    
    private SortedSet<Graph> subgraphs = new TreeSet<Graph>();
    
    private Set<String> nodes = new HashSet<String>();
    
    public Graph(String name) {
        this.name = name;
    }
    
    public void addNode(String n) {
        this.nodes.add(n);
    }
    
    /**
     * @param a
     * @param b
     * @param weight
     * @param edgeLabel must be either '"foo"' or '&lt;foo&gt;'
     */
    public void addEdge(String a, String b, int weight, String edgeLabel) {
        Edge e = new Edge(a, b, weight, edgeLabel);
        this.edges.add(e);
    }
    
    public Graph createSubgraph(String name) {
        Graph g = new Graph(name);
        g.setLabelRenderer(this.labelRenderer);
        this.subgraphs.add(g);
        return g;
    }
    
    public boolean concentrateEdges = false;
    
    private void writeAsMainGraph(Writer w) throws IOException {
        w.write("digraph " + getLabel() + " {\n");
        writeGraphAttribute(w, "size", "10,10");
        // concentrate edges
        writeGraphAttribute(w, "concentrate", "" + this.concentrateEdges);
        // portrait or landscape
        // writeGraphAttribute(w, "orientation", "landscape");
        writeGraphAttribute(w, "remincross", "true");
        writeGraphAttribute(w, "searchsize", "100");
        writeContent(w);
        w.write("}");
    }
    
    private static void writeGraphAttribute(Writer w, String key, String value) throws IOException {
        w.write(key + "=\"" + value + "\"\n");
    }
    
    private void writeContent(Writer w) throws IOException {
        // nodes
        if(this.nodes.size() > 0) {
            for(String n : this.nodes) {
                w.write(this.labelRenderer.render(n) + ";\n");
            }
        }
        
        // edges
        for(Edge e : this.edges) {
            e.write(w, this.labelRenderer);
        }
        
        // subgraphs
        for(Graph subgraph : this.subgraphs) {
            if(subgraph.size() > 0)
                subgraph.writeAsSubGraph(w);
        }
    }
    
    private void writeAsSubGraph(Writer w) throws IOException {
        w.write("subgraph " + getLabel() + " { \n" + "label=" + getLabel() + "\n");
        
        // writeGraphAttribute(w, "rankdir", "TB");
        writeContent(w);
        w.write("}\n");
    }
    
    private String getLabel() {
        if(this.name.equals(""))
            return "ROOT";
        return "cluster_" + this.name.replace('.', '_');
    }
    
    public int size() {
        int i = this.edges.size();
        for(Graph g : this.subgraphs) {
            i += g.size();
        }
        return i;
    }
    
    public void writeTo(File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        Writer w = new OutputStreamWriter(fos, "utf-8");
        writeAsMainGraph(w);
        w.close();
        fos.close();
    }
    
    public int hashCode() {
        return getLabel().hashCode();
    }
    
    public boolean equals(Object o) {
        if(o instanceof Graph) {
            Graph oGraph = (Graph)o;
            return oGraph.getLabel().equals(this.getLabel());
        }
        return false;
    }
    
    public void dump(String indent) {
        System.out.println(indent + this.getLabel());
        for(Edge e : this.edges) {
            System.out.println(indent + e.getSource() + " -> " + e.getTarget() + " causes: "
                    + e.getEdgeLabel());
        }
        for(Graph sub : this.subgraphs) {
            sub.dump(indent + "  ");
        }
    }
    
    @Override
    public int compareTo(Graph o) {
        return this.getLabel().compareTo(o.getLabel());
    }
    
    public String toString() {
        return this.getLabel() + " (" + this.size() + ")";
    }
    
    public void pruneEmptySubGraphs() {
        // prune children
        if(this.subgraphs.size() > 0) {
            List<Graph> toBeDeleted = new LinkedList<Graph>();
            for(Graph sub : this.subgraphs) {
                sub.pruneEmptySubGraphs();
                if(sub.isEmpty()) {
                    toBeDeleted.add(sub);
                }
            }
            for(Graph sub : toBeDeleted) {
                this.subgraphs.remove(sub);
            }
        }
    }
    
    private boolean isEmpty() {
        return this.subgraphs.isEmpty() && this.edges.isEmpty() && this.nodes.isEmpty();
    }
    
}
