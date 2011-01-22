package org.xydra.core.xml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.value.XValue;
import org.xydra.core.value.XV;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;


/**
 * Test serializing {@link XValue} types to/from XML.
 * 
 * @author dscharrer
 * 
 */
public class XmlValueTest {
	
	@Test
	public void testAddressListValue() {
		testValue(XV.toValue(new XAddress[] { XX.toAddress(XX.createUniqueID(), null, null, null),
		        XX.toAddress("/cookie/monster/-/-") }));
	}
	
	@Test
	public void testAddressListValueEmpty() {
		testValue(XV.toValue(new XAddress[] {}));
	}
	
	@Test
	public void testAddressListValueNull() {
		testValue(XV.toValue(new XAddress[] { null }));
	}
	
	@Test
	public void testAddressSetValue() {
		testValue(XV.toAddressSetValue(new XAddress[] {
		        XX.toAddress(XX.createUniqueID(), null, null, null),
		        XX.toAddress("/cookie/monster/-/-") }));
	}
	
	@Test
	public void testAddressSetValueEmpty() {
		testValue(XV.toAddressSetValue(new XAddress[] {}));
	}
	
	@Test
	public void testAddressSetValueNull() {
		testValue(XV.toAddressSetValue(new XAddress[] { null }));
	}
	
	@Test
	public void testAddressSortedSetValue() {
		testValue(XV.toAddressSortedSetValue(new XAddress[] {
		        XX.toAddress(XX.createUniqueID(), null, null, null),
		        XX.toAddress("/cookie/monster/-/-") }));
	}
	
	@Test
	public void testAddressSortedSetValueEmpty() {
		testValue(XV.toAddressSetValue(new XAddress[] {}));
	}
	
	@Test
	public void testAddressSortedSetValueNull() {
		testValue(XV.toAddressSetValue(new XAddress[] { null }));
	}
	
	@Test
	public void testAddressValue() {
		testValue(XX.toAddress(XX.createUniqueID(), null, null, null));
	}
	
	@Test
	public void testBooleanListValue() {
		testValue(XV.toValue(new boolean[] { true, true, false }));
	}
	
	@Test
	public void testBooleanListValueEmpty() {
		testValue(XV.toValue(new boolean[] {}));
	}
	
	@Test
	public void testBooleanValueFalse() {
		testValue(XV.toValue(false));
	}
	
	@Test
	public void testBooleanValueTrue() {
		testValue(XV.toValue(true));
	}
	
	@Test
	public void testByteListValue() {
		byte[] bytes = new byte[2 * 256];
		for(int i = 0; i < 256; i++) {
			bytes[i] = (byte)i;
			bytes[bytes.length - i - 1] = (byte)i;
		}
		testValue(XV.toValue(bytes));
	}
	
	@Test
	public void testByteListValueEmpty() {
		testValue(XV.toValue(new byte[] {}));
	}
	
	@Test
	public void testDoubleListValue() {
		testValue(XV.toValue(new double[] { 1.0, -2.1, 23.2342346, 42.0 }));
	}
	
	@Test
	public void testDoubleListValueEmpty() {
		testValue(XV.toValue(new double[] {}));
	}
	
	@Test
	public void testDoubleValue() {
		testValue(XV.toValue(3054.24358989015234));
	}
	
	@Test
	public void testIdListValue() {
		testValue(XV.toValue(new XID[] { XX.createUniqueID(), XX.toId("cookie") }));
	}
	
	@Test
	public void testIdListValueEmpty() {
		testValue(XV.toValue(new XID[] {}));
	}
	
	@Test
	public void testIdListValueNull() {
		testValue(XV.toValue(new XID[] { null }));
	}
	
	@Test
	public void testIdSetValue() {
		testValue(XV.toIDSetValue(new XID[] { XX.createUniqueID(), XX.toId("cookie") }));
	}
	
	@Test
	public void testIdSetValueEmpty() {
		testValue(XV.toIDSetValue(new XID[] {}));
	}
	
	@Test
	public void testIdSetValueNull() {
		testValue(XV.toIDSetValue(new XID[] { null }));
	}
	
	@Test
	public void testIdSortedSetValue() {
		testValue(XV.toIDSortedSetValue(new XID[] { XX.createUniqueID(), XX.toId("cookie") }));
	}
	
