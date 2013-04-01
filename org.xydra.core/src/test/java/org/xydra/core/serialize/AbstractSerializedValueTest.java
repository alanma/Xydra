package org.xydra.core.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Test serializing {@link XValue} types to/from XML.
 * 
 * @author dscharrer
 * 
 */
abstract public class AbstractSerializedValueTest extends AbstractSerializingTest {
	
	private static final Logger log = getLogger();
	
	private static Logger getLogger() {
		LoggerTestHelper.init();
		return LoggerFactory.getLogger(AbstractSerializedValueTest.class);
	}
	
	@Test
	public void testNullValue() {
		testValue(null);
	}
	
	@Test
	public void testAddressListValue() {
		testValue(XV.toValue(new XAddress[] { XX.toAddress(XX.createUniqueId(), null, null, null),
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
		        XX.toAddress(XX.createUniqueId(), null, null, null),
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
		        XX.toAddress(XX.createUniqueId(), null, null, null),
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
		testValue(XX.toAddress(XX.createUniqueId(), null, null, null));
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
	public void testBinaryValue() {
		byte[] bytes = new byte[2 * 256];
		for(int i = 0; i < 256; i++) {
			bytes[i] = (byte)i;
			bytes[bytes.length - i - 1] = (byte)i;
		}
		testValue(XV.toValue(bytes));
	}
	
	@Test
	public void testBinaryValueEmpty() {
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
		testValue(XV.toValue(new XId[] { XX.createUniqueId(), XX.toId("cookie") }));
	}
	
	@Test
	public void testIdListValueEmpty() {
		testValue(XV.toValue(new XId[] {}));
	}
	
	@Test
	public void testIdListValueNull() {
		testValue(XV.toValue(new XId[] { null }));
	}
	
	@Test
	public void testIdSetValue() {
		testValue(XV.toIdSetValue(new XId[] { XX.createUniqueId(), XX.toId("cookie") }));
	}
	
	@Test
	public void testIdSetValueEmpty() {
		testValue(XV.toIdSetValue(new XId[] {}));
	}
	
	@Test
	public void testIdSetValueNull() {
		testValue(XV.toIdSetValue(new XId[] { null }));
	}
	
	@Test
	public void testIdSortedSetValue() {
		testValue(XV.toIdSortedSetValue(new XId[] { XX.createUniqueId(), XX.toId("cookie") }));
	}
	
	@Test
	public void testIdSortedSetValueEmpty() {
		testValue(XV.toIdSortedSetValue(new XId[] {}));
	}
	
	@Test
	public void testIdSortedSetValueNull() {
		testValue(XV.toIdSortedSetValue(new XId[] { null }));
	}
	
	@Test
	public void testIdValue() {
		testValue(XX.createUniqueId());
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
	
	// @Test
	// public void testStringValueNewlines2() {
	// testValue(XV.toValue("aaa\rbbb"));
	// }
	
	// @Test
	// public void testStringValueNewlines3() {
	// testValue(XV.toValue("aaa\r\nbbb"));
	// }
	//
	// @Test
	// public void testStringValueNewlines4() {
	// testValue(XV.toValue("aaa\n\rbbb"));
	// }
	
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
		testValue(XV.toValue("&gt;<\"\\"));
	}
	
	private void testValue(XValue value) {
		
		XydraOut out = create();
		SerializedValue.serialize(value, out);
		assertTrue(out.isClosed());
		String data = out.getData();
		
		log.debug(data);
		
		XydraElement e = parse(data);
		XValue valueAgain = SerializedValue.toValue(e);
		assertEquals(value, valueAgain);
		
		// test with whitespace enabled
		
		out = create();
		out.enableWhitespace(true, true);
		SerializedValue.serialize(value, out);
		assertTrue(out.isClosed());
		data = out.getData();
		
		log.debug(data);
		
		e = parse(data);
		valueAgain = SerializedValue.toValue(e);
		assertEquals(value, valueAgain);
		
	}
	
}
