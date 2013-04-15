package org.xydra.core.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleRepository;
import org.xydra.base.value.XValue;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.impl.memory.CommandExecutor;
import org.xydra.sharedutils.XyAssert;


/*
 * TODO txn: maybe test executing transactions more thoroughly
 */
public class ChangedModelTest {
    
    private ChangedModel changedModel;
    private XRevWritableModel model;
    XRevWritableObject object;
    private XRevWritableObject object2;
    private XWritableField field, fieldWithValue;
    
    {
        LoggerTestHelper.init();
    }
    
    @Before
    public void setup() {
        XId modelId = XX.createUniqueId();
        XId objectId = XX.createUniqueId();
        XId object2Id = XX.createUniqueId();
        XId fieldId = XX.createUniqueId();
        XId fieldWithValueId = XX.createUniqueId();
        
        SimpleRepository repo = new SimpleRepository(XX.resolveRepository(XX.toId("testActor")));
        this.model = repo.createModel(modelId);
        this.object = this.model.createObject(objectId);
        this.object2 = this.model.createObject(object2Id);
        
        // add two fields
        this.field = this.object.createField(fieldId);
        this.fieldWithValue = this.object.createField(fieldWithValueId);
        
        // set its value
        XValue value = X.getValueFactory().createStringValue("test value");
        this.fieldWithValue.setValue(value);
        
        this.changedModel = new ChangedModel(this.model);
    }
    
    // Tests for commit() {
    
    /*
     * for commit():
     * 
     * TODO txn: check cases were something that already existed gets deleted
     * and something new with the same address gets added (see if the old thing
     * is actually replaced by something new)
     * 
     * TODO txn: maybe check forced commands in transactions
     */
    
    @Test
    public void testCommitSingleModelCommands() {
        
        // add a new XObject
        XId objectId = X.getIDProvider().createUniqueId();
        assertFalse(this.changedModel.hasObject(objectId) || this.model.hasObject(objectId));
        
        this.changedModel.createObject(objectId);
        
        assertTrue(this.changedModel.hasChanges());
        assertEquals(1, this.changedModel.countCommandsNeeded(10));
        
        long oldRevNr = this.model.getRevisionNumber();
        long revNr = this.commit();
        
        assertFalse(this.changedModel.hasChanges());
        
        assertTrue(revNr != XCommand.FAILED);
        assertEquals(oldRevNr + 1, revNr);
        assertEquals(revNr, this.changedModel.getRevisionNumber());
        
        // check that the object was added to the real model
        assertTrue(this.model.hasObject(objectId));
        
        // remove an XObject
        
        this.changedModel.removeObject(objectId);
        
        assertTrue(this.changedModel.hasChanges());
        
        oldRevNr = this.model.getRevisionNumber();
        revNr = this.commit();
        
        assertFalse(this.changedModel.hasChanges());
        
        assertTrue(revNr != XCommand.FAILED);
        assertEquals(oldRevNr + 1, revNr);
        assertEquals(revNr, this.changedModel.getRevisionNumber());
        
        // check that the object was removed from the real model
        assertFalse(this.model.hasObject(objectId));
    }
    
    private long commit() {
        XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
        tb.applyChanges(this.changedModel);
        XTransaction txn = tb.build();
        
        long result = CommandExecutor.checkPreconditionsChangeStateSendEvents(
                this.model.getAddress(), txn, XX.toId("testActor"), this.model, null, null);
        
        this.changedModel.reset();
        assert result == -1 || result == this.model.getRevisionNumber() : "result=" + result
                + " modelRev=" + this.model.getRevisionNumber();
        return result;
    }
    
    private long executeCommand(XCommand command, XLocalChangeCallback callback) {
        // careful
        ChangedModel wrapper = new ChangedModel(this.changedModel);
        boolean success = wrapper.executeCommand(command);
        
        if(!success) {
            if(callback != null) {
                callback.onFailure();
            }
            return XCommand.FAILED;
        }
        
        boolean success2 = this.changedModel.executeCommand(command);
        assert success == success2;
        long rev = this.changedModel.getRevisionNumber()
                + this.changedModel.countCommandsNeeded(1000);
        if(callback != null) {
            if(success)
                callback.onSuccess(rev);
        }
        return rev;
    }
    
    @Test
    public void testCommitSingleObjectCommands() {
        XWritableObject transObject = this.changedModel.getObject(this.object.getId());
        
        // add a new XField
        XId fieldId = X.getIDProvider().createUniqueId();
        assertFalse(transObject.hasField(fieldId) || this.object.hasField(fieldId));
        
        transObject.createField(fieldId);
        
        assertTrue(this.changedModel.hasChanges());
        
        long oldRevNr = this.model.getRevisionNumber();
        long revNr = this.commit();
        
        assertFalse(this.changedModel.hasChanges());
        
        assertTrue(revNr != XCommand.FAILED);
        assertEquals(oldRevNr + 1, revNr);
        assertEquals(revNr, this.changedModel.getRevisionNumber());
        
        // check that the field was added to the real object
        assertTrue(this.object.hasField(fieldId));
        
        // remove an XField
        transObject = this.changedModel.getObject(this.object.getId());
        transObject.removeField(fieldId);
        
        assertTrue(this.changedModel.hasChanges());
        
        oldRevNr = this.model.getRevisionNumber();
        revNr = this.commit();
        
        assertFalse(this.changedModel.hasChanges());
        
        assertTrue(revNr != XCommand.FAILED);
        assertEquals(oldRevNr + 1, revNr);
        assertEquals(revNr, this.changedModel.getRevisionNumber());
        
        // check that the field was removed from the real object
        assertFalse(this.object.hasField(fieldId));
    }
    
    @Test
    public void testSingeFieldCommands() {
        XWritableObject transObject = this.changedModel.getObject(this.object.getId());
        
        // add a value
        transObject = this.changedModel.getObject(this.object.getId());
        XValue value = X.getValueFactory().createStringValue("testValue");
        XWritableField field = transObject.getField(this.field.getId());
        
        field.setValue(value);
        
        assertTrue(this.changedModel.hasChanges());
        
        long oldRevNr = this.model.getRevisionNumber();
        long revNr = this.commit();
        
        assertFalse(this.changedModel.hasChanges());
        
        assertTrue(revNr != XCommand.FAILED);
        assertEquals(oldRevNr + 1, revNr);
        assertEquals(revNr, this.changedModel.getRevisionNumber());
        
        // check that the value was added to the field
        assertEquals(value, this.field.getValue());
        
        // change a value
        transObject = this.changedModel.getObject(this.object.getId());
        value = X.getValueFactory().createStringValue("testValue2");
        field = transObject.getField(this.field.getId());
        
        field.setValue(value);
        
        assertTrue(this.changedModel.hasChanges());
        
        oldRevNr = this.model.getRevisionNumber();
        revNr = this.commit();
        
        assertFalse(this.changedModel.hasChanges());
        
        assertTrue(revNr != XCommand.FAILED);
        assertEquals(oldRevNr + 1, revNr);
        assertEquals(revNr, this.changedModel.getRevisionNumber());
        
        // check that the value of the field was changed
        assertEquals(value, this.field.getValue());
        
        // remove a value
        transObject = this.changedModel.getObject(this.object.getId());
        field = transObject.getField(this.field.getId());
        
        field.setValue(null);
        
        assertTrue(this.changedModel.hasChanges());
        
        oldRevNr = this.model.getRevisionNumber();
        revNr = this.commit();
        
        assertFalse(this.changedModel.hasChanges());
        
        assertTrue(revNr != XCommand.FAILED);
        assertEquals(oldRevNr + 1, revNr);
        assertEquals(revNr, this.changedModel.getRevisionNumber());
        
        // check that the value was removed from the field
        assertEquals(null, this.field.getValue());
    }
    
