package org.xydra.core.serialize.json;

import org.xydra.base.minio.MiniBufferedReader;
import org.xydra.base.minio.MiniIOException;
import org.xydra.base.minio.MiniReader;
import org.xydra.base.minio.MiniStringReader;


/*
 * Copyright (c) 2002 JSON.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * The Software shall be used for Good, not Evil.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it.
 *
 * @author JSON.org
 * @version 2008-09-18
 */
public class JsonParserSAJ {

    private int index;
    private MiniReader reader;
    private char lastChar;
    private boolean useLastChar;
    private final SAJ saj;

    /**
     * Construct a JSONTokener from a string.
     *
     * @param saj A {@link SAJ} reader.
     */
    public JsonParserSAJ(final SAJ saj) {
        this.useLastChar = false;
        this.index = 0;
        this.saj = saj;
    }

    public void parse(final MiniReader reader) throws JSONException {
        this.reader = reader.markSupported() ? reader : new MiniBufferedReader(reader);
        parseValue();
    }

    /**
     * Construct a JSONTokener from a string.
     *
     * @param s A source string.
     * @throws JSONException if the string is invalif JSON
     */
    public void parse(final String s) throws JSONException {
        parse(new MiniStringReader(s));
    }

    /**
     * Back up one character. This provides a sort of lookahead capability, so
     * that you can test for a digit or letter before attempting to parse the
     * next number or identifier.
     *
     * @throws JSONException
     */
    public void back() throws JSONException {
        if(this.useLastChar || this.index <= 0) {
            throw new JSONException("Stepping back two steps is not supported");
        }
        this.index -= 1;
        this.useLastChar = true;
    }

    /**
     * Get the hex value of a character (base16).
     *
     * @param c A character between '0' and '9' or between 'A' and 'F' or
     *            between 'a' and 'f'.
     * @return An int between 0 and 15, or -1 if c was not a hex digit.
     */
    public static int dehexchar(final char c) {
        if(c >= '0' && c <= '9') {
            return c - '0';
        }
        if(c >= 'A' && c <= 'F') {
            return c - ('A' - 10);
        }
        if(c >= 'a' && c <= 'f') {
            return c - ('a' - 10);
        }
        return -1;
    }

    /**
     * Determine if the source string still contains characters that next() can
     * consume.
     *
     * @return true if not yet at the end of the source.
     * @throws JSONException
     */
    public boolean more() throws JSONException {
        final char nextChar = next();
        if(nextChar == 0) {
            return false;
        }
        back();
        return true;
    }

    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     * @throws JSONException if there is an underlying IO error
     */
    public char next() throws JSONException {
        if(this.useLastChar) {
            this.useLastChar = false;
            if(this.lastChar != 0) {
                this.index += 1;
            }
            return this.lastChar;
        }
        int c;
        try {
            c = this.reader.read();
        } catch(final MiniIOException exc) {
            throw new JSONException(exc);
        }

        if(c <= 0) { // End of stream
            this.lastChar = 0;
            return 0;
        }
        this.index += 1;
        this.lastChar = (char)c;
        return this.lastChar;
    }

    /**
     * Consume the next character, and check that it matches a specified
     * character.
     *
     * @param c The character to match.
     * @return The character.
     * @throws JSONException if the character does not match.
     */
    public char next(final char c) throws JSONException {
        final char n = next();
        if(n != c) {
            throw syntaxError("Expected '" + c + "' and instead saw '" + n + "'");
        }
        return n;
    }

    /**
     * Get the next n characters.
     *
     * @param n The number of characters to take.
     * @return A string of n characters.
     * @throws JSONException Substring bounds error if there are not n
     *             characters remaining in the source string.
     */
    public String next(final int n) throws JSONException {
        if(n == 0) {
            return "";
        }

        final char[] buffer = new char[n];
        int pos = 0;

        if(this.useLastChar) {
            this.useLastChar = false;
            buffer[0] = this.lastChar;
            pos = 1;
        }

        try {
            int len;
            while(pos < n && (len = this.reader.read(buffer, pos, n - pos)) != -1) {
                pos += len;
            }
        } catch(final MiniIOException exc) {
            throw new JSONException(exc);
        }
        this.index += pos;

        if(pos < n) {
            throw syntaxError("Substring bounds error");
        }

        this.lastChar = buffer[n - 1];
        return new String(buffer);
    }

    /**
     * Get the next char in the string, skipping whitespace.
     *
     * @throws JSONException if there is an underlying IO error
     * @return A character, or 0 if there are no more characters.
     */
    public char nextClean() throws JSONException {
        for(;;) {
            final char c = next();
            if(c == 0 || c > ' ') {
                return c;
            }
        }
    }

