package org.xydra.core.serialize;

/**
 * Handles line-break problems across platforms.
 *
 * Tags for fulltext search: newline, linebreak,
 *
 * Sources: http://en.wikipedia.org/wiki/Newline
 *
 * Common usages:
 *
 * Unix, Mac: LF
 *
 * Windows, HTTP: CR LF
 *
 * Old Mac: CR
 *
 * @author xamde
 */
public class LineBreaks {

    /*
     * The Unicode standard defines a number of characters that conforming
     * applications should recognise as line terminators
     */

    /** Line Feed, U+000A = \n */
    public static final String LF = "\n";

    /** Vertical Tab, U+000B */
    public static final String VT = "\u000B";

    /** Form Feed, U+000C */
    public static final String FF = "\u000C";

    /** Carriage Return, U+000D = \r */
    public static final String CR = "\r";

    /** CR (U+000D) followed by LF (U+000A) */
    public static final String CRLF = CR + LF;

    /** Next Line, U+0085 */
    public static final String NEL = "\u0085";

    /** Line Separator, U+2028 */
    public static final String LS = "\u2028";

    /** Paragraph Separator, U+2029 */
    public static final String PS = "\u2029";

    /**
     * @param in
     * @return a string in which all line breaks have been replaced with the
     *         standard java line break \n
     */
    public static String normalizeLinebreaks(final String in) {
        String result = in;
        result = result.replace(LF, "\n");
        result = result.replace(VT, "\n");
        result = result.replace(FF, "\n");
        // important: first CRLF, then CR
        result = result.replace(CRLF, "\n");
        result = result.replace(CR, "\n");
        result = result.replace(NEL, "\n");
        result = result.replace(LS, "\n");
        result = result.replace(PS, "\n");
        return result;
    }

}
