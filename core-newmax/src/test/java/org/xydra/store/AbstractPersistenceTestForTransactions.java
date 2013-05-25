package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.XydraPersistence;


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
public abstract class AbstractPersistenceTestForTransactions {
    
    static {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
    }
    
    private static final Logger log = LoggerFactory
            .getLogger(AbstractPersistenceTestForTransactions.class);
    
    public XydraPersistence persistence;
    
    public XCommandFactory comFactory;
    
    public XId repoId = X.getIDProvider().fromString("testRepo");
    public XAddress repoAddress = XX.resolveRepository(this.repoId);
    public XId actorId = X.getIDProvider().fromString("testActor");
    
    /**
     * most tests that deal with transactions built transactions pseudorandomly,
     * so it is recommended to execute them multiple times. This parameter
     * determines how many times these tests will be executed.
     */
    public int nrOfIterationsForTxnTests = 2;
    
    /*
     * TODO check if all types of forced commands work correctly with arbitrary
     * revision numbers (as they should)
     */
    
    @Test
    public void testExecuteTransactionAddObjectWithForcedCmd() {
        testExecuteTransactionAddObject(true);
    }
    
    @Test
    public void testExecuteTransactionAddObjectWithSafeCmd() {
        testExecuteTransactionAddObject(false);
    }
    
