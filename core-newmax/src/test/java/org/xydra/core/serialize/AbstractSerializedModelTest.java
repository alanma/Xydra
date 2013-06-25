package org.xydra.core.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.value.XV;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XX;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.util.XCompareSyncUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Test serializing {@link XReadableRepository}, {@link XReadableModel},
 * {@link XReadableObject} and {@link XReadableField} types to/from XML.
 * 
 * @author dscharrer
 * 
 */
abstract public class AbstractSerializedModelTest extends AbstractSerializingTest {
    
    private static final Logger log = getLogger();
    
    private static Logger getLogger() {
        LoggerTestHelper.init();
        return LoggerFactory.getLogger(AbstractSerializedModelTest.class);
    }
    
    private XId actorId = XX.toId("a-test-user");
    
    void checkNoRevisions(XReadableField field) {
        assertEquals(SerializedModel.NO_REVISION, field.getRevisionNumber());
    }
    
    void checkNoRevisions(XReadableModel model) {
        assertEquals(SerializedModel.NO_REVISION, model.getRevisionNumber());
        for(XId objectId : model) {
            checkNoRevisions(model.getObject(objectId));
        }
    }
    
    void checkNoRevisions(XReadableObject object) {
        assertEquals(SerializedModel.NO_REVISION, object.getRevisionNumber());
        for(XId fieldId : object) {
            checkNoRevisions(object.getField(fieldId));
        }
    }
    
    void checkNoRevisions(XReadableRepository repo) {
        for(XId modelId : repo) {
            checkNoRevisions(repo.getModel(modelId));
        }
    }
    
    @Test
    public void testEmptyField() {
        testField(new MemoryField(this.actorId, DemoModelUtil.ALIASES_ID));
    }
    
    @Test
    public void testEmptyModel() {
        MemoryModel empty = new MemoryModel(this.actorId, null, DemoModelUtil.PHONEBOOK_ID);
        assert empty.exists();
        testModel(empty);
    }
    
    @Test
    public void testEmptyObject() {
        testObject(new MemoryObject(this.actorId, null, DemoModelUtil.JOHN_ID));
    }
    
    @Test
    public void testEmptyRepository() {
        testRepository(new MemoryRepository(this.actorId, null, XX.toId("repo")));
    }
    
    private void testField(XReadableField field) {
        
        // test serializing with revisions
        XydraOut out = create();
        SerializedModel.serialize(field, out);
        assertTrue(out.isClosed());
        String data = out.getData();
        log.debug(data);
        XydraElement e = parse(data);
        XField fieldAgain = SerializedModel.toField(this.actorId, e);
        assertTrue(XCompareUtils.equalState(field, fieldAgain));
        
        // test serializing without revisions
        out = create();
        SerializedModel.serialize(field, out, false);
        assertTrue(out.isClosed());
        data = out.getData();
        log.debug(data);
        e = parse(data);
        fieldAgain = SerializedModel.toField(this.actorId, e);
        assertTrue(XCompareUtils.equalTree(field, fieldAgain));
        checkNoRevisions(fieldAgain);
        
    }
    
    @Test
    public void testFullField() {
        XField field = new MemoryField(this.actorId, DemoModelUtil.ALIASES_ID);
        field.setValue(XV.toStringSetValue(new String[] { "Cookie Monster" }));
        testField(field);
    }
    
    @Test
    public void testFullModel() {
        XModel model = new MemoryModel(this.actorId, null, DemoModelUtil.PHONEBOOK_ID);
        DemoModelUtil.setupPhonebook(model);
        testModel(model);
    }
    
    @Test
    public void testFullObject() {
        XObject object = new MemoryObject(this.actorId, null, DemoModelUtil.JOHN_ID);
        DemoModelUtil.setupJohn(object);
        testObject(object);
    }
    
    @Test
    public void testFullRepository() {
        XRepository repo = new MemoryRepository(this.actorId, null, XX.toId("repo"));
        DemoModelUtil.addPhonebookModel(repo);
        testRepository(repo);
    }
    
