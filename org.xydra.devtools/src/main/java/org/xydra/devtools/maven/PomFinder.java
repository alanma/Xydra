package org.xydra.devtools.maven;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.xydra.devtools.dot.Graph;
import org.xydra.devtools.dot.Graph.ILabelRenderer;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class PomFinder {
    
    private static final Logger log = LoggerFactory.getLogger(PomFinder.class);
    
    static Map<String,Artifact> artifactMap = new HashMap<String,Artifact>();
    
    public static List<File> findPomFiles(File dir) {
        List<File> list = new LinkedList<File>();
        File[] files = dir.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().equals("pom.xml");
            }
        });
        for(File f : files) {
            if(f.isDirectory()) {
                list.addAll(findPomFiles(f));
            } else {
                list.add(f);
            }
        }
        return list;
    }
    
    public static void main(String[] args) throws IOException, XmlPullParserException {
        showArtifactDependencies(new File("target/poms.dot"), new File("/Users/xamde/_data_/_p_"),
                new File("/Users/xamde/.m2/repository"));
    }
    
    private static void showArtifactDependencies(File targetDotFile, File ... sourceDirs)
            throws IOException, IOException, XmlPullParserException {
        
        List<File> list = new LinkedList<File>();
        for(File sourceDir : sourceDirs) {
            log.info("Parsing " + sourceDir.getAbsolutePath());
            list.addAll(findPomFiles(sourceDir));
        }
        
        for(File f : list) {
            parse(f);
        }
        
        // remove redundant edges = transitive dependencies
        for(Artifact artifact : artifactMap.values()) {
            artifact.removeRedundantTransitiveDependencies();
        }
        
        // build graph
        List<String> scope = new ArrayList<String>();
        scope.add("com.calpano");
        scope.add("org.xydra");
        List<String> ignoreArtifacts = new ArrayList<String>();
        ignoreArtifacts.add("com.calpano:scribble");
        
        // bugs
        ignoreArtifacts.add("org.xydra:{artifactId}");
        ignoreArtifacts.add("org.xydra:${artifactId}");
        
        // very stable
        ignoreArtifacts.add("org.xydra:annotations");
        ignoreArtifacts.add("org.xydra:log");
        ignoreArtifacts.add("org.xydra:log.gae");
        ignoreArtifacts.add("org.xydra:index");
        // ignoreArtifacts.add("org.xydra:restless");
        
        // almost abandoned
        ignoreArtifacts.add("com.calpano:app-prototype");
        ignoreArtifacts.add("com.calpano:lander");
        ignoreArtifacts.add("com.calpano:steamstart");
        ignoreArtifacts.add("com.calpano:goaltaxi");
        ignoreArtifacts.add("com.calpano:forward");
        ignoreArtifacts.add("com.calpano:nextapp");
        ignoreArtifacts.add("com.calpano:finance");
        ignoreArtifacts.add("com.calpano:repeatly");
        
        Graph g = new Graph("poms");
        g.concentrateEdges = true;
        g.setLabelRenderer(new ILabelRenderer() {
            @Override
            public String render(String s) {
                return "<" + s.replace(":", "\n") + ">";
            }
        });
        
        Map<String,Graph> subgraphMap = new HashMap<String,Graph>();
        for(Artifact artifact : artifactMap.values()) {
            if(isArtifactWithinScope(artifact, scope)
                    && !isArtifactIgnored(artifact, ignoreArtifacts)) {
                for(Artifact depArtifact : artifact.getDependencies()) {
                    if(isArtifactWithinScope(depArtifact, scope)
                            && !isArtifactIgnored(depArtifact, ignoreArtifacts)) {
                        Graph subGraph = subgraphMap.get(depArtifact.getGroupId());
                        if(subGraph == null) {
                            subGraph = g.createSubgraph(depArtifact.getGroupId());
                            subgraphMap.put(depArtifact.getGroupId(), subGraph);
                        }
                        // use subGraph.addEdge ... to cluster stuff
                        g.addEdge(artifact.id(), depArtifact.id(), 1, "\"\"");
                    }
                }
            }
        }
        
        g.writeTo(targetDotFile);
    }
    
    private static boolean isArtifactIgnored(Artifact artifact, List<String> ignoreArtifacts) {
        return ignoreArtifacts.contains(artifact.id());
    }
    
    private static boolean isArtifactWithinScope(Artifact artifact, List<String> scope) {
        return scope.contains(artifact.getGroupId());
    }
    
    private static void parse(File f) throws FileNotFoundException, IOException,
            XmlPullParserException {
        log.info("Parsing " + f.getAbsolutePath());
        
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        
        try {
            Model mavenModel = mavenReader.read(new FileInputStream(f));
            Artifact artifact = Artifact.createWithoutDependencies(mavenModel);
            artifact = getOrPutArtifactReferenceViaMap(artifact);
            
            for(Dependency dependency : mavenModel.getDependencies()) {
                @SuppressWarnings("unused")
                boolean isTest = dependency.getScope() != null
                        && dependency.getScope().equals("test");
                
                Artifact depArtifact = Artifact.createWithoutDependencies(dependency);
                depArtifact = getOrPutArtifactReferenceViaMap(depArtifact);
                artifact.addDependency(depArtifact);
            }
        } catch(XmlPullParserException e) {
            log.warn("Could not parse " + f.getAbsolutePath(), e);
        }
    }
    
    private static Artifact getOrPutArtifactReferenceViaMap(Artifact lookForArtifact) {
        Artifact artifact;
        if(artifactMap.containsKey(lookForArtifact.id())) {
            artifact = artifactMap.get(lookForArtifact.id());
        } else {
            artifactMap.put(lookForArtifact.id(), lookForArtifact);
            artifact = lookForArtifact;
        }
        return artifact;
    }
}
