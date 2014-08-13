package org.xydra.conf.impl;

import static org.junit.Assert.assertEquals;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import java.util.ArrayList;

import org.junit.Test;

import com.google.common.collect.Lists;


public class PropertyFileEscapingTest {
    
    private static final Logger log = LoggerFactory.getLogger(PropertyFileEscapingTest.class);
    
    public static final String a = "a";
    public static final String b = "b";
    public static final String c = "c";
    public static final String classicWindowsPath = "C:/Users/andre_000/Desktop/";
    public static final String d = "d";
    public static final String e = "e";
    public static final String eC1 = "\n";
    public static final String eC2 = ":";
    public static final String eC3 = "=";
    public static final String f = "f";
    public static final String strangeUnicodeSign = "TODO"; // TODO "";
    public static final String weirdWindowsPathWithEscapedBackslashes = "C:\\ners\\andre_000\\Desktop\\Usersandre_000Denkwerkzeug Knowledge Files\\my";
    
    public static final ArrayList<String> keys = Lists.newArrayList(
    
    a, b, c, d, e,
    
    f, eC1, eC2, eC3, classicWindowsPath,
    
    weirdWindowsPathWithEscapedBackslashes, strangeUnicodeSign
    
    );
    
    @Test
    public void test() {
        for(int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            log.info("Testing key " + i + " ='" + k + "' " + toCodepoints(k));
            String escaped = PropertyFileEscaping.escape(k);
            log.info("Escaped as '" + escaped + "' " + toCodepoints(escaped));
            String unescaped = PropertyFileEscaping.materializeEscapes(escaped);
            assertEquals("expected=\n" + toCodepoints(k) + "\nreceived=\n "
                    + toCodepoints(unescaped),
            
            k, unescaped);
        }
    }
    
    public static String toCodepoints(String s) {
        String res = "";
        int i = 0;
        while(i < s.length()) {
            int c = s.codePointAt(i);
            i += Character.charCount(c);
            res += "[" + Integer.toString(c) + "='" + ((char)c) + "']";
        }
        return res;
    }
    
    public static void main(String[] args) {
        System.out.println(toCodepoints("foo\nbar"));
    }
    
}
