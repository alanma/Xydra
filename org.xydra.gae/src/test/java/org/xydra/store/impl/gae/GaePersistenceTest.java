package org.xydra.store.impl.gae;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.util.DumpUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.XydraRuntime;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


public class GaePersistenceTest {
    
    private static final Logger log = LoggerFactory.getLogger(GaePersistenceTest.class);
    
    private static final XId ACTOR = XX.toId("tester");
    
    @Before
    public void setUp() {
        GaeTestfixer.enable();
        GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
    }
    
    @After
    public void tearDown() {
        XydraRuntime.finishRequest();
    }
    
    @Test
    public void testQueryIds() {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
        XId repoId = XX.toId("repo-testQueryIds");
        
        XydraPersistence pers = new GaePersistence(repoId);
        
        XId modelId = XX.createUniqueId();
        XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
        XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
        GetWithAddressRequest modelAddressRequest = new GetWithAddressRequest(modelAddr);
        
        assertFalse(pers.hasManagedModel(modelId));
        assertEquals(new ModelRevision(-1L, false), pers.getModelRevision(modelAddressRequest));
        
        /* Create model */
        pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
        assertEquals(new ModelRevision(0, true), pers.getModelRevision(modelAddressRequest));
        assertTrue(pers.hasManagedModel(modelId));
        
        log.info("###   ADD object ");
        XId objectId = XX.createUniqueId();
        assertFalse(pers.getModelSnapshot(modelAddressRequest).hasObject(objectId));
        /* Create object */
        long l = pers.executeCommand(ACTOR,
                MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
        assert l >= 0 : "" + l;
        log.info("###   Verify revNr ");
        assertEquals(1, pers.getModelRevision(modelAddressRequest).revision());
        assertEquals(1, pers.getModelRevision(modelAddressRequest).revision());
        assertEquals(1, pers.getModelRevision(modelAddressRequest).revision());
        
        log.info("###   Clear memcache");
        XydraRuntime.getMemcache().clear();
        
        pers = new GaePersistence(repoId);
        
        log.info("###   hasModel?");
        assertTrue(pers.hasManagedModel(modelId));
        log.info("###   getSnapshot");
        assertEquals(1, pers.getModelRevision(modelAddressRequest).revision());
        XWritableModel modelSnapshot = pers.getModelSnapshot(modelAddressRequest);
        assertNotNull("snapshot " + modelAddressRequest + " was null", modelSnapshot);
        log.info("###   dumping");
        DumpUtils.dump("modelSnapshot", modelSnapshot);
        assertTrue("model should have object", modelSnapshot.hasObject(objectId));
        assertNotNull(pers.getObjectSnapshot(new GetWithAddressRequest(XX.resolveObject(modelAddr,
                objectId), true)));
        assertTrue(pers.getModelSnapshot(modelAddressRequest).hasObject(objectId));
    }
    
    @Test
    public void getEmtpyModel() {
        XId repoId = XX.toId("repo-getEmtpyModel");
        XydraPersistence pers = new GaePersistence(repoId);
        XId modelId = XX.toId("model-getEmtpyModel");
        
        ModelRevision modelRev = pers.getModelRevision(new GetWithAddressRequest(XX.resolveModel(
                repoId, modelId), true));
        XyAssert.xyAssert(modelRev != null);
        assert modelRev != null;
        XyAssert.xyAssert(!modelRev.modelExists(), "modelExists should be false but rev is "
                + modelRev + " for " + modelId);
        
        WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(pers, ACTOR);
        XyAssert.xyAssert(!repo.hasModel(modelId));
        
        repo.createModel(modelId);
        assertNotNull(repo.getModel(modelId));
        assertNull(repo.getModel(XX.createUniqueId()));
    }
    
    @Test
    public void testTrickyRevisionNumbersForModels() {
        XydraPersistence pers = new GaePersistence(XX.toId("test-repo6"));
        
        XId modelId = XX.createUniqueId();
        XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
        XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
        GetWithAddressRequest modelAddressRequest = new GetWithAddressRequest(modelAddr);
        
        assert !pers.getManagedModelIds().contains(modelId);
        assertEquals(new ModelRevision(-1L, false), pers.getModelRevision(modelAddressRequest));
        
        pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
        
        assertEquals(new ModelRevision(0L, true), pers.getModelRevision(modelAddressRequest));
        assert pers.getManagedModelIds().contains(modelId);
        
        pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createRemoveCommand(repoAddr, -1, modelId));
        
        assertEquals(new ModelRevision(1L, false), pers.getModelRevision(modelAddressRequest));
        
        assert !pers.getModelRevision(modelAddressRequest).modelExists();
    }
    
