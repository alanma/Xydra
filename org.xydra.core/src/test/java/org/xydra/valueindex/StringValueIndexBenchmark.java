package org.xydra.valueindex;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.core.model.XModel;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.xml.XmlParser;


public class StringValueIndexBenchmark {
	private List<XModel> models;
	
	@Before
	public void setup() {
		// XID repoId = XX.toId("gae-data");
		// XydraPersistence persistence = new MemoryPersistence(repoId);
		//
		// XydraRuntime.setPersistence(repoId, persistence);
		//
		// File zipFile = new File("../TestData/anonymized-emails.zip");
		//
		// Writer w;
		// try {
		// w = new OutputStreamWriter(System.out, "utf-8");
		// RepositoryResource.loadRepositoryFromZipFile(zipFile,
		// XX.toId("gae-data"), w);
		// } catch(UnsupportedEncodingException e) {
		// e.printStackTrace();
		// } catch(IOException e) {
		// e.printStackTrace();
		// }
		//
		// XID actorId = XX.createUniqueId();
		// WritableRepositoryOnPersistence repo = new
		// WritableRepositoryOnPersistence(persistence,
		// actorId);
		//
		// for(XID id : repo) {
		// repo.getModel(id);
		// }
		
		this.models = new ArrayList<XModel>();
		XID actorId = XX.createUniqueId();
		
		File f = new File("../TestData/anonymized-emails/");
		File[] files = f.listFiles();
		
		for(int i = 0; i < files.length; i++) {
			if(files[i].getName().endsWith("tasks.xmodel.xml")) {
				XmlParser parser = new XmlParser();
				
				BufferedReader in;
				try {
					in = new BufferedReader(new FileReader(files[i]));
					String modelString = "";
					
					String row = null;
					while((row = in.readLine()) != null) {
						modelString += row;
					}
					
					// parse address
					XydraElement element = parser.parse(modelString);
					this.models.add(SerializedModel.toModel(actorId, "", element));
					
				} catch(FileNotFoundException e) {
					e.printStackTrace();
				} catch(IOException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	@Test
	public void benchmarkModelIndexing() {
		HashSet<XID> emptySet = new HashSet<XID>();
		
		for(XModel model : this.models) {
			StringMap map = new MockStringMap();
			StringValueIndex index = new StringValueIndex(map);
			
			SimpleValueIndexer indexer = new SimpleValueIndexer(index);
			
			long start = System.currentTimeMillis();
			XFieldLevelIndex fieldIndex = new XFieldLevelIndex(model, indexer, true, emptySet,
			        emptySet);
			long end = System.currentTimeMillis();
			
			System.out.println(end - start);
		}
	}
	
	@Test
	public void test() {
		assertTrue(true);
	}
}
