package org.xydra.devtools.maven;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class Artifact {
    private String artifactId;
    private String groupId;
    private String version;
    
    private Set<Artifact> dependencies = new HashSet<Artifact>();
    private Set<Artifact> transitiveDependencies = new HashSet<Artifact>();
    
    public Artifact(String artifactId, String groupId, String version) {
        this.artifactId = artifactId;
        this.groupId = groupId;
        this.version = version;
    }
    
    public void addDependency(Artifact artifact) {
        assert !this.equals(artifact);
        this.dependencies.add(artifact);
    }
    
    public String getArtifactId() {
        return this.artifactId;
    }
    
    public String getGroupId() {
        return this.groupId;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public Set<Artifact> getDependencies() {
        return this.dependencies;
    }
    
    public String id() {
        return this.groupId + ":" + this.artifactId;
    }
    
    @Override
    public String toString() {
        return id();
    }
    
    @Override
    public int hashCode() {
        return id().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Artifact))
            return false;
        Artifact other = (Artifact)obj;
        return id().equals(other.id());
    }
    
    public static Artifact createWithoutDependencies(Model mavenModel) {
        String artifactId = mavenModel.getArtifactId();
        String groupId = mavenModel.getGroupId();
        if(groupId == null) {
            groupId = mavenModel.getParent().getGroupId();
        }
        if(groupId == null) {
            System.out.println("Have no groupId for " + mavenModel);
        }
        String version = mavenModel.getVersion();
        return new Artifact(artifactId, groupId, version);
    }
    
    public static Artifact createWithoutDependencies(Dependency dependency) {
        String artifactId = dependency.getArtifactId();
        String groupId = dependency.getGroupId();
        String version = dependency.getVersion();
        return new Artifact(artifactId, groupId, version);
    }
    
    private boolean transitiveComputed = false;
    
    private static final Logger log = LoggerFactory.getLogger(Artifact.class);
    
    public Set<Artifact> getTransitiveDependencies() {
        if(!this.transitiveComputed) {
            log.info("Computing transitive dependencies of " + this.id());
            
            Set<Artifact> known = new HashSet<Artifact>();
            // avoid self-loops
            known.add(this);
            Set<Artifact> all = new HashSet<Artifact>();
            for(Artifact dep : this.getDependencies()) {
                all.addAll(getAllIncludingTransitiveDependencies(dep, known));
            }
            
            this.transitiveDependencies = all;
            
            for(Artifact t : all) {
                log.info("Transitive: " + this + " -> " + t);
            }
            
            this.transitiveComputed = true;
        }
        return this.transitiveDependencies;
    }
    
    private static Set<Artifact> getAllIncludingTransitiveDependencies(Artifact a,
            Set<Artifact> known) {
        Set<Artifact> all = new HashSet<Artifact>();
        for(Artifact dep : a.getDependencies()) {
            all.add(dep);
            if(!known.contains(dep)) {
                known.add(dep);
                all.addAll(getAllIncludingTransitiveDependencies(dep, known));
            }
        }
        return all;
    }
    
    public void removeRedundantTransitiveDependencies() {
        Iterator<Artifact> it = this.dependencies.iterator();
        while(it.hasNext()) {
            Artifact a = it.next();
            if(this.getTransitiveDependencies().contains(a)) {
                log.info("Removing edge to " + a);
                it.remove();
            }
        }
    }
}
