package org.xydra.devtools.maven;

import java.util.ArrayList;
import java.util.List;


public class Artifact {
    
    String artifactId;
    String groupId;
    String version;
    
    List<Artifact> dependencies = new ArrayList<Artifact>();
    
}
