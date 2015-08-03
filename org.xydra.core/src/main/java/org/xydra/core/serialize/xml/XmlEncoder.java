/*
 * Created on 16.05.2005
 */
package org.xydra.core.serialize.xml;

import java.io.IOException;
import java.io.StringReader;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;


/**
 * Helper class for XML encoding and decoding of XML escaped characters
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class XmlEncoder {

    /**
     * Standard XML header with UTF-8 encoding.
     */
    public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    private static final String ENC_AMP = "&amp;";
    private static final String ENC_LT = "&lt;";
    private static final String ENC_GT = "&gt;";
    private static final String ENC_APOS = "&apos;";
    private static final String ENC_QUOT = "&quot;";

    public static String decode(final String in) {
        String result = in;
        result = result.replace(ENC_AMP, "&");
        result = result.replace(ENC_LT, "<");
        result = result.replace(ENC_GT, ">");
        result = result.replace(ENC_APOS, "'");
        result = result.replace(ENC_QUOT, "\"");
        return result;

    }

    public static String encode(final String in) {
        if(in == null) {
            return null;
        }

        final StringBuilder b = new StringBuilder((int)(in.length() * 1.2));
        final StringReader r = new StringReader(in);
        int c;
        try {
            c = r.read();
            while(c >= 0) {
                switch(c) {
                case '&':
                    b.append(ENC_AMP);
                    break;
                case '<':
                    b.append(ENC_LT);
                    break;
                case '>':
                    b.append(ENC_GT);
                    break;
                case '\'':
                    b.append(ENC_APOS);
                    break;
                case '"':
                    b.append(ENC_QUOT);
                    break;
                default:
                    b.appendCodePoint(c);
                }
                c = r.read();
            }
        } catch(final IOException e) {
            throw new RuntimeException(e);
        }
        return b.toString();

        // String result = in;
        // result = result.replace("&", "&amp;");
        // result = result.replace("<", "&lt;");
        // result = result.replace(">", "&gt;");
        // result = result.replace("'", "&apos;");
        // result = result.replace("\"", "&quot;");
        // return result;
    }

    public static final String XMAP_ELEMENT = "xmap";
    public static final String XARRAY_ELEMENT = "xarray";
    public static final String XNULL_ELEMENT = "xnull";
    public static final String XVALUE_ELEMENT = "xvalue";
    public static final String NULL_ATTRIBUTE = "isNull";
    public static final String NULL_VALUE = "true";
    public static final String NULL_CONTENT_ATTRIBUTE = "nullContent";
    public static final String NULL_CONTENT_VALUE = "true";

}