    /**
     * Return the characters up to the next close quote character. Backslash
     * processing is done. The formal JSON format does not allow strings in
     * single quotes, but an implementation is allowed to accept them.
     *
     * @param quote The quoting character, either <code>"</code>
     *            &nbsp;<small>(double quote)</small> or <code>'</code>
     *            &nbsp;<small>(single quote)</small>.
     * @return A String.
     * @throws JSONException Unterminated string.
     */
    public String nextString(final char quote) throws JSONException {
        char c;
        final StringBuffer sb = new StringBuffer();
        for(;;) {
            c = next();
            switch(c) {
            case 0:
            case '\n':
            case '\r':
                /*
                 * FIXME This hack tells the JsonParser to read newlines as if
                 * they were encoded newlines. All newlines written by the
                 * encoder from now on are properly encoded. Reading
                 * pretty-printed valid JSON from other sources will fail here.
                 * There is currently production data with the wrong encoding
                 * and it would be too costly to re-encode all data. Therefore
                 * this fix must live until the last such production data has
                 * been migrated.
                 */

                // Was: throw syntaxError("Unterminated string");

                // Is: just pretend we read a properly escaped \n
                sb.append("\n");
                break;
            case '\\':
                c = next();
                switch(c) {
                case 'b':
                    sb.append('\b');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 'u':
                    sb.append((char)ParseNumber.parseInt(next(4), 16));
                    break;
                case '"':
                case '\'':
                case '\\':
                case '/':
                    sb.append(c);
                    break;
                default:
                    throw syntaxError("Illegal escape.");
                }
                break;
            default:
                if(c == quote) {
                    return sb.toString();
                }
                sb.append(c);
            }
        }
    }

