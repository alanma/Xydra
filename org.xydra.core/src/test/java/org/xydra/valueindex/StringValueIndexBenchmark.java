package org.xydra.valueindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.xml.XmlParser;


public class StringValueIndexBenchmark {
	private List<XModel> models;
	final static String lineSeparator = System.getProperty("line.separator");
	final String testDataPath = "../TestData/anonymized-emails-2/";
	
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
		
		File f = new File(this.testDataPath);
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
	
	private static Set<XAddress> getAddresses(Set<ValueIndexEntry> pairs) {
		HashSet<XAddress> addresses = new HashSet<XAddress>();
		
		for(ValueIndexEntry pair : pairs) {
			XAddress address = pair.getAddress();
			assertEquals(XType.XFIELD, address.getAddressedType());
			addresses.add(address);
		}
		
		return addresses;
	}
	
	@Test
	public void testModelIndexing() {
		HashSet<XID> emptySet = new HashSet<XID>();
		
		for(XModel model : this.models) {
			StringMap map = new MemoryStringMap();
			StringValueIndex index = new StringValueIndex(map);
			
			SimpleValueIndexer indexer = new SimpleValueIndexer(index);
			
			XFieldLevelIndex fieldIndex = new XFieldLevelIndex(model, indexer, true, emptySet,
			        emptySet);
			
			for(XID objectId : model) {
				XObject object = model.getObject(objectId);
				
				for(XID fieldId : object) {
					XField field = object.getField(fieldId);
					XValue value = field.getValue();
					
					List<String> list = indexer.getIndexStrings(value);
					
					for(String s : list) {
						Set<ValueIndexEntry> entries = fieldIndex.search(s);
						
						Set<XAddress> addresses = getAddresses(entries);
						assertTrue(addresses.contains(field.getAddress()));
						
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	@Test
	public void benchmarkModelIndexing() {
		String path = this.testDataPath + "CompleteModelIndexing/";
		HashSet<XID> emptySet = new HashSet<XID>();
		
		for(int i = 0; i < 10; i++) {
			System.out.println("ModelIndexing: " + i);
			for(XModel model : this.models) {
				writeDescription(path, model);
				
				StringMap map = new MemoryStringMap();
				StringValueIndex index = new StringValueIndex(map);
				
				SimpleValueIndexer indexer = new SimpleValueIndexer(index);
				
				long start = System.currentTimeMillis();
				XFieldLevelIndex fieldIndex = new XFieldLevelIndex(model, indexer, true, emptySet,
				        emptySet);
				long end = System.currentTimeMillis();
				
				writeIndexBenchmarkData(path, model, end - start);
			}
		}
	}
	
	@SuppressWarnings("unused")
	@Test
	public void benchmarkSearch() {
		String path = this.testDataPath + "Search/";
		HashSet<XID> emptySet = new HashSet<XID>();
		
		for(int i = 0; i < 5; i++) {
			System.out.println("BenchmarkSearch: " + i);
			for(XModel model : this.models) {
				writeDescription(path, model);
				
				StringMap map = new MemoryStringMap();
				StringValueIndex index = new StringValueIndex(map);
				
				SimpleValueIndexer indexer = new SimpleValueIndexer(index);
				
				XFieldLevelIndex fieldIndex = new XFieldLevelIndex(model, indexer, true, emptySet,
				        emptySet);
				
				for(XID objectId : model) {
					XObject object = model.getObject(objectId);
					
					for(XID fieldId : object) {
						XField field = object.getField(fieldId);
						XValue value = field.getValue();
						
						List<String> list = indexer.getIndexStrings(value);
						
						for(String s : list) {
							long start = System.currentTimeMillis();
							Set<ValueIndexEntry> entries = fieldIndex.search(s);
							long end = System.currentTimeMillis();
							
							writeSearchBenchmarkData(path, model, end - start);
						}
					}
				}
				
			}
		}
	}
	
	@Test
	@SuppressWarnings("unused")
	public void benchmarkIndexAndSearchLargeModel() {
		XID actorId = XX.createUniqueId();
		XRepository repo = X.createMemoryRepository(actorId);
		XID modelId = XX.createUniqueId();
		XModel largeModel = repo.createModel(modelId);
		
		int modelCount = 0;
		for(XModel m : this.models) {
			modelCount++;
			for(XID oId : m) {
				XObject o = m.getObject(oId);
				
				XID objectId = XX.createUniqueId();
				XObject object = largeModel.createObject(objectId);
				
				for(XID fId : o) {
					XField f = o.getField(fId);
					XField field = object.createField(fId);
					
					field.setValue(f.getValue());
				}
			}
		}
		System.out.println("Models: " + modelCount);
		
		String indexPath = this.testDataPath + "IndexLargeModel/";
		String searchPath = this.testDataPath + "SearchInLargeModel/";
		HashSet<XID> emptySet = new HashSet<XID>();
		
		for(int i = 0; i < 5; i++) {
			System.out.println("BenchmarkIndexAndSearchLargeModel: " + i);
			writeDescription(indexPath, largeModel);
			writeDescription(searchPath, largeModel);
			
			StringMap map = new MemoryStringMap();
			StringValueIndex index = new StringValueIndex(map);
			SimpleValueIndexer indexer = new SimpleValueIndexer(index);
			
			long start = System.currentTimeMillis();
			XFieldLevelIndex fieldIndex = new XFieldLevelIndex(largeModel, indexer, true, emptySet,
			        emptySet);
			long end = System.currentTimeMillis();
			writeIndexBenchmarkData(indexPath, largeModel, end - start);
			
			for(XID objectId : largeModel) {
				XObject object = largeModel.getObject(objectId);
				
				for(XID fieldId : object) {
					XField field = object.getField(fieldId);
					XValue value = field.getValue();
					
					List<String> list = indexer.getIndexStrings(value);
					
					for(String s : list) {
						start = System.currentTimeMillis();
						Set<ValueIndexEntry> entries = fieldIndex.search(s);
						end = System.currentTimeMillis();
						
						writeSearchBenchmarkData(searchPath, largeModel, end - start);
					}
				}
			}
			
		}
	}
	
	@Test
	@SuppressWarnings("unused")
	public void benchmarkIndexLargeModel() {
		XID actorId = XX.createUniqueId();
		XRepository repo = X.createMemoryRepository(actorId);
		XID modelId = XX.createUniqueId();
		XModel largeModel = repo.createModel(modelId);
		
		int modelCount = 0;
		for(XModel m : this.models) {
			modelCount++;
			for(XID oId : m) {
				XObject o = m.getObject(oId);
				
				XID objectId = XX.createUniqueId();
				XObject object = largeModel.createObject(objectId);
				
				for(XID fId : o) {
					XField f = o.getField(fId);
					XField field = object.createField(fId);
					
					field.setValue(f.getValue());
				}
			}
		}
		System.out.println("Models: " + modelCount);
		
		String indexPath = this.testDataPath + "IndexLargeModel/";
		String searchPath = this.testDataPath + "SearchInLargeModel/";
		HashSet<XID> emptySet = new HashSet<XID>();
		
		for(int i = 0; i < 50; i++) {
			StringMap map = new MemoryStringMap();
			StringValueIndex index = new StringValueIndex(map);
			SimpleValueIndexer indexer = new SimpleValueIndexer(index);
			
			long start = System.currentTimeMillis();
			XFieldLevelIndex fieldIndex = new XFieldLevelIndex(largeModel, indexer, true, emptySet,
			        emptySet);
			long end = System.currentTimeMillis();
			System.out.println(end - start);
		}
	}
	
	private static void writeDescription(String path, XModel model) {
		String idString = model.getId().toString();
		try {
			File f = new File(path + idString + ".txt");
			if(f.exists()) {
				return;
			}
			int objects = 0;
			int fields = 0;
			int values = 0;
			
			for(XID objectId : model) {
				objects++;
				XObject object = model.getObject(objectId);
				
				for(XID fieldId : object) {
					fields++;
					XField field = object.getField(fieldId);
					if(field.getValue() != null) {
						values++;
					}
				}
			}
			
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			out.write("#Objects:, " + objects);
			out.write(lineSeparator);
			out.write("#Fields:, " + fields);
			out.write(lineSeparator);
			out.write("#Non-Null Values:, " + values);
			out.write(lineSeparator);
			
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeIndexBenchmarkData(String path, XModel model, long data) {
		String idString = model.getId().toString();
		try {
			File f = new File(path + idString + ".txt");
			assert f.exists();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			out.write("" + data);
			out.write(lineSeparator);
			
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeSearchBenchmarkData(String path, XModel model, long data) {
		String idString = model.getId().toString();
		try {
			File f = new File(path + idString + ".txt");
			
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			out.write("" + data);
			out.write(lineSeparator);
			
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
