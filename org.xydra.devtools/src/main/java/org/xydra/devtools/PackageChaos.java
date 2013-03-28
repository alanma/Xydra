package org.xydra.devtools;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.xydra.devtools.architecture.Architecture;
import org.xydra.devtools.dot.Graph;
import org.xydra.devtools.dot.Graph.ILabelRenderer;
import org.xydra.devtools.java.IDependencyFilter;
import org.xydra.devtools.java.Package;
import org.xydra.devtools.java.Project;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * See {@link XydraArchitecture} for an example.
 * 
 * For GraphViz docu see
 * 
 * Grammar: http://www.graphviz.org/content/dot-language
 * 
 * Reference: http://www.graphviz.org/Documentation/dotguide.pdf
 * 
 * 
 * @author xamde
 * 
 */
public class PackageChaos {
    
    private static final Logger log = LoggerFactory.getLogger(PackageChaos.class);
    private Architecture arch;
    private File resultFile;
    private Project project;
    
    public PackageChaos(Architecture arch, File result) {
        this.arch = arch;
        this.resultFile = result;
        this.project = new Project();
    }
    
    public void analyse(String path) throws IOException {
        File f = new File(path);
        analyse(f);
    }
    
    public void analyse(File f) throws IOException {
        assert f.exists();
        assert f.isDirectory();
        this.project.scanDir(f);
        renderDotGraph();
    }
    
    public Project getProject() {
        return this.project;
    }
    
    public void renderDotGraph() throws IOException {
        // for(Package p : this.allPackages.values()) {
        // System.out.println("Indexed " + p.getName());
        // }
        log.info("Index has " + this.project.packageCount() + " packages");
        
        Graph g = new Graph("main");
        g.setLabelRenderer(new ILabelRenderer() {
            
            @Override
            public String render(String s) {
                String p = s;
                for(String scope : PackageChaos.this.arch.getScopes()) {
                    if(s.startsWith(scope)) {
                        p = s.substring((scope + ".").length());
                        break;
                    }
                }
                return "<" + p.replace(".", ".\n") + ">";
            }
        });
        
        // pre-process
        this.project.addLinksToChildPackages();
        
        System.out.println("--- Packages ---");
        this.project.dump();
        
        // --- Graph ---
        this.project.addPackagesAsSubgraphs(g, this.project, new IDependencyFilter() {
            
            @Override
            public boolean shouldBeShown(Package a, Package b, Set<String> causes) {
                return PackageChaos.this.arch.toBeShown(a, b, causes);
            }
        });
        
        g.pruneEmptySubGraphs();
        
        // addAllEdges(g);
        System.out.println("--- Graph ---");
        g.dump("");
        
        g.writeTo(this.resultFile);
        
        log.info("Written " + g.size() + " edges to DOT file");
    }
    
    public void setShowCauses(boolean b) {
        this.project.showCauses = b;
    }
    
}
