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
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
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

import com.sun.org.apache.xpath.internal.objects.XObject;


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
	public static int writeToCsv(XReadableModel model, Writer w) throws IOException {
		CsvTable table = new CsvTable(true);
		for(XID oid : model) {
			Row row = table.getOrCreateRow(oid.toString(), true);
			XReadableObject xo = model.getObject(oid);
			for(XID fid : xo) {
				XReadableField xf = xo.getField(fid);
				XValue value = xf.getValue();
				if(value != null) {
					Pair<String,String> pair = ValueDeSerializer.toStringPair(value);
					row.setValue(fid.toString() + TYPE_SUFFIX, pair.getFirst(), true);
					row.setValue(fid.toString(), pair.getSecond(), true);
				}
			}
		}
		table.writeTo(w);
		return table.rowCount();
	}
	
	/**
	 * @param r
	 * @param model
	 * @return number of rows in resulting table
	 * @throws IOException
	 */
	public static int readFromCsv(Reader r, XWritableModel model) throws IOException {
		CsvTable table = new CsvTable();
		table.readFrom(r, true);
		for(IRow row : table) {
			String oidStr = row.getKey();
			XID oid = XX.toId(oidStr);
			XWritableObject xo = model.createObject(oid);
			for(String col : row.getColumnNames()) {
				if(col.endsWith(TYPE_SUFFIX))
					continue;
				String valueStr = row.getValue(col);
				if(valueStr == null)
					continue;
				String typeStr = row.getValue(col + TYPE_SUFFIX);
				XValue value = ValueDeSerializer.fromStrings(typeStr, valueStr);
				XID fid = XX.toId(col);
				XWritableField field = xo.createField(fid);
				field.setValue(value);
			}
		}
		return table.rowCount();
	}
	
	protected static String serialize(XValue value) {
		// set up corresponding serialiser & parser
		JsonSerializer serializer = new JsonSerializer();
		// serialise with revisions
		XydraOut out = serializer.create();
		out.enableWhitespace(false, false);
		
		SerializedValue.serialize(value, out);
		
		String data = out.getData();
		
		return data;
	}
	
	protected static XValue deserialize(String data) {
		JsonParser parser = new JsonParser();
		XydraElement xydraElement = parser.parse(data);
		XValue value = SerializedValue.toValue(xydraElement);
		return value;
	}
	
	public static void main(String[] args) throws IOException {
		XValue test = XV.toIntegerListValue(Arrays.asList(11, 12, 13));
		String test2 = serialize(test);
		XValue test3 = deserialize(test2);
		System.out.println(test);
		System.out.println(test2);
		System.out.println(test3);
		
		XRepository repo = new MemoryRepository(XX.toId("actor"), "secret", XX.toId("repo"));
		DemoModelUtil.addPhonebookModel(repo);
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		File f = new File("./phonebook.csv");
		FileOutputStream fout = new FileOutputStream(f);
		Writer w = new OutputStreamWriter(fout, "utf-8");
		writeToCsv(model, w);
		w.close();
		
		FileInputStream fin = new FileInputStream(f);
		InputStreamReader in = new InputStreamReader(fin, "utf-8");
		SimpleModel model2 = new SimpleModel(XX.toAddress("/repo1/model1"));
		readFromCsv(in, model2);
		DumpUtils.dump("model2", model2);
	}
}