    @Test
    public void testAddFieldsValuesTwice() {
        XydraPersistence p;
        
        // prepare commands
        XStringValue value1 = XV.toValue("value1");
        XStringValue value2 = XV.toValue("value2");
        XFieldCommand addValue;
        XFieldCommand chgValue;
        XFieldCommand remValue;
        
        long result;
        /* Scenarios to test: */
        p = preparePersistenceWithModelAndObject("repo1", false);
        addValue = MemoryFieldCommand.createAddCommand(
                XX.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
                value1);
        result = p.executeCommand(ACTOR, addValue);
        assertEquals("no field: addValue (fail)", XCommand.FAILED, result);
        
        p = preparePersistenceWithModelAndObject("repo2", false);
        remValue = MemoryFieldCommand.createRemoveCommand(
                XX.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED);
        result = p.executeCommand(ACTOR, remValue);
        assertEquals("no field: remValue (fail)", XCommand.FAILED, result);
        
        p = preparePersistenceWithModelAndObject("repo3", false);
        chgValue = MemoryFieldCommand.createChangeCommand(
                XX.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
                value2);
        result = p.executeCommand(ACTOR, chgValue);
        assertEquals("no field: chgValue (fail)", XCommand.FAILED, result);
        
        /* now with field */
        
        p = preparePersistenceWithModelAndObject("repo4", true);
        addValue = MemoryFieldCommand.createAddCommand(
                XX.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
                value1);
        result = p.executeCommand(ACTOR, addValue);
        assertEquals("field exists: addValue (succ)", 3, result);
        
        p = preparePersistenceWithModelAndObject("repo5", true);
        remValue = MemoryFieldCommand.createRemoveCommand(
                XX.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED);
        result = p.executeCommand(ACTOR, remValue);
        assertEquals("field exists: remValue (fail)", XCommand.NOCHANGE, result);
        
        p = preparePersistenceWithModelAndObject("repo6", true);
        chgValue = MemoryFieldCommand.createChangeCommand(
                XX.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
                value2);
        result = p.executeCommand(ACTOR, chgValue);
        assertEquals("field exists: chgValue (succ)", 3, result);
        
        p = preparePersistenceWithModelAndObject("repo7", true);
        addValue = MemoryFieldCommand.createAddCommand(
                XX.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
                value1);
        result = p.executeCommand(ACTOR, addValue);
        assertEquals("field exists: addValue (succ)", 3, result);
        result = p.executeCommand(ACTOR, addValue);
        assertEquals("field exists: addValue (succ), addValue (noChg)", XCommand.NOCHANGE, result);
        chgValue = MemoryFieldCommand.createChangeCommand(
                XX.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
                value2);
        result = p.executeCommand(ACTOR, chgValue);
        assertEquals("field exists: addValue (succ), addValue (noChg),chgValue (succ)", 5, result);
        
        p = preparePersistenceWithModelAndObject("repo8", true);
        addValue = MemoryFieldCommand.createAddCommand(
                XX.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
                value1);
        chgValue = MemoryFieldCommand.createChangeCommand(
                XX.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
                value2);
        result = p.executeCommand(ACTOR, addValue);
        assertEquals("field exists: addValue (succ)", 3, result);
        result = p.executeCommand(ACTOR, chgValue);
        assertEquals("field exists: addValue (succ), chgValue (succ)", 4, result);
        result = p.executeCommand(ACTOR, chgValue);
        assertEquals("field exists: addValue (succ), chgValue (succ), chgValue (noChg)",
                XCommand.NOCHANGE, result);
        
        p = preparePersistenceWithModelAndObject("repo9", true);
        addValue = MemoryFieldCommand.createAddCommand(
                XX.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED,
                value1);
        remValue = MemoryFieldCommand.createRemoveCommand(
                XX.resolveField(p.getRepositoryId(), model1, object1, field1), XCommand.FORCED);
        result = p.executeCommand(ACTOR, addValue);
        assertEquals("field exists: addValue (succ)", 3, result);
        
        assertFalse(
                "field cannot be empty",
                p.getObjectSnapshot(
                        new GetWithAddressRequest(XX.resolveObject(p.getRepositoryId(), model1,
                                object1))).getField(field1).isEmpty());
        assertEquals(
                "field has value",
                value1,
                p.getObjectSnapshot(
                        new GetWithAddressRequest(XX.resolveObject(p.getRepositoryId(), model1,
                                object1))).getField(field1).getValue());
        
        result = p.executeCommand(ACTOR, remValue);
        assertEquals("field exists: addValue (succ), remValue (succ)", 4, result);
    }
    
