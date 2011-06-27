package org.xydra.testgae.server.rest.experimental;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.X;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


public class BenchmarkResource {
	
	public static void restless(Restless r) {
		r.addGet("/benchmark", BenchmarkResource.class, "benchmark", new RestlessParameter("turns",
		        "1"));
	}
	
	public void benchmark(HttpServletRequest req, HttpServletResponse res, String turnsStr)
	        throws IOException {
		res.setContentType("text/plain");
		res.setStatus(200);
		Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		w.write("Benchmark with ...?turns=" + turnsStr + " start at " + System.currentTimeMillis()
		        + "\n");
		int turns = Integer.parseInt(turnsStr);
		w.write("Initializing at " + System.currentTimeMillis() + "\n");
		long id = System.currentTimeMillis();
		XydraPersistence persistence = new GaePersistence(XX.toId("benchmark-" + id));
		WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(persistence,
		        XX.toId("benchmark-user"));
		w.write("Init done at " + System.currentTimeMillis() + "\n");
		long duration = 0;
		for(int i = 1; i <= turns; i++) {
			long start = System.currentTimeMillis();
			w.write("Turn " + i + " at " + start + "\n");
			XWritableModel model1 = repo.createModel(XX.toId("model" + i));
			XWritableObject object1 = model1.createObject(XX.toId("object" + i));
			XWritableField field1 = object1.createField(XX.toId("field" + i));
			field1.setValue(X.getValueFactory().createStringValue("value in model1-object1-field1"));
			long stop = System.currentTimeMillis();
			w.write("Created model, object, field, value at " + stop + " took " + (stop - start)
			        + "ms" + "\n");
			duration += (stop - start);
		}
		w.write("Benchmark stop. Duration " + duration + " = " + (duration / turns) + " per turn"
		        + "\n");
		w.flush();
	}
	
}
