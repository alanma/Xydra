package org.xydra.devtools.javapackages;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.xydra.devtools.XydraArchitecture;
import org.xydra.devtools.dot.Graph;
import org.xydra.devtools.dot.Graph.ILabelRenderer;
import org.xydra.devtools.javapackages.architecture.Architecture;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


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
    
    public void analyseAndRender(String path) throws IOException {
        readImportsFromDir(path);
        renderDotGraph();
    }
    
    public void readImportsFromDir(String path) throws IOException {
        File f = new File(path);
        assert f.exists();
        assert f.isDirectory();
        this.project.scanDir(f);
    }
    
    public Project getProject() {
        return this.project;
    }
    
    public void renderDotGraph() throws IOException {
        // for(Package p : this.allPackages.values()) {
        // System.out.println("Indexed " + p.getName());
        // }
        log.info("Index has " + this.project.packageCount() + " packages");
        
        Graph graph = new Graph("main");
        graph.setLabelRenderer(new ILabelRenderer() {
            
            @Override
            public String render(String s) {
                String p = s;
                for(String scope : PackageChaos.this.arch.getScopes()) {
                    if(scope.length() > 0 && s.startsWith(scope)) {
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
        this.project.addPackagesAsSubgraphs(graph, this.project, new IDependencyFilter() {
            
            @Override
            public boolean shouldBeShown(Package a, Package b, Set<String> causes) {
                return PackageChaos.this.arch.toBeShown(a, b, causes);
            }
        });
        
        graph.pruneEmptySubGraphs();
        
        // addAllEdges(g);
        System.out.println("--- Graph ---");
        graph.dump("");
        
        File temp = new File(this.resultFile.getAbsoluteFile() + ".temp");
        graph.writeTo(temp);
        temp.renameTo(this.resultFile);
        
        log.info("Written " + graph.size() + " edges to DOT file");
    }
    
    public void setShowCauses(boolean b) {
        this.project.showCauses = b;
    }
    
}
