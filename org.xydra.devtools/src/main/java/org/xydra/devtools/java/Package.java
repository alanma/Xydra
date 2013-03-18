package org.xydra.devtools.java;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


public class Package implements Comparable<Package> {
    
    private final Project project;
    
    public Package(Project packageChaos, String packageName) {
        this.project = packageChaos;
        this.name = packageName;
    }
    
    public static class Dependency implements Comparable<Dependency> {
        
        public Dependency(Package p, String cause) {
            assert p != null;
            assert cause != null;
            this.p = p;
            this.causes.add(cause);
        }
        
        private Package p;
        private Set<String> causes = new HashSet<String>();
        
        public boolean equals(Object o) {
            return o instanceof Dependency && ((Dependency)o).p.equals(this.p);
        }
        
        public int hashCode() {
            return this.p.hashCode();
        }
        
        public Package getPackage() {
            return this.p;
        }
        
        public Set<String> getCauses() {
            return this.causes;
        }
        
        @Override
        public int compareTo(Dependency o) {
            return this.p.compareTo(o.getPackage());
        }
    }
    
    private String name;
    public Set<Dependency> dependesOn = new TreeSet<Dependency>();
    public Set<Package> children = new TreeSet<Package>();
    
    public void addImport(String importName, String cause) {
        Package p = this.project.getOrCreatePackage(importName);
        Dependency dependency = new Dependency(p, cause);
        this.dependesOn.add(dependency);
    }
    
    public String getName() {
        return this.name;
    }
    
    @Override
    public int compareTo(Package o) {
        return this.getName().compareTo(o.getName());
    }
    
    public String getParentPackageName() {
        return Project.getParentPackageName(this.name);
    }
    
    public boolean equals(Object o) {
        return o instanceof Package && ((Package)o).getName().equals(getName());
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    
    public int depth() {
        String[] parts = this.name.split("[.]");
        return parts.length;
    }
    
    public boolean isRoot() {
        return this.name.equals("");
    }
    
    public String toString() {
        return this.name + " sub:" + this.children.size() + " dep:" + this.dependesOn.size();
    }
    
    public boolean hasNonEmptyChildren() {
        for(Package p : this.children) {
            if(p.size() > 0)
                return true;
        }
        return false;
    }
    
    private int size() {
        int size = this.dependesOn.size();
        for(Package child : this.children) {
            size += child.size();
        }
        return size;
    }
    
    public Set<Dependency> getDependesOn() {
        return this.dependesOn;
    }
    
}