    @Test
    public void testCommitTransaction() {
        // add some objects
        XId objectId1 = X.getIDProvider().createUniqueId();
        XId objectId2 = X.getIDProvider().createUniqueId();
        
        this.changedModel.createObject(objectId1);
        this.changedModel.createObject(objectId2);
        
        // remove an object
        
        this.changedModel.removeObject(objectId2);
        
        // add some fields
        XId fieldId1 = X.getIDProvider().createUniqueId();
        XId fieldId2 = X.getIDProvider().createUniqueId();
        XId fieldId3 = X.getIDProvider().createUniqueId();
        
        XWritableObject transObject = this.changedModel.getObject(this.object.getId());
        
        transObject.createField(fieldId1);
        transObject.createField(fieldId2);
        transObject.createField(fieldId3);
        
        // remove some fields
        transObject.removeField(this.field.getId());
        transObject.removeField(fieldId3);
        
        // add some values
        XValue value = X.getValueFactory().createStringValue("testValue");
        XWritableField field1 = transObject.getField(fieldId1);
        XWritableField field2 = transObject.getField(fieldId2);
        
        field1.setValue(value);
        field2.setValue(value);
        
        // remove a value
        field2.setValue(null);
        
        // change a value
        XWritableField temp = transObject.getField(this.fieldWithValue.getId());
        temp.setValue(value);
        
        // commit the transaction
        assertTrue(this.changedModel.hasChanges());
        
        long oldRevNr = this.model.getRevisionNumber();
        long revNr = this.commit();
        
        assertFalse(this.changedModel.hasChanges());
        
        assertTrue(revNr != XCommand.FAILED);
        assertEquals(oldRevNr + 1, revNr);
        assertEquals(revNr, this.changedModel.getRevisionNumber());
        
        // check that the changes were actually executed
        assertTrue(this.model.hasObject(objectId1));
        
        assertFalse(this.model.hasObject(objectId2));
        
        assertTrue(this.object.hasField(fieldId1));
        assertTrue(this.object.hasField(fieldId2));
        
        assertFalse(this.object.hasField(this.field.getId()));
        assertFalse(this.object.hasField(fieldId3));
        
        field1 = this.object.getField(fieldId1);
        assertEquals(value, field1.getValue());
        
        field2 = this.object.getField(fieldId2);
        assertEquals(null, field2.getValue());
        
        assertEquals(value, this.fieldWithValue.getValue());
        
        /*
         * execute another transaction to test whether cleared
         * TransactionObjects can be reused
         */
        this.changedModel.removeObject(objectId1);
        
        transObject = this.changedModel.getObject(this.object.getId());
        transObject.createField(fieldId3);
        XWritableField field3 = transObject.getField(fieldId3);
        
        field3.setValue(value);
        
        temp = transObject.getField(this.fieldWithValue.getId());
        temp.setValue(null);
        
        transObject.removeField(fieldId1);
        
        // commit the transaction
        assertTrue(this.changedModel.hasChanges());
        
        oldRevNr = this.model.getRevisionNumber();
        revNr = this.commit();
        
        assertFalse(this.changedModel.hasChanges());
        
        assertTrue(revNr != XCommand.FAILED);
        assertEquals(oldRevNr + 1, revNr);
        assertEquals(revNr, this.changedModel.getRevisionNumber());
        
        // check that the changes were actually executed
        assertFalse(this.model.hasObject(objectId1));
        
        assertTrue(this.object.hasField(fieldId3));
        
        assertFalse(this.object.hasField(fieldId1));
        
        field3 = this.object.getField(fieldId3);
        assertNotNull(field3);
        assertEquals(value, field3.getValue());
        
        assertEquals(null, this.fieldWithValue.getValue());
    }
    
    // Tests for getAddress()
    @Test
    public void testGetAddress() {
        assertEquals(this.model.getAddress(), this.changedModel.getAddress());
    }
    
    // Tests for getId()
    @Test
    public void testGetId() {
        assertEquals(this.model.getId(), this.changedModel.getId());
    }
    
    /*
     * Tests for executeCommand(XCommand command, XLocalCallback callback)
     * 
     * Note: the other executeCommand without the callback-parameter needs no
     * extra tests, since the only thing it does is call this method
     */
    
    @Test
    public void testExecuteCommandsWrongXAddress() {
        XCommandFactory factory = X.getCommandFactory();
        XId newId = X.getIDProvider().createUniqueId();
        TestCallback callback = new TestCallback();
        XAddress randomFieldAddress = XX.toAddress(XX.createUniqueId(), XX.createUniqueId(),
                XX.createUniqueId(), XX.createUniqueId());
        XAddress randomObjectAddress = XX.toAddress(XX.createUniqueId(), XX.createUniqueId(),
                XX.createUniqueId(), null);
        
        XCommand objectCommand = factory.createAddFieldCommand(randomObjectAddress, newId, false);
        
        long result = this.executeCommand(objectCommand, callback);
        assertEquals(XCommand.FAILED, result);
        assertFalse(this.changedModel.hasChanges());
        
        // check callback
        assertTrue(callback.failed);
        assertNull(callback.revision);
        
        XCommand fieldCommand = factory.createAddFieldCommand(randomFieldAddress, newId, false);
        
        result = executeCommand(fieldCommand, callback);
        assertEquals(XCommand.FAILED, result);
        assertFalse(this.changedModel.hasChanges());
        
        // check callback
        assertTrue(callback.failed);
        assertNull(callback.revision);
        
        XCommand valueCommand = factory.createAddValueCommand(randomFieldAddress, 0,
                XX.createUniqueId(), false);
        
        result = executeCommand(valueCommand, callback);
        assertEquals(XCommand.FAILED, result);
        assertFalse(this.changedModel.hasChanges());
        
        // check callback
        assertTrue(callback.failed);
        assertNull(callback.revision);
    }
    
