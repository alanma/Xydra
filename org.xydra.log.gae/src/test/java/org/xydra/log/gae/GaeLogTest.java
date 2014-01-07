package org.xydra.log.gae;

import org.junit.BeforeClass;
import org.xydra.log.XydraLogTest;
import org.xydra.log.impl.universal.UniversalLoggerFactorySPI;


public class GaeLogTest extends XydraLogTest {
    
    @BeforeClass
    public static void init() {
        UniversalLoggerFactorySPI.activate(false, false);
    }
    
}
