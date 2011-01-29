package org.xydra.testgae;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.X;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;


public class BenchmarkResource {
	
	public static void restless(Restless r) {
		r.addGet("/benchmark", BenchmarkResource.class, "benchmark", new RestlessParameter("turns",
		        "1"));
	}
	
	public void benchmark(HttpServletRequest req, HttpServletResponse res, String turnsStr)
	        throws IOException {
		res.setContentType("text/plain");
		res.setStatus(200);
		res.getWriter().println(
		        "Benchmark with ...?turns=" + turnsStr + " start at " + System.currentTimeMillis());
		int turns = Integer.parseInt(turnsStr);
		res.getWriter().println("Initializing at " + System.currentTimeMillis());
		long id = System.currentTimeMillis();
		XydraPersistence persistence = new GaePersistence(XX.toId("benchmark-" + id));
		WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(persistence,
		        XX.toId("benchmark-user"));
		res.getWriter().println("Init done at " + System.currentTimeMillis());
		long duration = 0;
		for(int i = 1; i <= turns; i++) {
			long start = System.currentTimeMillis();
			res.getWriter().println("Turn " + i + " at " + start);
			XWritableModel model1 = repo.createModel(XX.toId("model" + i));
			XWritableObject object1 = model1.createObject(XX.toId("object" + i));
			XWritableField field1 = object1.createField(XX.toId("field" + i));
			field1.setValue(X.getValueFactory().createStringValue("value in model1-object1-field1"));
			long stop = System.currentTimeMillis();
			res.getWriter().println(
			        "Created model, object, field, value at " + stop + " took " + (stop - start)
			                + "ms");
			duration += (stop - start);
		}
		res.getWriter().println(
		        "Benchmark stop. Duration " + duration + " = " + (duration / turns) + " per turn");
	}
	
}
