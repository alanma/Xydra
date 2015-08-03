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
import org.junit.Ignore;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.sharedutils.XyAssert;


@Ignore
public class StringValueIndexBenchmark {
	private List<XModel> models;
	final static String lineSeparator = System.getProperty("line.separator");
	final String testDataPath = "../TestData/anonymized-emails-2/";

	@Before
	public void setup() {
		// XId repoId = XX.toId("gae-data");
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
		// XId actorId = XX.createUniqueId();
		// WritableRepositoryOnPersistence repo = new
		// WritableRepositoryOnPersistence(persistence,
		// actorId);
		//
		// for(XId id : repo) {
		// repo.getModel(id);
		// }

		this.models = new ArrayList<XModel>();
		final XId actorId = Base.createUniqueId();

		final File f = new File(this.testDataPath);
		final File[] files = f.listFiles();

		for(int i = 0; i < files.length; i++) {
			if(files[i].getName().endsWith("tasks.xmodel.xml")) {
				final XmlParser parser = new XmlParser();

				BufferedReader in;
				try {
					in = new BufferedReader(new FileReader(files[i]));
					String modelString = "";

					String row = null;
					while((row = in.readLine()) != null) {
						modelString += row;
					}

					// parse address
					final XydraElement element = parser.parse(modelString);
					this.models.add(SerializedModel.toModel(actorId, "", element));

				} catch(final FileNotFoundException e) {
					e.printStackTrace();
				} catch(final IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	private static Set<XAddress> getAddresses(final Set<ValueIndexEntry> pairs) {
		final HashSet<XAddress> addresses = new HashSet<XAddress>();

		for(final ValueIndexEntry pair : pairs) {
			final XAddress address = pair.getAddress();
			assertEquals(XType.XFIELD, address.getAddressedType());
			addresses.add(address);
		}

		return addresses;
	}

	@Test
	public void testModelIndexing() {
		final HashSet<XId> emptySet = new HashSet<XId>();

		for(final XModel model : this.models) {
			final StringMap map = new MemoryStringMap();
			final StringValueIndex index = new StringValueIndex(map);

			final SimpleValueIndexer indexer = new SimpleValueIndexer(index);

			final XFieldLevelIndex fieldIndex = new XFieldLevelIndex(model, indexer, true, emptySet,
			        emptySet);

			for(final XId objectId : model) {
				final XObject object = model.getObject(objectId);

				for(final XId fieldId : object) {
					final XField field = object.getField(fieldId);
					final XValue value = field.getValue();

					final List<String> list = indexer.getIndexStrings(value);

					for(final String s : list) {
						final Set<ValueIndexEntry> entries = fieldIndex.search(s);

						final Set<XAddress> addresses = getAddresses(entries);
						assertTrue(addresses.contains(field.getAddress()));

					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void benchmarkModelIndexing() {
		final String path = this.testDataPath + "CompleteModelIndexing/";
		final HashSet<XId> emptySet = new HashSet<XId>();

		for(int i = 0; i < 10; i++) {
			System.out.println("ModelIndexing: " + i);
			for(final XModel model : this.models) {
				writeDescription(path, model);

				final StringMap map = new MemoryStringMap();
				final StringValueIndex index = new StringValueIndex(map);

				final SimpleValueIndexer indexer = new SimpleValueIndexer(index);

				final long start = System.currentTimeMillis();
				final XFieldLevelIndex fieldIndex = new XFieldLevelIndex(model, indexer, true, emptySet,
				        emptySet);
				final long end = System.currentTimeMillis();

				writeIndexBenchmarkData(path, model, end - start);
			}
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void benchmarkSearch() {
		final String path = this.testDataPath + "Search/";
		final HashSet<XId> emptySet = new HashSet<XId>();

		for(int i = 0; i < 5; i++) {
			System.out.println("BenchmarkSearch: " + i);
			for(final XModel model : this.models) {
				writeDescription(path, model);

				final StringMap map = new MemoryStringMap();
				final StringValueIndex index = new StringValueIndex(map);

				final SimpleValueIndexer indexer = new SimpleValueIndexer(index);

				final XFieldLevelIndex fieldIndex = new XFieldLevelIndex(model, indexer, true, emptySet,
				        emptySet);

				for(final XId objectId : model) {
					final XObject object = model.getObject(objectId);

					for(final XId fieldId : object) {
						final XField field = object.getField(fieldId);
						final XValue value = field.getValue();

						final List<String> list = indexer.getIndexStrings(value);

						for(final String s : list) {
							final long start = System.currentTimeMillis();
							final Set<ValueIndexEntry> entries = fieldIndex.search(s);
							final long end = System.currentTimeMillis();

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
		final XId actorId = Base.createUniqueId();
		final XRepository repo = X.createMemoryRepository(actorId);
		final XId modelId = Base.createUniqueId();
		final XModel largeModel = repo.createModel(modelId);

		int modelCount = 0;
		for(final XModel m : this.models) {
			modelCount++;
			for(final XId oId : m) {
				final XObject o = m.getObject(oId);

				final XId objectId = Base.createUniqueId();
				final XObject object = largeModel.createObject(objectId);

				for(final XId fId : o) {
					final XField f = o.getField(fId);
					final XField field = object.createField(fId);

					field.setValue(f.getValue());
				}
			}
		}
		System.out.println("Models: " + modelCount);

		final String indexPath = this.testDataPath + "IndexLargeModel/";
		final String searchPath = this.testDataPath + "SearchInLargeModel/";
		final HashSet<XId> emptySet = new HashSet<XId>();

		for(int i = 0; i < 5; i++) {
			System.out.println("BenchmarkIndexAndSearchLargeModel: " + i);
			writeDescription(indexPath, largeModel);
			writeDescription(searchPath, largeModel);

			final StringMap map = new MemoryStringMap();
			final StringValueIndex index = new StringValueIndex(map);
			final SimpleValueIndexer indexer = new SimpleValueIndexer(index);

			long start = System.currentTimeMillis();
			final XFieldLevelIndex fieldIndex = new XFieldLevelIndex(largeModel, indexer, true, emptySet,
			        emptySet);
			long end = System.currentTimeMillis();
			writeIndexBenchmarkData(indexPath, largeModel, end - start);

			for(final XId objectId : largeModel) {
				final XObject object = largeModel.getObject(objectId);

				for(final XId fieldId : object) {
					final XField field = object.getField(fieldId);
					final XValue value = field.getValue();

					final List<String> list = indexer.getIndexStrings(value);

					for(final String s : list) {
						start = System.currentTimeMillis();
						final Set<ValueIndexEntry> entries = fieldIndex.search(s);
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
		final XId actorId = Base.createUniqueId();
		final XRepository repo = X.createMemoryRepository(actorId);
		final XId modelId = Base.createUniqueId();
		final XModel largeModel = repo.createModel(modelId);

		int modelCount = 0;
		for(final XModel m : this.models) {
			modelCount++;
			for(final XId oId : m) {
				final XObject o = m.getObject(oId);

				final XId objectId = Base.createUniqueId();
				final XObject object = largeModel.createObject(objectId);

				for(final XId fId : o) {
					final XField f = o.getField(fId);
					final XField field = object.createField(fId);

					field.setValue(f.getValue());
				}
			}
		}
		System.out.println("Models: " + modelCount);

		final String indexPath = this.testDataPath + "IndexLargeModel/";
		final String searchPath = this.testDataPath + "SearchInLargeModel/";
		final HashSet<XId> emptySet = new HashSet<XId>();

		for(int i = 0; i < 50; i++) {
			final StringMap map = new MemoryStringMap();
			final StringValueIndex index = new StringValueIndex(map);
			final SimpleValueIndexer indexer = new SimpleValueIndexer(index);

			final long start = System.currentTimeMillis();
			final XFieldLevelIndex fieldIndex = new XFieldLevelIndex(largeModel, indexer, true, emptySet,
			        emptySet);
			final long end = System.currentTimeMillis();
			System.out.println(end - start);
		}
	}

	private static void writeDescription(final String path, final XModel model) {
		final String idString = model.getId().toString();
		try {
			final File f = new File(path + idString + ".txt");
			if(f.exists()) {
				return;
			}
			int objects = 0;
			int fields = 0;
			int values = 0;

			for(final XId objectId : model) {
				objects++;
				final XObject object = model.getObject(objectId);

				for(final XId fieldId : object) {
					fields++;
					final XField field = object.getField(fieldId);
					if(field.getValue() != null) {
						values++;
					}
				}
			}

			final BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			out.write("#Objects:, " + objects);
			out.write(lineSeparator);
			out.write("#Fields:, " + fields);
			out.write(lineSeparator);
			out.write("#Non-Null Values:, " + values);
			out.write(lineSeparator);

			out.close();
		} catch(final IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeIndexBenchmarkData(final String path, final XModel model, final long data) {
		final String idString = model.getId().toString();
		try {
			final File f = new File(path + idString + ".txt");
			XyAssert.xyAssert(f.exists());

			final BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			out.write("" + data);
			out.write(lineSeparator);

			out.close();
		} catch(final IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeSearchBenchmarkData(final String path, final XModel model, final long data) {
		final String idString = model.getId().toString();
		try {
			final File f = new File(path + idString + ".txt");

			final BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			out.write("" + data);
			out.write(lineSeparator);

			out.close();
		} catch(final IOException e) {
			e.printStackTrace();
		}
	}
}
