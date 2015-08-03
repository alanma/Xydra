package org.xydra.core.arch;

import java.io.File;
import java.io.IOException;

import org.xydra.devtools.javapackages.PackageChaos;
import org.xydra.devtools.javapackages.architecture.Architecture;
import org.xydra.devtools.javapackages.architecture.Layer;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.log.util.Log4jUtils;


public class XydraCoreArchitecture {

    static {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
        try {
            Log4jUtils.listConfigFromClasspath();
        } catch(final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) throws IOException {
        final String scope = "org.xydra.core";
        // step 1: check for violation of GWT layers
        final Architecture a = new Architecture(scope);
        final Layer shared = a.defineLayer(scope + ".shared");
        final Layer client = a.defineLayer(scope + ".client");
        final Layer server = a.defineLayer(scope + ".server");
        client.mayAccess(shared);
        server.mayAccess(shared);

        final File dot = new File("./target/core.dot");
        final PackageChaos pc = new PackageChaos(a, dot);
        pc.setShowCauses(true);
        pc.analyseAndRender("./src/main/java");
    }

}
