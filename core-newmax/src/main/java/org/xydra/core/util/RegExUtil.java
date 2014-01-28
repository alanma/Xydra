package org.xydra.core.util;

import java.util.regex.Pattern;

import org.xydra.annotations.RunsInGWT;


/**
 * Syntax defined in <a
 * href="http://www.w3.org/TR/xpath-functions/#regex-syntax">here</a> and most
 * is taken from <a href="http://www.w3.org/TR/xmlschema-2/#regexs">here</a>.
 * 
 * Note on ECMA script regex: Always escape '/' as '\/' (in Java: '\\/') to
 * avoid it being interpreted as a regex delimiter.
 * 
 * Take care of these characters in character classes: ']' can only be the first
 * char; '-' should be the last char; '^' may not be the first char, put in
 * middle.
 * 
 * TODO add support for string negation via negative lookaheads, see
 * http://stackoverflow.com/questions/1153856/string-negation-using-regular-
 * expressions
 * 
 */
@RunsInGWT(true)
public class RegExUtil {
    
    /**
     * Meta-characters that have specials semantics if used within regexes
     */
    public static final String METACHARS = "\\|.?*+{}()[]";
    
    /**
     * Checks for partial matches.
     * 
     * @param s string in which to search
     * @param regex regular expression
     * @param caseSensitive if false, flag "i" for case-insensitive matching is
     *            turned on and performed in a Unicode standard way.
     * @return true if a match of regex is found in s.
     */
    public static boolean isFound(String s, String regex, boolean caseSensitive) {
        boolean result = Pattern
                .compile(regex,
                        caseSensitive ? 0 : (Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE))
                .matcher(s).matches(); // we used 'find()' in earlier versions
        return result;
    }
    
    /**
     * @param raw a regular expression (syntax see class comment) , never null
     * @return an encoded regular expression
     */
    public static String regexEncode(String raw) {
        if(raw == null)
            throw new IllegalArgumentException("raw may not be null");
        
        String result = raw;
        // escape all meta characters
        for(int i = 0; i < METACHARS.length(); i++) {
            char c = METACHARS.charAt(i);
            result = result.replace("" + c, "" + '\\' + c);
        }
        return result;
    }
    
    /**
     * Character class for UniCode punctuation (all planes with 'punctuation' in
     * their name)
     */
    public static final String UNIVERSAL_PUNCTUATION = "[\\u2000-\\u206F\\u2070-\\u209F\\u20A0-\\u20CF\\u2E00-\\u2E7F\\u3000-\\u303F]";
    
    /** ECMA: Within ASCII: '[a-z]' */
    public static final String JAVA_LOWER = "\\p{Lower}";
    
    /** Java+ECMA, lower case ASCII letters */
    public static final String LOWER = "[a-z]";
    
    /** ECMA: Within ASCII: '[A-Z]' */
    public static final String JAVA_UPPER = "\\p{Upper}";
    
    /** Java+ECMA, upper case ASCII letters */
    public static final String UPPER = "[A-Z]";
    
    /** ECMA: '[0-9]' == '\d' */
    public static final String JAVA_DIGIT = "\\p{Digit}";
    
    /** Java+ECMA, digit */
    public static final String DIGIT = "[0-9]";
    
    public static final String HEXDIGIT = "[0-9a-fA-F]";
    
    /** ECMA: Within ASCII:'[a-zA-Z0-9]' */
    public static final String JAVA_ALNUM = "\\p{Alnum}";
    
    public static final String DOT = "\\.";
    
    /** Java+ECMA, alphanumeric (upper case, lower case or digit) */
    public static final String ALNUM = "[a-zA-Z0-9]";
    
    /** One of <tt>!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~<tt> */
    public static final String JAVA_PUNCT = "\\p{Punct}";
    
