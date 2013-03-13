package org.xydra.conf.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.xydra.conf.Conf;


@SuppressWarnings("deprecation")
public class ConfTest {
    
    @Test
    public void testBasics() {
        assertNotNull(Conf.ig());
        assertFalse(Conf.ig().getDefinedKeys().iterator().hasNext());
    }
    
}
