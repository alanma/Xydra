package org.xydra.base.id;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class MemoryStringIDProviderTest {

    @Test
    public void testIsValidId() {
        // 'ß' in UTF8 is 'ÃŸ' (second char is invisible control character)
        assertFalse(BaseStringIDProvider.isValidId("GenuÃrechte"));
        // contains an invisible control characters before the 'r'
        final String evil = "Genußrechte";
        assert evil.length() == 12;
        assertFalse(BaseStringIDProvider.isValidId(evil));
        final String normal = "Genußrechte";
        assert normal.length() == 11;
        assertTrue(BaseStringIDProvider.isValidId(normal));
    }

}
