package org.xydra.valueindex;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.memory.MemoryPersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.webadmin.RepositoryResource;


public class StringValueIndexBenchmark {
	
	@Before
	public void setup() {
		XID repoId = XX.toId("gae-data");
		XydraPersistence persistence = new MemoryPersistence(repoId);
		
		XydraRuntime.setPersistence(repoId, persistence);
		
		File zipFile = new File("../TestData/anonymized-emails.zip");
		
		Writer w;
		try {
			w = new OutputStreamWriter(System.out, "utf-8");
			RepositoryResource.loadRepositoryFromZipFile(zipFile, XX.toId("gae-data"), w);
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		XID actorId = XX.createUniqueId();
		WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(persistence,
		        actorId);
		
		for(XID id : repo) {
			repo.getModel(id);
		}
	}
	
	@Test
	public void test() {
		assertTrue(true);
	}
}