    // Tests for model commands
    
    public void executeCommandsAddObjectCommands(boolean forced) {
        XCommandFactory factory = X.getCommandFactory();
        XId newObjectId = X.getIDProvider().createUniqueId();
        TestCallback callback = new TestCallback();
        
        // make sure there is no object with this ID
        assertFalse(this.changedModel.hasObject(newObjectId));
        
        // add the object
        XCommand addCommand = factory.createAddObjectCommand(this.changedModel.getAddress(),
                newObjectId, forced);
        
        long result = executeCommand(addCommand, callback);
        assertTrue(result != XCommand.FAILED);
        
        // check callback
        assertFalse(callback.failed);
        assertEquals((Long)result, callback.revision);
        
        // check whether the object was added correctly
        assertTrue(this.changedModel.hasObject(newObjectId));
        
        XWritableObject object = this.changedModel.getObject(newObjectId);
        assertTrue(object.getRevisionNumber() >= 0);
        
        assertFalse(this.model.hasObject(newObjectId));
        
        // try to add an object that already exists
        addCommand = factory.createAddObjectCommand(this.changedModel.getAddress(),
                this.object.getId(), forced);
        callback = new TestCallback();
        
        result = executeCommand(addCommand, callback);
        if(forced) {
            // forced -> should succeed
            assertTrue(result != XCommand.FAILED);
            
            // check callback
            assertFalse(callback.failed);
            assertEquals((Long)result, callback.revision);
        } else {
            // not forced -> should fail
            assertEquals(XCommand.FAILED, result);
            
            // check callback
            assertTrue(callback.failed);
            assertNull(callback.revision);
        }
    }
    
    @Test
    public void testExecuteCommandsSafeAddObjectCommands() {
        executeCommandsAddObjectCommands(false);
    }
    
    @Test
    public void testExecuteCommandsForcedAddObjectCommands() {
        executeCommandsAddObjectCommands(true);
    }
    
