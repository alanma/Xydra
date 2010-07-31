package org.xydra.core.xml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.model.XID;
import org.xydra.core.model.XIDProvider;
import org.xydra.core.value.XValue;
import org.xydra.core.value.XValueFactory;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;


/**
 * Test serializing {@link XValue} types to/from XML.
 * 
 * @author dscharrer
 * 
 */
public class XmlValueTest {
	
	private final static XValueFactory fact = X.getValueFactory();
	private final static XIDProvider ids = X.getIDProvider();
	
	private void testValue(XValue value) {
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlValue.toXml(value, out);
		assertEquals("", out.getOpentags());
		String xml = out.getXml();
		// System.out.println(xml);
		MiniElement e = new MiniXMLParserImpl().parseXml(xml);
		XValue valueAgain = XmlValue.toValue(e);
		assertEquals(value, valueAgain);
	}
	
	@Test
	public void testBooleanValueTrue() {
		testValue(fact.createBooleanValue(true));
	}
	
	@Test
	public void testBooleanValueFalse() {
		testValue(fact.createBooleanValue(false));
	}
	
	@Test
	public void testDoubleValue() {
		testValue(fact.createDoubleValue(3054.24358989015234));
	}
	
	@Test
	public void testIdValue() {
		testValue(fact.createIDValue(ids.createUniqueID()));
	}
	
	@Test
	public void testIdValueNull() {
		testValue(fact.createIDValue(null));
	}
	
	@Test
	public void testIntegerValue() {
		testValue(fact.createIntegerValue(314));
	}
	
	@Test
	public void testLongValue() {
		testValue(fact.createLongValue(1236987216398637867L));
	}
	
	@Test
	public void testStringValue() {
		testValue(fact.createStringValue("cookie monster"));
	}
	
	@Test
	public void testStringValueEmpty() {
		testValue(fact.createStringValue(""));
	}
	
	@Test
	public void testStringValueUnsafe() {
		testValue(fact.createStringValue("&gt;<"));
	}
	
	@Test
	public void testStringValueSpace() {
		testValue(fact.createStringValue("  test  "));
	}
	
	@Test
	public void testStringValueTabs() {
		testValue(fact.createStringValue("\t\ttest\t\t"));
	}
	
	@Test
	public void testStringValueNewlines() {
		testValue(fact.createStringValue("\n\ntest\n\n"));
	}
	
	@Test
	public void testStringValueNull() {
		testValue(fact.createStringValue(null));
	}
	
	@Test
	public void testBooleanListValue() {
		testValue(fact.createBooleanListValue(new boolean[] { true, true, false }));
	}
	
	@Test
	public void testBooleanListValueEmpty() {
		testValue(fact.createBooleanListValue(new boolean[] {}));
	}
	
	@Test
	public void testByteListValue() {
		byte[] bytes = new byte[2 * 256];
		for(int i = 0; i < 256; i++) {
			bytes[i] = (byte)i;
			bytes[bytes.length - i - 1] = (byte)i;
		}
		testValue(fact.createByteListValue(bytes));
	}
	
	@Test
	public void testByteListValueEmpty() {
		testValue(fact.createByteListValue(new byte[] {}));
	}
	
	@Test
	public void testDoubleListValue() {
		testValue(fact.createDoubleListValue(new double[] { 1.0, -2.1, 23.2342346, 42.0 }));
	}
	
	@Test
	public void testDoubleListValueEmpty() {
		testValue(fact.createDoubleListValue(new double[] {}));
	}
	
	@Test
	public void testIdListValue() {
		testValue(fact
		        .createIDListValue(new XID[] { ids.createUniqueID(), ids.fromString("cookie") }));
	}
	
	@Test
	public void testIdListValueNull() {
		testValue(fact.createIDListValue(new XID[] { null }));
	}
	
	@Test
	public void testIdListValueEmpty() {
		testValue(fact.createIDListValue(new XID[] {}));
	}
	
	@Test
	public void testIntegerListValue() {
		testValue(fact.createIntegerListValue(new int[] { 1, 42, -8 }));
	}
	
	@Test
	public void testIntegerListValueEmpty() {
		testValue(fact.createIntegerListValue(new int[] {}));
	}
	
	@Test
	public void testLongListValue() {
		testValue(fact.createLongListValue(new long[] { 2385672643864235434L, -324L }));
	}
	
	@Test
	public void testLongListValueEmpty() {
		testValue(fact.createLongListValue(new long[] {}));
	}
	
	@Test
	public void testStringListValue() {
		testValue(fact.createStringListValue(new String[] { "cookie", "monster" }));
	}
	
	@Test
	public void testStringListValueNull() {
		testValue(fact.createStringListValue(new String[] { null }));
	}
	
	@Test
	public void testStringListValueEmpty() {
		testValue(fact.createStringListValue(new String[] {}));
	}
	
	@Test
	public void testStringListValueUnsafe() {
		testValue(fact.createStringListValue(new String[] { "&gt;<" }));
	}
	
	@Test
	public void testStringListValueSpace() {
		testValue(fact.createStringListValue(new String[] { "  test  " }));
	}
	
	@Test
	public void testStringListValueTabs() {
		testValue(fact.createStringListValue(new String[] { "\t\ttest\t\t" }));
	}
	
	@Test
	public void testStringListValueNewlines() {
		testValue(fact.createStringListValue(new String[] { "\n\ntest\n\n" }));
	}
	
	@Test
	public void testIdSetValue() {
		testValue(fact
		        .createIDSetValue(new XID[] { ids.createUniqueID(), ids.fromString("cookie") }));
	}
	
	@Test
	public void testIdSetValueNull() {
		testValue(fact.createIDSetValue(new XID[] { null }));
	}
	
	@Test
	public void testIdSetValueEmpty() {
		testValue(fact.createIDSetValue(new XID[] {}));
	}
	
	@Test
	public void testStringSetValue() {
		testValue(fact.createStringSetValue(new String[] { "cookie", "monster" }));
	}
	
	@Test
	public void testStringSetValueNull() {
		testValue(fact.createStringSetValue(new String[] { null }));
	}
	
	@Test
	public void testStringSetValueEmpty() {
		testValue(fact.createStringSetValue(new String[] {}));
	}
	
	@Test
	public void testStringSetValueUnsafe() {
		testValue(fact.createStringSetValue(new String[] { "&gt;<" }));
	}
	
	@Test
	public void testStringSetValueSpace() {
		testValue(fact.createStringSetValue(new String[] { "  test  " }));
	}
	
	@Test
	public void testStringSetValueTabs() {
		testValue(fact.createStringSetValue(new String[] { "\t\ttest\t\t" }));
	}
	
	@Test
	public void testStringSetValueNewlines() {
		testValue(fact.createStringSetValue(new String[] { "\n\ntest\n\n" }));
	}
	
}
