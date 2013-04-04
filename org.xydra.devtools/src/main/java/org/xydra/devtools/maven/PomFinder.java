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


public class PomFinder {
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
	
	public static void main(String[] args) {
		showArtifactDependencies(new File("target/poms.dot"), new File("/home/alpha/workspace37"),
		        new File("/home/alpha/.m2/repository"));
	}
	
	private static void showArtifactDependencies(File targetDotFile, File ... sourceDirs) {
		List<File> list = new LinkedList<File>();
		for(File sourceDir : sourceDirs) {
			list.addAll(findPomFiles(sourceDir));
		}
		
		for(File f : list) {
			try {
				parse(f);
			} catch(Exception e) {
				System.out.println(e.toString());
			}
		}
		// build graph
		List<String> scope = new ArrayList<String>();
		scope.add("com.calpano");
		scope.add("org.xydra");
		List<String> ignoreArtifacts = new ArrayList<String>();
		ignoreArtifacts.add("com.calpano:scribble");
		
		Graph g = new Graph("poms");
		for(Artifact artifact : artifactMap.values()) {
			if(isArtifactWithinScope(artifact, scope)
			        && !isArtifactIgnored(artifact, ignoreArtifacts)) {
				for(Artifact depArtifact : artifact.dependencies) {
					if(isArtifactWithinScope(depArtifact, scope))
						g.addEdge("<" + artifact.toString().replace(":", "\n") + ">", "<"
						        + depArtifact.toString().replace(":", "\n") + ">", 1, "\"\"");
				}
			}
		}
		
		try {
			g.writeTo(targetDotFile);
		} catch(IOException e) {
			System.out.println(e.toString());
		}
	}
	
	private static boolean isArtifactIgnored(Artifact artifact, List<String> ignoreArtifacts) {
		return ignoreArtifacts.contains(artifact.id());
	}
	
	private static boolean isArtifactWithinScope(Artifact artifact, List<String> scope) {
		return scope.contains(artifact.getGroupId());
	}
	
	private static void parse(File f) throws FileNotFoundException, IOException,
	        XmlPullParserException {
		System.out.println(f.getAbsolutePath());
		
		MavenXpp3Reader mavenReader = new MavenXpp3Reader();
		
		Model mavenModel = mavenReader.read(new FileInputStream(f));
		Artifact artifact = Artifact.createWithoutDependencies(mavenModel);
		artifact = getorPutArtifactReferenceViaMap(artifact);
		
		for(Dependency dependency : mavenModel.getDependencies()) {
			Artifact depArtifact = Artifact.createWithoutDependencies(dependency);
			depArtifact = getorPutArtifactReferenceViaMap(depArtifact);
			artifact.addDependency(depArtifact);
		}
	}
	
	private static Artifact getorPutArtifactReferenceViaMap(Artifact lookForArtifact) {
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