    public void executeCommandsRemoveObjectCommands(boolean forced) {
        XCommandFactory factory = X.getCommandFactory();
        XId newObjectId = X.getIDProvider().createUniqueId();
        TestCallback callback = new TestCallback();
        
        // make sure there is no field with this ID
        assertFalse(this.changedModel.hasObject(newObjectId));
        
        // try to remove not existing object
        XAddress temp = this.changedModel.getAddress();
        XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), newObjectId, null);
        
        XCommand removeCommand = factory.createRemoveObjectCommand(address, 0, forced);
        
        long result = executeCommand(removeCommand, callback);
        if(forced) {
            // forced --> should succeed
            assertTrue(result != XCommand.FAILED);
            
            // check callback
            assertFalse(callback.failed);
            assertEquals((Long)result, callback.revision);
            
        } else {
            // not forced --> should fail
            assertEquals(XCommand.FAILED, result);
            
            // check callback
            assertTrue(callback.failed);
            assertNull(callback.revision);
        }
        
        // try to remove an object that already exists, should succeed
        removeCommand = factory.createRemoveObjectCommand(this.object.getAddress(),
                this.object.getRevisionNumber(), forced);
        callback = new TestCallback();
        
        result = executeCommand(removeCommand, callback);
        assertTrue(result != XCommand.FAILED);
        
        // check callback
        assertFalse(callback.failed);
        assertEquals((Long)result, callback.revision);
        
        // check whether the object was removed correctly
        assertFalse(this.changedModel.hasObject(this.object.getId()));
        assertTrue(this.model.hasObject(this.object.getId()));
        
        /*
         * object was removed, add it again (otherwise the next case doesn't
         * test what it's supposed to test
         */
        this.changedModel.createObject(this.object.getId());
        
        /*
         * try to remove an object that already exists & use wrong revNr (use
         * revision number of the old object, not the new one)
         */
        removeCommand = factory.createRemoveObjectCommand(this.object.getAddress(),
                this.object.getRevisionNumber() + 1, forced);
        callback = new TestCallback();
        
        result = executeCommand(removeCommand, callback);
        
        if(forced) {
            // forced --> should succeed
            assertTrue(result != XCommand.FAILED);
            
            // check callback
            assertFalse(callback.failed);
            assertEquals((Long)result, callback.revision);
            
            // check whether the object was removed correctly
            assertFalse(this.changedModel.hasObject(this.object.getId()));
            assertTrue(this.model.hasObject(this.object.getId()));
        } else {
            // not forced --> should fail
            assertEquals(XCommand.FAILED, result);
            
            // check callback
            assertTrue(callback.failed);
            assertNull(callback.revision);
        }
        
    }
    
    @Test
    public void testExecuteCommandsSafeRemoveObjectCommands() {
        executeCommandsRemoveObjectCommands(false);
    }
    
    @Test
    public void testExecuteCommandsForcedRemoveObjectCommands() {
        executeCommandsRemoveObjectCommands(true);
    }
    
    // tests for object commands
    
    public void executeCommandsAddFieldCommands(boolean forced) {
        XCommandFactory factory = X.getCommandFactory();
        XId newFieldId = X.getIDProvider().createUniqueId();
        TestCallback callback = new TestCallback();
        XWritableObject transObject = this.changedModel.getObject(this.object.getId());
        
        // make sure there is no field with this ID
        XyAssert.xyAssert(!transObject.hasField(newFieldId));
        
        // add the field
        XCommand addCommand = factory.createAddFieldCommand(transObject.getAddress(), newFieldId,
                forced);
        
        long result = executeCommand(addCommand, callback);
        assertTrue(result != XCommand.FAILED);
        
        // check callback
        assertFalse(callback.failed);
        assertEquals((Long)result, callback.revision);
        
        // check whether the field was added correctly
        assertTrue(transObject.hasField(newFieldId));
        
        XWritableField field = transObject.getField(newFieldId);
        assertTrue(field.getRevisionNumber() >= 0);
        
        assertFalse(this.object.hasField(newFieldId));
        
        // try to add a field that already exists
        addCommand = factory.createAddFieldCommand(transObject.getAddress(), this.field.getId(),
                forced);
        callback = new TestCallback();
        
        result = executeCommand(addCommand, callback);
        if(forced) {
            // forced --> should succeed
            assertTrue(result != XCommand.FAILED);
            
            // check callback
            assertFalse(callback.failed);
            assertEquals((Long)result, callback.revision);
        } else {
            // not forced --> should fail
            assertEquals(XCommand.FAILED, result);
            
            // check callback
            assertTrue(callback.failed);
            assertNull(callback.revision);
        }
    }
    
    @Test
    public void testExecuteCommandsSafeAddFieldCommands() {
        executeCommandsAddFieldCommands(false);
    }
    
    @Test
    public void testExecuteCommandsForcedAddFieldCommands() {
        executeCommandsAddFieldCommands(true);
    }
    
    public void executeCommandsRemoveFieldCommands(boolean forced) {
        XCommandFactory factory = X.getCommandFactory();
        XId newFieldId = X.getIDProvider().createUniqueId();
        TestCallback callback = new TestCallback();
        XWritableObject transObject = this.changedModel.getObject(this.object.getId());
        
        // make sure there is no field with this ID
        XyAssert.xyAssert(!transObject.hasField(newFieldId));
        
        // try to remove not existing field
        XAddress temp = transObject.getAddress();
        XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
                newFieldId);
        
        XCommand removeCommand = factory.createRemoveFieldCommand(address, 0, forced);
        
        long result = executeCommand(removeCommand, callback);
        if(forced) {
            // forced --> should suceed
            assertTrue(result != XCommand.FAILED);
            
            // check callback
            assertFalse(callback.failed);
            assertEquals((Long)result, callback.revision);
        } else {
            // not forced --> should fail
            assertEquals(XCommand.FAILED, result);
            
            // check callback
            assertTrue(callback.failed);
            assertNull(callback.revision);
        }
        
        // try to remove a field that already exists, should succeed
        removeCommand = factory.createRemoveFieldCommand(this.field.getAddress(),
                this.field.getRevisionNumber(), forced);
        callback = new TestCallback();
        
        result = executeCommand(removeCommand, callback);
        assertTrue(result != XCommand.FAILED);
        
        // check callback
        assertFalse(callback.failed);
        assertEquals((Long)result, callback.revision);
        
        // check whether the field was removed correctly
        assertFalse(transObject.hasField(this.field.getId()));
        assertTrue(this.object.hasField(this.field.getId()));
        
        /*
         * field was removed, add it again (otherwise the next case doesn't test
         * what it's supposed to test
         */
        assertNotNull(transObject.createField(this.field.getId()));
        
        /*
         * try to remove a field that already exists & use wrong revNr (using
         * the revision number of the old field which was removed, not the one
         * of the current field)
         */
        
        removeCommand = factory.createRemoveFieldCommand(this.field.getAddress(),
                this.field.getRevisionNumber() + 1, forced);
        callback = new TestCallback();
        
        result = executeCommand(removeCommand, callback);
        if(forced) {
            // forced --> should suceed
            assertTrue(result != XCommand.FAILED);
            
            // check callback
            assertFalse(callback.failed);
            assertEquals((Long)result, callback.revision);
            
            // check whether the field was removed correctly
            assertFalse(transObject.hasField(this.field.getId()));
            assertTrue(this.object.hasField(this.field.getId()));
        } else {
            // not forced --> should fail
            assertEquals(XCommand.FAILED, result);
            
            // check callback
            assertTrue(callback.failed);
            assertNull(callback.revision);
        }
    }
    
    @Test
    public void testExecuteCommandsSafeRemoveFieldCommands() {
        executeCommandsRemoveFieldCommands(false);
    }
    
    @Test
    public void testExecuteCommandsForcedRemoveFieldCommands() {
        executeCommandsRemoveFieldCommands(true);
    }
    
    // Tests for field commands
    
    public void executeCommandsAddValueCommands(boolean forced) {
        XCommandFactory factory = X.getCommandFactory();
        XId newFieldId = X.getIDProvider().createUniqueId();
        TestCallback callback = new TestCallback();
        XWritableObject transObject = this.changedModel.getObject(this.object.getId());
        
        XValue value = X.getValueFactory().createStringValue("test");
        
        // add a value to a not existing field, should fail
        XyAssert.xyAssert(!transObject.hasField(newFieldId));
        
        XAddress temp = transObject.getAddress();
        XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
                newFieldId);
        XCommand addCommand = factory.createAddValueCommand(address, 0, value, forced);
        
        long result = executeCommand(addCommand, callback);
        assertEquals(XCommand.FAILED, result);
        
        // check callback
        assertTrue(callback.failed);
        assertNull(callback.revision);
        
        // add a value to an existing field, use wrong revNr
        addCommand = factory.createAddValueCommand(this.field.getAddress(),
                this.field.getRevisionNumber() + 1, value, forced);
        callback = new TestCallback();
        
        result = executeCommand(addCommand, callback);
        if(forced) {
            // forced --> should succeed
            assertTrue(result != XCommand.FAILED);
            
            // check callback
            assertFalse(callback.failed);
            assertEquals((Long)result, callback.revision);
            
            // check whether the simulated field was changed and the real field
            // wasn't
            XWritableField changedField = transObject.getField(this.field.getId());
            
            assertEquals(value, changedField.getValue());
            assertFalse(value.equals(this.field.getValue()));
            
            /*
             * value was added, remove it again or the next cases won't test
             * what they're suppossed to test
             */
            changedField.setValue(null);
        } else {
            // not forced --> should fail
            assertEquals(XCommand.FAILED, result);
            
            // check callback
            assertTrue(callback.failed);
            assertNull(callback.revision);
        }
        
        // add a value to an existing field, should succeed
        addCommand = factory.createAddValueCommand(this.field.getAddress(),
                this.field.getRevisionNumber(), value, forced);
        callback = new TestCallback();
        
        result = executeCommand(addCommand, callback);
        assertTrue(result != XCommand.FAILED);
        
        // check callback
        assertFalse(callback.failed);
        assertEquals((Long)result, callback.revision);
        
        // check whether the simulated field was changed and the real field
        // wasn't
        XWritableField changedField = transObject.getField(this.field.getId());
        
        assertEquals(value, changedField.getValue());
        assertFalse(value.equals(this.field.getValue()));
        
        // try to add a value to a field which value is already set
        XValue value2 = X.getValueFactory().createStringValue("test2");
        addCommand = factory.createAddValueCommand(this.field.getAddress(),
                this.field.getRevisionNumber(), value2, forced);
        callback = new TestCallback();
        
        result = executeCommand(addCommand, callback);
        if(forced) {
            // forced --> should succeed
            assertTrue(result != XCommand.FAILED);
            
            // check callback
            assertFalse(callback.failed);
            assertEquals((Long)result, callback.revision);
            
            // check whether the simulated field was changed and the real field
            // wasn't
            changedField = transObject.getField(this.field.getId());
            
            assertEquals(value2, changedField.getValue());
            assertFalse(value2.equals(this.field.getValue()));
        } else {
            // not forced --> should fail
            assertEquals(XCommand.FAILED, result);
            
            // check callback
            assertTrue(callback.failed);
            assertNull(callback.revision);
        }
    }
    
    @Test
    public void testExecuteCommandsSafeAddValueCommands() {
        executeCommandsAddValueCommands(false);
    }
    
    @Test
    public void testExecuteCommandsForcedAddValueCommands() {
        executeCommandsAddValueCommands(true);
    }
    
    public void executeCommandsChangeValueCommands(boolean forced) {
        XCommandFactory factory = X.getCommandFactory();
        XId newFieldId = X.getIDProvider().createUniqueId();
        TestCallback callback = new TestCallback();
        XWritableObject transObject = this.changedModel.getObject(this.object.getId());
        
        XValue value = X.getValueFactory().createStringValue("test");
        
        // change a value of a not existing field, should fail
        XyAssert.xyAssert(!transObject.hasField(newFieldId));
        
        XAddress temp = transObject.getAddress();
        XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
                newFieldId);
        XCommand changeCommand = factory.createChangeValueCommand(address, 0, value, forced);
        
        long result = executeCommand(changeCommand, callback);
        assertEquals(XCommand.FAILED, result);
        
        // check callback
        assertTrue(callback.failed);
        assertNull(callback.revision);
        
        // change the value of a field, which value is not set
        changeCommand = factory.createChangeValueCommand(this.field.getAddress(),
                this.field.getRevisionNumber(), value, forced);
        callback = new TestCallback();
        
        result = executeCommand(changeCommand, callback);
        if(forced) {
            // forced --> should succeed
            assertTrue(result != XCommand.FAILED);
            
            // check callback
            assertFalse(callback.failed);
            assertEquals((Long)result, callback.revision);
            
            // check the changes
            XWritableField simulatedField = transObject.getField(this.field.getId());
            assertEquals(value, simulatedField.getValue());
            assertNull(this.field.getValue());
            
        } else {
            // not forced --> should fail
            
            assertEquals(XCommand.FAILED, result);
            
            // check callback
            assertTrue(callback.failed);
            assertNull(callback.revision);
            
            // check that nothing was changed
            XWritableField simulatedField = transObject.getField(this.field.getId());
            assertNull(simulatedField.getValue());
            assertNull(this.field.getValue());
        }
        
        // change the value of a field, which value is already set, but use
        // wrong revNr
        changeCommand = factory.createChangeValueCommand(this.fieldWithValue.getAddress(),
                this.fieldWithValue.getRevisionNumber() + 1, value, forced);
        callback = new TestCallback();
        
        result = executeCommand(changeCommand, callback);
        if(forced) {
            // forced --> should succeed
            assertTrue(result != XCommand.FAILED);
            
            // check callback
            assertFalse(callback.failed);
            assertEquals((Long)result, callback.revision);
            
            // check whether the simulated field was changed and the real field
            // wasn't
            XWritableField changedField = transObject.getField(this.fieldWithValue.getId());
            
            assertEquals(value, changedField.getValue());
            assertFalse(value.equals(this.fieldWithValue.getValue()));
        } else {
            // not forced --> should fail
            assertEquals(XCommand.FAILED, result);
            
            // check callback
            assertTrue(callback.failed);
            assertNull(callback.revision);
            
            // check that nothing was changed
            XWritableField changedField = transObject.getField(this.fieldWithValue.getId());
            
            assertFalse(value.equals(changedField.getValue()));
            assertFalse(value.equals(this.fieldWithValue.getValue()));
        }
        
        // change the value of a field, which value is already set - should
        // succeed
        XValue value2 = X.getValueFactory().createStringValue("test2");
        changeCommand = factory.createChangeValueCommand(this.fieldWithValue.getAddress(),
                this.fieldWithValue.getRevisionNumber(), value2, forced);
        callback = new TestCallback();
        
        result = executeCommand(changeCommand, callback);
        assertTrue(result != XCommand.FAILED);
        
        // check callback
        assertFalse(callback.failed);
        assertEquals((Long)result, callback.revision);
        
        // check whether the simulated field was changed and the real field
        // wasn't
        XWritableField changedField = transObject.getField(this.fieldWithValue.getId());
        changedField = transObject.getField(this.fieldWithValue.getId());
        
        assertEquals(value2, changedField.getValue());
        assertFalse(value2.equals(this.fieldWithValue.getValue()));
    }
    
    @Test
    public void testExecuteCommandsSafeChangeValueCommands() {
        executeCommandsChangeValueCommands(false);
    }
    
    @Test
    public void testExecuteCommandsForcedChangeValueCommands() {
        executeCommandsChangeValueCommands(true);
    }
    
    public void executeCommandsRemoveValueCommands(boolean forced) {
        XCommandFactory factory = X.getCommandFactory();
        XId newFieldId = X.getIDProvider().createUniqueId();
        TestCallback callback = new TestCallback();
        XWritableObject transObject = this.changedModel.getObject(this.object.getId());
        
        // remove a value from a not existing field, should fail
        XyAssert.xyAssert(!transObject.hasField(newFieldId));
        
        XAddress temp = transObject.getAddress();
        XAddress address = XX.toAddress(temp.getRepository(), temp.getModel(), temp.getObject(),
                newFieldId);
        XCommand removeCommand = factory.createRemoveValueCommand(address, 0, forced);
        
        long result = executeCommand(removeCommand, callback);
        assertEquals(XCommand.FAILED, result);
        
        // check callback
        assertTrue(callback.failed);
        assertNull(callback.revision);
        
        // remove a value from an existing field, without a set value
        removeCommand = factory.createRemoveValueCommand(this.field.getAddress(),
                this.field.getRevisionNumber(), forced);
        callback = new TestCallback();
        result = executeCommand(removeCommand, callback);
        
        if(forced) {
            // forced --> should succeed
            assertTrue(result != XCommand.FAILED);
            
            // check callback
            assertFalse(callback.failed);
            assertEquals((Long)result, callback.revision);
        } else {
            // not forced --> should fail
            assertEquals(XCommand.FAILED, result);
            
            // check callback
            assertTrue(callback.failed);
            assertNull(callback.revision);
        }
        
        // remove a value from an existing field with set value - should succeed
        removeCommand = factory.createSafeRemoveValueCommand(this.fieldWithValue.getAddress(),
                this.fieldWithValue.getRevisionNumber());
        callback = new TestCallback();
        
        result = executeCommand(removeCommand, callback);
        assertTrue(result != XCommand.FAILED);
        
        // check callback
        assertFalse(callback.failed);
        assertEquals((Long)result, callback.revision);
        
        // check whether the simulated field was changed and the real field
        // wasn't
        XWritableField changedField = transObject.getField(this.fieldWithValue.getId());
        
        assertNull(changedField.getValue());
        assertNotNull(this.fieldWithValue.getValue());
        
        /*
         * value was removed, add a new one or the next case won't test what
         * it's suppossed to test
         */
        changedField.setValue(X.getValueFactory().createStringValue("test"));
        
        // remove a value from an existing field with set value, use wrong revNr
        removeCommand = factory.createRemoveValueCommand(this.fieldWithValue.getAddress(),
                this.fieldWithValue.getRevisionNumber() + 1, forced);
        callback = new TestCallback();
        
        result = executeCommand(removeCommand, callback);
        if(forced) {
            // forced --> should succeed
            assertTrue(result != XCommand.FAILED);
            
            // check callback
            assertFalse(callback.failed);
            assertEquals((Long)result, callback.revision);
            
            // check whether the simulated field was changed and the real field
            // wasn't
            changedField = transObject.getField(this.fieldWithValue.getId());
            
            assertNull(changedField.getValue());
            assertNotNull(this.fieldWithValue.getValue());
        } else {
            // not forced --> should fail
            assertEquals(XCommand.FAILED, result);
            
            // check callback
            assertTrue(callback.failed);
            assertNull(callback.revision);
        }
    }
    
    @Test
    public void testExecuteCommandsSafeRemoveValueCommands() {
        executeCommandsRemoveValueCommands(false);
    }
    
    @Test
    public void testExecuteCommandsForcedRemoveValueCommands() {
        executeCommandsRemoveValueCommands(true);
    }
    
    // Tests for transactions
    
    @Test
    public void testExecuteCommandSucceedingTransaction() {
        // manually build a transaction
        XTransactionBuilder builder = new XTransactionBuilder(this.model.getAddress());
        
        // add some objects
        XId objectId1 = X.getIDProvider().createUniqueId();
        XId objectId2 = X.getIDProvider().createUniqueId();
        
        builder.addObject(this.model.getAddress(), XCommand.SAFE, objectId1);
        builder.addObject(this.model.getAddress(), XCommand.SAFE, objectId2);
        
        // remove some objects
        builder.removeObject(this.model.getAddress(), this.object2.getRevisionNumber(),
                this.object2.getId());
        
        // add some fields
        XId fieldId1 = X.getIDProvider().createUniqueId();
        XId fieldId2 = X.getIDProvider().createUniqueId();
        
        builder.addField(this.object.getAddress(), XCommand.SAFE, fieldId1);
        builder.addField(this.object.getAddress(), XCommand.SAFE, fieldId2);
        
        // add something, remove it again
        XId temp = X.getIDProvider().createUniqueId();
        builder.addField(this.object.getAddress(), XCommand.SAFE, temp);
        builder.removeField(this.object.getAddress(), XCommand.NEW, temp);
        
        // remove some fields
        builder.removeField(this.object.getAddress(), this.field.getRevisionNumber(),
                this.field.getId());
        
        // add some values
        XValue value = X.getValueFactory().createStringValue("testValue");
        XAddress fieldAddress1 = XX.toAddress(this.object.getAddress().getRepository(), this.object
                .getAddress().getModel(), this.object.getAddress().getObject(), fieldId1);
        XAddress fieldAddress2 = XX.toAddress(this.object.getAddress().getRepository(), this.object
                .getAddress().getModel(), this.object.getAddress().getObject(), fieldId2);
        
        builder.addValue(fieldAddress1, XCommand.NEW, value);
        builder.addValue(fieldAddress2, XCommand.NEW, value);
        
        // remove values
        builder.removeValue(this.fieldWithValue.getAddress(),
                this.fieldWithValue.getRevisionNumber());
        builder.removeValue(fieldAddress2, XCommand.NEW);
        
        // execute the transaction - should succeed
        XTransaction transaction = builder.build();
        TestCallback callback = new TestCallback();
        
        long result = executeCommand(transaction, callback);
        
        assertTrue(result != XCommand.FAILED);
        assertTrue(this.changedModel.hasChanges());
        
        // check callback
        assertFalse(callback.failed);
        assertEquals((Long)result, callback.revision);
        
        /*
         * we do not need to check whether the single commands in the
         * transaction were correctly executed, since they all are executed by
         * separately calling the executeCommand() method, which was already
         * thoroughly tested above
         */
        
        // commit the transaction
        
        long oldRevNr = this.object.getRevisionNumber();
        long revNr = this.commit();
        
        assertFalse(this.changedModel.hasChanges());
        
        assertTrue(revNr != XCommand.FAILED);
        assertEquals(oldRevNr + 1, revNr);
        assertEquals(revNr, this.changedModel.getRevisionNumber());
        
        // check that the changes were actually executed
        assertTrue(this.object.hasField(fieldId1));
        assertTrue(this.object.hasField(fieldId2));
        
        assertFalse(this.object.hasField(this.field.getId()));
        assertFalse(this.object.hasField(temp));
        
        XRevWritableField field1 = this.object.getField(fieldId1);
        assertEquals(value, field1.getValue());
        
        XRevWritableField field2 = this.object.getField(fieldId2);
        assertNull(field2.getValue());
        
        assertEquals(null, this.fieldWithValue.getValue());
    }
    
    private void testExecuteCommandsFailingTransaction(XType type, ChangeType changeType) {
        // manually build a transaction
        XTransactionBuilder builder = new XTransactionBuilder(this.model.getAddress());
        
        // add some fields
        XId fieldId1 = X.getIDProvider().createUniqueId();
        XId fieldId2 = X.getIDProvider().createUniqueId();
        
        builder.addField(this.object.getAddress(), XCommand.SAFE, fieldId1);
        builder.addField(this.object.getAddress(), XCommand.SAFE, fieldId2);
        
        // add some values
        XValue value = X.getValueFactory().createStringValue("testValue");
        XAddress fieldAddress1 = XX.toAddress(this.object.getAddress().getRepository(), this.object
                .getAddress().getModel(), this.object.getAddress().getObject(), fieldId1);
        XAddress fieldAddress2 = XX.toAddress(this.object.getAddress().getRepository(), this.object
                .getAddress().getModel(), this.object.getAddress().getObject(), fieldId2);
        
        builder.addValue(fieldAddress1, XCommand.NEW, value);
        builder.addValue(fieldAddress2, XCommand.NEW, value);
        
        // add failing command
        XyAssert.xyAssert(type == XType.XMODEL || type == XType.XOBJECT || type == XType.XFIELD);
        
        if(type == XType.XMODEL) {
            XyAssert.xyAssert(changeType != ChangeType.CHANGE);
            
            switch(changeType) {
            case ADD:
                // try to add an object which already exists
                builder.addObject(this.model.getAddress(), XCommand.SAFE, this.object.getId());
                break;
            
            case REMOVE:
                // try to remove an object which doesn't exist
                builder.removeObject(this.model.getAddress(), XCommand.SAFE, XX.createUniqueId());
                break;
            case CHANGE:
            case TRANSACTION:
                assert false : "unexpected change type " + changeType + " for xtype " + type;
            }
            
        } else if(type == XType.XOBJECT) {
            XyAssert.xyAssert(changeType != ChangeType.CHANGE);
            
            switch(changeType) {
            case ADD:
                // try to add a field which already exists
                builder.addField(this.object.getAddress(), XCommand.SAFE, this.field.getId());
                break;
            
            case REMOVE:
                // try to remove a field which doesn't exist
                builder.removeField(this.object.getAddress(), XCommand.SAFE, XX.createUniqueId());
                break;
            case CHANGE:
            case TRANSACTION:
                assert false : "unexpected change type " + changeType + " for xtype " + type;
            }
        } else {
            switch(changeType) {
            case ADD:
                // try to add a value to a field which value is already set
                builder.addValue(this.fieldWithValue.getAddress(), XCommand.SAFE,
                        XX.createUniqueId());
                break;
            
            case REMOVE:
                // try to remove the vale from a field which value isn't set
                builder.removeValue(this.field.getAddress(), XCommand.SAFE);
                break;
            case CHANGE:
                // try to change the value of a field which value is set, but
                // use wrong revision number
                builder.changeValue(this.fieldWithValue.getAddress(),
                        this.fieldWithValue.getRevisionNumber() + 1, XX.createUniqueId());
                break;
            case TRANSACTION:
                assert false : "unexpected change type " + changeType + " for xtype " + type;
            }
            
        }
        
        // add some more commands after the command which should fail
        builder.addField(this.object.getAddress(), XCommand.SAFE, XX.createUniqueId());
        builder.addField(this.object.getAddress(), XCommand.SAFE, XX.createUniqueId());
        builder.addValue(this.field.getAddress(), XCommand.FORCED, XX.createUniqueId());
        
        // execute something before the transaction
        this.changedModel.createObject(XX.createUniqueId());
        
        // execute the transaction - should fail
        XTransaction transaction = builder.build();
        TestCallback callback = new TestCallback();
        
        long result = executeCommand(transaction, callback);
        
        assertEquals(XCommand.FAILED, result);
        
        // check callback
        assertTrue(callback.failed);
        assertNull(callback.revision);
        
        // check that nothing was changed by the transaction
        
        XWritableObject transObject = this.changedModel.getObject(this.object.getId());
        
        assertFalse(transObject.hasField(fieldId1));
        assertFalse(transObject.hasField(fieldId2));
    }
    
    /*
     * Tests for failing transaction that fail because of an XModelCommand
     */
    
    @Test
    public void testExecuteCommandsFailingChangedModelCommandAdd() {
        testExecuteCommandsFailingTransaction(XType.XMODEL, ChangeType.ADD);
    }
    
    @Test
    public void testExecuteCommandsFailingChangedModelCommandRemove() {
        testExecuteCommandsFailingTransaction(XType.XMODEL, ChangeType.REMOVE);
    }
    
    /*
     * Tests for failing transaction that fail because of an XObjectCommand
     */
    
    @Test
    public void testExecuteCommandsFailingTransactionObjectCommandAdd() {
        testExecuteCommandsFailingTransaction(XType.XOBJECT, ChangeType.ADD);
    }
    
    @Test
    public void testExecuteCommandsFailingTransactionObjectCommandRemove() {
        testExecuteCommandsFailingTransaction(XType.XOBJECT, ChangeType.REMOVE);
    }
    
    /*
     * Tests for transactions which fail because of an XFieldCommand
     */
    
    @Test
    public void testExecuteCommandsFailingTransactionFieldCommandAdd() {
        testExecuteCommandsFailingTransaction(XType.XFIELD, ChangeType.ADD);
    }
    
    @Test
    public void testExecuteCommandsFailingTransactionFieldCommandRemove() {
        testExecuteCommandsFailingTransaction(XType.XFIELD, ChangeType.REMOVE);
    }
    
    @Test
    public void testExecuteCommandsFailingTransactionFieldCommandChange() {
        testExecuteCommandsFailingTransaction(XType.XFIELD, ChangeType.CHANGE);
    }
    
    // Tests for getRevisionNumber
    @Test
    public void testGetRevisionNumber() {
        assertEquals(this.model.getRevisionNumber(), this.changedModel.getRevisionNumber());
    }
    
    // Tests for isEmpty
    @Test
    public void testIsEmpty() {
        // At first, the value should be the same as that of object.isEmpty()
        assertEquals(this.model.isEmpty(), this.changedModel.isEmpty());
        
        // remove object
        assertTrue(this.changedModel.removeObject(this.object.getId()));
        assertTrue(this.changedModel.removeObject(this.object2.getId()));
        
        assertTrue(this.changedModel.isEmpty());
        assertFalse(this.model.isEmpty());
        
        // add a new field, remove it, check again
        XId newObjectId = X.getIDProvider().createUniqueId();
        this.changedModel.createObject(newObjectId);
        
        assertTrue(this.changedModel.hasObject(newObjectId));
        assertFalse(this.model.hasObject(newObjectId));
        
        this.changedModel.removeObject(newObjectId);
        assertTrue(this.changedModel.isEmpty());
        assertFalse(this.model.isEmpty());
    }
    
    // Tests for hasObject()
    @Test
    public void testHasObject() {
        assertTrue(this.changedModel.hasObject(this.object.getId()));
        
        // add an object
        XId newObjectId = XX.createUniqueId();
        assertFalse(this.changedModel.hasObject(newObjectId));
        
        this.changedModel.createObject(newObjectId);
        assertTrue(this.changedModel.hasObject(newObjectId));
        
        // remove an object
        this.changedModel.removeObject(newObjectId);
        assertFalse(this.changedModel.hasObject(newObjectId));
        
        // do the same with commands
        XCommand addCommand = X.getCommandFactory().createSafeAddObjectCommand(
                this.changedModel.getAddress(), newObjectId);
        executeCommand(addCommand, null);
        assertTrue(this.changedModel.hasObject(newObjectId));
        
        XAddress temp = this.model.getAddress();
        XAddress objectAddress = XX.toAddress(temp.getRepository(), temp.getModel(), newObjectId,
                null);
        XCommand removeCommand = X.getCommandFactory().createSafeRemoveObjectCommand(objectAddress,
                XCommand.NEW);
        executeCommand(removeCommand, null);
        assertFalse(this.changedModel.hasObject(newObjectId));
    }
    
    // Tests for createObject()
    @Test
    public void testCreateObject() {
        XId objectId = XX.createUniqueId();
        XWritableObject object = this.changedModel.createObject(objectId);
        
        assertNotNull(object);
        
        // make sure it exists in the transObject but not in object
        assertFalse(this.model.hasObject(objectId));
        assertTrue(this.changedModel.hasObject(objectId));
        
        // try to add the same object again
        XWritableObject object2 = this.changedModel.createObject(objectId);
        assertEquals(object, object2);
        
        // make sure it exists in the transModel but not in model
        assertFalse(this.model.hasObject(objectId));
        assertTrue(this.changedModel.hasObject(objectId));
    }
    
    // Tests for removeObject()
    @Test
    public void testRemoveField() {
        // try to remove a not existing object
        XId objectId = XX.createUniqueId();
        
        assertFalse(this.changedModel.removeObject(objectId));
        
        // try to remove an existing object
        boolean removed = this.changedModel.removeObject(this.object.getId());
        assertTrue(removed);
        assertFalse(this.changedModel.hasObject(this.object.getId()));
        
        // make sure it wasn't removed from the underlying model
        assertTrue(this.model.hasObject(this.object.getId()));
        
        // add an object an remove it again
        this.changedModel.createObject(objectId);
        assertTrue(this.changedModel.hasObject(objectId));
        
        assertTrue(this.changedModel.removeObject(objectId));
        assertFalse(this.changedModel.hasObject(objectId));
        assertFalse(this.model.hasObject(objectId));
    }
    
    // Tests for getObject()
    @Test
    public void testGetObject() {
        // try to get an already existing object
        XWritableObject object2 = this.changedModel.getObject(this.object.getId());
        
        assertEquals(this.object, object2);
        
        // try to get a not existing object
        assertNull(this.changedModel.getObject(X.getIDProvider().createUniqueId()));
        
        // change the existing object and get it again
        XCommandFactory factory = X.getCommandFactory();
        XValue value = X.getValueFactory().createStringValue("test");
        XCommand command = factory.createSafeAddValueCommand(this.field.getAddress(),
                this.field.getRevisionNumber(), value);
        
        executeCommand(command, null);
        
        object2 = this.changedModel.getObject(this.object.getId());
        
        // revision numbers are not increased/managed by the ChangedModel,
        // therefore this should succeed
        assertTrue(this.object.equals(object2));
    }
    
    /*
     * Tests for the methods of {@link XWritableObject}
     */
    
    @Test
    public void testXWritableObject() {
        XWritableObject temp = this.changedModel.getObject(this.object.getId());
        assertTrue(temp instanceof XWritableObject);
        
        XWritableObject transObject = temp;
        
        assertEquals(this.object.getAddress(), transObject.getAddress());
        assertEquals(this.object.getId(), transObject.getId());
        assertEquals(this.object.getRevisionNumber(), transObject.getRevisionNumber());
        assertEquals(this.object.isEmpty(), transObject.isEmpty());
        assertEquals(this.object, transObject);
        
        assertTrue(transObject.hasField(this.field.getId()));
    }
    
    @Test
    public void testXWritableObjectAddField() {
        XWritableObject temp = this.changedModel.getObject(this.object.getId());
        assertTrue(temp instanceof XWritableObject);
        
        XWritableObject transObject = temp;
        
        XId fieldId = X.getIDProvider().createUniqueId();
        
        XWritableField field = transObject.createField(fieldId);
        assertNotNull(field);
        assertTrue(field instanceof XWritableField);
        
        // make sure it exists in the transObject but not in object
        assertFalse(this.object.hasField(fieldId));
        assertTrue(transObject.hasField(fieldId));
        
        // try to add the same field again
        XWritableField field2 = transObject.createField(fieldId);
        assertEquals(field, field2);
        
        // make sure it exists in the transObject but not in object
        assertFalse(this.object.hasField(fieldId));
        assertTrue(transObject.hasField(fieldId));
    }
    
    @Test
    public void testXWritableObjectRemoveField() {
        XWritableObject temp = this.changedModel.getObject(this.object.getId());
        assertTrue(temp instanceof XWritableObject);
        
        XWritableObject transObject = temp;
        
        // try to remove a not existing field
        XId fieldId = XX.createUniqueId();
        
        assertFalse(transObject.removeField(fieldId));
        
        // try to remove an existing field
        assertTrue(transObject.removeField(this.field.getId()));
        assertFalse(transObject.hasField(this.field.getId()));
        
        // make sure it wasn't removed from the underlying object
        assertTrue(this.object.hasField(this.field.getId()));
        
        // add a field and remove it again
        transObject.createField(fieldId);
        assertTrue(transObject.hasField(fieldId));
        
        assertTrue(transObject.removeField(fieldId));
        assertFalse(transObject.hasField(fieldId));
        assertFalse(this.object.hasField(fieldId));
    }
    
    /*
     * Note: there is no need to test the "executeCommand" methods of {@link
     * XWritableObject}, since they only pass the command to their ChangedModel
     * and do nothing else
     */
    
    /*
     * Tests for the methods of {@link XWritableField}
     */
    
    @Test
    public void testXWritableField() {
        XWritableObject transObject = this.changedModel.getObject(this.object.getId());
        XWritableField temp = transObject.getField(this.field.getId());
        assertTrue(temp instanceof XWritableField);
        
        XWritableField transField = (XWritableField)temp;
        
        assertEquals(this.field.getRevisionNumber(), transField.getRevisionNumber());
        assertEquals(this.field.getId(), transField.getId());
        assertEquals(this.field.isEmpty(), transField.isEmpty());
        assertEquals(this.field.getAddress(), transField.getAddress());
        assertEquals(this.field.getValue(), transField.getValue());
        assertEquals(this.field, transField);
    }
    
    @Test
    public void testXWritableFieldSetValueCorrectUsage() {
        XValue value = X.getValueFactory().createStringValue("42");
        XValue value2 = X.getValueFactory().createStringValue("test");
        XWritableObject transObject = this.changedModel.getObject(this.object.getId());
        
        XWritableField temp = transObject.getField(this.field.getId());
        assertTrue(temp instanceof XWritableField);
        
        XWritableField transField = (XWritableField)temp;
        
        // add value
        assertTrue(transField.setValue(value));
        
        assertEquals(value, transField.getValue());
        assertEquals(null, this.field.getValue());
        
        // change value
        assertTrue(transField.setValue(value2));
        
        assertEquals(value2, transField.getValue());
        assertEquals(null, this.field.getValue());
        
        // remove value
        assertTrue(transField.setValue(null));
        
        assertEquals(null, transField.getValue());
    }
    
    @Test
    public void testXWritableFieldSetValueIncorrectUsage() {
        XWritableObject transObject = this.changedModel.getObject(this.object.getId());
        
        XWritableField temp = transObject.getField(this.field.getId());
        assertTrue(temp instanceof XWritableField);
        
        XWritableField transField = (XWritableField)temp;
        
        // try to remove not existing value
        assertFalse(transField.setValue(null));
    }
    
    private class TestCallback implements XLocalChangeCallback {
        boolean failed = false;
        Long revision;
        
        @Override
        public void onFailure() {
            this.failed = true;
        }
        
        @Override
        public void onSuccess(long revision) {
            this.revision = revision;
        }
        
    }
}
