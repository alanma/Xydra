package org.xydra.core.serialize.rdf;

import org.xydra.base.XAddress;
import org.xydra.base.minio.MiniWriter;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XValue;
import org.xydra.core.serialize.Base64;
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
    
    /**
     * @param writer
     * @param addressPrefix to turn relative paths into absolute URIs
     */
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
            if(o.getType().isSortedCollection()) {
                // TODO RDF List
            } else {
                // TODO multiple triples
            }
        }
    }
    
    public void triple(XAddress s, String p, XValue o) {
        if(resultsInJustOneTriple(o)) {
            this.writer.write(toUri(s) + " <" + p + "> " + toLiteral(o) + ".\n");
        } else {
            // TODO ...
        }
    }
    
    public void triple(XAddress s, XAddress p, String plainLiteral) {
        this.writer.write(toUri(s) + " " + toUri(p) + " " + "\"" + plainLiteral + "\"" + ".\n");
    }
    
    public void triple(XAddress subjectAddress, String propertyUri, String plainLiteral) {
        this.writer.write(toUri(subjectAddress) + " <" + propertyUri + "> " + "\"" + plainLiteral
                + "\"" + ".\n");
    }
    
    private static boolean resultsInJustOneTriple(XValue v) {
        assert v != null;
        return v.getType().isSingle() || v.getType() == ValueType.Binary;
    }
    
    /**
     * @param literal
     * @return the given literal encoded to be written in an NT file/stream,
     *         i.e. 'x' becomes '"x"' and special chars are encoded
     */
    public static String ntriplesEncode(String literal) {
        String enc = literal.replace("\\", "\\\\");
        enc = enc.replace("\"", "\\\"");
        enc = enc.replace("\n", "\\n");
        enc = enc.replace("\r", "\\r");
        enc = enc.replace("\t", "\\t");
        return "\"" + enc + "\"";
        
    }
    
    private static String toLiteral(XValue v) {
        XyAssert.xyAssert(resultsInJustOneTriple(v));
        switch(v.getType()) {
        case String:
            return "\"" + v.toString() + "\"";
        case Address:
        case Boolean:
        case Binary:
            String base64 = Base64.encode(((XBinaryValue)v).contents(), true);
            return "\"" + base64 + "\"" + "^^<http://www.w3.org/2001/XMLSchema#base64Binary>";
        case Double:
        case Id:
        case Integer:
        case Long:
            // FIXME do
            return "\"" + v.toString() + "\"" + "^^<http://www.w3.org/2001/XMLSchema#integer>";
        default:
            throw new AssertionError("Unhandled type '" + v.getType() + "'");
        }
    }
    
    public void flush() {
        this.writer.flush();
    }
    
}
