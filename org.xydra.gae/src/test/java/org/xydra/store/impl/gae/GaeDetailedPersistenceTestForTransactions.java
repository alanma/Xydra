package org.xydra.store.impl.gae;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.core.X;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.AbstractPersistenceTestForTransactions;
import org.xydra.store.XydraRuntime;


public class GaeDetailedPersistenceTestForTransactions extends AbstractPersistenceTestForTransactions {
    
    /*
     * FIXME Running the complete test on my machine sometimes results in an IO
     * Exception ("IO Allocation Error" or something like that) of Eclipse (not
     * of Java), which crashes Eclipse. According to the Eclipse readme this
     * happens when the Java heap space is full and can be fixed by making the
     * heap space bigger through an Eclipse parameter - but this doesn't seem to
     * be the right way to go, using Xydra as I do in the test shouldn't result
     * in allocating the complete heap space, I think.
     * 
     * I unfortunately don't know the reason. Are the XModel-Java Objects too
     * big or is the output on the console from the logger "too much"? Running
     * the MemoryPersistenceTest of org.xydra.core never shows this behavior.
     * 
     * ~Kaidel
     */
    
    private static final Logger log = LoggerFactory.getLogger(GaeDetailedPersistenceTestForTransactions.class);
    
    @BeforeClass
    public static void beforeClazz() {
        GaeTestfixer.enable();
    }
    
    @Before
    public void before() {
        GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
        
        XydraRuntime.forceReInitialisation();
        
        super.persistence = new GaePersistence(super.repoId);
        super.persistence.clear();
        super.comFactory = X.getCommandFactory();
        
        Assert.assertTrue(log.isDebugEnabled());
    }
    
}
