package org.xydra.base.id;

import static org.junit.Assert.assertFalse;

import org.junit.Test;


public class MemoryStringIDProviderTest {
    
    @Test
    public void testIsValidId() {
        // 'ß' in UTF8 is 'ÃŸ'
        assertFalse(MemoryStringIDProvider.isValidId("GenuÃrechte"));
        assertFalse(MemoryStringIDProvider.isValidId("Genußrechte"));
    }
    
}
