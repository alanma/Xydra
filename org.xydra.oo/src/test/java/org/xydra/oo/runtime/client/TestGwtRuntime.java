package org.xydra.oo.runtime.client;

import java.io.IOException;

import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.util.DumpUtils;
import org.xydra.oo.testgen.tasks.client.GwtFactory;
import org.xydra.oo.testgen.tasks.shared.ITask;


/**
 * TODO ask thomas for test or look in sync2
 * 
 * @author xamde
 * 
 */
public class TestGwtRuntime {
    
    public static void main(String[] args) throws IOException {
        TestGwtRuntime t = new TestGwtRuntime();
        t.useAllTypes();
    }
    
    public void useAllTypes() throws IOException {
        
        // TODO how to test?
        
    }
    
    public void useTasks() {
        // setup
        XWritableModel model = new MemoryModel(XX.toId("actor"), "pass",
                XX.toAddress("/repo1/model1"));
        GwtFactory factory = new GwtFactory(model);
        
        // usage
        ITask task = factory.createTask("o1");
        task.setTitle("Foo");
        task.setNote("Der Schorsch braucht das dringend");
        task.setChecked(false);
        ITask o1_2 = factory.createTask("o1_2");
        assert task.subTasks() != null;
        task.subTasks().add(o1_2);
        
        String t = task.getTitle();
        System.out.println(t);
        
        ITask task2 = factory.getTask("o1");
        assert task2.getTitle().equals(t);
        
        DumpUtils.dump("filled", model);
    }
    
}
