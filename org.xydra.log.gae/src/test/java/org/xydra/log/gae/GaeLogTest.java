package org.xydra.log.gae;

import org.junit.BeforeClass;
import org.xydra.log.XydraLogTest;
import org.xydra.log.impl.universal.UniversalLogger;


public class GaeLogTest extends XydraLogTest {
    
    @BeforeClass
    public static void init() {
        UniversalLogger.activate(false, false);
    }
    
}
