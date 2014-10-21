package org.xydra.oo.runtime;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.oo.runtime.java.JavaTypeSpecUtils;
import org.xydra.oo.runtime.java.OOReflectionUtils;

public class TestOOTypeStuff {

	@Test
	public void testOOTypeBridge() {
		assertTrue(OOReflectionUtils.isTranslatableSingleType(XAddress.class));
	}

	@Test
	public void testOOTypeMapping() {
		assertNotNull(JavaTypeSpecUtils.getMapping(Boolean.class, null));
		assertNotNull(JavaTypeSpecUtils.getMapping(XBooleanValue.class, null));
		assertNotNull(JavaTypeSpecUtils.getMapping(XBooleanListValue.class, null));
		assertNotNull(JavaTypeSpecUtils.getMapping(XAddressSortedSetValue.class, null));
	}

}
