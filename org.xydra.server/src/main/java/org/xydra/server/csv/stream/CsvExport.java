package org.xydra.server.csv.stream;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XRevWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleRepository;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.util.SimpleSyntaxUtilsTest;
import org.xydra.csv.CsvRowHandler;
import org.xydra.csv.IReadableRow;
import org.xydra.csv.IRowHandler;
import org.xydra.csv.impl.memory.CsvReader;
import org.xydra.csv.impl.memory.SingleRow;

/**
 * Intended representation of XValues in CSV:
 * 
 * <pre>
 * XBoolean       true
 * XBooleanList   [true,false,false]
 * XInteger    34
 * XLong       34
 * XDouble     34.50
 * XString     'xxx'
 * XSringList  ['a','b']
 * XId         'john'
 * XAddress    '/repo1/model1/-/-'
 * null        null
 * </pre>
 * 
 * @author xamde
 */
public class CsvExport {

	public static void toWriter(XReadableRepository repository, Writer w)
			throws IllegalStateException, IOException {
		toRowHandler(repository, new CsvRowHandler(w));
	}

	public static void toRowHandler(XReadableRepository repository, IRowHandler rowHandler)
			throws IllegalStateException, IOException {
		rowHandler.handleHeaderRow(Arrays.asList("modelRev", "objectRev", "fieldRev", "value",
				"valueType"));
		for (XId modelId : repository) {
			XReadableModel model = repository.getModel(modelId);
			toRowHandler(model, rowHandler);
		}
	}

	/**
	 * Each model, even if empty, results at least in one row.
	 * 
	 * @param model
	 *            to be exported
	 * @param rowHandler
	 *            export target
	 * @throws IOException
	 *             from underlying I/O
	 * @throws IllegalStateException
	 *             if no header row has been sent
	 */
	public static void toRowHandler(XReadableModel model, IRowHandler rowHandler)
			throws IllegalStateException, IOException {
		String rowName = model.getAddress().toString();
		if (model.isEmpty()) {
			rowHandler.handleRow(rowName, new SingleRow(rowName, new String[][] { new String[] {
					"modelRev", "" + model.getRevisionNumber() } }));
		} else {
			for (XId objectId : model) {
				toRowHandler(model, model.getObject(objectId), rowHandler);
			}
		}
	}

	/**
	 * Each object, even if empty, results at least in one row.
	 * 
	 * @param model
	 * 
	 * @param object
	 *            to be exported
	 * @param rowHandler
	 *            export target
	 * @throws IOException
	 *             from underlying I/O
	 * @throws IllegalStateException
	 *             if no header row has been sent
	 */
	public static void toRowHandler(XReadableModel model, XReadableObject object,
			IRowHandler rowHandler) throws IllegalStateException, IOException {
		String rowName = object.getAddress().toString();
		if (object.isEmpty()) {
			rowHandler.handleRow(rowName, new SingleRow(rowName, new String[][] {

			new String[] { "modelRev", "" + model.getRevisionNumber() },

			new String[] { "objectRev", "" + object.getRevisionNumber() }

			}));
		} else {
			for (XId fieldId : object) {
				XReadableField field = object.getField(fieldId);
				toRowHandler(model, object, field, rowHandler);
			}
		}
	}

	/**
	 * @param model
	 *            to be exported
	 * @param object
	 *            to be exported
	 * @param field
	 *            to be exported
	 * @param rowHandler
	 *            export target
	 * @throws IOException
	 *             from underlying I/O
	 * @throws IllegalStateException
	 *             if no header row has been sent
	 */
	public static void toRowHandler(XReadableModel model, XReadableObject object,
			XReadableField field, IRowHandler rowHandler) throws IllegalStateException, IOException {
		String rowName = field.getAddress().toString();
		Map<String, String> map = new HashMap<String, String>();
		map.put("modelRev", "" + model.getRevisionNumber());
		map.put("objectRev", "" + object.getRevisionNumber());
		map.put("fieldRev", "" + field.getRevisionNumber());
		if (!field.isEmpty()) {
			XValue value = field.getValue();
			map.put("value", value == null ? "null" : CsvValueReader.toString(value));
			map.put("valueType", value == null ? "null" : value.getType().name());
		}
		IReadableRow row = new SingleRow(rowName, map);
		rowHandler.handleRow(rowName, row);
	}

