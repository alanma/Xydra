package org.xydra.core.serialize.xml;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xydra.base.minio.MiniReader;
import org.xydra.base.minio.MiniStringReader;
import org.xydra.core.serialize.XydraElement;
import org.xydra.log.impl.universal.UniversalLogger;

/**
 * TODO test normal {@link XmlParser} and {@link XmlStreamParser}
 *
 * @author xamde
 */
public class XmlParserTest {

	@BeforeClass
	public static void setup() {
		UniversalLogger.activate();
	}

	String xml1 = "<aaa bbb='ccc'><ddd>eee</ddd></aaa>";

	@Test
	public void testDocumentXmlParser() {
		final XmlParser xp = new XmlParser();
		final MiniReader miniReader = new MiniStringReader(this.xml1);
		final XydraElement doc = xp.parse(miniReader);
		final String xml2 = XmlUtils.toString(doc);
		assertEquals(this.xml1, xml2);
	}

	@Test
	@Ignore
	public void testStreamingXmlParser() {
		// XmlStreamParser xsp = new XmlStreamParser();
		// MiniReader miniReader = new MiniStringReader(this.xml1);
		// XmlOut xmlOut = new XmlOut();
		// xsp.parse(miniReader, xmlOut);
		// String xml2 = xmlOut.getData();
		// assertEquals(this.xml1, xml2);
	}
}
