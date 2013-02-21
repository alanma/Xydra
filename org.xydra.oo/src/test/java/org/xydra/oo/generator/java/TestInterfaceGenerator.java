package org.xydra.oo.generator.java;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.util.RememberLogListener;
import org.xydra.oo.testspecs.AllTypesSpec;
import org.xydra.oo.testspecs.TasksSpec;


public class TestInterfaceGenerator {
    
    static final RememberLogListener REMEMBER_LISTENER = new RememberLogListener();
    
    static {
        // trigger init
        LoggerFactory.addLogListener(REMEMBER_LISTENER);
    }
    
    private static final Logger log = LoggerFactory.getLogger(TestInterfaceGenerator.class);
    private static final String TESTGEN = "org.xydra.oo.testgen";
    
    public static void main(String[] args) throws IOException {
        TestInterfaceGenerator t = new TestInterfaceGenerator();
        t.generateAllTypesInterfaces();
        t.generateTasksInterfaces();
    }
    
    @Test
    public void generateTasksInterfaces() throws IOException {
        JavaCodeGenerator.generateInterfaces(TasksSpec.class, new File("./src/test/java"), TESTGEN
                + ".tasks");
        
        String logs = REMEMBER_LISTENER.getLogs();
        assertFalse("No fields should be ignored", logs.contains("Ignoring field"));
    }
    
    @Test
    public void generateAllTypesInterfaces() throws IOException {
        log.info("Start");
        
        JavaCodeGenerator.generateInterfaces(AllTypesSpec.class, new File("./src/test/java"),
                TESTGEN + ".alltypes");
        
        String logs = REMEMBER_LISTENER.getLogs();
        assertFalse("No fields should be ignored", logs.contains("Ignoring field"));
    }
    
}