	/**
	 * @param r
	 *            expect a valid, complete CSV data stream and add it to the
	 *            given repository
	 * @param repository
	 *            should be empty
	 * @throws IOException
	 *             from underlying reader
	 */
	public static void toRepository(Reader r, XRevWritableRepository repository) throws IOException {
		CsvReader csvReader = new CsvReader(r, -1);
		toRepository(csvReader, repository);
	}

	public static void toRepository(CsvReader csvReader, XRevWritableRepository repository)
			throws IOException {
		// read headers, ignore them.
		csvReader.readHeaders();

		IReadableRow row = csvReader.readDataRow();
		while (row != null) {
			addToRepository(row, repository);
			row = csvReader.readDataRow();
		}
	}

	public static void addToRepository(IReadableRow row, XRevWritableRepository repository) {
		String key = row.getKey();
		XAddress address = X.getIDProvider().fromAddress(key);

		if (address.getAddressedType() == XType.XREPOSITORY) {
			throw new IllegalStateException("Repositories are not stored in CSV as a row");
		}
		long modelRev = row.getValueAsLong("modelRev");
		XRevWritableModel model = repository.createModel(address.getModel());
		model.setRevisionNumber(modelRev);
		if (address.getAddressedType() == XType.XOBJECT
				|| address.getAddressedType() == XType.XFIELD) {
			long objectRev = row.getValueAsLong("objectRev");
			XRevWritableObject object = model.createObject(address.getObject());
			object.setRevisionNumber(objectRev);
			if (address.getAddressedType() == XType.XFIELD) {
				long fieldRev = row.getValueAsLong("fieldRev");
				XRevWritableField field = object.createField(address.getField());
				field.setRevisionNumber(fieldRev);
				// === content
				XValue xvalue = null;
				// might be null
				String valueString = row.getValue("value");
				if (valueString != null && !valueString.equals("null")) {
					// might be null
					String valueTypeString = row.getValue("valueType");
					ValueType valueType = ValueType.valueOf(valueTypeString);
					xvalue = CsvValueReader.parseValue(valueString, valueType);
				}
				field.setValue(xvalue);
			}
		}

	}

	public static void main(String[] args) throws IllegalStateException, IOException {
		XRepository repository = new MemoryRepository(XX.toId("repoactor"), "huhu",
				XX.toId("repoId1"));
		DemoModelUtil.addPhonebookModel(repository);
		StringWriter sw = new StringWriter();
		IRowHandler csvHandler = new CsvRowHandler(sw);
		toRowHandler(repository, csvHandler);
		sw.flush();
		String s = sw.getBuffer().toString();
		System.out.println(s);
		// parse
		StringReader reader = new StringReader(s);
		SimpleRepository repository2 = new SimpleRepository(X.getIDProvider().fromComponents(
				XX.toId("repoId2"), null, null, null));
		toRepository(reader, repository2);

		System.out.println(SimpleSyntaxUtilsTest.toXml(repository2
				.getModel(DemoModelUtil.PHONEBOOK_ID)));

		for (XId modelId : repository2) {
			for (XId oid : repository2.getModel(modelId)) {
				for (XId fid : repository2.getModel(modelId).getObject(oid)) {
					XRevWritableField field = repository2.getModel(modelId).getObject(oid)
							.getField(fid);
					System.out.println(modelId + "-" + oid + "-" + fid + ": " + field.getValue()
							+ " [" + field.getRevisionNumber() + "]");
				}
			}
		}

	}
}
