package org.xydra.csv.impl.memory;

/**
 * @author xamde
 * 
 *         Runs in GWT
 */
public class CsvCodec {
	
	public static String excelDecode(String encoded) {
		if(encoded.equals("") || encoded.equals("\"\"")) {
			return null;
		}
		
		String decoded = encoded;
		if(encoded.startsWith("\"") && encoded.endsWith("\"")) {
			decoded = decoded.substring(1, encoded.length() - 1);
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
	 * @param value to be encoded
	 */
	public static String excelEncode(String value) {
		if(value == null) {
			return "\"\"";
		}
		
		String escaped;
		if(value.contains("\"")) {
			escaped = value.replace("\"", "\"\"");
		} else {
			escaped = value;
		}
		
		// proprietary multi-line-handling
		escaped = escaped.replace("\n", "§N");
		escaped = escaped.replace("\r", "§R");
		
		return "\"" + escaped + "\"";
	}
	
}
