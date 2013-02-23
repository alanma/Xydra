package org.xydra.oo.generator.gwt;

import java.io.IOException;

import org.junit.Test;
import org.xydra.oo.generator.codespec.ClassSpec;
import org.xydra.oo.generator.codespec.PackageSpec;
import org.xydra.oo.testgen.tasks.shared.ITask;


/**
 * This is usually done automatically as part of gwt:compile
 */
public class TestGwtGenerator {
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        TestGwtGenerator t = new TestGwtGenerator();
        t.generateGwtClasses();
    }
    
    @Test
    public void generateGwtClasses() throws IOException, ClassNotFoundException {
        PackageSpec ps = new PackageSpec(ITask.class.getPackage().getName(), false);
        ClassSpec c = GwtCodeGenerator.constructClassSpec(ps, ITask.class.getCanonicalName(),
                "GwtTask");
        c.dump();
    }
    
}
