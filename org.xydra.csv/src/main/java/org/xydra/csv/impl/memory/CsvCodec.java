package org.xydra.csv.impl.memory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xamde
 * 
 *         Runs in GWT
 */
public class CsvCodec {

	public static final byte[] BOM_UTF8 = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

	public static final byte[] BOM_UTF16LE = new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0x00,
			(byte) 0x00 };

	public static final String CELL_DELIMITER = ";";

	/**
	 * Decode a single cell
	 * 
	 * @param encoded
	 * @return the pure value string
	 */
	public static String excelDecode(String encoded) {
		if (encoded.equals("") || encoded.equals("\"\"") || encoded.equals("=\"\"")) {
			return null;
		}

		String decoded = encoded;
		/* Excel formula mode */
		if (decoded.startsWith("=")) {
			decoded = decoded.substring(1);
		}
		if (decoded.startsWith("\"") && decoded.endsWith("\"")) {
			decoded = decoded.substring(1, decoded.length() - 1);
		}
		// unescape
		decoded = decoded.replace("\"\"", "\"");

		return decoded;
	}

	/**
	 * Use safe CSV encoding, that is, surround by " and escape all quotes.
	 * 
	 * Enclosing all fields in quotes is not strictly required, but lets Excel
	 * open an CSV file with default settings without mangling e.g. dates.
	 * 
	 * @param value
	 *            to be encoded
	 * @return a string that excel accepts
	 */
	public static String excelEncode(String value) {
		if (value == null || value.equals("null")) {
			return "\"\"";
		}

		String escaped;
		if (value.contains("\"")) {
			escaped = value.replace("\"", "\"\"");
		} else {
			escaped = value;
		}

		// proprietary multi-line-handling
		escaped = escaped.replace("\n", "§N");
		escaped = escaped.replace("\r", "§R");

		/* Adding the '=' sign makes it more robust to open on Mac Office */
		return "\"" + escaped + "\"";
	}

	public static String[] splitAtUnquotedSemicolon(String line) {
		boolean inQuote = false;
		int index = 0;
		List<String> result = new LinkedList<String>();
		StringBuffer currentString = new StringBuffer();
		while (index < line.length()) {
			char c = line.charAt(index);
			switch (c) {
			case '\"': {
				if (inQuote) {
					inQuote = false;
				} else {
					inQuote = true;
				}
				currentString.append(c);
			}
				break;
			case ';':
			case '\t': {
				if (inQuote) {
					// just copy over
					currentString.append(c);
				} else {
					// terminate current string
					result.add(currentString.toString());
					currentString = new StringBuffer();
				}
			}
				break;
			default: {
				// just copy over
				currentString.append(c);
			}
				break;
			}
			index++;
		}
		result.add(currentString.toString());
		return result.toArray(new String[result.size()]);
	}

	/**
	 * @param line
	 *            can be null
	 * @return the given line as excel-decoded list of Strings
	 */
	public static List<String> excelDecodeRow(String line) {
		if (line == null) {
			return Collections.emptyList();
		}
		assert line != null;
		List<String> row = new LinkedList<String>();
		String[] datas = CsvCodec.splitAtUnquotedSemicolon(line);
		for (int i = 0; i < datas.length; i++) {
			String value = CsvCodec.excelDecode(datas[i]);
			row.add(value);
		}
		return row;
	}

	public static String excelEncodeRow(List<String> values) {
		if (values == null) {
			return "";
		}
		assert values != null;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.size(); i++) {
			sb.append(CsvCodec.excelEncode(values.get(i)));
			if (i + 1 < values.size()) {
				sb.append(CELL_DELIMITER);
			}
		}
		return sb.toString();
	}

	// public static void main(String[] args) {
	// System.out.println(excelDecodeRow(excelEncodeRow(Arrays.asList("\"a\"",
	// null, "c"))));
	// }
}
