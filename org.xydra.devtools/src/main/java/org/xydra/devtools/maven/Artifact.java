package org.xydra.devtools.maven;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;


public class Artifact {
	String artifactId;
	String groupId;
	String version;
	
	Set<Artifact> dependencies = new HashSet<Artifact>();
	
	public Artifact(String artifactId, String groupId, String version) {
		this.artifactId = artifactId;
		this.groupId = groupId;
		this.version = version;
	}
	
	public void addDependency(Artifact artifact) {
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
}