    private void testExecuteTransactionAddObject(boolean forced) {
        XId modelId = X.getIDProvider().fromString("executeTransactionAddObject-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString("executeTransactionAddObject-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, false);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(addObjectCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertTrue("Object wasn't added correctly.", revNr > 0);
        
        GetWithAddressRequest addressRequest = new GetWithAddressRequest(XX.resolveObject(
                this.repoId, modelId, objectId));
        XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
        
        assertNotNull("Object does not exist, but the transactions execution reported a success.",
                object);
    }
    
    @Test
    public void testExecuteTransactionAddAlreadyExistingObjectWithForcedCmd() {
        testExecuteTransactionAddAlreadyExistingObject(true);
    }
    
    @Test
    public void testExecuteTransactionAddAlreadyExistingObjectWithSafeCmd() {
        testExecuteTransactionAddAlreadyExistingObject(false);
    }
    
    private void testExecuteTransactionAddAlreadyExistingObject(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionAddAlreadyExistingObject-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionAddAlreadyExistingObject-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, false);
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        
        XCommand addObjectAgainCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, forced);
        txnBuilder.addCommand(addObjectAgainCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        if(forced) {
            assertEquals("Execution should return \"No Change\", since the command was forced.",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertEquals("Execution should return \"Failed\", since the command wasn't forced.",
                    XCommand.FAILED, revNr);
        }
    }
    
    @Test
    public void testExecuteTransactionRemoveExistingObjectWithForcedCmd() {
        testExecuteTransactionRemoveExistingObject(true);
    }
    
    @Test
    public void testExecuteTransactionRemoveExistingObjectWithSafeCmd() {
        testExecuteTransactionRemoveExistingObject(false);
    }
    
    private void testExecuteTransactionRemoveExistingObject(boolean forced) {
        XId modelId = X.getIDProvider().fromString("executeTransactionRemoveExistingObject-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionRemoveNotExistingObject-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, false);
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        
        XCommand removeObjectCommand = this.comFactory.createRemoveObjectCommand(this.repoId,
                modelId, objectId, revNr, forced);
        txnBuilder.addCommand(removeObjectCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertTrue("Object wasn't correclty removed/Transaction failed.", revNr >= 0);
        
        GetWithAddressRequest addressRequest = new GetWithAddressRequest(XX.resolveObject(
                this.repoId, modelId, objectId));
        XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
        
        assertEquals("The persistence should not contain the specified object at this point.",
                null, object);
    }
    
    @Test
    public void testExecuteTransactionRemoveNotExistingObjectWithForcedCmd() {
        testExecuteTransactionRemoveNotExistingObject(true);
    }
    
    @Test
    public void testExecuteTransactionRemoveNotExistingObjectWithSafeCmd() {
        testExecuteTransactionRemoveNotExistingObject(false);
    }
    
    private void testExecuteTransactionRemoveNotExistingObject(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionRemoveNotExistingObject-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionRemoveNotExistingObject-Object");
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        
        XCommand removeObjectCommand = this.comFactory.createRemoveObjectCommand(this.repoId,
                modelId, objectId, revNr, forced);
        txnBuilder.addCommand(removeObjectCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        if(forced) {
            assertEquals("Execution should return \"No Change\", since the command was forced.",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertEquals("Execution should return \"Failed\", since the command wasn't forced.",
                    XCommand.FAILED, revNr);
        }
    }
    
    @Test
    public void testExecuteTransactionAddObjectAndFieldWithForcedCmd() {
        testExecuteTransactionAddObjectAndField(true);
    }
    
    @Test
    public void testExecuteTransactionAddObjectAndFieldWithSafeCmd() {
        testExecuteTransactionAddObjectAndField(false);
    }
    
    private void testExecuteTransactionAddObjectAndField(boolean forced) {
        XId modelId = X.getIDProvider().fromString("executeTransactionAddObjectAndField-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString("executeTransactionAddObjectAndField-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, forced);
        
        XId fieldId = X.getIDProvider().fromString("executeTransactionAddObjectAndField-Field");
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(addObjectCommand);
        txnBuilder.addCommand(addFieldCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertTrue("Transaction wasn't executed correctly.", revNr > 0);
        
        GetWithAddressRequest addressRequest = new GetWithAddressRequest(XX.resolveObject(
                this.repoId, modelId, objectId));
        XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
        
        assertNotNull("Object does not exist, but the transactions execution reported a success.",
                object);
        
        assertTrue("Object does not contain the field the transaction should've added.",
                object.hasField(fieldId));
    }
    
    @Test
    public void testExecuteTransactionAddFieldToExistingObjectWithForcedCmd() {
        testExecuteTransactionAddFieldToExistingObject(true);
    }
    
    @Test
    public void testExecuteTransactionAddFieldToExistingObjectWithSafeCmd() {
        testExecuteTransactionAddFieldToExistingObject(false);
    }
    
    private void testExecuteTransactionAddFieldToExistingObject(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionAddFieldToExistingObject-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionAddFieldToExistingObject-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, false);
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionAddAlreadyExistingFieldToExistingObject-Field");
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, false);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(addFieldCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertTrue("Transaction wasn't executed correctly.", revNr > 0);
        
        GetWithAddressRequest addressRequest = new GetWithAddressRequest(XX.resolveObject(
                this.repoId, modelId, objectId));
        XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
        
        assertNotNull("Object does not exist, but the transactions execution reported a success.",
                object);
        
        assertTrue("Object does not contain the field the transaction should've added.",
                object.hasField(fieldId));
    }
    
    @Test
    public void testExecuteTransactionAddAlreadyExistingFieldToExistingObjectWithForcedCmd() {
        testExecuteTransactionAddAlreadyExistingFieldToExistingObject(true);
    }
    
    @Test
    public void testExecuteTransactionAddAlreadyExistingFieldToExistingObjectWithSafeCmd() {
        testExecuteTransactionAddAlreadyExistingFieldToExistingObject(false);
    }
    
    private void testExecuteTransactionAddAlreadyExistingFieldToExistingObject(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionAddAlreadyExistingFieldToExistingObject-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionAddFieldToExistingObject-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, false);
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionAddFieldToExistingObject-Field");
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, false);
        revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
        
        assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XCommand addFieldAgainCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(addFieldAgainCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        if(forced) {
            assertEquals("Execution should return \"No Change\", since the command was forced.",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertEquals("Execution should return \"Failed\", since the command wasn't forced.",
                    XCommand.FAILED, revNr);
        }
    }
    
    @Test
    public void testExecuteTransactionTryToAddFieldToNotExistingObjectWithForcedCmd() {
        testExecuteTransactionTryToAddFieldToNotExistingObject(true);
    }
    
    @Test
    public void testExecuteTransactionTryToAddFieldToNotExistingObjectWithSafeCmd() {
        testExecuteTransactionTryToAddFieldToNotExistingObject(false);
    }
    
    private void testExecuteTransactionTryToAddFieldToNotExistingObject(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionTryToRemoveFieldFromNotExistingObject-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactioRemoveTryToRemoveFieldFromNotExistingObject-Object");
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionTryToRemoveFieldFromNotExistingObject-Field");
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(addFieldCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertEquals("Execution should return \"Failed\".", XCommand.FAILED, revNr);
        
    }
    
    @Test
    public void testExecuteTransactionRemoveExistingFieldFromExistingObjectWithForcedCmd() {
        testExecuteTransactionRemoveExistingFieldFromExistingObject(true);
    }
    
    @Test
    public void testExecuteTransactionRemoveExistingFieldFromExistingObjectWithSafeCmd() {
        testExecuteTransactionRemoveExistingFieldFromExistingObject(false);
    }
    
    private void testExecuteTransactionRemoveExistingFieldFromExistingObject(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionRemoveExistingFieldFromExistingObject-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactioRemoveExistingFieldFromExistingObject-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, false);
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionRemoveExistingFieldFromExistingObject-Field");
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(addFieldCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertTrue("Transaction wasn't executed correctly.", revNr > 0);
        
        GetWithAddressRequest addressRequest = new GetWithAddressRequest(XX.resolveObject(
                this.repoId, modelId, objectId));
        XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
        
        assertNotNull("Object does not exist, but the transactions execution reported a success.",
                object);
        
        assertTrue("Object does not contain the field the transaction should've added.",
                object.hasField(fieldId));
    }
    
    @Test
    public void testExecuteTransactionRemoveNotExistingFieldFromExistingObjectWithForcedCmd() {
        testExecuteTransactionRemoveNotExistingFieldFromExistingObject(true);
    }
    
    @Test
    public void testExecuteTransactionRemoveNotExistingFieldFromExistingObjectWithSafeCmd() {
        testExecuteTransactionRemoveNotExistingFieldFromExistingObject(false);
    }
    
    private void testExecuteTransactionRemoveNotExistingFieldFromExistingObject(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionRemoveNotExistingFieldFromExistingObject-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactioRemoveNotExistingFieldFromExistingObject-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, false);
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionRemoveNotExistingFieldFromExistingObject-Field");
        XCommand removeFieldCommand = this.comFactory.createRemoveFieldCommand(this.repoId,
                modelId, objectId, fieldId, revNr, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(removeFieldCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        if(forced) {
            assertEquals("Execution should return \"No Change\", since the command was forced.",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertEquals("Execution should return \"Failed\", since the command wasn't forced.",
                    XCommand.FAILED, revNr);
        }
    }
    
    @Test
    public void testExecuteTransactionTryToRemoveFieldFromNotExistingObjectWithForcedCmd() {
        testExecuteTransactionTryToRemoveFieldFromNotExistingObject(true);
    }
    
    @Test
    public void testExecuteTransactionTryToRemoveFieldFromNotExistingObjectWithSafeCmd() {
        testExecuteTransactionTryToRemoveFieldFromNotExistingObject(false);
    }
    
    private void testExecuteTransactionTryToRemoveFieldFromNotExistingObject(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionTryToRemoveFieldFromNotExistingObject-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactioRemoveTryToRemoveFieldFromNotExistingObject-Object");
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionTryToRemoveFieldFromNotExistingObject-Field");
        XCommand removeFieldCommand = this.comFactory.createRemoveFieldCommand(this.repoId,
                modelId, objectId, fieldId, revNr, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(removeFieldCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertEquals("Execution should return \"Failed\".", XCommand.FAILED, revNr);
        
    }
    
    @Test
    public void testExecuteTransactionAddObjectFieldAndValueWithForcedCmd() {
        testExecuteTransactionAddObjectFieldAndValue(true);
    }
    
    @Test
    public void testExecuteTransactionAddObjectFieldAndValueWithSafeCmd() {
        testExecuteTransactionAddObjectFieldAndValue(false);
    }
    
    private void testExecuteTransactionAddObjectFieldAndValue(boolean forced) {
        XId modelId = X.getIDProvider()
                .fromString("executeTransactionAddObjectFieldAndValue-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionAddObjectFieldAndValue-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, forced);
        
        XId fieldId = X.getIDProvider()
                .fromString("executeTransactionAddObjectFieldAndValue-Field");
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, forced);
        
        XValue value = X.getValueFactory().createStringValue("test");
        XCommand addValueCommand = this.comFactory.
        
        createAddValueCommand(XX.resolveField(this.repoId, modelId, objectId, fieldId), revNr,
                value, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(addObjectCommand);
        txnBuilder.addCommand(addFieldCommand);
        txnBuilder.addCommand(addValueCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertTrue("Transaction wasn't executed correctly (" + revNr + ").", revNr > 0);
        
        GetWithAddressRequest addressRequest = new GetWithAddressRequest(XX.resolveObject(
                this.repoId, modelId, objectId));
        XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
        
        assertNotNull("Object does not exist, but the transactions execution reported a success.",
                object);
        
        XReadableField field = object.getField(fieldId);
        assertNotNull("Object does not contain the field the transaction should've added.", field);
        
        assertEquals("Field doesn't have the right value.", value, field.getValue());
    }
    
    @Test
    public void testExecuteTransactionAddValueToExistingFieldWithForcedCmd() {
        testExecuteTransactionAddValueToExistingField(true);
    }
    
    @Test
    public void testExecuteTransactionAddValueToExistingFieldWithSafeCmd() {
        testExecuteTransactionAddValueToExistingField(false);
    }
    
    private void testExecuteTransactionAddValueToExistingField(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionAddValueToExistingField-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionAddValueToExistingField-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionAddValueToExistingField-Field");
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
        
        assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XValue value = X.getValueFactory().createStringValue("test");
        XCommand addValueCommand = this.comFactory.createAddValueCommand(
                XX.resolveField(this.repoId, modelId, objectId, fieldId), revNr, value, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(addValueCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertTrue("Transaction wasn't executed correctly.", revNr > 0);
        
        GetWithAddressRequest addressRequest = new GetWithAddressRequest(XX.resolveObject(
                this.repoId, modelId, objectId));
        XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
        
        assertNotNull("Object does not exist, but the transactions execution reported a success.",
                object);
        
        XReadableField field = object.getField(fieldId);
        assertNotNull("Object does not contain the field the transaction should've added.", field);
        
        assertEquals("Field doesn't have the right value.", value, field.getValue());
    }
    
    @Test
    public void testExecuteTransactionAddValueToNotExistingFieldWithForcedCmd() {
        testExecuteTransactionAddValueToNotExistingField(true);
    }
    
    @Test
    public void testExecuteTransactionAddValueToNotExistingFieldWithSafeCmd() {
        testExecuteTransactionAddValueToExistingField(false);
    }
    
    private void testExecuteTransactionAddValueToNotExistingField(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionAddValueToNotExistingField-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionAddValueToNotExistingField-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionAddValueToNotExistingField-Field");
        
        XValue value = X.getValueFactory().createStringValue("test");
        XCommand addValueCommand = this.comFactory.createAddValueCommand(
                XX.resolveField(this.repoId, modelId, objectId, fieldId), revNr, value, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(addValueCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertEquals("Transaction should've failed, since we never added the needed field.",
                XCommand.FAILED, revNr);
    }
    
    @Test
    public void testExecuteTransactionAddValueToExistingFieldWithValueWithForcedCmd() {
        testExecuteTransactionAddValueToExistingFieldWithValue(true);
    }
    
    @Test
    public void testExecuteTransactionAddValueToExistingFieldWithValueWithSafeCmd() {
        testExecuteTransactionAddValueToExistingFieldWithValue(false);
    }
    
    private void testExecuteTransactionAddValueToExistingFieldWithValue(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionAddValueToExistingFieldWithValue-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionAddValueToExistingFieldWithValue-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionAddValueToExistingFieldWithValue-Field");
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
        
        assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XValue value = X.getValueFactory().createStringValue("test");
        XCommand addValueCommand = this.comFactory.createAddValueCommand(
                XX.resolveField(this.repoId, modelId, objectId, fieldId), revNr, value, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addValueCommand);
        
        assertTrue("Value wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XValue value2 = X.getValueFactory().createStringValue("test2");
        XCommand addValueCommand2 = this.comFactory.createAddValueCommand(
                XX.resolveField(this.repoId, modelId, objectId, fieldId), revNr, value2, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(addValueCommand2);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        if(forced) {
            assertTrue("Transaction wasn't executed correctly.", revNr > 0);
            
            GetWithAddressRequest addressRequest = new GetWithAddressRequest(XX.resolveObject(
                    this.repoId, modelId, objectId));
            XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
            
            assertNotNull(
                    "Object does not exist, but the transactions execution reported a success.",
                    object);
            
            XReadableField field = object.getField(fieldId);
            assertNotNull("Object does not contain the field the transaction should've added.",
                    field);
            
            assertEquals("Field doesn't have the right value.", value2, field.getValue());
        } else {
            assertEquals(
                    "Execution should return \"Failed\", since the command wasn't forced and the value was already set.",
                    XCommand.FAILED, revNr);
        }
    }
    
    @Test
    public void testExecuteTransactionRemoveValueFromExistingFieldWithForcedCmd() {
        testExecuteTransactionRemoveValueFromExistingField(true);
    }
    
    @Test
    public void testExecuteTransactionRemoveValueFromExistingFieldWithSafeCmd() {
        testExecuteTransactionRemoveValueFromExistingField(false);
    }
    
    private void testExecuteTransactionRemoveValueFromExistingField(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionRemoveValueFromExistingField-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionRemoveValueFromExistingField-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionRemoveValueFromExistingField-Field");
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
        
        assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XValue value = X.getValueFactory().createStringValue("test");
        XCommand addValueCommand = this.comFactory.createAddValueCommand(
                XX.resolveField(this.repoId, modelId, objectId, fieldId), revNr, value, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addValueCommand);
        
        assertTrue("Value wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XCommand removeValueCommand = this.comFactory.createRemoveValueCommand(this.repoId,
                modelId, objectId, fieldId, revNr, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(removeValueCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertTrue("Transaction wasn't executed correctly.", revNr > 0);
        
        GetWithAddressRequest addressRequest = new GetWithAddressRequest(XX.resolveObject(
                this.repoId, modelId, objectId));
        XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
        
        assertNotNull("Object does not exist, but the transactions execution reported a success.",
                object);
        
        XReadableField field = object.getField(fieldId);
        assertNotNull("Object does not contain the field the transaction should've added.", field);
        
        assertEquals("Field should have no value.", null, field.getValue());
    }
    
    @Test
    public void testExecuteTransactionRemoveValueFromExistingFieldWithoutValueForcedCmd() {
        testExecuteTransactionRemoveValueFromExistingFieldWithoutValue(true);
    }
    
    @Test
    public void testExecuteTransactionRemoveValueFromExistingFieldWithoutValueSafeCmd() {
        testExecuteTransactionRemoveValueFromExistingFieldWithoutValue(false);
    }
    
    private void testExecuteTransactionRemoveValueFromExistingFieldWithoutValue(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionRemoveValueFromExistingFieldWithoutValue-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionRemoveValueFromExistingFieldWithoutValue-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionRemoveValueFromExistingFieldWithoutValue-Field");
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
        
        assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XCommand removeValueCommand = this.comFactory.createRemoveValueCommand(this.repoId,
                modelId, objectId, fieldId, revNr, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(removeValueCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        if(forced) {
            assertEquals("Execution should return \"No Change\", since the command was forced.",
                    XCommand.NOCHANGE, revNr);
        } else {
            assertEquals("Execution should return \"Failed\", since the command wasn't forced.",
                    XCommand.FAILED, revNr);
        }
    }
    
    @Test
    public void testExecuteTransactionRemoveValueFromNotExistingFieldWithForcedCmd() {
        testExecuteTransactionRemoveValueFromNotExistingField(true);
    }
    
    @Test
    public void testExecuteTransactionRemoveValueFromNotExistingFieldWithSafeCmd() {
        testExecuteTransactionRemoveValueFromNotExistingField(false);
    }
    
    private void testExecuteTransactionRemoveValueFromNotExistingField(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionRemoveValueFromNotExistingField-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionRemoveValueFromNotExistingField-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionRemoveValueFromNotExistingField-Field");
        
        XCommand removeValueCommand = this.comFactory.createRemoveValueCommand(this.repoId,
                modelId, objectId, fieldId, revNr, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(removeValueCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertEquals("Execution should return \"Failed\".", XCommand.FAILED, revNr);
        
    }
    
    @Test
    public void testExecuteTransactionChangeValueOfExistingFieldWithForcedCmd() {
        testExecuteTransactionChangeValueOfExistingField(true);
    }
    
    @Test
    public void testExecuteTransactionChangeValueOfExistingFieldWithSafeCmd() {
        testExecuteTransactionChangeValueOfExistingField(false);
    }
    
    private void testExecuteTransactionChangeValueOfExistingField(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionChangeValueOfExistingField-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionChangeValueOfExistingField-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionChangeValueOfExistingField-Field");
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
        
        assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XValue value1 = X.getValueFactory().createStringValue("test1");
        
        XCommand addValueCommand = this.comFactory.createAddValueCommand(
                XX.resolveField(this.repoId, modelId, objectId, fieldId), revNr, value1, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addValueCommand);
        
        assertTrue("Value wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XValue value2 = X.getValueFactory().createStringValue("test2");
        
        XCommand changeValueCommand = this.comFactory.createChangeValueCommand(
                XX.resolveField(this.repoId, modelId, objectId, fieldId), revNr, value2, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(changeValueCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        assertTrue("Transaction wasn't executed correctly.", revNr > 0);
        
        GetWithAddressRequest addressRequest = new GetWithAddressRequest(XX.resolveObject(
                this.repoId, modelId, objectId));
        XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
        
        assertNotNull("Object does not exist, but the transactions execution reported a success.",
                object);
        
        XReadableField field = object.getField(fieldId);
        assertNotNull("Object does not contain the field the transaction should've added.", field);
        
        assertEquals("Field doesn't have the right value.", value2, field.getValue());
        
    }
    
    @Test
    public void testExecuteTransactionChangeValueOfExistingFieldWithoutValueForcedCmd() {
        testExecuteTransactionChangeValueOfExistingFieldWithoutValue(true);
    }
    
    @Test
    public void testExecuteTransactionChangeValueOfExistingFieldWithoutValueSafeCmd() {
        testExecuteTransactionChangeValueOfExistingFieldWithoutValue(false);
    }
    
    private void testExecuteTransactionChangeValueOfExistingFieldWithoutValue(boolean forced) {
        XId modelId = X.getIDProvider().fromString(
                "executeTransactionChangeValueOfExistingFieldWithoutValue-Model");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
                false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
        
        assertTrue("Model wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "executeTransactionChangeValueOfExistingFieldWithoutValue-Object");
        XCommand addObjectCommand = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addObjectCommand);
        
        assertTrue("Object wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XId fieldId = X.getIDProvider().fromString(
                "executeTransactionChangeValueOfExistingFieldWithoutValue-Field");
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(
                XX.resolveObject(this.repoId, modelId, objectId), fieldId, forced);
        
        revNr = this.persistence.executeCommand(this.actorId, addFieldCommand);
        
        assertTrue("Field wasn't added correctly, test cannot be executed.", revNr >= 0);
        
        XValue value = X.getValueFactory().createStringValue("test");
        
        XCommand changeValueCommand = this.comFactory.createChangeValueCommand(
                XX.resolveField(this.repoId, modelId, objectId, fieldId), revNr, value, forced);
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addCommand(changeValueCommand);
        
        XTransaction txn = txnBuilder.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        
        if(forced) {
            assertTrue("Transaction wasn't executed correctly.", revNr > 0);
            
            GetWithAddressRequest addressRequest = new GetWithAddressRequest(XX.resolveObject(
                    this.repoId, modelId, objectId));
            XReadableObject object = this.persistence.getObjectSnapshot(addressRequest);
            
            assertNotNull(
                    "Object does not exist, but the transactions execution reported a success.",
                    object);
            
            XReadableField field = object.getField(fieldId);
            assertNotNull("Object does not contain the field the transaction should've added.",
                    field);
            
            assertEquals("Field doesn't have the right value.", value, field.getValue());
        } else {
            assertEquals("Execution should return \"Failed\", since the command wasn't forced.",
                    XCommand.FAILED, revNr);
        }
    }
    
    @Test
    public void testExecuteTxnThatAddsAndRemovesFiels() {
        String modelIdString = "testExecuteTxnThatAddsAndRemovesFiels-model";
        
        XId modelId = X.getIDProvider().fromString(modelIdString);
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        XTransactionBuilder builder = new XTransactionBuilder(modelAddress);
        
        /**
         * Transaction XYDRA/testRepo/testModel /-/-:
         * 
         * [
         * 
         * ModelCommand: ADD 'object1' (safe) XYDRA/testRepo/testModel/-/-,
         * 
         * ObjectCommand: ADD 'field1' (safe)
         * XYDRA/testRepo/testModel/object1/-,
         * 
         * FieldCommand: ADD value 'Foo' safe-r0
         * XYDRA/testRepo/testModel/object1/field1,
         * 
         * FieldCommand : REMOVE value '' safe-r0 XYDRA/
         * testRepo/testModel/object1/field1
         * 
         * ]
         */
        XId object1 = XX.toId("object1");
        XId field1 = XX.toId("field1");
        builder.addObject(XX.resolveModel(this.repoId, modelId), XCommand.SAFE_STATE_BOUND, object1);
        
        builder.addField(XX.resolveObject(this.repoId, modelId, object1),
                XCommand.SAFE_STATE_BOUND, field1);
        
        builder.addValue(XX.resolveField(this.repoId, modelId, object1, field1),
                XCommand.SAFE_STATE_BOUND, XV.toValue("Foo"));
        
        builder.removeValue(XX.resolveField(this.repoId, modelId, object1, field1),
                XCommand.SAFE_STATE_BOUND);
        
        XTransaction txn = builder.build();
        
        if(txn != null) {
            XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
                    false);
            // add a model on which an object can be created first
            long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
            
            assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
            
            revNr = this.persistence.executeCommand(this.actorId, txn);
            
            assertTrue("Transaction failed, should've succeeded.", revNr >= 0);
        }
        
    }
    
    @Test
    public void testExecuteTxnThatOnlyAddsAndRemovesFields() {
        String modelIdString = "testExecuteTxnThatAddsAndRemovesOnlyFields-model1";
        
        XId model1 = X.getIDProvider().fromString(modelIdString);
        XId object1 = XX.toId("object1");
        XId field1 = XX.toId("field1");
        XAddress modelAddress = XX.resolveModel(this.repoId, model1);
        
        // add a model on which an object can be created first
        XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, model1, false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
        assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
        
        /**
         * ModelCommand: ADD 'object1' (safe) XYDRA/testRepo/testModel/-/-,
         * 
         * ObjectCommand: ADD 'field1' (safe)
         * XYDRA/testRepo/testModel/object1/-,
         */
        XTransactionBuilder builder1 = new XTransactionBuilder(modelAddress);
        builder1.addObject(XX.resolveModel(this.repoId, model1), XCommand.SAFE_STATE_BOUND, object1);
        builder1.addField(XX.resolveObject(this.repoId, model1, object1),
                XCommand.SAFE_STATE_BOUND, field1);
        XTransaction txn = builder1.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        assertTrue("Transaction failed, should've succeeded.", revNr >= 0);
        
        // real test starts here
        /**
         * FieldCommand: ADD value 'Foo' safe-r0
         * XYDRA/testRepo/testModel/object1/field1,
         * 
         * FieldCommand : REMOVE value '' safe-r0 XYDRA/
         * testRepo/testModel/object1/field1
         */
        XTransactionBuilder builder2 = new XTransactionBuilder(modelAddress);
        builder2.addValue(XX.resolveField(this.repoId, model1, object1, field1),
                XCommand.SAFE_STATE_BOUND, XV.toValue("Foo"));
        builder2.removeValue(XX.resolveField(this.repoId, model1, object1, field1),
                XCommand.SAFE_STATE_BOUND);
        XTransaction txn2 = builder2.build();
        revNr = this.persistence.executeCommand(this.actorId, txn2);
        
        assertTrue("Transaction failed, should've succeeded.", revNr == XCommand.NOCHANGE);
    }
    
    @Test
    public void testExecuteTxnThatRemovesAValueSafely() {
        String modelIdString = "testExecuteTxnThatRemovesAValueSafely-model1";
        
        XId model1 = X.getIDProvider().fromString(modelIdString);
        XId object1 = XX.toId("object1");
        XId field1 = XX.toId("field1");
        XAddress modelAddress = XX.resolveModel(this.repoId, model1);
        
        // add a model on which an object can be created first
        XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, model1, false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
        assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
        
        /**
         * ModelCommand: ADD 'object1' (safe) XYDRA/testRepo/testModel/-/-,
         * 
         * ObjectCommand: ADD 'field1' (safe)
         * XYDRA/testRepo/testModel/object1/-,
         */
        XTransactionBuilder builder1 = new XTransactionBuilder(modelAddress);
        builder1.addObject(XX.resolveModel(this.repoId, model1), XCommand.SAFE_STATE_BOUND, object1);
        builder1.addField(XX.resolveObject(this.repoId, model1, object1),
                XCommand.SAFE_STATE_BOUND, field1);
        builder1.addValue(XX.resolveField(this.repoId, model1, object1, field1),
                XCommand.SAFE_STATE_BOUND, XV.toValue("Foo"));
        XTransaction txn = builder1.build();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        assertTrue("Transaction failed, should've succeeded.", revNr >= 0);
        
        // real test starts here
        /**
         * FieldCommand: ADD value 'Foo' safe-r0
         * XYDRA/testRepo/testModel/object1/field1,
         * 
         * FieldCommand : REMOVE value '' safe-r0 XYDRA/
         * testRepo/testModel/object1/field1
         */
        XTransactionBuilder builder2 = new XTransactionBuilder(modelAddress);
        builder2.removeValue(XX.resolveField(this.repoId, model1, object1, field1),
                XCommand.SAFE_STATE_BOUND);
        XTransaction txn2 = builder2.build();
        revNr = this.persistence.executeCommand(this.actorId, txn2);
        
        assertTrue("Transaction failed, should've succeeded.", revNr >= 0);
    }
    
    private static XValue createRandomValue(Random rand) {
        int type = rand.nextInt(6);
        
        XValue value = null;
        
        switch(type) {
        case 0:
            // random integer value
            value = X.getValueFactory().createIntegerValue(rand.nextInt());
            
            break;
        case 1:
            // random long value
            value = X.getValueFactory().createLongValue(rand.nextLong());
            
            break;
        case 2:
            // random double value
            value = X.getValueFactory().createDoubleValue(rand.nextDouble());
            
            break;
        case 3:
            // random boolean value
            value = X.getValueFactory().createBooleanValue(rand.nextBoolean());
            
            break;
        case 4:
            // random String value
            value = X.getValueFactory().createStringValue("" + rand.nextInt());
            
            break;
        case 5:
            // random XId
            value = XX.toId("XId" + rand.nextInt());
            
            break;
        case 6:
            // random XAddress
            XId repoId = XX.toId("RepoID" + rand.nextInt());
            XId modelId = XX.toId("ModelID" + rand.nextInt());
            XId objectId = XX.toId("ObjectID" + rand.nextInt());
            XId fieldId = XX.toId("FieldID" + rand.nextInt());
            
            value = XX.toAddress(repoId, modelId, objectId, fieldId);
            
            break;
        }
        
        return value;
    }
    
    /**
     * Create Transaction of a simple MOF structure on a ChangedModel and check
     * revision numbers after a subsequent fieldCommand
     * 
     * Check in particular, if after an update of a field field.getOldFieldRev()
     * actually returns the former field rev (currently it seems to return 0).
     * 
     * This is causing an error right now as seen in FieldProperty.setValue,
     * where subsequent forced add cmd are used to set a value opposed to change
     * cmds.
     * 
     * This code is a mess, only commited so Max can test this special case.
     * 
     * @author xamde
     */
    @Test
    public void testExecuteCommandTransaction() {
        // constants
        XId modelId = XX.toId("testExecuteCommandTransaction");
        XId objectId = XX.toId("objectId");
        XId fieldId = XX.toId("fiedlId");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
        XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
        GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
        GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
        XStringValue valueFirst = X.getValueFactory().createStringValue(new String("first"));
        XStringValue valueSecond = X.getValueFactory().createStringValue(new String("second"));
        
        // add a model on which an object can be created first
        long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
        
        XWritableModel model = this.persistence.getModelSnapshot(modelAdrRequest);
        
        XTransactionBuilder txBuilder = new XTransactionBuilder(modelAddress);
        ChangedModel cm = new ChangedModel(model);
        XWritableObject xo = cm.createObject(objectId);
        XWritableField field = xo.createField(fieldId);
        
        field.setValue(valueFirst);
        txBuilder.applyChanges(cm);
        XTransaction txn = txBuilder.build();
        // create object and field with value as tx
        revNr = this.persistence.executeCommand(this.actorId, txn);
        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
        
        /* get object from persistence ; check that the field actually exists */
        XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
        field = object.getField(fieldId);
        
        assertNotNull("The field we tried to create actually wasn't correctly added.", field);
        assertEquals("Returned field did not have the correct revision number.", revNr,
                field.getRevisionNumber());
        
        long fieldRevNrBeforeUpdate = field.getRevisionNumber();
        
        /* overwrite field */
        XFieldCommand fieldChangeValueCommand = MemoryFieldCommand.createAddCommand(
                field.getAddress(), XCommand.FORCED, valueSecond);
        long newRevNr = this.persistence.executeCommand(this.actorId, fieldChangeValueCommand);
        assertTrue("The field value wasn't correctly added, test cannot be executed.",
                newRevNr >= 0);
        
        List<XEvent> events = this.persistence.getEvents(modelAddress, revNr + 1, newRevNr);
        assertEquals("Got more than one event from field cmd", 1, events.size());
        XEvent event = events.get(0);
        assertTrue("event is not a FieldEvent", event instanceof XFieldEvent);
        long oldRevNr = event.getOldFieldRevision();
        
        // System.out.println("========================================");
        // List<XEvent> events1 = this.persistence.getEvents(modelAddress, 0,
        // 1000);
        // for(XEvent e : events1) {
        // System.out.println("========= " + e.getRevisionNumber() + " " + e);
        // if(e instanceof XTransactionEvent) {
        // XTransactionEvent te = (XTransactionEvent)e;
        // for(int i = 0; i < te.size(); i++) {
        // XAtomicEvent sube = te.getEvent(i);
        // System.out.println("========= " + sube.getRevisionNumber() + " --- "
        // + sube);
        // }
        // }
        // }
        
        assertEquals("Old field rev number and rev nr before field cmd did not match",
                fieldRevNrBeforeUpdate, oldRevNr);
    }
    
    @Test
    public void testCompleteEntityTreeTransactionForRightRevisionNumbers() {
        XId modelId = XX.toId("modelId");
        XId objectId = XX.toId("objectId");
        XId fieldId = XX.toId("fiedlId");
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
        XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, true);
        XCommand addObjectCom = this.comFactory
                .createAddObjectCommand(modelAddress, objectId, true);
        XCommand addFieldCom = this.comFactory.createAddFieldCommand(objectAddress, fieldId, true);
        XCommand addValueCom = this.comFactory.createAddValueCommand(
                XX.resolveField(objectAddress, fieldId), -1, XV.toValue(true), true);
        XTransactionBuilder transactionBuilder = new XTransactionBuilder(modelAddress);
        transactionBuilder.addCommand(addModelCom);
        transactionBuilder.addCommand(addObjectCom);
        transactionBuilder.addCommand(addFieldCom);
        transactionBuilder.addCommand(addValueCom);
        XTransaction transaction = transactionBuilder.build();
        
        long result = this.persistence.executeCommand(this.actorId, transaction);
        assertTrue(XCommandUtils.success(result));
        assertTrue(XCommandUtils.changedSomething(result));
        
        XWritableModel modelSnapshot = this.persistence.getModelSnapshot(new GetWithAddressRequest(
                modelAddress));
        System.out.println(modelSnapshot);
    }
    
}
