package org.xydra.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.common.NanoClock;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.XydraRuntime;


public abstract class AbstractPersistencePerformanceTest {

    private static final Logger log = LoggerFactory
            .getLogger(AbstractPersistencePerformanceTest.class);

    private static XId actorId = XX.toId("actor1");
    private static int x = 3;

    @Before
    public void setUp() {
        log.info("Creating fresh persistence");
    }

    @After
    public void after() {
        XydraRuntime.finishRequest();
    }

    public abstract XydraPersistence createPersistence(XId repositoryId);

    @Test
    public void testPerformanceDirect() {
        log.info("TEST testPerformanceDirect");
        final XId repositoryId = Base.toId("testPerformanceDirect");
        final XydraPersistence persistence = createPersistence(repositoryId);
        final NanoClock c = new NanoClock().start();
        final XId modelId = Base.toId("model1");
        /* Add model1 */
        final XRepositoryCommand addModelCmd = BaseRuntime.getCommandFactory().createForcedAddModelCommand(
                repositoryId, modelId);
        c.stopAndStart("create-cmd");
        long l = persistence.executeCommand(actorId, addModelCmd);
        assertTrue("" + l, l == 0);
        c.stopAndStart("add-model");

        /* Add same model again */
        l = persistence.executeCommand(actorId, addModelCmd);
        assertTrue("" + l, l == XCommand.NOCHANGE);
        c.stopAndStart("add-model-again");

        /* Add objects */
        createObjects(persistence, repositoryId, modelId, x);
        c.stopAndStart("add-" + x + "-objects");

        final GetWithAddressRequest getRequest = new GetWithAddressRequest(Base.toAddress(repositoryId,
                modelId, null, null));

        final long modelRev = persistence.getModelRevision(getRequest).revision();
        assertTrue("modelRev=" + modelRev + " x=" + x, modelRev >= x);

        /* Get snapshot */
        XWritableModel snap = persistence.getModelSnapshot(getRequest);
        c.stopAndStart("get-modelsnapshot");
        assertEquals(modelRev, snap.getRevisionNumber());

        /* Get snapshot again */
        snap = persistence.getModelSnapshot(getRequest);
        c.stopAndStart("get-modelsnapshot2");
        assertEquals(modelRev, snap.getRevisionNumber());

        final Set<XId> set = org.xydra.index.IndexUtils.toSet(snap.iterator());
        assertEquals(x, set.size());
        log.info(c.getStats());
    }

    private static void createObjects(final XydraPersistence persistence, final XId repositoryId, final XId modelId,
            final int ocount) {
        for(int i = 0; i < ocount; i++) {
            final XId objectId = Base.toId("object" + i);
            final long l = persistence
                    .executeCommand(
                            actorId,
                            BaseRuntime.getCommandFactory().createForcedAddObjectCommand(
                                    Base.resolveModel(repositoryId, modelId), objectId));
            assertTrue(l >= 0);
        }
    }

    @SuppressWarnings("unused")
    private static void createObjects(final XWritableModel model, final int ocount) {
        for(int i = 0; i < ocount; i++) {
            final XId objectId = Base.toId("object" + i);
            final XWritableObject object = model.createObject(objectId);
            XyAssert.xyAssert(objectId.equals(object.getId()));
            XyAssert.xyAssert(model.hasObject(objectId));
        }
    }

    @Test
    // TODO rewrite with sessions
    public void testPerformanceIndirect() {
        log.info("TEST testPerformanceIndirect");
        // XId repositoryId = XX.toId("testPerformanceIndirect");
        // XydraPersistence persistence = createPersistence(repositoryId);
        // XId modelId = XX.toId("model1");
        // XAddress modelAddress = XX.toAddress(repositoryId, modelId, null,
        // null);
        //
        // NanoClock c = new NanoClock().start();
        //
        // WritableRepositoryOnPersistence baseRepository = new
        // WritableRepositoryOnPersistence(
        // persistence, actorId);
        // RWCachingRepository repo = new RWCachingRepository(baseRepository,
        // persistence, true);
        //
        // XWritableModel model = repo.createModel(modelId);
        // c.stopAndStart("add-model");
        //
        // model = repo.createModel(modelId);
        // c.stopAndStart("add-model-again");
        // assertEquals("model has just been created, empty", 0,
        // org.xydra.index.IndexUtils.toSet(model.iterator()).size());
        //
        // createObjects(model, x);
        // c.stopAndStart("add-" + x + "-objects");
        // assertEquals(x,
        // org.xydra.index.IndexUtils.toSet(model.iterator()).size());
        //
        // int result = RWCachingRepository.commit(repo, actorId);
        // assertEquals(200, result);
        // c.stopAndStart("commit");
        //
        // long rev = persistence.getModelRevision(modelAddress).revision();
        // assertEquals("0=createModel,1=txnWithXObjects => expect 1 but is " +
        // rev, 1, rev);
        //
        // XWritableModel snap = persistence.getModelSnapshot(modelAddress);
        // XyAssert.xyAssert(snap != null); assert snap != null;
        // Set<XId> set = org.xydra.index.IndexUtils.toSet(snap.iterator());
        // assertEquals(x, set.size());
        //
        // c.stopAndStart("get-modelsnapshot");
        // snap = persistence.getModelSnapshot(XX.toAddress(repositoryId,
        // modelId, null, null));
        // c.stopAndStart("get-modelsnapshot2");
        // set = org.xydra.index.IndexUtils.toSet(snap.iterator());
        // assertEquals(x, set.size());
        // log.info(c.getStats());
    }

}