    static final XId model1 = XX.toId("model1");
    static final XId object1 = XX.toId("object1");
    static final XId field1 = XX.toId("field1");
    
    private static XydraPersistence preparePersistenceWithModelAndObject(String id, boolean addField) {
        XydraPersistence p = new GaePersistence(XX.toId(id));
        WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(p, ACTOR);
        XWritableModel model = repo.createModel(model1);
        model.createObject(object1);
        if(addField) {
            /* create field */
            XObjectCommand addFieldCommand = MemoryObjectCommand
                    .createAddCommand(XX.resolveObject(p.getRepositoryId(), model1, object1),
                            XCommand.FORCED, field1);
            p.executeCommand(ACTOR, addFieldCommand);
        }
        return p;
    }
    
    @Test
    public void testAddAndRemove() {
        XydraPersistence pers = new GaePersistence(XX.toId("test-repo3"));
        
        XId modelId = XX.createUniqueId();
        XId objectId = XX.createUniqueId();
        XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
        XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
        GetWithAddressRequest modelAddressRequest = new GetWithAddressRequest(modelAddr);
        
        assert !pers.getManagedModelIds().contains(modelId);
        // action: create model
        pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
        // post-conditions:
        assert pers.getManagedModelIds().contains(modelId);
        assert !pers.getModelSnapshot(modelAddressRequest).hasObject(objectId);
        // action: create object in model
        pers.executeCommand(ACTOR, MemoryModelCommand.createAddCommand(modelAddr, true, objectId));
        
        // post-conditions:
        assert pers.getModelSnapshot(modelAddressRequest).hasObject(objectId);
        assertEquals(1, pers.getModelRevision(modelAddressRequest).revision());
        
        // action: delete model (implicitly delete object, too)
        long l = pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createRemoveCommand(repoAddr, 1, modelId));
        // post-conditions:
        assertEquals(2, l);
        assertEquals(2, pers.getModelRevision(modelAddressRequest).revision());
        assertNull(
                "modelsnapshot should be null after repo command remove, but is "
                        + pers.getModelRevision(modelAddressRequest),
                pers.getModelSnapshot(modelAddressRequest));
        assert !pers.getModelRevision(modelAddressRequest).modelExists();
        
        // action: re-create model
        pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
        assertEquals(3, pers.getModelRevision(modelAddressRequest).revision());
        assert pers.getModelRevision(modelAddressRequest).modelExists();
        
