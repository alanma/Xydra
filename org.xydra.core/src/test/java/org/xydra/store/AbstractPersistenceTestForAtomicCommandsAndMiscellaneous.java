package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XAtomicCommand.Intent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.change.impl.memory.RevisionConstants;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


/**
 * the following variables need to be instantiated in the @Before method by
 * implementations of this test
 *
 * - persistence needs to be an empty XydraPersistence with this.repoId as its
 * repository id.
 *
 * - comFactory needs to be an implementation of XCommandFactory which creates
 * commands that can be executed by persistence.
 */
/**
 * @author kahmann
 *
 */
/**
 * @author kahmann
 *
 */
public abstract class AbstractPersistenceTestForAtomicCommandsAndMiscellaneous {

    static {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
    }

    private static final Logger log = LoggerFactory
            .getLogger(AbstractPersistenceTestForAtomicCommandsAndMiscellaneous.class);

    public XydraPersistence persistence;

    public XCommandFactory comFactory;

    public XId repoId = X.getIDProvider().fromString("testRepo");
    public XAddress repoAddress = XX.resolveRepository(this.repoId);
    public XId actorId = X.getIDProvider().fromString("testActor");

    @Test
    public void testExecuteCommandRepositorySafeCommandAddType() {
        testExecuteCommandRepositoryCommandAddType(false);
    }

    @Test
    public void testExecuteCommandRepositoryForcedCommandAddType() {
        testExecuteCommandRepositoryCommandAddType(true);
    }

