package org.xydra.base.id;

import org.junit.Assert;
import org.junit.Test;
import org.xydra.base.XId;
import org.xydra.base.XIdProvider;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;


public class XidCodedTest {
    
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(XidCodedTest.class);
    
    static class Sample {
        public Sample(String s, boolean valid, String comment) {
            super();
            this.str = s;
            this.valid = valid;
            this.comment = comment;
        }
        
        @Override
        public String toString() {
            return "'" + this.str + "' valid=" + this.valid + " \"" + this.comment + "\"";
        }
        
        String str;
        boolean valid;
        String comment;
        
        public static Sample valid(String s, String comment) {
            return new Sample(s, true, comment);
        }
        
        public static Sample invalid(String s, String comment) {
            return new Sample(s, false, comment);
        }
    }
    
    static String hundredChars = "a123456789" + "a123456789" + "a123456789" + "a123456789"
            + "a123456789" + "a123456789" + "a123456789" + "a123456789" + "a123456789"
            + "a123456789";
    
    static {
        assert hundredChars.length() == 100;
    }
    
    public static final Sample[] SAMPLES =
    
    {
            
            Sample.valid("s", "legal xml name"),
            Sample.valid("aaa", "legal xml name"),
            Sample.valid("a", "a short one"),
            Sample.valid("äöü", "some umlauts are legal in XML 1.0"),
            Sample.invalid("a b", "fails if some regex uses a partial match"),
            Sample.valid("aaa", "same again"),
            Sample.invalid("aaa bbb", "fails if some regex uses a partial match"),
            Sample.invalid(" ", "just a space"),
            Sample.invalid("0a", "number at start is not allowed in XML 1.0"),
            Sample.valid("Genußrechte", "ß is valid in XML 1.0"),
            Sample.valid("ß", "valid"),
            Sample.invalid("========", "illegal in XML 1.0"),
            Sample.invalid("a========", "illegal in XML 1.0"),
            Sample.invalid("GenuÃrechte",
                    "??? contains unicode control character (sequence 195=C3,159=9F; this is UTF-8 encoded 'ß')"),
            Sample.valid(hundredChars, "just right"),
            Sample.invalid(hundredChars + "a", "too long"),
    
    };
    
    public static void main(String[] args) {
        String h = "GenuÃrechte";
        System.out.println(h);
        for(int i = 0; i < h.length(); i++) {
            System.out.println("char[" + i + "]= " + h.charAt(i) + "=(" + ((int)h.charAt(i))
                    + ") codepoint='" + h.codePointAt(i) + "'");
        }
    }
    
    @Test
    public void testUsualSuspects() {
        for(Sample sample : SAMPLES) {
            if(sample.valid) {
                Assert.assertTrue("Should be valid '" + sample + "'",
                        MemoryStringIDProvider.isValidId(sample.str));
            } else {
                Assert.assertFalse("Should be invalid '" + sample + "'",
                        MemoryStringIDProvider.isValidId(sample.str));
            }
            if(sample.str.length() < 100)
                testEncodeDecode(sample.str);
        }
    }
    
    @Test
    public void testValid() {
        assertValid("aaa");
        assertValid("aaa");
        assertValid("a");
        assertValid("öäü");
        assertValid("a");
        assertInvalid("a b");
        assertInvalid("aaa bbb");
        assertInvalid(" ");
        assertInvalid("0a");
    }
    
    private static void assertValid(String s) {
        Assert.assertTrue(s, MemoryStringIDProvider.isValidId(s));
        Assert.assertTrue(s, gwt_matchesXydraId(s));
    }
    
    private static void assertInvalid(String s) {
        Assert.assertFalse(s, MemoryStringIDProvider.isValidId(s));
        Assert.assertFalse(s, gwt_matchesXydraId(s));
    }
    
    private static final RegExp p = RegExp.compile(MemoryStringIDProvider.nameRegex);
    
    // GWT version
    public static boolean gwt_matchesXydraId(String uriString) {
        MatchResult m = p.exec(uriString);
        if(m == null)
            return false;
        String match = m.getGroup(0);
        return match != null && match.length() == uriString.length();
    }
    
    private static void testEncodeDecode(String raw) {
        XId enc = XidCodec.encodeAsXId(raw, XIdProvider.MAX_LENGTH);
        System.out.println("'" + raw + "'='" + enc + "'");
        String dec = XidCodec.decodeFromXId(enc);
        Assert.assertEquals(raw, dec);
    }
    
}
