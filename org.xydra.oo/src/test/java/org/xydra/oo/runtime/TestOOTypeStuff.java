package org.xydra.oo.runtime;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.oo.runtime.shared.OOTypeBridge;
import org.xydra.oo.runtime.shared.OOTypeMapping;


public class TestOOTypeStuff {
    
    @Test
    public void testOOTypeBridge() {
        assertTrue(OOTypeBridge.isTranslatableSingleType(XAddress.class));
    }
    
    @Test
    public void testOOTypeMapping() {
        assertNotNull(OOTypeMapping.getMapping(Boolean.class, null));
        assertNotNull(OOTypeMapping.getMapping(XBooleanValue.class, null));
        assertNotNull(OOTypeMapping.getMapping(XBooleanListValue.class, null));
        assertNotNull(OOTypeMapping.getMapping(XAddressSortedSetValue.class, null));
    }
    
}
