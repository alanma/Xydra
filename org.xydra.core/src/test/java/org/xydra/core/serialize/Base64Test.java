package org.xydra.core.serialize;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.core.serialize.Base64;


public class Base64Test {
	
	@Test
	public void testRound() {
		String encoded = Base64.encode("hello world}".getBytes());
		byte[] bytes = Base64.decode(encoded);
		String decoded = new String(bytes);
		assertEquals("hello world}", decoded);
	}
	
	@Test
	public void testUtf8() {
		assertEquals("hello", Base64.utf8(Base64.utf8("hello")));
	}
	
	@Test
	public void testUrlCode() {
		String raw = "hel-lo/foo+bar_he";
		String enc = Base64.urlEncode(Base64.utf8(raw));
		byte[] dec = Base64.urlDecode(enc);
		assertEquals(raw, Base64.utf8(dec));
	}
	
}
