package org.xydra.base.id;

import org.junit.Assert;
import org.junit.Test;
import org.xydra.base.XId;
import org.xydra.base.XIdProvider;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class XidCodecTest {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XidCodecTest.class);

	static class Sample {
		public Sample(final String s, final boolean valid, final String comment) {
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

		public static Sample valid(final String s, final String comment) {
			return new Sample(s, true, comment);
		}

		public static Sample invalid(final String s, final String comment) {
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

				Sample.valid("s", "legal xml name"), Sample.valid("aaa", "legal xml name"),
				Sample.valid("a", "a short one"),
				Sample.valid("äöü", "some umlauts are legal in XML 1.0"),
				Sample.invalid("a b", "fails if some regex uses a partial match"),
				Sample.valid("aaa", "same again"),
				Sample.invalid("aaa bbb", "fails if some regex uses a partial match"),
				Sample.invalid(" ", "just a space"),
				Sample.invalid("0a", "number at start is not allowed in XML 1.0"),
				Sample.valid("Genußrechte", "ß is valid in XML 1.0"), Sample.valid("ß", "valid"),
				Sample.invalid("========", "illegal in XML 1.0"),
				Sample.invalid("a========", "illegal in XML 1.0"),
				Sample.invalid("GenuÃrechte",
						"??? contains unicode control character (sequence 195=C3,159=9F; this is UTF-8 encoded 'ß')"),
				Sample.valid(hundredChars, "just right"),
				Sample.invalid(hundredChars + "a", "too long"),
				Sample.valid("_foo", "one leading underscores"),
				Sample.valid("__foo", "two leading underscores"),
				Sample.valid("___foo", "three leading underscores"), Sample.invalid(
						"4*(var_1-min(o$8:o$49))/(max(o$8:o$49)-min(o$8:o$49))+1\"", "too long") };

	public static void main(final String[] args) {


		final String p = "=(var_0*var_1+var_2*var_3+var_4*var_5+var_6*var_7+var_8*var_9+var_10*var_11+var_12*var_13)/sum($var_1:$var_13)";
		System.out.println(XidCodec.encode(p, 100));
		System.out.println(XidCodec.decode(XidCodec.encode(p, 100)));


		final String h = "GenuÃrechte";
		System.out.println(h);
		final String k = "4*(var_1-min(o$8:o$49))/(max(o$8:o$49)-min(o$8:o$49))+1\"";
		System.out.println(XidCodec.encode(k, 500));

		final String l = "=average(var_0:VAR_41)";
		System.out.println(l);
		System.out.println(XidCodec.encode(l, 200));
		System.out.println(XidCodec.decode(XidCodec.encode(l, 200)));

		System.out.println(XidCodec.decode(XidCodec.encode(k, 200)));

		for (int i = 0; i < h.length(); i++) {
			System.out.println("char[" + i + "]= " + h.charAt(i) + "=(" + (int) h.charAt(i)
			+ ") codepoint='" + h.codePointAt(i) + "'");
		}

		System.out.println("159 = '" + (char) 159 + "' = a unicode control character");
		System.out.println("159 = '" + (char) 159 + "'");

		final StringBuilder b = new StringBuilder();
		XidCodec.appendEncoded(b, XidCodec.ENCODING_CHAR, 1, 100);
		b.append(" | ");
		XidCodec.appendEncoded(b, XidCodec.ENCODING_CHAR, 1042, 100);
		b.append(" | ");
		XidCodec.appendEncoded(b, XidCodec.ENCODING_CHAR, 65699, 100);
		System.out.println(b);

		System.out.println(XidCodec.encodeAsXId("40", XIdProvider.MAX_LENGTH));

		final String[] s = new String[] {

				"Self-organized+Reuse+of+Software+Engineering+Knowledge+Supported+by+Semantic+Wikis",
		"CallAutomata?)>\\" };
		for (final String a : s) {
			System.out.println(XidCodec.encodeAsXId(a, XIdProvider.MAX_LENGTH));
		}
		System.out.println(XidCodec.decode("_30install"));
		System.out.println(XidCodec.decode("_meta"));
	}

	@Test
	public void testUsualSuspects() {
		for (final Sample sample : SAMPLES) {
			if (sample.valid) {
				Assert.assertTrue("Should be valid '" + sample + "'",
						MemoryStringIDProvider.isValidId(sample.str));
			} else {
				Assert.assertFalse("Should be invalid '" + sample + "'",
						MemoryStringIDProvider.isValidId(sample.str));
			}
			if (sample.str.length() < 100) {
				testEncodeDecode(sample.str);
			}
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

	private static void assertValid(final String s) {
		Assert.assertTrue(s, MemoryStringIDProvider.isValidId(s));
		Assert.assertTrue(s, gwt_matchesXydraId(s));
	}

	private static void assertInvalid(final String s) {
		Assert.assertFalse(s, MemoryStringIDProvider.isValidId(s));
		Assert.assertFalse(s, gwt_matchesXydraId(s));
	}

	private static final RegExp p = RegExp.compile(MemoryStringIDProvider.nameRegex);

	// GWT version
	public static boolean gwt_matchesXydraId(final String uriString) {
		final MatchResult m = p.exec(uriString);
		if (m == null) {
			return false;
		}
		final String match = m.getGroup(0);
		return match != null && match.length() == uriString.length();
	}

	private static void testEncodeDecode(final String raw) {
		final XId enc = XidCodec.encodeAsXId(raw, XIdProvider.MAX_LENGTH);
		System.out.println("'" + raw + "'='" + enc + "'");
		final String dec = XidCodec.decodeFromXId(enc);
		Assert.assertEquals(raw, dec);
	}

}
