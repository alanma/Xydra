package org.xydra.core.serialize.rdf;

import org.xydra.base.XAddress;
import org.xydra.base.minio.MiniWriter;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XValue;
import org.xydra.sharedutils.XyAssert;


public class NTriplesWriter {
	
	public static final String FILENAME_EXTENSION = ".nt";
	
	/**
	 * @return the MIME content type of the produced output.
	 */
	public static String getContentType() {
		return "text/plain";
	}
	
	private String addressPrefix;
	
	public NTriplesWriter(MiniWriter writer, String addressPrefix) {
		this.writer = writer;
		this.addressPrefix = addressPrefix;
	}
	
	private MiniWriter writer;
	
	public void comment(String comment) {
		this.writer.write("# " + comment + "\n");
	}
	
	public void triple(XAddress s, XAddress p, XAddress o) {
		this.writer.write(toUri(s) + " " + toUri(p) + " " + toUri(o) + ".\n");
	}
	
	private String toUri(XAddress a) {
		return "<" + this.addressPrefix + a.toURI() + ">";
	}
	
	public void triple(XAddress s, XAddress p, XValue o) {
		if(resultsInJustOneTriple(o)) {
			this.writer.write(toUri(s) + " " + toUri(p) + " " + toLiteral(o) + ".\n");
		} else {
			// TODO ...
		}
	}
	
	private static boolean resultsInJustOneTriple(XValue v) {
		return v.getType().isSingle() || v.getType() == ValueType.Binary;
	}
	
	public static String ntriplesEncode(String s) {
		String enc = s.replace("\\", "\\\\");
		enc = enc.replace("\"", "\\\"");
		enc = enc.replace("\n", "\\n");
		enc = enc.replace("\r", "\\r");
		enc = enc.replace("\t", "\\t");
		return "\"" + enc + "\"";
		
	}
	
	private static String toLiteral(XValue v) {
		XyAssert.xyAssert(resultsInJustOneTriple(v));
		switch(v.getType()) {
		case Address:
		case Boolean:
		case Binary:
		case Double:
		case Id:
		case Integer:
		case Long:
		case String:
		default:
			throw new AssertionError("");
		}
	}
	
	public void flush() {
		this.writer.flush();
	}
	
}