    /**
     * Java + ECMA, same as {@link #JAVA_PUNCT}
     */
    public static final String PUNCT = "[-}!\"#$%&'()*+,.\\/:;<=>?@\\[\\\\\\]^_`|{~]";
    
    /** ECMA: '[ \t]' */
    public static final String JAVA_BLANK = "\\p{Blank}";
    
    /*
     * There are a number of additional predefined classes that you will find
     * useful. Taken from: http://javascript.about.com/library/blre10.htm
     */
    
    /** any non-digit ~ equivalent to [^0-9] */
    public static final String ECMA_NON_DIGIT = "\\D";
    
    /**
     * \w any normal word character (letters numbers and underscore) ~
     * equivalent to [a-zA-Z0-9_]
     */
    public static final String ECMA_NORMAL_WORD = "\\w";
    
    /**
     * \W any non- word character (anything except letters, numbers, and
     * underscore)
     */
    public static final String ECMA_NON_WORD = "\\W";
    
    /**
     * \s any whitespace character (spaces, tabs, linefeeds, carriage returns,
     * and nulls)
     */
    public static final String ECMA_WHITESPACE = "\\s";
    
    /** \S anything except whitespace characters */
    public static final String ECMA_NON_WHITE = "\\S";
    
    /**
     * All word/text-like characters except ASCII
     */
    public static final String UNICODE_RANGE_ALL_EXTENDED_LATIN = "\\u0080-\\u024F"
            + "\\u1E00-\\u1EFF" + "\\u2C60-\\u2C7F" + "\\uA720-\\uA7FF";
    
    public static final String UNICODE_RANGE_NON_LATIN_WORDS = "\\u0370-\\u1FFF"
    // Katakana+Hiragana
            + "\\u3040-\\u30FF";
    
    public static final String UNICODE_RANGE_NONASCII_ALPHA = UNICODE_RANGE_ALL_EXTENDED_LATIN
            + UNICODE_RANGE_NON_LATIN_WORDS;
    
    public static final String UNICODE_NONASCII_ALPHA = "[" + UNICODE_RANGE_NONASCII_ALPHA + "]";
    
    public static final String UNICODE_RANGE_ALPHANUM = "a-zA-Z0-9" + UNICODE_RANGE_NONASCII_ALPHA;
    
    public static final String UNICODE_ALPHANUM = "[" + UNICODE_RANGE_ALPHANUM + "]";
    
    public static final String UNICODE_WHITESPACE_CHARS =
    
    "\\u0009" // CHARACTER TABULATION
            + "\\u000A" // LINE FEED (LF)
            + "\\u000B" // LINE TABULATION
            + "\\u000C" // FORM FEED (FF)
            + "\\u000D" // CARRIAGE RETURN (CR)
            + "\\u0020" // SPACE
            + "\\u0085" // NEXT LINE (NEL)
            + "\\u00A0" // NO-BREAK SPACE
            + "\\u1680" // OGHAM SPACE MARK
            + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
            + "\\u2000" // EN QUAD
            + "\\u2001" // EM QUAD
            + "\\u2002" // EN SPACE
            + "\\u2003" // EM SPACE
            + "\\u2004" // THREE-PER-EM SPACE
            + "\\u2005" // FOUR-PER-EM SPACE
            + "\\u2006" // SIX-PER-EM SPACE
            + "\\u2007" // FIGURE SPACE
            + "\\u2008" // PUNCTUATION SPACE
            + "\\u2009" // THIN SPACE
            + "\\u200A" // HAIR SPACE
            + "\\u2028" // LINE SEPARATOR
            + "\\u2029" // PARAGRAPH SEPARATOR
            + "\\u202F" // NARROW NO-BREAK SPACE
            + "\\u205F" // MEDIUM MATHEMATICAL SPACE
            + "\\u3000" // IDEOGRAPHIC SPACE
    ;
    
    public static final String UNICODE_WHITESPACE = "[" + UNICODE_WHITESPACE_CHARS + "]";
    
}
