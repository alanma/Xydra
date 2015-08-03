package org.xydra.testgae.server.rest.experimental;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.persistence.XydraPersistence;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;

public class BenchmarkResource {

	public static void restless(final Restless r) {
		r.addGet("/benchmark", BenchmarkResource.class, "benchmark", new RestlessParameter("turns",
				"1"));
	}

	public void benchmark(final HttpServletRequest req, final HttpServletResponse res, final String turnsStr)
			throws IOException {
		res.setContentType("text/plain");
		res.setStatus(200);
		final Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		w.write("Benchmark with ...?turns=" + turnsStr + " start at " + System.currentTimeMillis()
				+ "\n");
		final int turns = Integer.parseInt(turnsStr);
		w.write("Initializing at " + System.currentTimeMillis() + "\n");
		final long id = System.currentTimeMillis();
		final XydraPersistence persistence = new GaePersistence(Base.toId("benchmark-" + id));
		final WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(persistence,
				Base.toId("benchmark-user"));
		w.write("Init done at " + System.currentTimeMillis() + "\n");
		long duration = 0;
		for (int i = 1; i <= turns; i++) {
			final long start = System.currentTimeMillis();
			w.write("Turn " + i + " at " + start + "\n");
			final XWritableModel model1 = repo.createModel(Base.toId("model" + i));
			final XWritableObject object1 = model1.createObject(Base.toId("object" + i));
			final XWritableField field1 = object1.createField(Base.toId("field" + i));
			field1.setValue(BaseRuntime.getValueFactory().createStringValue("value in model1-object1-field1"));
			final long stop = System.currentTimeMillis();
			w.write("Created model, object, field, value at " + stop + " took " + (stop - start)
					+ "ms" + "\n");
			duration += stop - start;
		}
		w.write("Benchmark stop. Duration " + duration + " = " + duration / turns + " per turn"
				+ "\n");
		w.flush();
	}

}
