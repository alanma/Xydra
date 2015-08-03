package org.xydra.server.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.util.DumpUtilsBase;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.XX;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.serialize.SerializedValue;
import org.xydra.core.serialize.ValueDeSerializer;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;
import org.xydra.core.util.DumpUtils;
import org.xydra.csv.IRow;
import org.xydra.csv.impl.memory.CsvTable;
import org.xydra.csv.impl.memory.Row;
import org.xydra.index.query.Pair;

/**
 * Write any {@link XReadableModel} into a CSV file. One row per {@link XObject}
 * , one column per {@link XField}. Values are serialised as JSON fragments.
 * Looks messy, but works with round-trip, i.e. the CSV can be parsed back
 * correctly to an XModel. This can be a handy debugging tool.
 *
 * @author xamde
 *
 */
@RunsInGWT(false)
public class CsvSerialisation {

	private static final String TYPE_SUFFIX = "__type";

	/**
	 * @param model
	 * @param w
	 * @return number of table rows written
	 * @throws IOException
	 */
	public static int writeToCsv(final XReadableModel model, final Writer w) throws IOException {
		final CsvTable table = toCsvTable(model);
		table.writeTo(w);
		return table.rowCount();
	}

	/**
	 * @param model
	 * @return the CsvTable
	 * @throws IOException
	 */
	public static CsvTable toCsvTable(final XReadableModel model) throws IOException {
		final CsvTable table = new CsvTable(false);
		for (final XId oid : model) {
			final Row row = table.getOrCreateRow(oid.toString(), true);
			final XReadableObject xo = model.getObject(oid);
			for (final XId fid : xo) {
				final XReadableField xf = xo.getField(fid);
				assert xf != null;
				final XValue xvalue = xf.getValue();
				String csvTypeStr;
				String csvValueStr;
				if (xvalue == null) {
					// a boolean field?
					csvTypeStr = ValueType.Boolean.name();
					csvValueStr = "fieldPresent";
				} else {
					final Pair<String, String> pair = ValueDeSerializer.toStringPair(xvalue);
					csvTypeStr = pair.getFirst();
					csvValueStr = pair.getSecond();
				}
				row.setValue(fid.toString() + TYPE_SUFFIX, csvTypeStr, true);
				row.setValue(fid.toString(), csvValueStr, true);
			}
		}
		return table;
	}

	/**
	 * @param r
	 * @param model
	 * @return number of rows in resulting table
	 * @throws IOException
	 */
	public static int readFromCsv(final Reader r, final XWritableModel model) throws IOException {
		final CsvTable table = new CsvTable();
		table.readFrom(r, true);
		for (final IRow row : table) {
			final String oidStr = row.getKey();
			final XId oid = Base.toId(oidStr);
			final XWritableObject xo = model.createObject(oid);
			for (final String col : row.getColumnNames()) {
				if (col.endsWith(TYPE_SUFFIX)) {
					continue;
				}
				final String valueStr = row.getValue(col);
				if (valueStr == null) {
					continue;
				}
				final String typeStr = row.getValue(col + TYPE_SUFFIX);
				try {
					final XValue value = ValueDeSerializer.fromStrings(typeStr, valueStr);
					final XId fid = Base.toId(col);
					final XWritableField field = xo.createField(fid);
					field.setValue(value);
				} catch (final IllegalArgumentException e) {
					throw new RuntimeException("Could not parse " + row + " as Xydra data", e);
				}
			}
		}
		return table.rowCount();
	}

	protected static String serialize(final XValue value) {
		// set up corresponding serialiser & parser
		final JsonSerializer serializer = new JsonSerializer();
		// serialise with revisions
		final XydraOut out = serializer.create();
		out.enableWhitespace(false, false);

		SerializedValue.serialize(value, out);

		final String data = out.getData();

		return data;
	}

	protected static XValue deserialize(final String data) {
		final JsonParser parser = new JsonParser();
		final XydraElement xydraElement = parser.parse(data);
		final XValue value = SerializedValue.toValue(xydraElement);
		return value;
	}

	public static void main(final String[] args) throws IOException {
		final XValue test = XV.toIntegerListValue(Arrays.asList(11, 12, 13));
		final String test2 = serialize(test);
		final XValue test3 = deserialize(test2);
		System.out.println(test);
		System.out.println(test2);
		System.out.println(test3);

		final XRepository repo = new MemoryRepository(Base.toId("actor"), "secret", Base.toId("repo"));
		DemoModelUtil.addPhonebookModel(repo);
		final XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		final File f = new File("./phonebook.csv");
		final FileOutputStream fout = new FileOutputStream(f);
		final Writer w = new OutputStreamWriter(fout, "utf-8");
		writeToCsv(model, w);
		w.close();

		final FileInputStream fin = new FileInputStream(f);
		final InputStreamReader in = new InputStreamReader(fin, "utf-8");
		final SimpleModel model2 = new SimpleModel(Base.toAddress("/repo1/model1"));
		readFromCsv(in, model2);
		DumpUtilsBase.dump("model2", model2);
	}
}
