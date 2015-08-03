package org.xydra.conf.escape;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import com.google.common.collect.Lists;

public class EscapingTest {

	private static final Logger log = LoggerFactory.getLogger(EscapingTest.class);

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

	public static String[] nasty = {
			"a",
			"",
			"\" \"",
			"\n so \n",
			"\" \n deep \\\n value \"",
			// TODO handle high unicode "",
			"\\",
			"\t",
			"\r",
			"\"",
			"C:\\\\ners\\\\andre_000\\\\Desktop\\\\Usersandre_000Denkwerkzeug Knowledge Files\\\\my",
			"^a°a!a\"a§a%a&a/a{a(a[a)a]a=a}a?a \\a´a`a*a*a~a'a#a,a;a.a:a-a_a<a>a|" };

	@Test
	public void testRoundtrip() {
		for (int i = 0; i < nasty.length; i++) {
			testRoundtrip(i, nasty[i]);
		}
	}

	@Test
	public void testMaterialiseEscape() {
		for (int i = 0; i < nasty.length; i++) {
			testMaterialiseEscape(i, nasty[i]);
		}
	}

	private static void testMaterialiseEscape(final int i, final String s) {
		log.info("Testing key " + i + " ='" + s + "' " + Escaping.toCodepoints(s));

		boolean escapeColonSpaceEqual = true;
		boolean swallowBaskslashNewline = true;
		testMaterialiseEscape(s, escapeColonSpaceEqual, swallowBaskslashNewline);
		swallowBaskslashNewline = false;
		testMaterialiseEscape(s, escapeColonSpaceEqual, swallowBaskslashNewline);

		escapeColonSpaceEqual = false;
		swallowBaskslashNewline = true;
		testMaterialiseEscape(s, escapeColonSpaceEqual, swallowBaskslashNewline);
		swallowBaskslashNewline = false;
		testMaterialiseEscape(s, escapeColonSpaceEqual, swallowBaskslashNewline);
	}

	private static void testMaterialiseEscape(final String s, final boolean escapeColonSpaceEqual,
			final boolean swallowBaskslashNewline) {

		Escaping.materializeEscapes(s, swallowBaskslashNewline, escapeColonSpaceEqual);
		// no bug? we're happy
	}

	@Test
	public void test() {
		for (int i = 0; i < keys.size(); i++) {
			final String k = keys.get(i);
			testRoundtrip(i, k);
		}
	}

	/**
	 * @param i
	 *            to help debugging, some index
	 * @param s
	 */
	private static void testRoundtrip(final int i, final String s) {
		log.info("Testing key " + i + " ='" + s + "' " + Escaping.toCodepoints(s));

		boolean escapeColonSpaceEqual = true;
		boolean swallowBaskslashNewline = true;
		testRoundtrip(s, escapeColonSpaceEqual, swallowBaskslashNewline);
		swallowBaskslashNewline = false;
		testRoundtrip(s, escapeColonSpaceEqual, swallowBaskslashNewline);

		escapeColonSpaceEqual = false;
		swallowBaskslashNewline = true;
		testRoundtrip(s, escapeColonSpaceEqual, swallowBaskslashNewline);
		swallowBaskslashNewline = false;
		testRoundtrip(s, escapeColonSpaceEqual, swallowBaskslashNewline);
	}

	private static void testRoundtrip(final String s, final boolean escapeColonSpaceEqual,
			final boolean swallowBaskslashNewline) {
		final String escaped = Escaping.escape(s, escapeColonSpaceEqual, false);
		log.info("Escaped as '" + escaped + "' " + Escaping.toCodepoints(escaped));
		final String unescaped = Escaping.materializeEscapes(escaped, swallowBaskslashNewline,
				escapeColonSpaceEqual);
		assertEquals(
				"expected=\n" + Escaping.toCodepoints(s) + "\nreceived=\n "
						+ Escaping.toCodepoints(unescaped), s, unescaped);
	}

	public static void main(final String[] args) {
		System.out.println(Escaping.toCodepoints("foo\nbar"));
	}

}
