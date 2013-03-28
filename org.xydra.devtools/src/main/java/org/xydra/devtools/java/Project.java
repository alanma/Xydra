package org.xydra.devtools.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.devtools.dot.Graph;
import org.xydra.devtools.java.Package.Dependency;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Contains collected information about packages and their dependencies.
 * 
 * Can be rendered into a DOT graph.
 * 
 * @author xamde
 */
public class Project {
    
    private static final Logger log = LoggerFactory.getLogger(Project.class);
    
    private Map<String,Package> allPackages = new HashMap<String,Package>();
    
    private Map<Package,Graph> graphs = new HashMap<Package,Graph>();
    
    public boolean showCauses;
    
    @NeverNull
    public static String getParentPackageName(String packageName) {
        int i = packageName.lastIndexOf('.');
        if(i <= 0)
            return "";
        String s = packageName.substring(0, i);
        return s;
    }
    
    public Package getOrCreatePackage(String packageName) {
        Package p = this.allPackages.get(packageName);
        if(p == null) {
            p = new Package(this, packageName);
            this.allPackages.put(packageName, p);
        }
        return p;
    }
    
    public void addPackagesAsSubgraphs(Graph g, Project project, IDependencyFilter filter) {
        addSubPackagesAsSubGraphs(project.getRootPackage(), g);
        addEdges(project.getRootPackage(), this.graphs, filter);
    }
    
    private void addSubPackagesAsSubGraphs(Package p, Graph g) {
        this.graphs.put(p, g);
        for(Package child : p.children) {
            Graph subgraph = g.createSubgraph(child.getName());
            addSubPackagesAsSubGraphs(child, subgraph);
        }
    }
    
    private void addEdges(Package p, Map<Package,Graph> graphs, IDependencyFilter filter) {
        addDependenciesAsEdge(p, graphs, filter);
        for(Package child : p.children) {
            // avoid creating a box for single ellipses
            addEdges(child, graphs, filter);
        }
    }
    
    private void addDependenciesAsEdge(Package p, Map<Package,Graph> graphs,
            IDependencyFilter filter) {
        for(Dependency d : p.getDependesOn()) {
            assert d != null;
            Package dP = d.getPackage();
            if(filter.shouldBeShown(p, dP, d.getCauses())) {
                String commonPackagePrefix = getCommonParentPackage(p.getName(), dP.getName());
                
                log.info("+ Adding in '" + commonPackagePrefix + "' edge " + p.getName() + " -> "
                        + dP.getName());
                
                /* "If an edge belongs to a cluster, its endpoints belong to that cluster." */
                Graph source = graphs.get(p);
                assert source != null;
                source.addNode(p.getName());
                
                Graph target = graphs.get(dP);
                assert target != null;
                target.addEdge(p.getName(), dP.getName(), 1,
                        this.showCauses ? toLabel(d.getCauses()) : null
                
                );
                
                //
                // Graph target = graphs.get(d);
                // assert target != null : d.getName();
                // target.addNode(d.getName());
                //
                // Package commonPackage =
                // this.allPackages.get(commonPackagePrefix);
                // assert commonPackage != null : commonPackagePrefix;
                // Graph commonGraph = graphs.get(commonPackage);
                // commonGraph.addEdge(p.getName(), d.getName(), 1, null);
            } else {
                log.info("- Hiding edge '" + p.getName() + "' -> '" + d.getPackage().getName()
                        + "'");
            }
        }
    }
    
    private static String toLabel(Set<String> causes) {
        StringBuilder b = new StringBuilder();
        List<String> list = new ArrayList<String>(causes);
        for(int i = 0; i < list.size(); i++) {
            b.append(list.get(i));
            if(i + 1 < list.size())
                b.append(",\n");
        }
        return "<" + b.toString() + ">";
    }
    
    private static String getCommonParentPackage(String a, String b) {
        int max = Math.min(a.length(), b.length());
        int lastDot = -1;
        for(int i = 0; i < max; i++) {
            if(a.charAt(i) == b.charAt(i)) {
                // continue
                if(a.charAt(i) == '.')
                    lastDot = i;
            } else {
                if(i == 0) {
                    return "";
                } else {
                    String common = a.substring(0, i);
                    
                    if(lastDot <= 0)
                        return "";
                    
                    return common.substring(0, lastDot);
                }
            }
        }
        
        if(a.length() < b.length())
            return a;
        return b;
    }
    
    @SuppressWarnings("unused")
    private void addAllEdges(Graph g, IDependencyFilter filter) {
        List<String> packageNames = new ArrayList<String>(this.allPackages.keySet());
        Collections.sort(packageNames);
        int hiddenEdges = 0;
        for(String pn : packageNames) {
            log.debug("Package " + pn + ":");
            Package pA = getOrCreatePackage(pn);
            for(Dependency d : pA.dependesOn) {
                Package pB = d.getPackage();
                log.debug(pn + " dependsOn " + pB.getName());
                
                if(filter.shouldBeShown(pA, pB, d.getCauses())) {
                    int weight = 1;
                    String a = pn;
                    String b = pB.getName();
                    
                    String groupId = null;
                    if(a.startsWith(b)) {
                        groupId = b;
                        weight = 10 - pB.depth();
                    }
                    if(b.startsWith(a)) {
                        groupId = a;
                        weight = 10 - pA.depth();
                    }
                    
                    g.addEdge(a, b, weight, groupId);
                } else {
                    hiddenEdges++;
                }
            }
        }
        log.info(hiddenEdges + " edges hidden");
    }
    
    public void addLinksToChildPackages() {
        Set<String> completePackageNames = new HashSet<String>();
        completePackageNames.add("");
        for(Package p : this.allPackages.values()) {
            String name = p.getName();
            while(!name.equals("")) {
                completePackageNames.add(name);
                name = getParentPackageName(name);
            }
        }
        
        for(String s : completePackageNames) {
            if(!this.allPackages.containsKey(s)) {
                Package p = new Package(this, s);
                this.allPackages.put(s, p);
            }
        }
        
        for(Package p : this.allPackages.values()) {
            if(!p.isRoot()) {
                Package parent = this.allPackages.get(p.getParentPackageName());
                assert parent != null : "no parent found for '" + p.getName() + "'";
                parent.children.add(p);
            }
        }
    }
    
    public Package getRootPackage() {
        Package root = this.allPackages.get("");
        assert root != null;
        return root;
    }
    
    public void dump() {
        Package root = getRootPackage();
        dump("", root);
    }
    
    private void dump(String indent, Package p) {
        System.out.println(indent + p.getName());
        for(Dependency d : p.dependesOn) {
            for(String cause : d.getCauses()) {
                System.out.println(indent + "-> " + d.getPackage().getName() + " cause:" + cause);
            }
        }
        for(Package child : p.children) {
            dump(indent + "  ", child);
        }
    }
    
    public int packageCount() {
        return this.allPackages.size();
    }
    
    public void scanDir(File dir) throws IOException {
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
                scanDir(f);
            } else {
                scanFile(f);
            }
        }
    }
    
    private void scanFile(File f) throws IOException {
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
                p = this.getOrCreatePackage(packageName);
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
    
    private static String extractFromLine(String keyword, String line) {
        if(!line.startsWith(keyword))
            return null;
        
        String result = line.substring((keyword + " ").length());
        assert result.endsWith(";");
        result = result.substring(0, result.length() - 1);
        return result;
    }
    
    private static String getPackageName(String fullyQualifiedClassName) {
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
    
    public void scanDir(String pathName) throws IOException {
        this.scanDir(new File(pathName));
    }
    
}
