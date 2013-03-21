package org.xydra.devtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
        
        scanDir(f, this.project);
        
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
            public boolean shouldBeShown(Package a, Package b) {
                return PackageChaos.this.arch.toBeShown(a, b);
            }
        });
        
        g.pruneEmptySubGraphs();
        
        // addAllEdges(g);
        System.out.println("--- Graph ---");
        g.dump("");
        
        g.writeTo(this.resultFile);
        
        log.info("Written " + g.size() + " edges to DOT file");
    }
    
    private void scanDir(File dir, Project project) throws IOException {
        assert dir.exists();
        assert dir.isDirectory();
        
        File[] files = dir.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || (f.isFile() && f.getName().endsWith(".java"));
            }
        });
        
        for(File f : files) {
            if(f.isDirectory()) {
                scanDir(f, project);
            } else {
                scanFile(f, project);
            }
        }
    }
    
    private static void scanFile(File f, Project project) throws IOException {
        assert f.getName().endsWith(".java");
        // parse java file
        FileInputStream fis = new FileInputStream(f);
        InputStreamReader r = new InputStreamReader(fis, "utf-8");
        BufferedReader br = new BufferedReader(r);
        String line = br.readLine();
        
        Package p = null;
        while(line != null) {
            // process line
            if(line.startsWith("package")) {
                String packageName = extractFromLine("package", line);
                p = project.getOrCreatePackage(packageName);
            } else if(line.startsWith("import")) {
                assert p != null;
                // TODO static imports?
                String importName = extractFromLine("import static", line);
                if(importName == null) {
                    importName = extractFromLine("import", line);
                }
                // strip last part, the class name
                String packageName = getPackageName(importName);
                p.addImport(packageName,
                        f.getName().substring(0, f.getName().length() - ".java".length()));
            }
            line = br.readLine();
        }
        br.close();
        r.close();
        fis.close();
    }
    
    public static String getPackageName(String fullyQualifiedClassName) {
        int i = fullyQualifiedClassName.lastIndexOf('.');
        String s = fullyQualifiedClassName.substring(0, i);
        // might be an inner class
        String c = getClassName(s);
        if(Character.isUpperCase(c.charAt(0))) {
            return getPackageName(s);
        }
        return s;
    }
    
    private static String getClassName(String fullyQualifiedClassName) {
        int i = fullyQualifiedClassName.lastIndexOf('.');
        String c = fullyQualifiedClassName.substring(i + 1);
        return c;
    }
    
    private static String extractFromLine(String keyword, String line) {
        if(!line.startsWith(keyword))
            return null;
        
        String result = line.substring((keyword + " ").length());
        assert result.endsWith(";");
        result = result.substring(0, result.length() - 1);
        return result;
    }
    
    public void setShowCauses(boolean b) {
        this.project.showCauses = b;
    }
    
}
