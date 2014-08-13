package org.xydra.conf.impl;

public class PropertyFileEscaping {
    
    /**
     * Escape the characters (written as regex char group without
     * backslash-escaping) [\\n\t\r:= ]
     * 
     * @param raw
     * @return ...
     */
    public static String escape(String raw) {
        assert raw != null;
        
        StringBuilder esc = new StringBuilder();
        int i = 0;
        while(i < raw.length()) {
            int c = raw.codePointAt(i);
            i += Character.charCount(c);
            
            switch(c) {
            case '\\':
            case ':':
            case '=':
            case ' ':
                // simple escaping
                esc.append('\\');
                esc.appendCodePoint(c);
                break;
            case '\n':
                esc.append('\\');
                esc.append('n');
                break;
            case '\t':
                esc.append('\\');
                esc.append('t');
                break;
            case '\r':
                esc.append('\\');
                esc.append('r');
                break;
            default:
                if(c < 0x0020 || c > 0x007e) {
                    // unicode
                    appendAsUnicodeEscapeSequence(esc, c);
                } else {
                    esc.appendCodePoint(c);
                }
            }
        }
        
        return esc.toString();
    }
    
    static final int UNICODE_RESERVED_START = Integer.parseInt("D800", 16);
    static final int UNICODE_RESERVED_END = Integer.parseInt("DFFF", 16);
    
    /**
     * @param b @NeverNull
     * @param codepoint must be below xD800 (55296 as int)
     */
    public static void appendAsUnicodeEscapeSequence(StringBuilder b, int codepoint) {
        /*
         * let's be clever here: encoding in range 0..255 is represented as
         * ENCODING_CHAR + 2 hex;
         * 
         * 255.. 65535 is ENCODING_CHAR + ENCODING_CHAR + 4 hex
         * 
         * range 65536..1114111 is represented as ENCODING_CHAR + ENCODING_CHAR
         * + ENCODING_CHAR + 6 hex
         */
        
        b.append("\\u");
        
        if(codepoint >= UNICODE_RESERVED_START && codepoint <= UNICODE_RESERVED_END) {
            throw new IllegalArgumentException("Codepoints in range xD800-xDFFF are reserved.");
        }
        
        if(codepoint < UNICODE_RESERVED_START) {
            // hack to get padding with leading zeroes
            b.append(Integer.toHexString(0x10000 | codepoint).substring(1).toUpperCase());
        } else {
            throw new UnsupportedOperationException(
                    "surrogate pairs not yet implemented, could not encode codePoint " + codepoint);
            // wrong b.append(Integer.toHexString(0x1000000 |
            // codepoint).substring(1));
        }
    }
    
    /**
     * @param escaped a string which may contain '\\', '\n', '\t', '\ r', or
     *            Unicode '\ uXXXX' where XXXX is hex. The space is not there.
     * @return a string in which java and unicode escapes have been replaced
     *         with the correct unicode codepoint
     */
    public static String materializeEscapes(String escaped) {
        assert escaped != null;
        
        StringBuilder unescaped = new StringBuilder();
        int i = 0;
        while(i < escaped.length()) {
            int c = escaped.codePointAt(i);
            i += Character.charCount(c);
            switch(c) {
            case '\\':
                i += materializeBackslashEscapes(escaped, i, unescaped);
                break;
            default:
                unescaped.appendCodePoint(c);
            }
        }
        
        return unescaped.toString();
    }
    
    /**
     * @param escaped
     * @param i
     * @param unescaped
     * @return number of interpreted characters
     */
    private static int materializeBackslashEscapes(String escaped, int i, StringBuilder unescaped) {
        int c = escaped.codePointAt(i);
        switch(c) {
        case '\n':
            // special java property behaviour: backslash-newline = not there
            break;
        case '\\':
        case ':':
        case '=':
        case ' ':
            // just un-escape
            unescaped.appendCodePoint(c);
            break;
        case 't':
            unescaped.append('\t');
            break;
        case 'r':
            unescaped.append('\r');
            break;
        case 'n':
            unescaped.append('\n');
            break;
        case 'u':
            return 1 + materializeUnicode(escaped, i + 1, unescaped);
        default:
            // ignore escaping and write back verbatim
            unescaped.append('\\');
            unescaped.appendCodePoint(c);
        }
        return 1;
    }
    
    /**
     * @param escaped
     * @param i
     * @param unescaped
     * @return how many chars used
     */
    private static int materializeUnicode(String escaped, int i, StringBuilder unescaped) {
        // try to get hex chars
        String hex4 = escaped.substring(i + 2, i + 6);
        // TODO optimisation potential
        if(hex4.matches("[a-fA-F0-9]{4}")) {
            int codepointNumber = Integer.parseInt(hex4, 16);
            unescaped.appendCodePoint(codepointNumber);
            return 4;
        } else {
            // there was just any string with ...'single backslash followed
            // by character u'... in it
            return 0;
        }
    }
    
}
