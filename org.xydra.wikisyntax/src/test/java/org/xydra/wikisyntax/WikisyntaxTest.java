package org.xydra.wikisyntax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;


public class WikisyntaxTest {
	
	@Test
	public void testBold() {
		String input = "This is __such__ a great parser";
		String output = Wikisyntax.toHtml(input);
		System.out.println(output);
		assertEquals("This is <strong>such</strong> a great parser", output);
	}
	
	@Test
	@Ignore
	// TODO clarify newline handling
	public void testLists() {
		String input = "My todo list for today is:\n* get up\n* go shopping\n* sleep\nAnd now my next list...";
		String output = Wikisyntax.toHtml(input);
		System.out.println(output);
		assertTrue(("My todo list for today is:\r\n" + "<ul><li>get up</li>\r\n"
		        + "<li>go shopping</li>\r\n" + "<li>sleep</li>\r\n"
		        + "</ul>And now my next list...").equals(output));
	}
	
	@Test
	public void testLink() {
		String input = "Try out [http://xydra.org] today";
		String output = Wikisyntax.toHtml(input);
		System.out.println(output);
		assertEquals("Try out <a href=\"http://xydra.org\">http://xydra.org</a> today", output);
	}
	
	@Test
	public void testLinkWithLabel() {
		String input = "Try out [http://xydra.org Xydra] today";
		String output = Wikisyntax.toHtml(input);
		System.out.println(output);
		assertEquals("Try out <a href=\"http://xydra.org\">Xydra</a> today", output);
	}
	
	@Test
	public void testLinkWithLabel2() {
		String input = "Try out [http://xydra.org|Xydra] today";
		String output = Wikisyntax.toHtml(input);
		System.out.println(output);
		assertEquals("Try out <a href=\"http://xydra.org\">Xydra</a> today", output);
	}
}