    /**
     * Get the text up but not including the specified character or the end of
     * line, whichever comes first.
     *
     * @param d A delimiter character.
     * @return A string.
     * @throws JSONException
     */
    public String nextTo(final char d) throws JSONException {
        final StringBuffer sb = new StringBuffer();
        for(;;) {
            final char c = next();
            if(c == d || c == 0 || c == '\n' || c == '\r') {
                if(c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }

    /**
     * Get the text up but not including one of the specified delimiter
     * characters or the end of line, whichever comes first.
     *
     * @param delimiters A set of delimiter characters.
     * @return A string, trimmed.
     * @throws JSONException
     */
    public String nextTo(final String delimiters) throws JSONException {
        char c;
        final StringBuffer sb = new StringBuffer();
        for(;;) {
            c = next();
            if(delimiters.indexOf(c) >= 0 || c == 0 || c == '\n' || c == '\r') {
                if(c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }

    /**
     * @throws JSONException If there is a syntax error.
     */
    public void parseArray() throws JSONException {
        this.saj.arrayStart();
        char c = nextClean();
        char q;
        if(c == '[') {
            q = ']';
        } else if(c == '(') {
            q = ')';
        } else {
            throw syntaxError("A JSONArray text must start with '['");
        }
        if(nextClean() == ']') {
            this.saj.arrayEnd();
            return;
        }
        back();
        for(;;) {
            if(nextClean() == ',') {
                back();
                this.saj.onNull();
            } else {
                back();
                parseValue();
            }
            c = nextClean();
            switch(c) {
            case ';':
            case ',':
                if(nextClean() == ']') {
                    return;
                }
                back();
                break;
            case ']':
            case ')':
                if(q != c) {
                    throw syntaxError("Expected a '" + new Character(q) + "'");
                }
                this.saj.arrayEnd();
                return;
            default:
                throw syntaxError("Expected a ',' or ']'");
            }
        }
    }

    public void parseObject() throws JSONException {
        this.saj.objectStart();
        char c;

        if(nextClean() != '{') {
            throw syntaxError("A JSONObject text must begin with '{'");
        }
        for(;;) {
            c = nextClean();
            switch(c) {
            case 0:
                throw syntaxError("A JSONObject text must end with '}'");
            case '}':
                this.saj.objectEnd();
                return;
            default:
                back();
                parseKey();
            }

            /*
             * The key is followed by ':'. We will also tolerate '=' or '=>'.
             */

            c = nextClean();
            if(c == '=') {
                if(next() != '>') {
                    back();
                }
            } else if(c != ':') {
                throw syntaxError("Expected a ':' after a key");
            }
            parseValue();

            /*
             * Pairs are separated by ','. We will also tolerate ';'.
             */

            switch(nextClean()) {
            case ';':
            case ',':
                if(nextClean() == '}') {
                    this.saj.objectEnd();
                    return;
                }
                back();
                break;
            case '}':
                this.saj.objectEnd();
                return;
            default:
                throw syntaxError("Expected a ',' or '}'");
            }
        }
    }

    private void parseKey() throws JSONException {
        final char c = nextClean();
        switch(c) {
        case '"':
        case '\'':
            final String key = nextString(c);
            this.saj.onKey(key);
            break;
        case '{':
        case '[':
        case '(':
            syntaxError("Character '" + c + "' not allowed in key names.");
        }
    }

    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, Long, or String, or null.
     *
     * @throws JSONException If syntax error.
     */
    public void parseValue() throws JSONException {
        char c = nextClean();
        String s;

        switch(c) {
        case '"':
        case '\'':
            this.saj.onString(nextString(c));
            return;
        case '{':
            back();
            parseObject();
            return;
        case '[':
        case '(':
            back();
            parseArray();
            return;
        }

        /*
         * Handle unquoted text. This could be the values true, false, or null,
         * or it can be a number. An implementation (such as this one) is
         * allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        final StringBuffer sb = new StringBuffer();
        while(c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = next();
        }
        back();

        s = sb.toString().trim();
        if(s.equals("")) {
            throw syntaxError("Missing value");
        }
        parsePrimitiveValue(s);
    }

    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string.
     *
     * @param s A String.
     * @throws JSONException if the string is invalid JSON
     */
    public void parsePrimitiveValue(final String s) throws JSONException {
        if(s.equals("")) {
            this.saj.onString("");
        } else if(s.equalsIgnoreCase("true")) {
            this.saj.onBoolean(true);
        } else if(s.equalsIgnoreCase("false")) {
            this.saj.onBoolean(false);
        } else if(s.equalsIgnoreCase("null")) {
            this.saj.onNull();
        } else {

            /*
             * If it might be a number, try converting it. We support the 0- and
             * 0x- conventions. If a number cannot be produced, then the value
             * will just be a string. Note that the 0-, 0x-, plus, and implied
             * string conventions are non-standard. A JSON parser is free to
             * accept non-JSON forms as long as it accepts all correct JSON
             * forms.
             */

            final char b = s.charAt(0);
            if(b >= '0' && b <= '9' || b == '.' || b == '-' || b == '+') {
                if(b == '0') {
                    if(s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
                        try {
                            final int i = ParseNumber.parseInt(s.substring(2), 16);
                            this.saj.onInteger(i);
                            return;
                        } catch(final Exception e) {
                            /* Ignore the error */
                        }
                    } else {
                        try {
                            final int i = ParseNumber.parseInt(s, 8);
                            this.saj.onInteger(i);
                            return;
                        } catch(final Exception e) {
                            /* Ignore the error */
                        }
                    }
                }
                try {
                    if(s.indexOf('.') > -1 || s.indexOf('e') > -1 || s.indexOf('E') > -1) {
                        final double d = Double.valueOf(s);
                        this.saj.onDouble(d);
                        return;
                    } else {
                        final Long myLong = new Long(s);
                        if(myLong.longValue() == myLong.intValue()) {
                            this.saj.onInteger(myLong.intValue());
                            return;
                        } else {
                            this.saj.onLong(myLong.longValue());
                            return;
                        }
                    }
                } catch(final Exception f) {
                    /* Ignore the error */
                }
            }
            this.saj.onString(s);
        }
    }

    /**
     * Skip characters until the next character is the requested character. If
     * the requested character is not found, no characters are skipped.
     *
     * @param to A character to skip to.
     * @return The requested character, or zero if the requested character is
     *         not found.
     * @throws JSONException
     */
    public char skipTo(final char to) throws JSONException {
        char c;
        try {
            final int startIndex = this.index;
            this.reader.mark(Integer.MAX_VALUE);
            do {
                c = next();
                if(c == 0) {
                    this.reader.reset();
                    this.index = startIndex;
                    return c;
                }
            } while(c != to);
        } catch(final MiniIOException exc) {
            throw new JSONException(exc);
        }

        back();
        return c;
    }

    /**
     * Make a JSONException to signal a syntax error.
     *
     * @param message The error message.
     * @return A JSONException object, suitable for throwing
     */
    public JSONException syntaxError(final String message) {
        return new JSONException(message + toString());
    }

    /**
     * Make a printable string of this JSONTokener.
     *
     * @return " at character [this.index]"
     */
    @Override
    public String toString() {
        return " at character " + this.index;
    }
}