        // action: redundantly create model again
        l = pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
        assert l == XCommand.NOCHANGE : l;
        assert pers.getManagedModelIds().contains(modelId);
        assertEquals("nothing changed, so rev should stay the same", 3,
                pers.getModelRevision(modelAddressRequest).revision());
        assert pers.getModelSnapshot(modelAddressRequest) != null;
    }
    
    @Test
    public void testAddAndRemoveModel() {
        // XydraRuntime.getConfigMap().put(XydraRuntime.PROP_MEMCACHESTATS,
        // "true");
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
        
        XydraPersistence pers = new GaePersistence(XX.toId("test-repo4"));
        XId modelId = XX.toId("model1");
        XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
        XAddress modelAdd = XX.resolveModel(repoAddr, modelId);
        GetWithAddressRequest modelAddr = new GetWithAddressRequest(modelAdd);
        
        // assert absence
        assert !pers.getManagedModelIds().contains(modelId);
        // assert pers.getModelSnapshot(modelAddr) == null;
        
        // add model
        log.info("\n\n\n=== add\n\n\n");
        long l = pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
        assert l >= 0;
        assert pers.getManagedModelIds().contains(modelId);
        assertEquals(0, pers.getModelRevision(modelAddr).revision());
        
        // remove model
        log.info("\n\n\n=== remove\\n\\n\\n");
        l = pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createRemoveCommand(repoAddr, 0, modelId));
        assertEquals(1, l);
        assert !pers.getModelRevision(modelAddr).modelExists();
        // assert pers.getModelSnapshot(modelAddr) == null;
        assertEquals(1, pers.getModelRevision(modelAddr).revision());
        
        // add model
        log.info("\n\n\n=== add again\\n\\n\\n");
        l = pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
        assert l >= 0 : l;
        assert pers.getModelRevision(modelAddr).modelExists();
        assert pers.getManagedModelIds().contains(modelId);
        assert pers.getModelSnapshot(modelAddr) != null;
        assertEquals(2, pers.getModelRevision(modelAddr).revision());
        
        // System.out.println(StatsGatheringMemCacheWrapper.INSTANCE.stats());
    }
    
    public XydraPersistence createPersistence(XId repositoryId) {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
        // configureLog4j();
        GaeTestfixer.enable();
        GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
        InstanceContext.clear();
        XydraRuntime.init();
        XydraPersistence p = new GaePersistence(repositoryId);
        assert p.getManagedModelIds().isEmpty();
        return p;
    }
    
    @Test
    public void testSimpleOperations() {
        XId repositoryId = XX.toId("testSimpleOperations");
        XydraPersistence persistence = createPersistence(repositoryId);
        XId modelId = XX.toId("model1");
        GetWithAddressRequest getRequest = new GetWithAddressRequest(XX.toAddress(repositoryId,
                modelId, null, null));
        long modelRev;
        
        modelRev = persistence.getModelRevision(getRequest).revision();
        assertEquals(-1, modelRev);
        
        /* Add model1 */
        XRepositoryCommand addModelCmd = X.getCommandFactory().createForcedAddModelCommand(
                repositoryId, modelId);
        long l = persistence.executeCommand(ACTOR, addModelCmd);
        assertTrue("" + l, l == 0);
        modelRev = persistence.getModelRevision(getRequest).revision();
        assertEquals(0, modelRev);
        
        /* Add same model again */
        l = persistence.executeCommand(ACTOR, addModelCmd);
        assertTrue("" + l, l == XCommand.NOCHANGE);
        modelRev = persistence.getModelRevision(getRequest).revision();
        assertEquals(0, modelRev);
        
        /* Add object */
        XId objectId = XX.toId("object1");
        l = persistence.executeCommand(
                ACTOR,
                X.getCommandFactory().createForcedAddObjectCommand(
                        XX.resolveModel(repositoryId, modelId), objectId));
        assertEquals(2, l);
        modelRev = persistence.getModelRevision(getRequest).revision();
        assertEquals(2, modelRev);
        
        /* Get snapshot */
        XWritableModel snap = persistence.getModelSnapshot(getRequest);
        assertEquals(modelRev, snap.getRevisionNumber());
        
        Set<XId> set = org.xydra.index.IndexUtils.toSet(snap.iterator());
        assertEquals(1, set.size());
    }
    
    @Test
    public void testAddAndRemoveModelWithObject() {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
        
        XydraPersistence pers = new GaePersistence(XX.toId("test-repo5"));
        XId modelId = XX.toId("model1");
        XId objectId = XX.toId("object1");
        XAddress repoAddr = XX.toAddress(pers.getRepositoryId(), null, null, null);
        XAddress modelAddress = XX.resolveModel(repoAddr, modelId);
        GetWithAddressRequest modelAddressRequest = new GetWithAddressRequest(modelAddress);
        
        XCommand addObjectCommand = MemoryModelCommand.createAddCommand(modelAddress, true,
                objectId);
        
        // assert absence
        assert !pers.getManagedModelIds().contains(modelId);
        
        // add model & object
        long l = pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
        assert l >= 0;
        assert pers.getManagedModelIds().contains(modelId);
        assertEquals(0, pers.getModelRevision(modelAddressRequest).revision());
        l = pers.executeCommand(ACTOR, addObjectCommand);
        assert l >= 0;
        assertEquals(1, pers.getModelRevision(modelAddressRequest).revision());
        
        // remove model
        l = pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createRemoveCommand(repoAddr, -1, modelId));
        assert l >= 0 : l;
        assert pers.getManagedModelIds().contains(modelId);
        assert !pers.getModelRevision(modelAddressRequest).modelExists();
        // assert pers.getModelSnapshot(modelAddr) == null;
        assertEquals(2, pers.getModelRevision(modelAddressRequest).revision());
        
        // add model & object
        l = pers.executeCommand(ACTOR,
                MemoryRepositoryCommand.createAddCommand(repoAddr, true, modelId));
        assert l >= 0;
        assert pers.getManagedModelIds().contains(modelId);
        assert pers.getModelRevision(modelAddressRequest).modelExists();
        assertEquals(3, pers.getModelRevision(modelAddressRequest).revision());
        
        l = pers.executeCommand(ACTOR, addObjectCommand);
        
        assert l >= 0 : "" + l;
        assertEquals(4, pers.getModelRevision(modelAddressRequest).revision());
        
        XWritableModel snap = pers.getModelSnapshot(modelAddressRequest);
        assertEquals(4, snap.getRevisionNumber());
        
        // TODO compare snapshot & revNr directly
        
        // System.out.println(StatsGatheringMemCacheWrapper.INSTANCE.stats());
    }
    
}
