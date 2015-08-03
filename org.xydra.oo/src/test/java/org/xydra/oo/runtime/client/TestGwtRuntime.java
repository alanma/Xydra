package org.xydra.oo.runtime.client;

import java.io.IOException;

import org.xydra.base.Base;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.util.DumpUtilsBase;
import org.xydra.core.XX;
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

	public static void main(final String[] args) throws IOException {
		final TestGwtRuntime t = new TestGwtRuntime();
		t.useAllTypes();
	}

	public void useAllTypes() throws IOException {

		// TODO how to test?

	}

	public void useTasks() {
		// setup
		final XWritableModel model = new MemoryModel(Base.toId("actor"), "pass",
				Base.toAddress("/repo1/model1"));
		final GwtFactory factory = new GwtFactory(model);

		// usage
		final ITask task = factory.createTask("o1");
		task.setTitle("Foo");
		task.setNote("Der Schorsch braucht das dringend");
		task.setChecked(false);
		final ITask o1_2 = factory.createTask("o1_2");
		assert task.subTasks() != null;
		task.subTasks().add(o1_2);

		final String t = task.getTitle();
		System.out.println(t);

		final ITask task2 = factory.getTask("o1");
		assert task2.getTitle().equals(t);

		DumpUtilsBase.dump("filled", model);
	}

}
