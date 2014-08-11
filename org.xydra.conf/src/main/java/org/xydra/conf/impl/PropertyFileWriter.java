package org.xydra.conf.impl;

import java.io.IOException;
import java.io.Writer;


/**
 * Writes java property files with comments
 * 
 * @author xamde
 * 
 */
public class PropertyFileWriter {
	
	private Writer w;
	
	public PropertyFileWriter(Writer w) {
		this.w = w;
	}
	
	/**
	 * @param key
	 * @param value @CanBeNull
	 * @throws IOException
	 */
	public void keyValue(String key, String value) throws IOException {
		assert key != null;
		this.w.write(escape(key));
		this.w.write("=");
		this.w.write(value == null ? "" : escape(value));
		this.w.write("\n");
	}
	
	static String escape(String raw) {
		assert raw != null;
		String s = raw;
		s = s.replace("\\", "\\\\");
		s = s.replace("\n", "\\n");
		s = s.replace(":", "\\:");
		s = s.replace("=", "\\=");
		s = s.replace(" ", "\\ ");
		
		// FIXME make legal
		StringBuffer outBuffer = new StringBuffer(1000);
		char thisChar = raw.toCharArray()[0];
		if(((thisChar < 0x0020) || (thisChar > 0x007e))) {
			raw = "\\u";
			
			outBuffer.append(toHex((thisChar >> 12) & 0xF));
			outBuffer.append(toHex((thisChar >> 8) & 0xF));
			outBuffer.append(toHex((thisChar >> 4) & 0xF));
			outBuffer.append(toHex(thisChar & 0xF));
		}
		return raw + outBuffer.toString();
	}
	
	// FIXME make legal
	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}
	
	// FIXME make legal
	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
	        'B', 'C', 'D', 'E', 'F' };
	
	public void comment(String comment) throws IOException {
		assert comment != null;
		this.w.write("# " + comment + "\n");
	}
	
	/**
	 * @param raw a string which may contain '\\', '\n', '\t', '\ r', or Unicode
	 *            '\ uXXXX' where XXXX is hex. The space is not there.
	 * @return a string in which java and unicode escapes have been replaced
	 *         with the correct unicode codepoint
	 */
	public static String materializeEscapes(String raw) {
		assert raw != null;
		String s = raw;
		s = s.replace("\\n", "\n");
		s = s.replace("\\r", "\r");
		s = s.replace("\\t", "\t");
		s = s.replace("\\\\", "\\");
		
		// unicode
		int i = s.indexOf("\\u", 0);
		// IMPROVE by iterating over content only once
		while(i >= 0) {
			// try to get hex chars
			String hex4 = s.substring(i + 2, i + 6);
			if(hex4.matches("[a-fA-F0-9]{4}")) {
				int codepointNumber = Integer.parseInt(hex4, 16);
				StringBuilder concat = new StringBuilder();
				concat.append(s.substring(0, i));
				concat.appendCodePoint(codepointNumber);
				concat.append(s.substring(i + 6));
				s = concat.toString();
			} else {
				// there was just any string with ...'single backslash followed
				// by character u'... in it
				i += 2;
			}
			
			i = s.indexOf("\\u", i);
		}
		
		return s;
	}
	
}