    @Test
    public void testFullModelWithNoSyncLog() {
        XModel model = new MemoryModel(this.actorId, null, DemoModelUtil.PHONEBOOK_ID);
        DemoModelUtil.setupPhonebook(model);
        
        // test serializing with revisions
        XydraOut out = create();
        out.enableWhitespace(true, true);
        SerializedModel.serialize(model, out, true, false, false);
        assertTrue(out.isClosed());
        String data = out.getData();
        log.info(data);
        XydraElement e = parse(data);
        XModel modelAgain = SerializedModel.toModel(this.actorId, null, e);
        assertTrue(XCompareUtils.equalState(model, modelAgain));
        assertFalse(XCompareSyncUtils.equalHistory(model, modelAgain));
        
        // check that there is a change log
        XChangeLog changeLog = modelAgain.getChangeLog();
        assertNotNull(changeLog);
    }
    
    @Test
    public void testEmptyModelWithNoSyncLog() {
        MemoryModel model = new MemoryModel(this.actorId, null, DemoModelUtil.PHONEBOOK_ID);
        
        // test serializing with revisions
        XydraOut out = create();
        out.enableWhitespace(true, true);
        SerializedModel.serialize(model, out, true, false, false);
        assertTrue(out.isClosed());
        String data = out.getData();
        log.info(data);
        XydraElement e = parse(data);
        XModel modelAgain = SerializedModel.toModel(this.actorId, null, e);
        assertTrue(XCompareUtils.equalState(model, modelAgain));
        
        // check that there is a change log
        XChangeLog changeLog = modelAgain.getChangeLog();
        assertNotNull(changeLog);
    }
    
    private void testModel(XReadableModel model) {
        
        // test serializing with revisions
        XydraOut out = create();
        out.enableWhitespace(true, true);
        SerializedModel.serialize(model, out);
        assertTrue(out.isClosed());
        String data = out.getData();
        log.info(data);
        XydraElement e = parse(data);
        XModel modelAgain = SerializedModel.toModel(this.actorId, null, e);
        assertTrue(XCompareUtils.equalState(model, modelAgain));
        assertTrue(XCompareSyncUtils.equalHistory(model, modelAgain));
        
        // check that there is a change log
        XChangeLog changeLog = modelAgain.getChangeLog();
        assertNotNull(changeLog);
        
        // test serializing without revisions
        out = create();
        SerializedModel.serialize(model, out, false, true, false);
        assertTrue(out.isClosed());
        data = out.getData();
        log.debug(data);
        e = parse(data);
        modelAgain = SerializedModel.toModel(this.actorId, null, e);
        assertTrue(XCompareUtils.equalTree(model, modelAgain));
        checkNoRevisions(modelAgain);
        
    }
    
    private void testObject(XReadableObject object) {
        
        // test serializing with revisions
        XydraOut out = create();
        SerializedModel.serialize(object, out);
        assertTrue(out.isClosed());
        String data = out.getData();
        log.debug(data);
        XydraElement e = parse(data);
        XObject objectAgain = SerializedModel.toObject(this.actorId, null, e);
        assertTrue(XCompareUtils.equalState(object, objectAgain));
        assertTrue(XCompareSyncUtils.equalHistory(object, objectAgain));
        
        // check that there is a change log
        XChangeLog changeLog = objectAgain.getChangeLog();
        assertNotNull(changeLog);
        
        // test serializing without revisions
        out = create();
        SerializedModel.serialize(object, out, false, true, false);
        assertTrue(out.isClosed());
        data = out.getData();
        log.debug(data);
        e = parse(data);
        objectAgain = SerializedModel.toObject(this.actorId, null, e);
        assertTrue(XCompareUtils.equalTree(object, objectAgain));
        checkNoRevisions(objectAgain);
        
    }
    
    /**
     * DESERIALIZED REPOSITORIES DO NOT CONTAIN SYNCLOG
     * 
     * @param repo
     */
    private void testRepository(XReadableRepository repo) {
        
        // test serializing with revisions
        XydraOut out = create();
        SerializedModel.serialize(repo, out);
        assertTrue(out.isClosed());
        String data = out.getData();
        log.debug(data);
        XydraElement e = parse(data);
        XRepository repoAgain = SerializedModel.toRepository(this.actorId, null, e);
        assertTrue(XCompareUtils.equalState(repo, repoAgain));
        // assertTrue(XCompareUtils.equalHistory(repo, repoAgain));
        
        // test serializing without revisions
        out = create();
        SerializedModel.serialize(repo, out, false, true, false);
        assertTrue(out.isClosed());
        data = out.getData();
        log.debug(data);
        e = parse(data);
        repoAgain = SerializedModel.toRepository(this.actorId, null, e);
        assertTrue(XCompareUtils.equalTree(repo, repoAgain));
        checkNoRevisions(repoAgain);
        
    }
    
}
