package org.xydra.core.serialize.rdf;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.minio.MiniStreamWriter;
import org.xydra.base.minio.MiniWriter;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.MemoryModel;


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
	
	private boolean resultsInJustOneTriple(XValue v) {
		return v.getType().isSingle() || v.getType() == ValueType.ByteList;
	}
	
	public static String ntriplesEncode(String s) {
		String enc = s.replace("\\", "\\\\");
		enc = enc.replace("\"", "\\\"");
		enc = enc.replace("\n", "\\n");
		enc = enc.replace("\r", "\\r");
		enc = enc.replace("\t", "\\t");
		return "\"" + enc + "\"";
		
	}
	
	private String toLiteral(XValue v) {
		assert resultsInJustOneTriple(v);
		switch(v.getType()) {
		case Address:
		case Boolean:
		case ByteList:
		case Double:
		case Id:
		case Integer:
		case Long:
		case String:
		default:
			throw new AssertionError("");
		}
	}
	
	public static void main(String[] args) {
		MiniWriter writer = new MiniStreamWriter(System.out);
		NTriplesWriter nt = new NTriplesWriter(writer, "http://localhost:8765/admin/rdf");
		XID actorId = XX.toId("actor");
		XModel model = new MemoryModel(actorId, "secret", XX.toId("model1"));
		XObject john = model.createObject(XX.toId("john"));
		XField phone = john.createField(XX.toId("phone"));
		phone.setValue(XV.toValue(1877));
		nt.triple(john.getAddress(), phone.getAddress(), model.getAddress());
		nt.flush();
	}
	
	public void flush() {
		this.writer.flush();
	}
	
}