    public void testExecuteCommandRepositoryCommandAddType(final boolean forcedCommands) {
        /*
         * RepositoryCommands of add type add new models to the repository.
         */

        /*
         * add a new model, should succeed
         */
        final XId modelId = Base.toId("testExecuteCommandRepositoryCommandAddType-" + forcedCommands);
        final XAddress modelAddress = Base.resolveModel(this.repoId, modelId);
        final XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId,
                forcedCommands);

        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);

        assertTrue("Executing \"Adding a new model\"-command failed (should succeed), revNr was "
                + revNr, revNr >= 0);

        // check that the model actually exists
        final GetWithAddressRequest addressRequest = new GetWithAddressRequest(modelAddress);
        final XWritableModel model = this.persistence.getModelSnapshot(addressRequest);
        assertNotNull(
                "The model we tried to create with an \"Adding a new model\"-command actually wasn't correctly added.",
                model);
        assertEquals("Returned model snapshot did not have the correct XAddress.", modelAddress,
                model.getAddress());
        assertEquals("Returned model did not have the correct revision number.", revNr,
                model.getRevisionNumber());

        /*
         * try to add the same model again.
         */

        revNr = this.persistence.executeCommand(this.actorId, addModelCmd);

        if(forcedCommands) {

            assertEquals(
                    "Executing \"Adding an existing model\"-command did not return \"No Change\", although the command was forced and the model we tried to add already exists.",
                    XCommand.NOCHANGE, revNr);
        } else {

            assertEquals(
                    "Trying to add an already existing model with an unforced command succeeded (should fail).",
                    XCommand.FAILED, revNr);
        }

    }

    @Test
    public void testExecuteCommandRepositorySafeCommandRemoveType() {
        testExecuteCommandRepositoryCommandRemoveType(false);
    }

    @Test
    public void testExecuteCommandRepositoryForcedCommandRemoveType() {
        testExecuteCommandRepositoryCommandRemoveType(true);
    }

    public void testExecuteCommandRepositoryCommandRemoveType(final boolean forcedCommands) {
        /*
         * RepositoryCommands of the remove type remove models from the
         * repository
         */

        /*
         * add a model, we'll delete afterwards
         */
        XId modelId = Base.toId("testExecuteCommandRepositoryCommandRemoveTypeModel1");
        XAddress modelAddress = Base.resolveModel(this.repoId, modelId);
        XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId,
                forcedCommands);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);

        assertTrue("Adding a model failed, test cannot be executed.", revNr >= 0);

        GetWithAddressRequest addressRequest = new GetWithAddressRequest(modelAddress);
        XWritableModel model = this.persistence.getModelSnapshot(addressRequest);

        assertNotNull("The model we tried to add doesn't exist, test cannot be executed.", model);
        assertEquals(
                "The returned model has the wrong address, test cannot be executed, since it seems like adding models doesn't work correctly.",
                modelAddress, model.getAddress());

        /*
         * delete the model again
         */
        XCommand deleteModelCmd = this.comFactory.createRemoveModelCommand(this.repoId, modelId,
                model.getRevisionNumber(), forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, deleteModelCmd);

        assertTrue(
                "Executing \"Deleting an existing model\"-command failed (should succeed), revNr was "
                        + revNr, revNr >= 0);
        // check that the model was actually removed
        model = this.persistence.getModelSnapshot(addressRequest);
        assertNull(
                "The model we tried to remove with an \"Removing a model\"-command actually wasn't correctly removed.",
                model);

        /*
         * try to delete the model again
         */
        deleteModelCmd = this.comFactory.createRemoveModelCommand(this.repoId, modelId, revNr,
                forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, deleteModelCmd);

        if(forcedCommands) {
            assertEquals(
                    "Trying to remove and already removed model with a forced command should return \"No change\".",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertEquals(
                    "Trying to remove and already removed model with a safe command succeeded (should fail).",
                    XCommand.FAILED, revNr);
        }

        // check that the model was actually removed
        model = this.persistence.getModelSnapshot(addressRequest);
        assertNull(
                "The model we tried to remove with an \"Removing a model\"-command actually wasn't correctly removed.",
                model);

        /*
         * try to remove a not existing model
         */

        modelId = Base.toId("testExecuteCommandRepositoryCommandRemoveTypeModel2");
        final XCommand deleteNotExistingModelCmd = this.comFactory.createRemoveModelCommand(this.repoId,
                modelId, 0, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, deleteNotExistingModelCmd);

        if(forcedCommands) {
            /*
             * Should suceed/return "No Change" since the command was forced.
             */

            assertEquals(
                    "Trying to remove a not existing model should return \"No change\" when the command is forced.",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertTrue(
                    "Removing a not existing model with an unforced command succeeded (should fail), revNr was "
                            + revNr, revNr == XCommand.FAILED);
        }

        /*
         * test if removing a model also removes the object (snapshots) and
         * their fields
         */

        modelId = Base.toId("testExecuteCommandRepositoryCommandRemoveTypeModel3");
        modelAddress = Base.resolveModel(this.repoId, modelId);
        addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId, forcedCommands);
        revNr = this.persistence.executeCommand(this.actorId, addModelCmd);

        assertTrue("Adding a model failed, test cannot be executed.", revNr >= 0);

        final XId objectId = Base.toId("testExecuteCommandRepositoryCommandRemoveTypeObject1");

        final XAddress objectAddress = Base.resolveObject(this.repoId, modelId, objectId);
        final XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, forcedCommands);

        final long objectRevNr = this.persistence.executeCommand(this.actorId, addObjectCom);

        assertTrue("Adding an object failed, test cannot be executed.", objectRevNr >= 0);

        // remove model and check if object is also removed
        addressRequest = new GetWithAddressRequest(modelAddress);
        model = this.persistence.getModelSnapshot(addressRequest);
        deleteModelCmd = this.comFactory.createRemoveModelCommand(this.repoId, modelId,
                model.getRevisionNumber(), forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, deleteModelCmd);

        assertTrue(
                "Executing \"Deleting an existing model\"-command failed (should succeed), revNr was "
                        + revNr, revNr >= 0);
        // check that the model was actually removed
        model = this.persistence.getModelSnapshot(addressRequest);
        assertNull(
                "The model we tried to remove with an \"Removing a model\"-command actually wasn't correctly removed.",
                model);

        final GetWithAddressRequest objectAddressRequest = new GetWithAddressRequest(objectAddress);
        final XWritableObject object = this.persistence.getObjectSnapshot(objectAddressRequest);

        assertNull("The model was removed but its objects weren't removed.", object);
    }

    @Test
    public void testExecuteCommandModelSafeCommandAddType() {
        testExecuteCommandModelCommandAddType(false);
    }

    @Test
    public void testExecuteCommandModelForcedCommandAddType() {
        testExecuteCommandModelCommandAddType(true);
    }

    public void testExecuteCommandModelCommandAddType(final boolean forcedCommands) {
        /*
         * ModelCommands of add type add new objects to models.
         */

        // add a model on which the commands can be executed first

        final XId modelId = Base.toId("testExecuteCommandModelCommandAddTypeModel");
        final XAddress modelAddress = Base.resolveModel(this.repoId, modelId);

        final XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId,
                forcedCommands);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);
        assertTrue("The model wasn't correctly added (" + revNr + "), test cannot be executed.",
                revNr >= 0);

        /*
         * add a new object, should succeed
         */

        final XId objectId = Base.toId("testExecuteCommandModelCommandAddTypeObject");

        final XAddress objectAddress = Base.resolveObject(this.repoId, modelId, objectId);
        final XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);

        assertTrue("Executing \"Adding a new object\"-command failed (should succeed), revNr was "
                + revNr, revNr >= 0);

        // check that the object actually exists
        final GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
        final XWritableModel model = this.persistence.getModelSnapshot(modelAdrRequest);
        final XWritableObject object = model.getObject(objectId);

        assertNotNull(
                "Model Snapshot Failure: The object we tried to create with an \"Adding a new object\"-command actually wasn't correctly added.",
                object);

        final GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
        final XWritableObject objectSnapshot = this.persistence.getObjectSnapshot(objectAdrRequest);

        assertNotNull(
                "Object Snapshot Failure: The object we tried to create with an \"Adding a new object\"-command actually wasn't correctly added.",
                objectSnapshot);
        assertEquals("Returned object snapshot did not have the correct XAddress.", objectAddress,
                objectSnapshot.getAddress());
        assertEquals("Returned object snapshot did not have the correct revision number.", revNr,
                objectSnapshot.getRevisionNumber());
        // check equals in both directions
        assertTrue("Returned object snapshot is not equal to the object stored in the model.",
                object.equals(objectSnapshot));
        assertTrue("Returned object snapshot is not equal to the object stored in the model.",
                objectSnapshot.equals(object));
        // JUnits equals is in fact smart
        assertEquals("Returned object snapshot is not equal to the object stored in the model.",
                object, objectSnapshot);
        assertEquals("Returned object snapshot is not equal to the object stored in the model.",
                objectSnapshot, object);

        /*
         * try to add the same object again
         */

        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
        if(forcedCommands) {
            assertEquals(
                    "Executing \"Adding an existing object\"-command did not return \"No Change\", although the command was forced and the object we tried to add already exists.",
                    XCommand.NOCHANGE, revNr);

        } else {
            assertEquals(
                    "Trying to add an already existing object  with an unforced command succeeded (should fail).",
                    XCommand.FAILED, revNr);

        }

    }

    @Test
    public void testExecuteCommandModelSafeCommandRemoveType() {
        testExecuteCommandModelCommandRemoveType(false);
    }

    @Test
    public void testExecuteCommandModelForcedCommandRemoveType() {
        testExecuteCommandModelCommandRemoveType(true);
    }

    public void testExecuteCommandModelCommandRemoveType(final boolean forcedCommands) {
        /*
         * ModelCommands of the remove type remove objects from models
         */

        /*
         * add a model with an object which we'll delete afterwards
         */
        final XId modelId = Base.toId("testExecuteCommandModelCommandRemoveTypeModel");
        final XAddress modelAddress = Base.resolveModel(this.repoId, modelId);
        final XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId,
                forcedCommands);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);

        assertTrue("Adding a model failed, test cannot be executed.", revNr >= 0);

        final GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
        XWritableModel model = this.persistence.getModelSnapshot(modelAdrRequest);

        assertNotNull("The model we tried to add doesn't exist, test cannot be executed.", model);
        assertEquals(
                "The returned model has the wrong address, test cannot be executed, since it seems like adding models doesn't work correctly.",
                modelAddress, model.getAddress());

        XId objectId = Base.toId("testExecuteCommandModelCommandRemoveTypeObject1");
        final XAddress objectAddress = Base.resolveObject(this.repoId, modelId, objectId);
        final XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, forcedCommands);
        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);

        assertTrue("Adding an object failed, test cannot be executed.", revNr >= 0);

        final GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
        XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);

        assertNotNull("The object we tried to add doesn't exist, test cannot be executed.", object);
        assertEquals(
                "The returned object has the wrong address, test cannot be executed, since it seems like adding object doesn't work correctly.",
                objectAddress, object.getAddress());

        /*
         * delete the object again
         */
        XCommand deleteObjectCom = this.comFactory.createRemoveObjectCommand(this.repoId, modelId,
                objectId, object.getRevisionNumber(), forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, deleteObjectCom);

        assertTrue(
                "Executing \"Deleting an existing object\"-command failed (should succeed), revNr was "
                        + revNr, revNr >= 0);
        // check that the object was actually removed
        model = this.persistence.getModelSnapshot(modelAdrRequest);
        assertNull(
                "The object we tried to remove with an \"Removing an existing object\"-command actually wasn't correctly removed and still exists in the stored model.",
                model.getObject(objectId));

        object = this.persistence.getObjectSnapshot(objectAdrRequest);
        assertNull(
                "The object we tried to remove with an \"Removing an existing object \"-command actually wasn't correctly removed.",
                object);

        /*
         * try to delete the same object again
         */
        deleteObjectCom = this.comFactory.createRemoveObjectCommand(this.repoId, modelId, objectId,
                revNr, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, deleteObjectCom);

        if(forcedCommands) {
            assertEquals(
                    "Trying to remove and already removed object with a forced command should return \"No change\".",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertEquals(
                    "Trying to remove and already removed object with a safe command succeeded (should fail).",
                    XCommand.FAILED, revNr);
        }

        /*
         * try to remove a not existing object
         */

        objectId = Base.toId("testExecuteCommandModelCommandRemoveTypeObject2");
        final XCommand deleteNotExistingObjectCom = this.comFactory.createRemoveObjectCommand(
                this.repoId, modelId, objectId, 0, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, deleteNotExistingObjectCom);

        if(forcedCommands) {
            assertEquals(
                    "Trying to remove a not existing object should return \"No change\" when the command is forced.",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertEquals(
                    "Removing a not existing object with an unforced command succeeded (should fail).",
                    XCommand.FAILED, revNr);
        }

    }

    @Test
    public void testExecuteCommandObjectSafeCommandAddType() {
        testExecuteCommandObjectCommandAddType(false);
    }

    @Test
    public void testExecuteCommandObjectForcedCommandAddType() {
        testExecuteCommandObjectCommandAddType(true);
    }

    public void testExecuteCommandObjectCommandAddType(final boolean forcedCommands) {
        /*
         * ObjectCommands of add type add new fields to objects.
         */

        // add a model on which an object can be created first

        final XId modelId = Base.toId("testExecuteCommandObjectCommandAddTypeModel");
        final XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId,
                forcedCommands);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);
        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        /*
         * add a new object on which a new field can be created
         */

        final XId objectId = Base.toId("testExecuteCommandObjectCommandAddTypeObject");

        final XAddress objectAddress = Base.resolveObject(this.repoId, modelId, objectId);
        final XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);

        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        /*
         * add a new field, should succeed
         */

        final XId fieldId = Base.toId("testExecuteCommandObjectCommandAddTypeField");

        final XAddress fieldAddress = Base.resolveField(this.repoId, modelId, objectId, fieldId);
        final XCommand addFieldCom = this.comFactory.createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addFieldCom);

        assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
                + revNr, revNr >= 0);

        // check that the field actually exists
        final GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
        final XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
        final XWritableField field = object.getField(fieldId);

        assertNotNull(
                "The field we tried to create with an \"Adding a new feld\"-command actually wasn't correctly added.",
                field);
        assertEquals("Returned field did not have the correct XAddress.", fieldAddress,
                field.getAddress());
        assertEquals("Returned field did not have the correct revision number.", revNr,
                field.getRevisionNumber());

        /*
         * try to add the same field again (with an unforced command), should
         * fail
         */

        revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
        if(forcedCommands) {
            assertEquals(
                    "Trying to add an already existing field with a forced command should return \"No change\".",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertEquals(
                    "Trying to add an already existing field with an unforced command succeeded (should fail).",
                    XCommand.FAILED, revNr);
        }

    }

    @Test
    public void testExecuteCommandObjectSafeCommandRemoveType() {
        testExecuteCommandObjectCommandRemoveType(false);
    }

    @Test
    public void testExecuteCommandObjectForcedCommandRemoveType() {
        testExecuteCommandObjectCommandRemoveType(true);
    }

    public void testExecuteCommandObjectCommandRemoveType(final boolean forcedCommands) {
        /*
         * FieldCommands of remove type remove fields from objects.
         */

        // add a model on which an object can be created first

        final XId modelId = Base.toId("testExecuteCommandObjectCommandRemoveTypeModel");
        final XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId,
                forcedCommands);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);
        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        /*
         * add a new object on which a new field can be created
         */

        final XId objectId = Base.toId("testExecuteCommandObjectCommandRemoveTypeObject");

        final XAddress objectAddress = Base.resolveObject(this.repoId, modelId, objectId);
        final XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);

        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        /*
         * add a new field, should succeed
         */

        XId fieldId = Base.toId("testExecuteCommandObjectCommandRemoveTypeField");

        final XCommand addFieldCom = this.comFactory.createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addFieldCom);

        assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
                + revNr, revNr >= 0);

        /*
         * remove field, should succeed
         */

        XCommand removeFieldCom = this.comFactory.createRemoveFieldCommand(this.repoId, modelId,
                objectId, fieldId, revNr, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, removeFieldCom);

        assertTrue("Executing \"Removing a new field\"-command failed (should succeed), revNr was "
                + revNr, revNr >= 0);

        // check that the field was actually removed
        final GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
        final XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
        final XWritableField field = object.getField(fieldId);

        assertNull(
                "The field we tried to create with an \"Remove an existing field\"-command actually wasn't correctly removed.",
                field);

        /*
         * try to remove the same field again
         */

        removeFieldCom = this.comFactory.createRemoveFieldCommand(this.repoId, modelId, objectId,
                fieldId, revNr, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, removeFieldCom);

        if(forcedCommands) {
            assertEquals(
                    "Trying to remove a not existing field with an forced command should return \"No change\".",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertEquals(
                    "Trying to remove a not existing field with an unforced command succeeded (should fail).",
                    XCommand.FAILED, revNr);
        }

        /*
         * try to remove a not existing field
         */

        fieldId = Base.toId("testExecuteCommandObjectCommandRemoveTypeNotExistingField");
        final XCommand deleteNotExistingFieldCom = this.comFactory.createRemoveFieldCommand(this.repoId,
                modelId, objectId, fieldId, 0, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, deleteNotExistingFieldCom);

        if(forcedCommands) {
            assertEquals(
                    "Trying to remove a not existing field should return \"No change\" when the command is forced.",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertEquals(
                    "Removing a not existing field with an unforced command succeeded (should fail).",
                    XCommand.FAILED, revNr);
        }

    }

    @Test
    public void testExecuteCommandFieldSafeCommandAddType() {
        testExecuteCommandFieldCommandAddType(false);
    }

    @Test
    public void testExecuteCommandFieldForcedCommandAddType() {
        testExecuteCommandFieldCommandAddType(true);
    }

    public void testExecuteCommandFieldCommandAddType(final boolean forcedCommands) {
        /*
         * FieldCommands of add type add new value to fields.
         */

        // add a model on which an object can be created first

        final XId modelId = Base.toId("testExecuteCommandFieldCommandAddTypeModel");
        final XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId,
                forcedCommands);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);
        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        /*
         * add a new object on which a new field can be created
         */

        final XId objectId = Base.toId("testExecuteCommandFieldCommandAddTypeObject");

        final XAddress objectAddress = Base.resolveObject(this.repoId, modelId, objectId);
        final XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);

        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        /*
         * add a new field, should succeed
         */

        final XId fieldId = Base.toId("testExecuteCommandFieldCommandAddTypeField");

        final XAddress fieldAddress = Base.resolveField(this.repoId, modelId, objectId, fieldId);
        final XCommand addFieldCom = this.comFactory.createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addFieldCom);

        assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
                + revNr, revNr >= 0);

        /*
         * add a new value to the field, should succeed
         */
        final XValue value = BaseRuntime.getValueFactory().createStringValue("test");
        final XCommand addValueCom = this.comFactory.createAddValueCommand(fieldAddress, revNr, value,
                forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addValueCom);

        assertTrue("Executing \"Adding a new value\"-command failed (should succeed), revNr was "
                + revNr, revNr >= 0);

        // check that the value actually exists
        final GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
        XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
        XWritableField field = object.getField(fieldId);

        assertNotNull(
                "The field we tried to create with an \"Adding a new feld\"-command actually wasn't correctly added.",
                field);

        XValue storedValue = field.getValue();

        assertNotNull(
                "The value we tried to add with an \"Add a new value to a field without a value\"-command actually wasn't correctly added.",
                storedValue);
        assertEquals("The stored value is not equal to the value we wanted to add.", value,
                storedValue);

        /*
         * try to add a value to the same field again (with an unforced
         * command), should fail, since the value is now set and adding new
         * values no longer works
         */

        long revNr2 = this.persistence.executeCommand(this.actorId, addValueCom);

        if(forcedCommands) {
            assertEquals(
                    "Trying to add the same value to a field which value is already set with a forced command should return \"No change\".",
                    XCommand.NOCHANGE, revNr2);
        } else {
            assertEquals(
                    "Trying to add the same value to a field which value is already set with an unforced command succeeded (should fail).",
                    XCommand.FAILED, revNr2);
        }

        // try to add a different value
        final XValue value2 = BaseRuntime.getValueFactory().createIntegerValue(42);
        final XFieldCommand addValueCom2 = this.comFactory.createAddValueCommand(fieldAddress, revNr,
                value2, forcedCommands);

        revNr2 = this.persistence.executeCommand(this.actorId, addValueCom2);

        if(forcedCommands) {
            assertTrue(
                    "A forced add-command should succeed, even though the value is already set.",
                    revNr2 >= 0);

            object = this.persistence.getObjectSnapshot(objectAdrRequest);
            field = object.getField(fieldId);

            assertEquals(
                    "Value wasn't changed, although the command was forced and its execution reported \"success\".",
                    value2, field.getValue());
        } else {
            assertEquals(
                    "Safe command("
                            + addValueCom2.getRevisionNumber()
                            + "): Trying to add a new value to a field which value is already set succeeded (should fail).",
                    XCommand.FAILED, revNr2);

            // check that the value wasn't changed
            object = this.persistence.getObjectSnapshot(objectAdrRequest);
            field = object.getField(fieldId);

            storedValue = field.getValue();

            assertEquals("The stored value was changed, although the add-command failed.", value,
                    storedValue);
        }

    }

    @Test
    public void testExecuteCommandFieldSafeCommandRemoveType() {
        testExecuteCommandFieldCommandRemoveType(false);
    }

    @Test
    public void testExecuteCommandFieldForcedCommandRemoveType() {
        testExecuteCommandFieldCommandRemoveType(true);
    }

    public void testExecuteCommandFieldCommandRemoveType(final boolean forcedCommands) {
        /*
         * FieldCommands of remove type remove values from fields.
         */

        // add a model on which an object can be created first

        final XId modelId = Base.toId("testExecuteCommandFieldCommandRemoveTypeModel");
        final XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId,
                forcedCommands);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);
        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        /*
         * add a new object on which a new field can be created
         */

        final XId objectId = Base.toId("testExecuteCommandFieldCommandRemoveTypeObject");

        final XAddress objectAddress = Base.resolveObject(this.repoId, modelId, objectId);
        final XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);

        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        /*
         * add a new field, should succeed
         */

        final XId fieldId = Base.toId("testExecuteCommandFieldCommandRemoveTypeField");

        final XAddress fieldAddress = Base.resolveField(this.repoId, modelId, objectId, fieldId);
        final XCommand addFieldCom = this.comFactory.createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addFieldCom);

        assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
                + revNr, revNr >= 0);

        /*
         * add a new value to the field, should succeed
         */
        final XValue value = BaseRuntime.getValueFactory().createStringValue("test");
        final XCommand addValueCom = this.comFactory.createAddValueCommand(fieldAddress, revNr, value,
                forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addValueCom);

        assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
                + revNr, revNr >= 0);

        /*
         * remove the value again, should succeed
         */

        final XCommand removeValueCom = this.comFactory.createRemoveValueCommand(fieldAddress, revNr,
                forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, removeValueCom);

        assertTrue("Executing \"Remove a value \"-command failed (should succeed), revNr was "
                + revNr, revNr >= 0);

        // check that the value actually was removed
        final GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
        final XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
        final XWritableField field = object.getField(fieldId);

        assertNotNull(
                "The field we tried to create with an \"Adding a new feld\"-command actually wasn't correctly added.",
                field);

        final XValue storedValue = field.getValue();

        assertNull(
                "The value we tried to remove with an \"Remove a value from a field with a value\"-command actually wasn't correctly removed.",
                storedValue);

        /*
         * try to remove a value from a field with no value (should fail)
         */
        revNr = this.persistence.executeCommand(this.actorId, removeValueCom);

        if(forcedCommands) {
            assertEquals(
                    "Trying to remove a value from a field which has no value with a forced command should return \"No change\".",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertEquals(
                    "Removing a value from a field with no value with an unforced command succeeded (should fail).",
                    XCommand.FAILED, revNr);
        }

    }

    @Test
    public void testExecuteCommandFieldSafeCommandChangeType() {
        testExecuteCommandFieldCommandChangeType(false);
    }

    @Test
    public void testExecuteCommandFieldForcedCommandChangeType() {
        testExecuteCommandFieldCommandChangeType(true);
    }

    public void testExecuteCommandFieldCommandChangeType(final boolean forcedCommands) {
        /*
         * FieldCommands of change type change values of fields with values.
         */

        // add a model on which an object can be created first

        final XId modelId = Base.toId("testExecuteCommandFieldCommandChangeTypeModel");
        final XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId,
                forcedCommands);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);
        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        /*
         * add a new object on which a new field can be created
         */

        final XId objectId = Base.toId("testExecuteCommandFieldCommandChangeTypeObject");

        final XAddress objectAddress = Base.resolveObject(this.repoId, modelId, objectId);
        final XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);

        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        /*
         * add a new field, should succeed
         */

        final XId fieldId = Base.toId("testExecuteCommandFieldCommandChangeTypeField");

        final XAddress fieldAddress = Base.resolveField(this.repoId, modelId, objectId, fieldId);
        final XCommand addFieldCom = this.comFactory.createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addFieldCom);

        assertTrue("Executing \"Adding a new field\"-command failed (should succeed), revNr was "
                + revNr, revNr >= 0);

        /*
         * try to change a value which does not exist, should fail
         */
        final XValue value = BaseRuntime.getValueFactory().createStringValue("test");
        XCommand changeValueCom = this.comFactory.createChangeValueCommand(fieldAddress, revNr,
                value, forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, changeValueCom);

        if(forcedCommands) {
            assertTrue(
                    "Executing a forced \"Change a value\"-command failed on a field with no value (should succeed).",
                    revNr >= 0);
        } else {
            assertEquals(
                    "Executing a safe \"Change a value\"-command succeeded on a field with no value (should fail).",
                    XCommand.FAILED, revNr);
        }

        /*
         * add a value to the field, should succeed
         *
         * Note: if forcedCommands is set to false, the value of the field will
         * not be set. In contrast, if forcedCommands is set to true, the value
         * of the field will already be set, but since all commands will be
         * forced, the following add command also has to succeed and the test
         * can continue without any problems.
         */
        // get the correct revision number
        GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
        XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
        XWritableField field = object.getField(fieldId);
        revNr = field.getRevisionNumber();

        final XCommand addValueCom = this.comFactory.createAddValueCommand(fieldAddress, revNr, value,
                forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, addValueCom);

        if(forcedCommands) {
            assertEquals(
                    "Trying to add the same value again with a forced command should return \"No change\".",
                    XCommand.NOCHANGE, revNr);
            /*
             * Although we need the revNr for the following events, it is not
             * problematic that it is now set to XCommand.NOCHANGE, since the
             * following events will also be forced events and will therefore
             * not care whether the revision number is correct or not.
             */

        } else {
            assertTrue(
                    "Executing \"Adding a new value\"-command failed (should succeed), revNr was "
                            + revNr, revNr >= 0);
        }

        /*
         * change the value, should succeed
         */
        final XValue value2 = BaseRuntime.getValueFactory().createIntegerValue(42);
        changeValueCom = this.comFactory.createChangeValueCommand(fieldAddress, revNr, value2,
                forcedCommands);

        revNr = this.persistence.executeCommand(this.actorId, changeValueCom);

        assertTrue("Executing \"Change a value\"-command failed (should succeed), revNr was "
                + revNr, revNr >= 0);

        // check that the value actually was changed
        objectAdrRequest = new GetWithAddressRequest(objectAddress);
        object = this.persistence.getObjectSnapshot(objectAdrRequest);
        field = object.getField(fieldId);

        assertNotNull(
                "The field we tried to create with an \"Adding a new feld\"-command actually wasn't correctly added.",
                field);

        final XValue storedValue = field.getValue();

        assertNotNull(
                "The value we tried to add with an \"Add a new value to a field without a value\"-command actually wasn't correctly added.",
                storedValue);
        assertEquals("The stored value is not equal to the value that we tried to change it to.",
                value2, storedValue);

        /*
         * try to change the value to itself, should return
         * "nothing was changed".
         */

        /*
         * TODO Unsure if this should really return XCommand.NOCHANGE
         *
         * long failRevNr = this.persistence.executeCommand(this.actorId,
         * changeValueCom); assertTrue(
         * "Trying to change the value to itself with an unforced command succeeded or failed (should return \"nothing was changed\"), revNr was "
         * + failRevNr, failRevNr == XCommand.NOCHANGE);
         */

        /*
         * trying to change the value to "null" doesn't need to be tested, since
         * we cannot create CHANGE-commands which change a value to "null"
         * (changing an existing value to "null" is done by a REMOVE-command)
         */

    }

    /*
     * TODO check if all types of forced commands work correctly with arbitrary
     * revision numbers (as they should)
     */

    @Test
    public void testGetEvents() {
        /*
         * This testcase is unfortunately pretty large, but since it's better to
         * test the events in context with multiple commands there's really no
         * good way around it.
         */

        final XId modelId = Base.toId("testGetEventsModel");
        final XAddress modelAddress = Base.resolveModel(this.repoId, modelId);
        final XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);
        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        List<XEvent> events = this.persistence.getEvents(modelAddress, revNr, revNr);

        /*
         * check if the returned list (only) contains the "model was added"
         * event
         */
        assertEquals(
                "List of events should only contain one event (the \"create model\"-event), but actually contains zero or multiple events.",
                1, events.size());

        final XEvent modelAddEvent = events.get(0);
        assertTrue(
                "The returned event is not an XRepositoryEvent, but \"model was created\"-events are XRepositoryEvents",
                modelAddEvent instanceof XRepositoryEvent);
        assertEquals("The returned event was not of add-type.", ChangeType.ADD,
                modelAddEvent.getChangeType());
        assertEquals("The event didn't refer to the correct old revision number.",
                RevisionConstants.NOT_EXISTING, modelAddEvent.getOldModelRevision());
        assertEquals("The event didn't refer to the correct revision number.", revNr,
                modelAddEvent.getRevisionNumber());
        assertEquals("Event doesn't refer to the correct target.", this.repoAddress,
                modelAddEvent.getTarget());
        assertEquals("Event doesn't refer to the correct model.", modelAddress,
                modelAddEvent.getChangedEntity());
        assertEquals("The actor of the event is not correct.", this.actorId,
                modelAddEvent.getActor());
        assertFalse("The event is wrongly marked as implied.", modelAddEvent.isImplied());
        assertFalse("the event is wrongly marked as being part of a transaction.",
                modelAddEvent.inTransaction());

        /*
         * add an object and check if the correct events are returned. Check
         * getEvents(objectAddress...) and getEvents(modelAddress...) since they
         * might behave differently.
         */

        final XId objectId = Base.toId("testGetEventsObject");
        final XAddress objectAddress = Base.resolveObject(this.repoId, modelId, objectId);
        final XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, false);
        final long oldModelRev = revNr;
        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
        assertTrue("The object wasn't correctly added, test cannot be executed.", revNr >= 0);

        // get events from the object first and check them

        events = this.persistence.getEvents(objectAddress, revNr, revNr);

        /*
         * check if the returned list (only) contains the "object was added"
         * event
         */
        assertEquals(
                "List of events should only contain one event (the \"create object\"-event), but contains 0 or multiple events.",
                1, events.size());

        final XEvent objectAddEvent = events.get(0);
        assertTrue(
                "The returned event is not an XModelEvent, but \"object was created\"-events are XModelEvents",
                objectAddEvent instanceof XModelEvent);
        assertEquals("The returned event was not of add-type.", ChangeType.ADD,
                objectAddEvent.getChangeType());
        assertEquals("The event didn't refer to the correct old revision number.", oldModelRev,
                objectAddEvent.getOldModelRevision());
        assertEquals("The event didn't refer to the correct revision number.", revNr,
                objectAddEvent.getRevisionNumber());
        assertEquals("Event doesn't refer to the correct target.", modelAddress,
                objectAddEvent.getTarget());
        assertEquals("Event doesn't refer to the correct object.", objectAddress,
                objectAddEvent.getChangedEntity());
        assertEquals("The actor of the event is not correct.", this.actorId,
                objectAddEvent.getActor());
        assertFalse("The event is wrongly marked as implied.", objectAddEvent.isImplied());
        assertFalse("the event is wrongly marked as being part of a transaction.",
                objectAddEvent.inTransaction());

        /*
         * check if the list returned by the model contains the correct events.
         */
        events = this.persistence.getEvents(modelAddress, revNr, revNr);
        assertEquals(
                "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
                1, events.size());
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));

        events = this.persistence.getEvents(modelAddress, 0, revNr);

        assertEquals(
                "The list does not contain exactly 2 elements, although only 2 commands were executed.",
                2, events.size());
        assertTrue("The list does not contain the \"add model\"-event.",
                events.contains(modelAddEvent));
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));

        /*
         * add a field and check if the correct events are returned. Check
         * getEvents(fieldAddress...), getEvents(objectAddress...) and
         * getEvents(modelAddress...) since they might behave differently.
         */

        final XId fieldId = Base.toId("testGetEventsField");
        final XAddress fieldAddress = Base.resolveField(this.repoId, modelId, objectId, fieldId);
        final XCommand addFieldCom = this.comFactory.createAddFieldCommand(
                Base.resolveObject(this.repoId, modelId, objectId), fieldId, false);
        final long oldObjectRev = revNr;
        revNr = this.persistence.executeCommand(this.actorId, addFieldCom);
        assertTrue("The field  wasn't correctly added, test cannot be executed.", revNr >= 0);

        // get events from the field first and check them
        events = this.persistence.getEvents(fieldAddress, revNr, revNr);

        assertEquals(
                "List of events should only contain one event (the \"create field\"-event), but actually contains zero or multiple events.",
                1, events.size());

        final XEvent fieldAddEvent = events.get(0);
        assertTrue(
                "The returned event is not an XObjectEvent, but \"field was created\"-events are XObjectEvents",
                fieldAddEvent instanceof XObjectEvent);
        assertEquals("The returned event was not of add-type.", ChangeType.ADD,
                fieldAddEvent.getChangeType());

        // oldObjectRev is also the old revision number of the model (before the
        // field was added)
        assertEquals("The event didn't refer to the correct old model revision number.",
                oldObjectRev, fieldAddEvent.getOldModelRevision());

        assertEquals("The event didn't refer to the correct old object revision number.",
                oldObjectRev, fieldAddEvent.getOldObjectRevision());
        assertEquals("The event didn't refer to the correct revision number.", revNr,
                fieldAddEvent.getRevisionNumber());
        assertEquals("Event doesn't refer to the correct target.", objectAddress,
                fieldAddEvent.getTarget());
        assertEquals("Event doesn't refer to the correct field.", fieldAddress,
                fieldAddEvent.getChangedEntity());
        assertEquals("The actor of the event is not correct.", this.actorId,
                fieldAddEvent.getActor());
        assertFalse("The event is wrongly marked as implied.", fieldAddEvent.isImplied());
        assertFalse("the event is wrongly marked as being part of a transaction.",
                fieldAddEvent.inTransaction());

        /*
         * check if the list returned by the object contains the correct events.
         */
        events = this.persistence.getEvents(objectAddress, revNr, revNr);
        assertEquals(
                "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
                1, events.size());
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));

        events = this.persistence.getEvents(objectAddress, 0, revNr);

        assertEquals(
                "The list does not contain exactly 2 elements, although only 2 commands were executed.",
                2, events.size());
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));

        /*
         * check if the list returned by the model contains the correct events.
         */
        events = this.persistence.getEvents(modelAddress, revNr, revNr);
        assertEquals(
                "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
                1, events.size());
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));

        // oldObjectRev is also the old revision number of the model (before the
        // field was added)
        events = this.persistence.getEvents(modelAddress, oldObjectRev, revNr);

        assertEquals(
                "The list does not contain exactly 2 elements, although only 2 commands were executed.",
                2, events.size());
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));

        events = this.persistence.getEvents(modelAddress, 0, revNr);

        assertEquals(
                "The list does not contain exactly 3 elements, although only 3 commands were executed.",
                3, events.size());
        assertTrue("The list does not contain the \"add model\"-event.",
                events.contains(modelAddEvent));
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));

        /*
         * add a value and check if the correct events are returned. Check
         * getEvents(fieldAddress...), getEvents(objectAddress...) and
         * getEvents(modelAddress...) since they might behave differently.
         */

        final XValue value = BaseRuntime.getValueFactory().createStringValue("testValue");
        final XCommand addValueCom = this.comFactory.createAddValueCommand(
                Base.resolveField(this.repoId, modelId, objectId, fieldId), revNr, value, false);
        final long oldFieldRev = revNr;
        revNr = this.persistence.executeCommand(this.actorId, addValueCom);
        assertTrue("The value  wasn't correctly added, test cannot be executed.", revNr >= 0);

        // get events from the field first and check them
        events = this.persistence.getEvents(fieldAddress, revNr, revNr);

        assertEquals(
                "List of events should only contain one event (the \"add value\"-event), but actually contains zero or multiple events.",
                1, events.size());

        final XEvent valueAddEvent = events.get(0);
        assertTrue(
                "The returned event is not an XFieldEvent, but \"value was added\"-events are XFieldEvents",
                valueAddEvent instanceof XFieldEvent);
        assertEquals("The returned event was not of add-type.", ChangeType.ADD,
                valueAddEvent.getChangeType());

        // oldFieldRev is also the old revision number of the model & object
        // (before the
        // value was added)
        assertEquals("The event didn't refer to the correct old model revision number.",
                oldFieldRev, valueAddEvent.getOldModelRevision());
        assertEquals("The event didn't refer to the correct old object revision number.",
                oldFieldRev, valueAddEvent.getOldObjectRevision());

        assertEquals("The event didn't refer to the correct revision number.", revNr,
                valueAddEvent.getRevisionNumber());
        assertEquals("Event doesn't refer to the correct target.", fieldAddress,
                valueAddEvent.getTarget());
        assertEquals("Event doesn't refer to the correct field.", fieldAddress,
                valueAddEvent.getChangedEntity());
        assertEquals("The actor of the event is not correct.", this.actorId,
                valueAddEvent.getActor());
        assertFalse("The event is wrongly marked as implied.", valueAddEvent.isImplied());
        assertFalse("the event is wrongly marked as being part of a transaction.",
                valueAddEvent.inTransaction());

        events = this.persistence.getEvents(fieldAddress, oldFieldRev, revNr);
        assertEquals(
                "The list does not contain exactly 2 elements, even though only 2 commands were executed.",
                2, events.size());
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(valueAddEvent));

        events = this.persistence.getEvents(fieldAddress, 0, revNr);
        assertEquals(
                "The list does not contain exactly 2 elements, even though only 2 commands were executed which affected the field.",
                2, events.size());

        /*
         * check if the list returned by the object contains the correct events.
         */
        events = this.persistence.getEvents(objectAddress, revNr, revNr);
        assertEquals(
                "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
                1, events.size());
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));

        events = this.persistence.getEvents(objectAddress, oldFieldRev, revNr);

        assertEquals(
                "The list does not contain exactly 2 elements, even though only 2 commands were executed.",
                2, events.size());
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));

        events = this.persistence.getEvents(objectAddress, oldObjectRev, revNr);

        assertEquals(
                "The list does not contain exactly 3 elements, even though only 3 commands were executed.",
                3, events.size());
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));

        events = this.persistence.getEvents(objectAddress, 0, revNr);

        assertEquals(
                "The list does not contain exactly 3 elements, even though only 3 commands were executed which affected the object.",
                3, events.size());

        /*
         * check if the list returned by the model contains the correct events.
         */
        events = this.persistence.getEvents(modelAddress, revNr, revNr);
        assertEquals(
                "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
                1, events.size());
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));

        events = this.persistence.getEvents(modelAddress, oldFieldRev, revNr);

        assertEquals(
                "The list does not contain exactly 2 elements, although only 2 commands were executed.",
                2, events.size());
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));

        events = this.persistence.getEvents(modelAddress, oldObjectRev, revNr);

        assertEquals(
                "The list does not contain exactly 3 elements, although only 3 commands were executed.",
                3, events.size());
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));

        events = this.persistence.getEvents(modelAddress, 0, revNr);

        assertEquals(
                "The list does not contain exactly 4 elements, although only 4 commands were executed.",
                4, events.size());
        assertTrue("The list does not contain the \"add model\"-event.",
                events.contains(modelAddEvent));
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));

        /*
         * change the value and check if the correct events are returned. Check
         * getEvents(fieldAddress...), getEvents(objectAddress...) and
         * getEvents(modelAddress...) since they might behave differently.
         */

        final XValue value2 = BaseRuntime.getValueFactory().createStringValue("testValue2");
        final XCommand changeValueCom = this.comFactory.createChangeValueCommand(
                Base.resolveField(this.repoId, modelId, objectId, fieldId), revNr, value2, false);
        final long oldFieldRev2 = revNr;
        revNr = this.persistence.executeCommand(this.actorId, changeValueCom);
        assertTrue("The value  wasn't correctly changed, test cannot be executed.", revNr >= 0);

        // get events from the field first and check them
        events = this.persistence.getEvents(fieldAddress, revNr, revNr);

        assertEquals(
                "List of events should only contain one event (the \"change value\"-event), but actually contains zero or multiple events.",
                1, events.size());

        final XEvent valueChangeEvent = events.get(0);
        assertTrue(
                "The returned event is not an XFieldEvent, but \"value was changed\"-events are XFieldEvents",
                valueChangeEvent instanceof XFieldEvent);
        assertEquals("The returned event was not of change-type.", ChangeType.CHANGE,
                valueChangeEvent.getChangeType());

        /*
         * oldFieldRev2 is also the old revision number of the model & object
         * (before the value was changed)
         */
        assertEquals("The event didn't refer to the correct old model revision number.",
                oldFieldRev2, valueChangeEvent.getOldModelRevision());
        assertEquals("The event didn't refer to the correct old object revision number.",
                oldFieldRev2, valueChangeEvent.getOldObjectRevision());

        assertEquals("The event didn't refer to the correct revision number.", revNr,
                valueChangeEvent.getRevisionNumber());
        assertEquals("Event doesn't refer to the correct target.", fieldAddress,
                valueChangeEvent.getTarget());
        assertEquals("Event doesn't refer to the correct field.", fieldAddress,
                valueChangeEvent.getChangedEntity());
        assertEquals("The actor of the event is not correct.", this.actorId,
                valueChangeEvent.getActor());
        assertFalse("The event is wrongly marked as implied.", valueChangeEvent.isImplied());
        assertFalse("the event is wrongly marked as being part of a transaction.",
                valueChangeEvent.inTransaction());

        events = this.persistence.getEvents(fieldAddress, oldFieldRev2, revNr);
        assertEquals(
                "The list does not contain exactly 2 elements, although only 2 new command was executed since the last revision.",
                2, events.size());
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));

        events = this.persistence.getEvents(fieldAddress, oldFieldRev, revNr);
        assertEquals(
                "The list does not contain exactly 3 elements, even though only 3 commands were executed.",
                3, events.size());
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));

        events = this.persistence.getEvents(fieldAddress, 0, revNr);
        assertEquals(
                "The list contains more than 3 elements, even though only 3 commands were executed which affected the field.",
                3, events.size());

        /*
         * check if the list returned by the object contains the correct events.
         */
        events = this.persistence.getEvents(objectAddress, revNr, revNr);
        assertEquals(
                "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
                1, events.size());
        assertTrue("The list does not contain the \"change value\"-event.",
                events.contains(valueChangeEvent));

        events = this.persistence.getEvents(objectAddress, oldFieldRev2, revNr);

        assertEquals(
                "The list does not contain exactly 2 elements, even though only 2 commands were executed.",
                2, events.size());
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));

        events = this.persistence.getEvents(objectAddress, oldFieldRev, revNr);

        assertEquals(
                "The list does not contain exactly 3 elements, even though only 3 commands were executed.",
                3, events.size());
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));

        events = this.persistence.getEvents(objectAddress, oldObjectRev, revNr);

        assertEquals(
                "The list does not contain exactly 4 elements, even though only 4 commands were executed.",
                4, events.size());
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));

        events = this.persistence.getEvents(objectAddress, 0, revNr);

        assertEquals(
                "The list does not contain exactly 4 elements, even though only 4 commands were executed which affected the object.",
                4, events.size());

        /*
         * check if the list returned by the model contains the correct events.
         */
        events = this.persistence.getEvents(modelAddress, revNr, revNr);
        assertEquals(
                "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
                1, events.size());
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));

        events = this.persistence.getEvents(modelAddress, oldFieldRev2, revNr);

        assertEquals(
                "The list does not contain exactly 2 elements, although only 2 commands were executed.",
                2, events.size());
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));

        events = this.persistence.getEvents(modelAddress, oldFieldRev, revNr);

        assertEquals(
                "The list does not contain exactly 3 elements, although only 3 commands were executed.",
                3, events.size());
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));

        events = this.persistence.getEvents(modelAddress, oldObjectRev, revNr);

        assertEquals(
                "The list does not contain exactly 4 elements, although only 4 commands were executed.",
                4, events.size());
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));

        events = this.persistence.getEvents(modelAddress, 0, revNr);

        assertEquals(
                "The list does not contain exactly 5 elements, although only 5 commands were executed.",
                5, events.size());
        assertTrue("The list does not contain the \"add model\"-event.",
                events.contains(modelAddEvent));
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));

        /*
         * remove the value and check if the correct events are returned. Check
         * getEvents(fieldAddress...), getEvents(objectAddress...) and
         * getEvents(modelAddress...) since they might behave differently.
         */

        final XCommand removeValueCom = this.comFactory.createRemoveValueCommand(this.repoId, modelId,
                objectId, fieldId, revNr, false);
        final long oldFieldRev3 = revNr;
        revNr = this.persistence.executeCommand(this.actorId, removeValueCom);
        assertTrue("The value  wasn't correctly removed, test cannot be executed.", revNr >= 0);

        // get events from the field first and check them
        events = this.persistence.getEvents(fieldAddress, revNr, revNr);

        assertEquals(
                "List of events should only contain one event (the \"change value\"-event), but actually contains zero or multiple events.",
                1, events.size());

        final XEvent valueRemoveEvent = events.get(0);
        assertTrue(
                "The returned event is not an XFieldEvent, but \"value was removed\"-events are XFieldEvents",
                valueRemoveEvent instanceof XFieldEvent);
        assertEquals("The returned event was not of remove-type.", ChangeType.REMOVE,
                valueRemoveEvent.getChangeType());

        /*
         * oldFieldRev3 is also the old revision number of the model & object
         * (before the value was changed)
         */
        assertEquals("The event didn't refer to the correct old model revision number.",
                oldFieldRev3, valueRemoveEvent.getOldModelRevision());
        assertEquals("The event didn't refer to the correct old object revision number.",
                oldFieldRev3, valueRemoveEvent.getOldObjectRevision());

        assertEquals("The event didn't refer to the correct revision number.", revNr,
                valueRemoveEvent.getRevisionNumber());
        assertEquals("Event doesn't refer to the correct target.", fieldAddress,
                valueRemoveEvent.getTarget());
        assertEquals("Event doesn't refer to the correct field.", fieldAddress,
                valueRemoveEvent.getChangedEntity());
        assertEquals("The actor of the event is not correct.", this.actorId,
                valueRemoveEvent.getActor());
        assertFalse("The event is wrongly marked as implied.", valueRemoveEvent.isImplied());
        assertFalse("the event is wrongly marked as being part of a transaction.",
                valueRemoveEvent.inTransaction());

        events = this.persistence.getEvents(fieldAddress, oldFieldRev3, revNr);
        assertEquals(
                "The list does not contain exactly 2 element, although only 2 new command was executed since the last revision.",
                2, events.size());
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(fieldAddress, oldFieldRev2, revNr);
        assertEquals(
                "The list does not contain exactly 3 element, although only 3 new command was executed since the last revision.",
                3, events.size());
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(fieldAddress, oldFieldRev, revNr);
        assertEquals(
                "The list does not contain exactly 4 elements, even though only 4 commands were executed.",
                4, events.size());
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(fieldAddress, 0, revNr);
        assertEquals(
                "The list does not contain exactly 4 elements, even though only 4 commands were executed which affected the field.",
                4, events.size());

        /*
         * check if the list returned by the object contains the correct events.
         */
        events = this.persistence.getEvents(objectAddress, revNr, revNr);
        assertEquals(
                "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
                1, events.size());
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(objectAddress, oldFieldRev3, revNr);

        assertEquals(
                "The list does not contain exactly 2 elements, even though only 2 commands were executed.",
                2, events.size());
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(objectAddress, oldFieldRev2, revNr);

        assertEquals(
                "The list does not contain exactly 3 elements, even though only 3 commands were executed.",
                3, events.size());
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(objectAddress, oldFieldRev, revNr);

        assertEquals(
                "The list does not contain exactly 4 elements, even though only 4 commands were executed.",
                4, events.size());
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(objectAddress, oldObjectRev, revNr);

        assertEquals(
                "The list does not contain exactly 5 elements, even though only 5 commands were executed.",
                5, events.size());
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(objectAddress, 0, revNr);

        assertEquals(
                "The list does not contain exactly 5 elements, even though only 5 commands were executed which affected the object.",
                5, events.size());

        /*
         * check if the list returned by the model contains the correct events.
         */
        events = this.persistence.getEvents(modelAddress, revNr, revNr);
        assertEquals(
                "The list contains zero or more than 1 element, although only 1 new command was executed since the last revision.",
                1, events.size());
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(modelAddress, oldFieldRev3, revNr);

        assertEquals(
                "The list does not contain exactly 2 elements, although only 2 commands were executed.",
                2, events.size());
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(modelAddress, oldFieldRev2, revNr);

        assertEquals(
                "The list does not contain exactly 3 elements, although only 3 commands were executed.",
                3, events.size());
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(modelAddress, oldFieldRev, revNr);

        assertEquals(
                "The list does not contain exactly 4 elements, although only 4 commands were executed.",
                4, events.size());
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(modelAddress, oldObjectRev, revNr);

        assertEquals(
                "The list does not contain exactly 5 elements, although only 5 commands were executed.",
                5, events.size());
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        events = this.persistence.getEvents(modelAddress, 0, revNr);

        assertEquals(
                "The list does not contain exactly 6 elements, although only 6 commands were executed.",
                6, events.size());
        assertTrue("The list does not contain the \"add model\"-event.",
                events.contains(modelAddEvent));
        assertTrue("The list does not contain the \"add object\"-event.",
                events.contains(objectAddEvent));
        assertTrue("The list does not contain the \"add field\"-event.",
                events.contains(fieldAddEvent));
        assertTrue("The list does not contain the \"add value\"-event.",
                events.contains(valueAddEvent));
        assertTrue("The list does not contain the \"change field\"-event.",
                events.contains(valueChangeEvent));
        assertTrue("The list does not contain the \"remove field\"-event.",
                events.contains(valueRemoveEvent));

        /*
         * Remove the object and check if the correct events are returned.
         *
         * The returned event should be an XTransactionEvent since removing the
         * object also removes the field. Check that the \"field was
         * removed\"-event is implicit.
         */

        final XCommand removeObjectCom = this.comFactory.createRemoveObjectCommand(this.repoId, modelId,
                objectId, revNr, false);
        final long oldObjectRev2 = revNr;
        revNr = this.persistence.executeCommand(this.actorId, removeObjectCom);
        assertTrue("The object  wasn't correctly removed, test cannot be executed.", revNr >= 0);

        // get events from the model first and check them
        events = this.persistence.getEvents(modelAddress, revNr, revNr);

        assertEquals(
                "List of events should contain one transaction event (which represents the \"object was removed\"- and the implicit \"field was removed\"-event), but actually contains zero or more than 2 events.",
                1, events.size());

        assertTrue(
                "The returned event is not an XTransactionEvent, but it should be an XTransactionEvent, since we removed the object and thereby also implicitly removed the field.",
                events.get(0) instanceof XTransactionEvent);
        final XTransactionEvent transEvent = (XTransactionEvent)events.get(0);

        /*
         * check the transaction event
         */
        assertEquals("The transaction event is not of the transaction-type.",
                ChangeType.TRANSACTION, transEvent.getChangeType());
        assertEquals(
                "The transcation event should contain 2 events, but actually contains zero or more than 2 events.",
                2, transEvent.size());
        assertEquals("The event didn't refer to the correct old model revision number.",
                oldObjectRev2, transEvent.getOldModelRevision());

        assertEquals("The event didn't refer to the correct revision number.", revNr,
                transEvent.getRevisionNumber());
        assertEquals("Event doesn't refer to the correct target.", modelAddress,
                transEvent.getTarget());
        assertEquals("Event doesn't refer to the correct changed entity.", modelAddress,
                transEvent.getChangedEntity());
        assertEquals("The actor of the event is not correct.", this.actorId, transEvent.getActor());
        assertFalse("The event is wrongly marked as bein implied implied.", transEvent.isImplied());
        assertFalse("the event is wrongly marked as being part of a transaction.",
                transEvent.inTransaction());

        XEvent fieldRemoveEvent, objectRemoveEvent;

        /*
         * check that the transaction event consists of one XModelEvent (=
         * Object was removed) and one XObjectEvent (= Field was removed)
         */
        if(transEvent.getEvent(0) instanceof XObjectEvent) {
            fieldRemoveEvent = transEvent.getEvent(0);
            objectRemoveEvent = transEvent.getEvent(1);

            assertTrue("There was an XObjectEvent, but no XModelEvent.",
                    objectRemoveEvent instanceof XModelEvent);
        } else {
            fieldRemoveEvent = transEvent.getEvent(1);
            objectRemoveEvent = transEvent.getEvent(0);

            assertTrue("The first event is neither an XObject- nor an XModelEvent.",
                    objectRemoveEvent instanceof XModelEvent);
            assertTrue("The second event is not an XModelEvent",
                    fieldRemoveEvent instanceof XObjectEvent);
        }

        assertEquals("The returned event was not of remove-type.", ChangeType.REMOVE,
                objectRemoveEvent.getChangeType());
        assertEquals("The returned event was not of remove-type.", ChangeType.REMOVE,
                fieldRemoveEvent.getChangeType());

        /*
         * oldObjectRev2 is also the old revision number of the model & object
         * (before the value was changed)
         */
        assertEquals("The event didn't refer to the correct old model revision number.",
                oldObjectRev2, fieldRemoveEvent.getOldModelRevision());
        assertEquals("The event didn't refer to the correct old object revision number.",
                oldObjectRev2, fieldRemoveEvent.getOldObjectRevision());

        assertEquals("The event didn't refer to the correct revision number.", revNr,
                fieldRemoveEvent.getRevisionNumber());
        assertEquals("Event doesn't refer to the correct target.", objectAddress,
                fieldRemoveEvent.getTarget());
        assertEquals("Event doesn't refer to the correct field.", fieldAddress,
                fieldRemoveEvent.getChangedEntity());
        assertEquals("The actor of the event is not correct.", this.actorId,
                fieldRemoveEvent.getActor());
        assertTrue("The event is wrongly marked as not implied.", fieldRemoveEvent.isImplied());
        assertTrue("the event is wrongly marked as not being part of a transaction.",
                fieldRemoveEvent.inTransaction());

        assertEquals("The event didn't refer to the correct old model revision number.",
                oldObjectRev2, objectRemoveEvent.getOldModelRevision());
        assertEquals("The event didn't refer to the correct old object revision number.",
                oldObjectRev2, objectRemoveEvent.getOldObjectRevision());

        assertEquals("The event didn't refer to the correct revision number.", revNr,
                objectRemoveEvent.getRevisionNumber());
        assertEquals("Event doesn't refer to the correct target.", modelAddress,
                objectRemoveEvent.getTarget());
        assertEquals("Event doesn't refer to the correct field.", objectAddress,
                objectRemoveEvent.getChangedEntity());
        assertEquals("The actor of the event is not correct.", this.actorId,
                objectRemoveEvent.getActor());
        assertFalse("The event is wrongly marked as implied.", objectRemoveEvent.isImplied());
        assertTrue("the event is wrongly marked as not being part of a transaction.",
                objectRemoveEvent.inTransaction());

        /*
         * removing the model doesn't need to be checked, since the getEvents
         * method only gets events from models, objects and fields, but not from
         * the repository.
         */

    }

    @Test
    public void testGetManagedModelIds() {

        Set<XId> managedIds = this.persistence.getManagedModelIds();

        assertTrue("The persistence already has some managed IDs, although no models were added. "
                + managedIds, managedIds.isEmpty());

        // add a model
        XId modelId = Base.toId("testGetManagedModelsIdModel1");
        XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);
        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        managedIds = this.persistence.getManagedModelIds();

        assertTrue("The set of managed ids does not contain the XId of the newly added model.",
                managedIds.contains(modelId));
        assertEquals(
                "The set of managed ids contains more than one XId, although we only added one model.",
                1, managedIds.size());

        // add some more models
        final Set<XId> addedModelIds = new HashSet<XId>();
        addedModelIds.add(modelId);
        for(int i = 2; i < 32; i++) {
            modelId = Base.toId("testGetManagedModelsIdModel" + i);
            addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
            revNr = this.persistence.executeCommand(this.actorId, addModelCmd);
            assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

            addedModelIds.add(modelId);
        }

        managedIds = this.persistence.getManagedModelIds();

        assertEquals("The amount of managed XIds does not match the amount of added models.",
                addedModelIds.size(), managedIds.size());

        for(final XId id : addedModelIds) {
            assertTrue(
                    "The set of managed XIds doesn't contain one of the XIds of a model we've added.",
                    managedIds.contains(id));
        }
    }

    @Test
    public void testGetModelRevision() {

        /*
         * TODO what are tentative revision numbers? These need to be tested,
         * too.
         */

        final XId modelId = Base.toId("testGetModelRevisionModel1");
        final XAddress modelAddress = Base.resolveModel(this.repoId, modelId);
        final XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);
        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        final GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
        XWritableModel model = this.persistence.getModelSnapshot(modelAdrRequest);

        ModelRevision storedRevision = this.persistence.getModelRevision(modelAdrRequest);

        assertEquals("Revision number does not match.", model.getRevisionNumber(),
                storedRevision.revision());
        assertTrue("The model still exists, but exists() returns false.",
                storedRevision.modelExists());

        // add some objects, to increase the revision number
        final int nrOfModels = 10;
        for(int i = 2; i < nrOfModels; i++) {
            final XId objectId = Base.toId("testGetModelRevisionModel" + i);
            final XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                    Base.resolveModel(this.repoId, modelId), objectId, false);
            this.persistence.executeCommand(this.actorId, addObjectCom);
        }

        model = this.persistence.getModelSnapshot(modelAdrRequest);
        storedRevision = this.persistence.getModelRevision(modelAdrRequest);

        assertEquals("Revision number does not match.", model.getRevisionNumber(),
                storedRevision.revision());
        assertTrue("The model still exists, but exists() returns false.",
                storedRevision.modelExists());

        // remove the model
        final XCommand removeModelCmd = this.comFactory.createRemoveModelCommand(this.repoId, modelId,
                model.getRevisionNumber(), false);
        revNr = this.persistence.executeCommand(this.actorId, removeModelCmd);
        assertTrue("The model wasn't correctly removed, test cannot be executed.", revNr >= 0);

        storedRevision = this.persistence.getModelRevision(modelAdrRequest);

        /*
         * according to the documentation of {@link ModelRevision}, a
         * ModelRevision stores the old revision number after the model was
         * removed.
         */
        assertEquals("Model was removed, the stored revision should be XCommand.FAILED.", revNr,
                storedRevision.revision());
        assertFalse("The model does no longer exist, but exists() returns true.",
                storedRevision.modelExists());

        /*
         * try to get the model revision of a not existing model
         *
         * According to the documentation of {@link XydraPersistence} null is
         * returned, if the state is not known.
         *
         * TODO It is unclear what is meant with that, but I suppose
         * "state is not known" means that no such model ever existed. ~ Kaidel
         */
        final XId modelId2 = Base.toId("testGetModelRevisionModel" + (nrOfModels + 1));
        final XAddress modelAddress2 = Base.resolveModel(this.repoId, modelId2);
        final GetWithAddressRequest modelAdr2Request = new GetWithAddressRequest(modelAddress2);

        storedRevision = this.persistence.getModelRevision(modelAdr2Request);

        assertEquals(
                "Returned model revision should be ModelRevision.MODEL_DOES_NOT_EXIST_YET, since the model never existed.",
                ModelRevision.MODEL_DOES_NOT_EXIST_YET, storedRevision);
    }

    @Test
    public void testGetModelSnapshot() {

        /*
         * create a model and execute some commands on it, manage a separate
         * model in parallel, execute the same commands on that one and compare
         * the returned model to the self-managed model
         */

        final XId modelId = BaseRuntime.getIDProvider().fromString("testGetModelSnapshotModel");
        final XAddress modelAddress = Base.resolveModel(this.repoId, modelId);

        final XRepository repo = X.createMemoryRepository(this.repoId);
        final XModel model = repo.createModel(modelId);

        final XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        assertTrue("Model could not be created, test cannot be executed.", revNr >= 0);

        final int numberOfObjects = 15;
        final int numberOfObjectsWithFields = 10;
        final int numberOfObjectsToRemove = 5;

        assert numberOfObjects >= numberOfObjectsWithFields;
        assert numberOfObjectsWithFields >= numberOfObjectsToRemove;

        final int numberOfFields = 15;
        final int numberOfFieldsWithValue = 10;
        final int numberOfFieldsToRemove = 5;

        assert numberOfFields >= numberOfFieldsWithValue;
        assert numberOfFieldsWithValue >= numberOfFieldsToRemove;

        final List<XId> objectIds = new ArrayList<XId>();
        for(int i = 0; i < numberOfObjects; i++) {
            final XId objectId = BaseRuntime.getIDProvider().fromString("testGetModelSnapshotObject" + i);
            objectIds.add(objectId);

            model.createObject(objectId);

            final XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                    Base.resolveModel(this.repoId, modelId), objectId, false);
            revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
            assertTrue("The " + i + ". Object could not be created, test cannot be executed.",
                    revNr > 0);
        }

        for(int i = 0; i < numberOfObjectsWithFields; i++) {
            final XId objectId = objectIds.get(i);
            final XObject object = model.getObject(objectId);

            for(int j = 0; j < numberOfFields; j++) {
                final XId fieldId = BaseRuntime.getIDProvider().fromString("testGetModelSnapshotField" + j);

                final XField field = object.createField(fieldId);

                final XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                        Base.resolveObject(this.repoId, modelId, objectId), fieldId, false);
                revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
                assertTrue("The " + j + ". field could not be created in the Object with id "
                        + objectId + ", test cannot be executed.", revNr > 0);

                if(j < numberOfFieldsWithValue) {
                    final XValue value = BaseRuntime.getValueFactory().createStringValue(
                            "testGetModelSnapshotValue" + j);
                    field.setValue(value);

                    final XCommand addValueCommand = this.comFactory.createAddValueCommand(
                            Base.resolveField(this.repoId, modelId, objectId, fieldId), revNr, value,
                            false);
                    revNr = this.persistence.executeCommand(this.actorId, addValueCommand);
                    assertTrue("The value for the " + j
                            + ". field could not be created in the Object with id " + objectId
                            + ", test cannot be executed.", revNr >= 0);

                    if(j < numberOfFieldsToRemove) {
                        object.removeField(fieldId);

                        final XCommand removeFieldCommand = this.comFactory.createRemoveFieldCommand(
                                this.repoId, modelId, objectId, fieldId, revNr, false);
                        revNr = this.persistence.executeCommand(this.actorId, removeFieldCommand);
                        assertTrue("The " + j
                                + ". field could not be removed in the object with id " + objectId
                                + ", test cannot be executed.", revNr >= 0);
                    }
                }
            }

            if(i < numberOfObjectsToRemove) {
                model.removeObject(objectId);

                final XCommand removeObjectCommand = this.comFactory.createRemoveObjectCommand(
                        this.repoId, modelId, objectId, revNr, false);
                revNr = this.persistence.executeCommand(this.actorId, removeObjectCommand);
                assertTrue("The " + i + ". object could not be removed, test cannot be executed.",
                        revNr >= 0);
            }
        }

        // get the model snapshot
        final GetWithAddressRequest modelReq = new GetWithAddressRequest(modelAddress);
        final XWritableModel modelSnapshot = this.persistence.getModelSnapshot(modelReq);

        int objectsInManagedModel = 0;
        int objectsInSnapshot = 0;

        for(@SuppressWarnings("unused") final
        XId objectId : model) {
            objectsInManagedModel++;
        }

        for(@SuppressWarnings("unused") final
        XId objectId : modelSnapshot) {
            objectsInSnapshot++;
        }

        assertEquals("The snapshot does not have the correct amount of objects.",
                objectsInManagedModel, objectsInSnapshot);

        /*
         * compare the managed model with the snapshot. "equals" might not work
         * here, since the returned java objects might be of different types, so
         * we'll have to test the equality manually.
         */

        assertEquals(modelId, modelSnapshot.getId());
        assertEquals(model.getRevisionNumber(), modelSnapshot.getRevisionNumber());

        int nrOfObjectsInManagedModel = 0;
        int nrOfObjectsInModelSnapshot = 0;

        for(@SuppressWarnings("unused") final
        XId objectId : model) {
            nrOfObjectsInManagedModel++;
        }

        for(@SuppressWarnings("unused") final
        XId objectId : modelSnapshot) {
            nrOfObjectsInModelSnapshot++;
        }

        assertEquals("The snapshot does not have the correct amount of objects.",
                nrOfObjectsInManagedModel, nrOfObjectsInModelSnapshot);

        for(final XId objectId : model) {
            assertTrue("Snapshot does not contain an object which it should contain.",
                    modelSnapshot.hasObject(objectId));

            final XObject object = model.getObject(objectId);
            final XWritableObject objectSnapshot = modelSnapshot.getObject(objectId);

            int nrOfFieldsInManagedObject = 0;
            int nrOfFieldsInObjectSnapshot = 0;

            for(@SuppressWarnings("unused") final
            XId fieldId : object) {
                nrOfFieldsInManagedObject++;
            }

            for(@SuppressWarnings("unused") final
            XId fieldId : objectSnapshot) {
                nrOfFieldsInObjectSnapshot++;
            }

            assertEquals("The object snapshot does not have the correct amount of fields.",
                    nrOfFieldsInManagedObject, nrOfFieldsInObjectSnapshot);

            for(final XId fieldId : object) {
                assertTrue("Snapshot does not contain a field which it should contain.",
                        objectSnapshot.hasField(fieldId));

                final XField field = object.getField(fieldId);
                final XWritableField fieldSnapshot = objectSnapshot.getField(fieldId);

                assertEquals("The field snapshot does not contain the correct value.",
                        field.getValue(), fieldSnapshot.getValue());
            }

        }

    }

    @Test
    public void testGetModelSnapshotWrongAddressType() {
        final XId modelId = Base.createUniqueId();
        final XId objectId = Base.createUniqueId();
        final XId fieldId = Base.createUniqueId();

        final XAddress repoAddress = Base.resolveRepository(this.repoId);
        final XAddress objectAddress = Base.resolveObject(this.repoId, objectId, fieldId);
        final XAddress fieldAddress = Base.resolveField(this.repoId, modelId, objectId, fieldId);

        try {
            this.persistence.getModelSnapshot(new GetWithAddressRequest(repoAddress));
            assertTrue("The method should've thrown an execption", false);
        } catch(final Exception e) {
            assertTrue(true);
        }

        try {
            this.persistence.getModelSnapshot(new GetWithAddressRequest(objectAddress));
            assertTrue("The method should've thrown an execption", false);
        } catch(final Exception e) {
            assertTrue(true);
        }

        try {
            this.persistence.getModelSnapshot(new GetWithAddressRequest(fieldAddress));
            assertTrue("The method should've thrown an execption", false);
        } catch(final Exception e) {
            assertTrue(true);
        }
    }

    /** This is the only test covering TentativeObjectState */
    @Test
    public void testGetLargeModelSnapshot() {
        /*
         * Test that such a large snapshot at least is computed without throwing
         * an error
         */
        final WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(
                this.persistence, this.actorId);
        final XId model1 = Base.toId("model1");
        final XWritableModel model = repo.createModel(model1);
        for(int i = 0; i < 600; i++) {
            model.createObject(Base.toId("object" + i));
        }

        log.info("Getting snapshot");
        final XWritableModel snapshot = this.persistence.getModelSnapshot(new GetWithAddressRequest(Base
                .resolveModel(this.repoId, model1), true));
        assertNotNull(snapshot);
    }

    @Test
    public void testGetObjectSnapshot() {

        /*
         * create an object and execute some commands on it, manage a separate
         * object in parallel, execute the same commands on that one and compare
         * the returned object to the self-managed object
         */

        // create appropriate objects first
        final XId modelId = BaseRuntime.getIDProvider().fromString("testGetObjectSnapshotModel");

        final XRepository repo = X.createMemoryRepository(this.repoId);
        final XModel model = repo.createModel(modelId);

        final XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        assertTrue("Model could not be created, test cannot be executed.", revNr >= 0);

        final int numberOfFields = 15;
        final int numberOfFieldsWithValue = 10;
        final int numberOfFieldsToRemove = 5;

        assert numberOfFields >= numberOfFieldsWithValue;
        assert numberOfFieldsWithValue >= numberOfFieldsToRemove;

        final XId objectId = BaseRuntime.getIDProvider().fromString("testGetObjectSnapshotObject");
        final XAddress objectAddress = Base.resolveObject(this.repoId, modelId, objectId);
        final XObject object = model.createObject(objectId);

        final XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                Base.resolveModel(this.repoId, modelId), objectId, false);
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        assertTrue("The object could not be created, test cannot be executed.", revNr >= 0);

        final List<XId> fieldIds = new ArrayList<XId>();
        for(int i = 0; i < numberOfFields; i++) {
            final XId fieldId = BaseRuntime.getIDProvider().fromString("testGetObjectSnapshotField" + i);
            fieldIds.add(fieldId);

            final XField field = object.createField(fieldId);

            final XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                    Base.resolveObject(this.repoId, modelId, objectId), fieldId, false);
            revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
            assertTrue("The " + i + ". field could not be created in the Object with id "
                    + objectId + ", test cannot be executed.", revNr >= 0);

            if(i < numberOfFieldsWithValue) {
                final XValue value = BaseRuntime.getValueFactory().createStringValue(
                        "testGetObjectSnapshotValue" + i);
                field.setValue(value);

                final XCommand addValueCommand = this.comFactory.createAddValueCommand(
                        Base.resolveField(this.repoId, modelId, objectId, fieldId), revNr, value,
                        false);
                revNr = this.persistence.executeCommand(this.actorId, addValueCommand);
                assertTrue("The value for the " + i
                        + ". field could not be created in the Object with id " + objectId
                        + ", test cannot be executed.", revNr >= 0);

                if(i < numberOfFieldsToRemove) {
                    object.removeField(fieldId);

                    final XCommand removeFieldCommand = this.comFactory.createRemoveFieldCommand(
                            this.repoId, modelId, objectId, fieldId, revNr, false);
                    revNr = this.persistence.executeCommand(this.actorId, removeFieldCommand);
                    assertTrue("The " + i + ". field could not be removed in the object with id "
                            + objectId + ", test cannot be executed.", revNr >= 0);
                }
            }
        }

        // get the object snapshot
        final GetWithAddressRequest objectReq = new GetWithAddressRequest(objectAddress);
        final XWritableObject objectSnapshot = this.persistence.getObjectSnapshot(objectReq);

        /*
         * compare the managed object with the snapshot. "equals" might not work
         * here, since the returned java objects might be of different types, so
         * we'll have to test the equality manually.
         */

        assertEquals(objectId, objectSnapshot.getId());
        assertEquals(object.getRevisionNumber(), objectSnapshot.getRevisionNumber());

        int nrOfFields = 0;
        int nrOfFieldsInSnapshot = 0;

        for(@SuppressWarnings("unused") final
        XId fieldId : objectSnapshot) {
            nrOfFieldsInSnapshot++;
        }

        for(final XId fieldId : object) {
            nrOfFields++;

            assertTrue("Snapshot does not contain a field which it should contain.",
                    objectSnapshot.hasField(fieldId));

            final XField field = object.getField(fieldId);
            final XWritableField fieldSnapshot = objectSnapshot.getField(fieldId);

            assertEquals("Field snapshot does not contain the correct value.", field.getValue(),
                    fieldSnapshot.getValue());
        }

        assertEquals("Object snapshot does not contain the correct amount of fields.", nrOfFields,
                nrOfFieldsInSnapshot);
    }

    @Test
    public void testGetObjectSnapshotWrongAddressType() {
        final XId modelId = Base.createUniqueId();
        final XId objectId = Base.createUniqueId();
        final XId fieldId = Base.createUniqueId();

        final XAddress repoAddress = Base.resolveRepository(this.repoId);
        final XAddress modelAddress = Base.resolveModel(this.repoId, modelId);
        final XAddress fieldAddress = Base.resolveField(this.repoId, modelId, objectId, fieldId);

        try {
            this.persistence.getObjectSnapshot(new GetWithAddressRequest(repoAddress));
            assertTrue("The method should've thrown an execption", false);
        } catch(final Exception e) {
            assertTrue(true);
        }

        try {
            this.persistence.getObjectSnapshot(new GetWithAddressRequest(modelAddress));
            assertTrue("The method should've thrown an execption", false);
        } catch(final Exception e) {
            assertTrue(true);
        }

        try {
            this.persistence.getObjectSnapshot(new GetWithAddressRequest(fieldAddress));
            assertTrue("The method should've thrown an execption", false);
        } catch(final Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void testGetRepositoryId() {
        assertEquals(
                "The repositor id of the XydraPersistence wasn't set correctly - might also cause other tests to fail.",
                this.repoId, this.persistence.getRepositoryId());
    }

    // @Test
    public void testHasManagedModel() {
        XId modelId = Base.toId("testHasManagedModelModel1");
        assertFalse(
                "hasManagedModels() returns true, although the persistence has no managed models yet.",
                this.persistence.hasManagedModel(modelId));

        // add a model
        XCommand addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCmd);
        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

        assertTrue(
                "hasManagedModels(modelId) returns false, although we correctly added a model with the given modelId.",
                this.persistence.hasManagedModel(modelId));

        // add some more models
        final List<XId> addedModelIds = new ArrayList<XId>();
        addedModelIds.add(modelId);
        for(int i = 2; i < 32; i++) {
            modelId = Base.toId("testHasManagedModelModel" + i);
            addModelCmd = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
            revNr = this.persistence.executeCommand(this.actorId, addModelCmd);
            assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);

            addedModelIds.add(modelId);
        }

        for(final XId id : addedModelIds) {
            assertTrue(
                    "hasManagedModels(id) returns false, although we correctly added a model with the given id.",
                    this.persistence.hasManagedModel(id));
        }

        // delete some models and check if hasManagedModel still returns true

        final int nrOfModels = 12;

        for(int i = 0; i < nrOfModels; i++) {
            final XId id = addedModelIds.get(i);
            final XAddress modelAddress = Base.resolveModel(this.repoId, id);
            final GetWithAddressRequest addressRequest = new GetWithAddressRequest(modelAddress);
            final XWritableModel model = this.persistence.getModelSnapshot(addressRequest);

            final XCommand deleteModelCmd = this.comFactory.createRemoveModelCommand(this.repoId, id,
                    model.getRevisionNumber(), false);

            revNr = this.persistence.executeCommand(this.actorId, deleteModelCmd);
            assertTrue("The model wasn't correctly removed, test cannot be executed.", revNr >= 0);
        }

        for(int i = 0; i < nrOfModels; i++) {
            final XId id = addedModelIds.get(i);
            assertTrue(
                    "hasManagedModels(id) returns false after we removed the model with the given id, although the persistence once managed a model with this id.",
                    this.persistence.hasManagedModel(id));
        }
    }

    /**
     * test state bound safe commands for all relevant entity change types
     */
    @Test
    public void testExecuteSafeCommandStateBound() {

        final XId modelId = Base.toId("model1");
        final XAddress modelAddress = Base.resolveModel(this.repoId, modelId);
        final XId objectId = Base.toId("object1");
        final XAddress objectAddress = Base.resolveObject(this.repoId, modelId, objectId);
        final XId fieldId = Base.toId("fiedl1");
        final XAddress fieldAddress = Base.resolveField(objectAddress, fieldId);
        final XValue value = XV.toValue("value");
        final XStringValue newValue = XV.toValue("newValue");

        /*
         * test add, add although already exists, remove and remove although not
         * existing model commands
         */
        final XCommand addModelCmd = this.comFactory.createSafeAddModelCommand(this.repoId, modelId);
        long result = this.persistence.executeCommand(this.actorId, addModelCmd);
        assertTrue(XCommandUtils.success(result));
        assertTrue(XCommandUtils.changedSomething(result));
        assertTrue(this.persistence.getManagedModelIds().contains(modelId));

        final XRepositoryCommand addModelAgainCom = this.comFactory.createSafeAddModelCommand(
                this.repoId, modelId);
        result = this.persistence.executeCommand(this.actorId, addModelAgainCom);
        assertTrue(XCommandUtils.failed(result));

        final XCommand removeModelCmd = this.comFactory.createSafeRemoveModelCommand(modelAddress,
                RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND);
        result = this.persistence.executeCommand(this.actorId, removeModelCmd);
        assertTrue(XCommandUtils.success(result));
        assertTrue(XCommandUtils.changedSomething(result));
        assertNull(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress)));

        final XCommand removeModelAgainCom = this.comFactory.createSafeRemoveModelCommand(modelAddress,
                RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND);
        result = this.persistence.executeCommand(this.actorId, removeModelAgainCom);
        assertEquals(-1, result);

        /*
         * test add, add although already exists, remove and remove although not
         * existing object commands
         */
        // the model to whom we will add objects:
        this.persistence.executeCommand(this.actorId, addModelCmd);
        XWritableModel model;

        final XModelCommand addObjectCom = this.comFactory.createSafeAddObjectCommand(modelAddress,
                objectId);
        result = this.persistence.executeCommand(this.actorId, addObjectCom);
        assertTrue(XCommandUtils.success(result));
        assertTrue(XCommandUtils.changedSomething(result));
        model = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress));
        assertTrue(model.getObject(objectId) != null);

        final XModelCommand addObjectAgainCom = this.comFactory.createSafeAddObjectCommand(modelAddress,
                objectId);
        result = this.persistence.executeCommand(this.actorId, addObjectAgainCom);
        assertTrue(XCommandUtils.failed(result));

        final XModelCommand removeObjectCom = this.comFactory.createSafeRemoveObjectCommand(
                objectAddress, RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND);
        result = this.persistence.executeCommand(this.actorId, removeObjectCom);
        assertTrue(XCommandUtils.success(result));
        assertTrue(XCommandUtils.changedSomething(result));
        model = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress));
        assertTrue(model.isEmpty());

        final XModelCommand removeObjectAgainCom = this.comFactory.createSafeRemoveObjectCommand(
                objectAddress, RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND);
        result = this.persistence.executeCommand(this.actorId, removeObjectAgainCom);
        assertTrue(XCommandUtils.failed(result));

        /*
         * test add, add although already exists, remove and remove although not
         * existing field commands
         */
        // the object to whom we will add fields
        this.persistence.executeCommand(this.actorId, addObjectAgainCom);
        XWritableObject object;

        final XObjectCommand addFieldCom = this.comFactory.createSafeAddFieldCommand(objectAddress,
                fieldId);
        result = this.persistence.executeCommand(this.actorId, addFieldCom);
        assertTrue(XCommandUtils.success(result));
        assertTrue(XCommandUtils.changedSomething(result));
        object = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId);
        assertTrue(object.getField(fieldId) != null);

        final XObjectCommand addFieldAgainCom = this.comFactory.createSafeAddFieldCommand(objectAddress,
                fieldId);
        result = this.persistence.executeCommand(this.actorId, addFieldAgainCom);
        assertTrue(XCommandUtils.failed(result));

        final XObjectCommand removeFieldCom = this.comFactory.createSafeRemoveFieldCommand(fieldAddress,
                RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND);
        result = this.persistence.executeCommand(this.actorId, removeFieldCom);
        assertTrue(XCommandUtils.success(result));
        assertTrue(XCommandUtils.changedSomething(result));
        object = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId);
        assertTrue(object.isEmpty());

        final XObjectCommand removeFieldAgainCom = this.comFactory.createSafeRemoveFieldCommand(
                fieldAddress, RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND);
        result = this.persistence.executeCommand(this.actorId, removeFieldAgainCom);
        assertTrue(XCommandUtils.failed(result));

        /*
         * test add and add although already existing, remove remove although
         * already removed as well as change value and change value although
         * different old value commands
         */
        // the field to whom we will add and change values
        this.persistence.executeCommand(this.actorId, addFieldAgainCom);
        XWritableField field;

        final XFieldCommand addValueCom = this.comFactory.createSafeAddValueCommand(fieldAddress,
                RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND, value);
        result = this.persistence.executeCommand(this.actorId, addValueCom);
        assertTrue(XCommandUtils.success(result));
        assertTrue(XCommandUtils.changedSomething(result));
        field = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId).getField(fieldId);
        assertTrue(field.getValue() != null);

        final XFieldCommand addValueAgainCom = this.comFactory.createSafeAddValueCommand(fieldAddress,
                RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND, value);
        result = this.persistence.executeCommand(this.actorId, addValueAgainCom);
        assertTrue(XCommandUtils.failed(result));

        final XFieldCommand changeValueCom = this.comFactory.createSafeChangeValueCommand(fieldAddress,
                RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND, newValue);
        result = this.persistence.executeCommand(this.actorId, changeValueCom);
        assertTrue(XCommandUtils.success(result));
        assertTrue(XCommandUtils.changedSomething(result));
        assertTrue(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId).getField(fieldId).getValue().equals(newValue));

        // IMPROVE currently not supported: state bound value changing
        // XFieldCommand changeValueButDifferentOldValueCom = this.comFactory
        // .createSafeChangeValueCommand(fieldAddress,
        // RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND, newValue);
        // result = this.persistence.executeCommand(this.actorId,
        // changeValueButDifferentOldValueCom);
        // assertTrue(XCommandUtils.success(result));
        // assert field.getValue().equals(newValue);

        final XFieldCommand removeValueCom = this.comFactory.createSafeRemoveValueCommand(fieldAddress,
                RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND);
        result = this.persistence.executeCommand(this.actorId, removeValueCom);
        assertTrue(XCommandUtils.success(result));
        assertTrue(XCommandUtils.changedSomething(result));
        assertTrue(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId).getField(fieldId).getValue() == null);

        final XFieldCommand removeValueAgainCom = this.comFactory.createSafeRemoveValueCommand(
                fieldAddress, RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND);
        result = this.persistence.executeCommand(this.actorId, removeValueAgainCom);
        assertTrue(XCommandUtils.failed(result));
    }

    /**
     * test revision-bound SAFE commands for all relevant entity change types
     */
    @Test
    public void testExecuteSafeCommandRevisionBound() {

        final XId modelId = Base.toId("model1");
        final XAddress modelAddress = Base.resolveModel(this.repoId, modelId);
        final XId objectId = Base.toId("object1");
        final XAddress objectAddress = Base.resolveObject(this.repoId, modelId, objectId);
        final XId fieldId = Base.toId("fiedl1");
        final XAddress fieldAddress = Base.resolveField(objectAddress, fieldId);
        final XValue value = XV.toValue("value1");
        final XStringValue newValue = XV.toValue("newValue2");

        testRevisionSafeRepositoryCommands(modelId, modelAddress);

        /* ADD model to whom we will add objects */
        final XCommand forcedAddModelCmd = MemoryRepositoryCommand.createAddCommand(this.repoAddress,
                true, modelId);
        long result = this.persistence.executeCommand(this.actorId, forcedAddModelCmd);
        assertTrue(XCommandUtils.success(result));
        assertNotNull(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress)));

        testRevisionSafeModelCommands(modelAddress, objectId, objectAddress);

        /* ADD object to whom we will add fields */
        final XModelCommand forcedAddObjectCmd = MemoryModelCommand.createAddCommand(modelAddress, true,
                objectId);
        result = this.persistence.executeCommand(this.actorId, forcedAddObjectCmd);
        assertTrue(XCommandUtils.success(result));
        assertNotNull(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress)));
        assertTrue(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .hasObject(objectId));

        testRevisionSafeObjectCommands(modelAddress, objectId, objectAddress, fieldId, fieldAddress);

        /* ADD field to whom we will add, change and delete values */
        final XObjectCommand forcedAddFieldCmd = this.comFactory.createForcedAddFieldCommand(
                objectAddress, fieldId);
        result = this.persistence.executeCommand(this.actorId, forcedAddFieldCmd);
        assertTrue(XCommandUtils.success(result));

        testRevisionSafeFieldCommands(modelAddress, objectId, fieldId, fieldAddress, value,
                newValue);

    }

    /**
     * test add and add although differing revision numbers already existing,
     * remove and remove although differing revision numbers as well as change
     * value and change value although differing revision numbers field commands
     */
    private void testRevisionSafeFieldCommands(final XAddress modelAddress, final XId objectId, final XId fieldId,
            final XAddress fieldAddress, final XValue value, final XStringValue newValue) {
        long result;
        // the field to whom we will add and change values
        XWritableField field;

        /*
         * test failing add field command
         */
        final XFieldCommand failingAddValueCom = this.comFactory.createSafeAddValueCommand(fieldAddress,
                999, value);
        result = this.persistence.executeCommand(this.actorId, failingAddValueCom);
        assertTrue(XCommandUtils.failed(result));
        field = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId).getField(fieldId);
        assertNull(field.getValue());

        /*
         * test successful add value command
         */
        final XFieldCommand successfulAddValueCom = this.comFactory.createSafeAddValueCommand(
                fieldAddress, field.getRevisionNumber(), value);
        result = this.persistence.executeCommand(this.actorId, successfulAddValueCom);
        assertTrue(XCommandUtils.success(result));

        /*
         * test failing change value field command
         */
        final XFieldCommand failingChangeValueCom = this.comFactory.createSafeChangeValueCommand(
                fieldAddress, 999, newValue);
        result = this.persistence.executeCommand(this.actorId, failingChangeValueCom);
        assertTrue(XCommandUtils.failed(result));
        field = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId).getField(fieldId);
        assertFalse(field.getValue().equals(newValue));

        /*
         * test successful change value field command
         */
        final XFieldCommand successfulChangeValueCom = this.comFactory.createSafeChangeValueCommand(
                fieldAddress, field.getRevisionNumber(), newValue);
        result = this.persistence.executeCommand(this.actorId, successfulChangeValueCom);
        assertTrue(XCommandUtils.success(result));
        field = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId).getField(fieldId);
        assertEquals(newValue, field.getValue());

        /* test failing remove value command */
        final XFieldCommand failingRemoveValueCom = this.comFactory.createSafeRemoveValueCommand(
                fieldAddress, 999);
        result = this.persistence.executeCommand(this.actorId, failingRemoveValueCom);
        assertTrue(XCommandUtils.failed(result));
        field = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId).getField(fieldId);
        assertNotNull(field.getValue());

        /*
         * test successful remove value command
         */
        final XFieldCommand successfulRemoveValueCom = this.comFactory.createSafeRemoveValueCommand(
                fieldAddress, field.getRevisionNumber());
        result = this.persistence.executeCommand(this.actorId, successfulRemoveValueCom);
        assertTrue(XCommandUtils.success(result));
        assertNull(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId).getField(fieldId).getValue());
    }

    /**
     * test add, add differing revision numbers exists, remove and remove
     * although differing revision numbers field commands
     *
     * @param modelAddress
     * @param objectId
     * @param objectAddress
     * @param fieldId
     * @param fieldAddress
     * @param addObjectCom
     */
    private void testRevisionSafeObjectCommands(final XAddress modelAddress, final XId objectId,
            final XAddress objectAddress, final XId fieldId, final XAddress fieldAddress) {

        long result;
        XWritableObject object;

        object = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId);
        assertNotNull(object);
        assertNull(object.getField(fieldId));

        /*
         * test failing add field command
         */
        // FIXME it should be possible to use a command factory here
        final XObjectCommand failingAddFieldCom = MemoryObjectCommand.createAddCommand(objectAddress,
                999, fieldId);
        result = this.persistence.executeCommand(this.actorId, failingAddFieldCom);
        assertTrue(XCommandUtils.failed(result));
        object = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId);
        assertNotNull(object);
        assertNull(object.getField(fieldId));

        /*
         * test successful add field command
         */
        final XObjectCommand successfulAddFieldCom = MemoryObjectCommand.createAddCommand(objectAddress,
                object.getRevisionNumber(), fieldId);
        result = this.persistence.executeCommand(this.actorId, successfulAddFieldCom);
        assertTrue(XCommandUtils.success(result));
        object = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId);
        assertNotNull(object.getField(fieldId));

        /*
         * test failing remove field command
         */
        final XObjectCommand failingRemoveFieldCom = this.comFactory.createSafeRemoveFieldCommand(
                fieldAddress, 999);
        result = this.persistence.executeCommand(this.actorId, failingRemoveFieldCom);
        assertTrue(XCommandUtils.failed(result));
        object = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId);
        assertFalse(object.isEmpty());

        /*
         * test successful remove field command
         */
        final XObjectCommand successfulRemoveFieldCom = this.comFactory.createSafeRemoveFieldCommand(
                fieldAddress, object.getRevisionNumber());
        result = this.persistence.executeCommand(this.actorId, successfulRemoveFieldCom);
        assertTrue(XCommandUtils.success(result));
        object = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress))
                .getObject(objectId);
        assertTrue(object.isEmpty());
    }

    /**
     * test failing add, add, add although differing revision numbers, failing
     * remove and remove object commands
     *
     * @param modelAddress
     * @param objectId
     * @param objectAddress
     * @param forcedAddModelCmd
     */
    private void testRevisionSafeModelCommands(final XAddress modelAddress, final XId objectId,
            final XAddress objectAddress) {
        long result;
        long currentModelRev;

        XWritableModel model;

        // FIXME it should be possible to use a command factory here
        // XModelCommand addObjectCom =
        // this.comFactory.createSafeAddObjectCommand(modelAddress,
        // objectId);
        /*
         * test failing add object command: wrong revision number
         */
        final XModelCommand failingAddObjectCom = MemoryModelCommand.createAddCommand(modelAddress, 999,
                objectId);
        assertNotNull(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress)));
        result = this.persistence.executeCommand(this.actorId, failingAddObjectCom);
        assertTrue(XCommandUtils.failed(result));
        model = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress));
        assertNull(model.getObject(objectId));

        /*
         * test successful add object commands
         */
        currentModelRev = this.persistence
                .getModelRevision(new GetWithAddressRequest(modelAddress)).revision();
        final XModelCommand addObjectCom = MemoryModelCommand.createAddCommand(modelAddress,
                currentModelRev, objectId);
        /* ADD object */
        result = this.persistence.executeCommand(this.actorId, addObjectCom);
        assertTrue(XCommandUtils.success(result));
        model = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress));
        assertNotNull(model.getObject(objectId));

        /*
         * test failing remove object command
         */
        final XModelCommand failingRemoveObjectCom = this.comFactory.createSafeRemoveObjectCommand(
                objectAddress, 999);
        result = this.persistence.executeCommand(this.actorId, failingRemoveObjectCom);
        assertTrue(XCommandUtils.failed(result));
        model = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress));
        assertFalse(model.isEmpty());

        /*
         * test remove object command
         */
        final XModelCommand successfulRemoveObjectCom = this.comFactory.createSafeRemoveObjectCommand(
                objectAddress, RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND);
        /* REMOVE object */
        result = this.persistence.executeCommand(this.actorId, successfulRemoveObjectCom);
        model = this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress));
        assertTrue(XCommandUtils.success(result));
        assertTrue(model.isEmpty());

    }

    /**
     * test failing add, add, add although differing revision numbers, failing
     * remove, remove and remove although already removed model commands
     *
     * @param modelId
     * @param modelAddress
     */
    private void testRevisionSafeRepositoryCommands(final XId modelId, final XAddress modelAddress) {

        assertNull(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress)));

        // FIXME it should be possible to use a command factory here
        // XCommand addModelCmd=
        // this.comFactory.createSafeAddModelCommand(this.repoId, modelId);
        final XAtomicCommand failingAddModelCmd = MemoryRepositoryCommand.createAddCommand(
                this.repoAddress, 999, modelId);
        assertEquals(Intent.SafeRevBound, failingAddModelCmd.getIntent());

        long result = this.persistence.executeCommand(this.actorId, failingAddModelCmd);
        assertTrue("revBoundSafe ADD command (" + failingAddModelCmd + ") should fail",
                XCommandUtils.failed(result));
        /*
         * one some persistence implementations executing a failed command still
         * 'uses up' a revision number
         */

        final XCommand safeAddModelCmd = MemoryRepositoryCommand.createAddCommand(this.repoAddress,
                RevisionConstants.NOT_EXISTING, modelId);

        result = this.persistence.executeCommand(this.actorId, safeAddModelCmd);
        assertTrue(XCommandUtils.success(result));
        assertTrue(this.persistence.getManagedModelIds().contains(modelId));
        assertNotNull(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress)));

        // XRepositoryCommand addModelAgainCom =
        // this.comFactory.createSafeAddModelCommand(
        // this.repoId, modelId);
        final long currentModelRev = this.persistence.getModelRevision(
                new GetWithAddressRequest(modelAddress)).revision();
        final XCommand addModelAgainCom = MemoryRepositoryCommand.createAddCommand(this.repoAddress,
                currentModelRev, modelId);
        result = this.persistence.executeCommand(this.actorId, addModelAgainCom);
        assertTrue(XCommandUtils.failed(result));

        final XCommand failingRemoveModelCmd = this.comFactory.createSafeRemoveModelCommand(modelAddress,
                10);
        result = this.persistence.executeCommand(this.actorId, failingRemoveModelCmd);
        assertTrue(XCommandUtils.failed(result));
        assertNotNull(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress)) == null);

        final XCommand removeModelCmd = this.comFactory.createSafeRemoveModelCommand(modelAddress,
                currentModelRev);

        result = this.persistence.executeCommand(this.actorId, removeModelCmd);
        assertTrue(XCommandUtils.success(result));
        assertNull(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress)));

        final XCommand removeModelAgain2Com = this.comFactory.createSafeRemoveModelCommand(modelAddress,
                0);

        result = this.persistence.executeCommand(this.actorId, removeModelAgain2Com);
        assertTrue(XCommandUtils.failed(result));
        assertNull(this.persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress)));

    }

}