	@Test
	public void testIdSortedSetValueEmpty() {
		testValue(XV.toIDSortedSetValue(new XID[] {}));
	}
	
	@Test
	public void testIdSortedSetValueNull() {
		testValue(XV.toIDSortedSetValue(new XID[] { null }));
	}
	
	@Test
	public void testIdValue() {
		testValue(XX.createUniqueID());
	}
	
	@Test(expected = NullPointerException.class)
	public void testIdValueNull() {
		testValue((XID)null);
	}
	
	@Test
	public void testIntegerListValue() {
		testValue(XV.toValue(new int[] { 1, 42, -8 }));
	}
	
	@Test
	public void testIntegerListValueEmpty() {
		testValue(XV.toValue(new int[] {}));
	}
	
	@Test
	public void testIntegerValue() {
		testValue(XV.toValue(314));
	}
	
	@Test
	public void testLongListValue() {
		testValue(XV.toValue(new long[] { 2385672643864235434L, -324L }));
	}
	
	@Test
	public void testLongListValueEmpty() {
		testValue(XV.toValue(new long[] {}));
	}
	
	@Test
	public void testLongValue() {
		testValue(XV.toValue(1236987216398637867L));
	}
	
	@Test
	public void testStringListValue() {
		testValue(XV.toValue(new String[] { "cookie", "monster" }));
	}
	
	@Test
	public void testStringListValueEmpty() {
		testValue(XV.toValue(new String[] {}));
	}
	
	@Test
	public void testStringListValueNewlines() {
		testValue(XV.toValue(new String[] { "\n\ntest\n\n" }));
	}
	
	@Test
	public void testStringListValueNull() {
		testValue(XV.toValue(new String[] { null }));
	}
	
	@Test
	public void testStringListValueSpace() {
		testValue(XV.toValue(new String[] { "  test  " }));
	}
	
	@Test
	public void testStringListValueTabs() {
		testValue(XV.toValue(new String[] { "\t\ttest\t\t" }));
	}
	
	@Test
	public void testStringListValueUnsafe() {
		testValue(XV.toValue(new String[] { "&gt;<" }));
	}
	
	@Test
	public void testStringSetValue() {
		testValue(XV.toStringSetValue(new String[] { "cookie", "monster" }));
	}
	
	@Test
	public void testStringSetValueEmpty() {
		testValue(XV.toStringSetValue(new String[] {}));
	}
	
	@Test
	public void testStringSetValueNewlines() {
		testValue(XV.toStringSetValue(new String[] { "\n\ntest\n\n" }));
	}
	
	@Test
	public void testStringSetValueNull() {
		testValue(XV.toStringSetValue(new String[] { null }));
	}
	
	@Test
	public void testStringSetValueSpace() {
		testValue(XV.toStringSetValue(new String[] { "  test  " }));
	}
	
	@Test
	public void testStringSetValueTabs() {
		testValue(XV.toStringSetValue(new String[] { "\t\ttest\t\t" }));
	}
	
	@Test
	public void testStringSetValueUnsafe() {
		testValue(XV.toStringSetValue(new String[] { "&gt;<" }));
	}
	
	@Test
	public void testStringValue() {
		testValue(XV.toValue("cookie monster"));
	}
	
	@Test
	public void testStringValueEmpty() {
		testValue(XV.toValue(""));
	}
	
	@Test
	public void testStringValueNewlines() {
		testValue(XV.toValue("\n\ntest\n\n"));
	}
	
	@Test
	public void testStringValueNull() {
		testValue(XV.toValue((String)null));
	}
	
	@Test
	public void testStringValueSpace() {
		testValue(XV.toValue("  test  "));
	}
	
	@Test
	public void testStringValueTabs() {
		testValue(XV.toValue("\t\ttest\t\t"));
	}
	
	@Test
	public void testStringValueUnsafe() {
		testValue(XV.toValue("&gt;<"));
	}
	
	private void testValue(XValue value) {
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlValue.toXml(value, out);
		assertEquals("", out.getOpentags());
		String xml = out.getXml();
		MiniElement e = new MiniXMLParserImpl().parseXml(xml);
		XValue valueAgain = XmlValue.toValue(e);
		assertEquals(value, valueAgain);
	}
	
}
