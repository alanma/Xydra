package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
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
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.index.query.Pair;
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
        
        /* workaround!!! */
        // long executeCommand2 = this.persistence.executeCommand(actorId,
        // addModelCom);
        
        transactionBuilder.addCommand(addObjectCom);
        transactionBuilder.addCommand(addFieldCom);
        transactionBuilder.addCommand(addValueCom);
        XTransaction transaction = transactionBuilder.build();
        
        long result = this.persistence.executeCommand(this.actorId, transaction);
        assertTrue(XCommandUtils.success(result));
        assertTrue(XCommandUtils.changedSomething(result));
        
        XWritableModel modelSnapshot = this.persistence.getModelSnapshot(new GetWithAddressRequest(
                modelAddress));
        
        long modelRevision = modelSnapshot.getRevisionNumber();
        assertEquals(0, modelRevision);
        XWritableObject object = modelSnapshot.getObject(objectId);
        assertNotNull(object);
        long objectRevision = object.getRevisionNumber();
        assertEquals(0, objectRevision);
        XWritableField field = object.getField(fieldId);
        long fieldRevision = field.getRevisionNumber();
        assertEquals(0, fieldRevision);
    }
    
    /*
     * TODO check if all types of forced commands work correctly with arbitrary
     * revision numbers (as they should)
     */
    
    /**
     * This test pseudorandomly creates model transactions which execution is
     * supposed to succeed. It uses {@link java.util.Random} to create random
     * transactions. We set the seed manually and always print it on the screen.
     * 
     * If the test fails, simply copy the seed which created the transaction
     * which was the cause of the failure and set the seed to this value
     * (instead of using random seeds, as the test normally does). This makes
     * the test deterministic and enables debugging.
     */
    
    @Test
    public void testExecuteCommandSucceedingModelTransaction() {
        SecureRandom seedGen = new SecureRandom();
        
        for(int i = 0; i <= this.nrOfIterationsForTxnTests; i++) {
            /*
             * Info: if the test fails, do the following to enable deterministic
             * debugging: Set the seed to the value which caused the test to
             * fail. This makes the test deterministic .
             */
            long seed = seedGen.nextLong();
            testExecuteCommandSucceedingModelTransaction_withSeed(i, seed, 10, 10);
        }
    }
    
    /**
     * Pseudorandomly manipulates the fields of the given object (removes and
     * changes values of fields which already have a value or remove fields)
     * using the given pseudorandom generator and adds the appropriate commands
     * to the given transaction builder.
     */
    private void randomlyChangeFields(Random rand, XWritableObject changedObject,
            XTransactionBuilder txnBuilder) {
        XAddress objectAddress = changedObject.getAddress();
        
        // randomly determine if some of the fields should be removed
        List<XId> toBeRemovedFields = new LinkedList<XId>();
        for(XId fieldId : changedObject) {
            boolean removeField = rand.nextBoolean();
            XAddress fieldAddress = XX.resolveField(objectAddress, fieldId);
            XWritableField field = changedObject.getField(fieldId);
            
            if(removeField) {
                toBeRemovedFields.add(fieldId);
                XCommand removeFieldCommand = this.comFactory.createRemoveFieldCommand(
                        fieldAddress, XCommand.SAFE_STATE_BOUND, false);
                
                txnBuilder.addCommand(removeFieldCommand);
                
            } else {
                // randomly determine if its value should be changed/removed
                XValue currentValue = field.getValue();
                
                if(currentValue != null) {
                    boolean removeValue = rand.nextBoolean();
                    if(removeValue) {
                        field.setValue(null);
                        
                        assertEquals(null, field.getValue());
                        
                        XCommand removeValueCommand = this.comFactory.createRemoveValueCommand(
                                fieldAddress, XCommand.SAFE_STATE_BOUND, false);
                        
                        txnBuilder.addCommand(removeValueCommand);
                        
                    } else {
                        boolean changeValue = rand.nextBoolean();
                        
                        if(changeValue) {
                            XValue newValue = null;
                            do {
                                newValue = createRandomValue(rand);
                            } while(newValue.equals(currentValue));
                            
                            field.setValue(newValue);
                            
                            assertEquals(newValue, field.getValue());
                            
                            XCommand changeValueCommand = this.comFactory.createChangeValueCommand(
                                    fieldAddress, XCommand.SAFE_STATE_BOUND, newValue, false);
                            
                            txnBuilder.addCommand(changeValueCommand);
                        }
                    }
                }
            }
        }
        
        /*
         * we need to remove the fields in a separate loop because modifying the
         * set of fields of the changedObject while we iterate over it results
         * in ConcurrentModificationExceptions
         */
        for(XId fieldId : toBeRemovedFields) {
            changedObject.removeField(fieldId);
            
            assertFalse(changedObject.hasField(fieldId));
        }
    }
    
    private void testExecuteCommandSucceedingModelTransaction_withSeed(int i, long seed,
            int maxNrOfObjects, int maxNrOfFields) {
        XId modelId = X.getIDProvider().fromString(
                "testExecuteCommandSucceedingModelTransactionModel" + i);
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
        XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
        // add a model on which an object can be created first
        long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
        
        assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
        
        XWritableModel modelSnapshot = this.persistence.getModelSnapshot(modelAdrRequest);
        
        log.info("Creating transaction " + i + " with seed " + seed + ".");
        Pair<ChangedModel,XTransaction> pair = createRandomSucceedingModelTransaction(
                modelSnapshot, seed, maxNrOfObjects, maxNrOfFields);
        ChangedModel changedModel = pair.getFirst();
        XTransaction txn = pair.getSecond();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        assertTrue(
                "Transaction failed, should succeed, seed was: "
                        + seed
                        + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                revNr >= 0);
        
        modelSnapshot = this.persistence.getModelSnapshot(modelAdrRequest);
        
        int nrOfObjectsInModelSnapshot = 0;
        int nrOfObjectsInChangedModel = 0;
        for(@SuppressWarnings("unused")
        XId objectId : changedModel) {
            nrOfObjectsInChangedModel++;
        }
        
        for(@SuppressWarnings("unused")
        XId objectId : modelSnapshot) {
            nrOfObjectsInModelSnapshot++;
        }
        
        assertEquals(
                "The transaction wasn't correctly executed, the stored model does not store the correct amount of objects it should be storing after execution of the transaction, seed was "
                        + seed
                        + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                nrOfObjectsInChangedModel, nrOfObjectsInModelSnapshot);
        
        for(XId objectId : changedModel) {
            assertTrue(
                    "The stored model does not contain an object it should contain after the transaction was executed, seed was "
                            + seed
                            + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                    modelSnapshot.hasObject(objectId));
            
            XReadableObject changedObject = changedModel.getObject(objectId);
            XReadableObject objectSnapshot = modelSnapshot.getObject(objectId);
            
            int nrOfFieldsInObjectSnapshot = 0;
            int nrOfFieldsInChangedObject = 0;
            for(@SuppressWarnings("unused")
            XId id : changedObject) {
                nrOfFieldsInChangedObject++;
            }
            
            for(@SuppressWarnings("unused")
            XId id : objectSnapshot) {
                nrOfFieldsInObjectSnapshot++;
            }
            
            assertEquals(
                    "The transaction wasn't correctly executed, one of the stored objects does not store the correct amount of fields it should be storing after execution of the transaction, seed was "
                            + seed
                            + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                    nrOfFieldsInChangedObject, nrOfFieldsInObjectSnapshot);
            
            for(XId fieldId : changedObject) {
                assertTrue(
                        "One of the stored objects does not contain a field it should contain after the transaction was executed, seed was "
                                + seed
                                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                        objectSnapshot.hasField(fieldId));
                
                XReadableField changedField = changedObject.getField(fieldId);
                XReadableField fieldSnapshot = objectSnapshot.getField(fieldId);
                
                assertEquals(
                        "One of the stored fields does not contain the value it should contain after the transaction was executed, seed was "
                                + seed
                                + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                        changedField.getValue(), fieldSnapshot.getValue());
            }
        }
    }
    
    private void testExecuteCommandSucceedingModelTransactionFindFailingSubTxn(int maxNrOfObjects,
            int maxNrOfFields, long seed) {
        
        String modelIdString = "testExecuteCommandSucceedingModelTransactionFindFailingSubTxn-Model_seed"
                + seed;
        
        XTransaction txn = findFailingSubtransactionInSucceedingModelTransaction(modelIdString,
                seed, maxNrOfObjects, maxNrOfFields);
        
        if(txn != null) {
            XId modelId = X.getIDProvider().fromString(modelIdString);
            
            XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
                    false);
            // add a model on which an object can be created first
            long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
            
            assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
            
            revNr = this.persistence.executeCommand(this.actorId, txn);
            
            assertTrue("Transaction failed (" + revNr + "), should've succeeded.", revNr >= 0);
        }
    }
    
    @Test
    public void testExecuteCommandSucceedingModelTransactionFindFailingSubTxn_seed2634857159770016178() {
        /*
         * TODO setting the "maxNrOfObjectsParameter" to 1 also causes the test
         * to fail
         */
        
        int maxNrOfObjects = 1;
        int maxNrOfFields = 3;
        
        long seed = 2634857159770016178l;
        
        testExecuteCommandSucceedingModelTransactionFindFailingSubTxn(maxNrOfObjects,
                maxNrOfFields, seed);
    }
    
    @Test
    public void testExecuteCommandSucceedingModelTransactionFindFailingSubTxn_seed2758510421983848470() {
        int maxNrOfObjects = 10;
        int maxNrOfFields = 10;
        
        long seed = 2758510421983848470l;
        
        testExecuteCommandSucceedingModelTransactionFindFailingSubTxn(maxNrOfObjects,
                maxNrOfFields, seed);
    }
    
    @Test
    public void testExecuteCommandSucceedingModelTransactionFindFailingSubTxn_seedMinus3557338955757355024() {
        int maxNrOfObjects = 10;
        int maxNrOfFields = 10;
        
        long seed = -7411071230466049407l;
        
        testExecuteCommandSucceedingModelTransactionFindFailingSubTxn(maxNrOfObjects,
                maxNrOfFields, seed);
    }
    
    @Test
    public void testExecuteCommandSucceedingModelTransactionFindFailingSubTxn_seedMinus7411071230466049407() {
        int maxNrOfObjects = 10;
        int maxNrOfFields = 10;
        
        long seed = -7411071230466049407l;
        
        testExecuteCommandSucceedingModelTransactionFindFailingSubTxn(maxNrOfObjects,
                maxNrOfFields, seed);
    }
    
    /**
     * This test pseudorandomly creates object transactions which execution is
     * supposed to succeed. It uses {@link java.util.Random} to create random
     * transactions. We set the seed manually and always print it on the screen.
     * 
     * If the test fails, simply copy the seed which created the transaction
     * which was the cause of the failure and set the seed to this value
     * (instead of using random seeds, as the test normally does). This makes
     * the test deterministic and enables debugging.
     */
    @Test
    public void testExecuteCommandSucceedingObjectTransaction() {
        SecureRandom seedGen = new SecureRandom();
        
        for(int i = 0; i <= this.nrOfIterationsForTxnTests; i++) {
            /*
             * Info: if the test fails, do the following to enable deterministic
             * debugging: Set the seed to the value which caused the test to
             * fail. This makes the test deterministic .
             */
            long seed = seedGen.nextLong();
            testExecuteCommandSucceedingObjectTransaction_withSeed(i, seed, 10);
        }
    }
    
    private void testExecuteCommandSucceedingObjectTransaction_withSeed(int i, long seed,
            int maxNrOfFields) {
        
        XId modelId = X.getIDProvider().fromString(
                "testExecuteCommandSucceedingObjectTransactionModel" + i);
        
        XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
        // add a model on which an object can be created first
        long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
        
        assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
        
        XId objectId = X.getIDProvider().fromString(
                "testExecuteCommandSucceedingObjectTransactionObject" + i);
        XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
        
        GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
        XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, false);
        // add a model on which an object can be created first
        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
        
        assertTrue("Object could not be added, test cannot be executed.", revNr >= 0);
        
        XWritableObject objectSnapshot = this.persistence.getObjectSnapshot(objectAdrRequest);
        
        /*
         * Info: if the test fails, do the following to enable deterministic
         * debugging: Set the seed to the value which caused the test to fail.
         * This makes the test deterministic .
         */
        log.info("Creating transaction " + i + " with seed " + seed + ".");
        Pair<ChangedObject,XTransaction> pair = createRandomSucceedingObjectTransaction(
                objectSnapshot, seed, maxNrOfFields);
        ChangedObject changedObject = pair.getFirst();
        XTransaction txn = pair.getSecond();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        assertTrue(
                "Transaction failed, should succeed, seed was: "
                        + seed
                        + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                revNr >= 0);
        
        objectSnapshot = this.persistence.getObjectSnapshot(objectAdrRequest);
        
        int nrOfFieldsInObjectSnapshot = 0;
        int nrOfFieldsInChangedObject = 0;
        for(@SuppressWarnings("unused")
        XId id : changedObject) {
            nrOfFieldsInChangedObject++;
        }
        
        for(@SuppressWarnings("unused")
        XId id : objectSnapshot) {
            nrOfFieldsInObjectSnapshot++;
        }
        
        assertEquals(
                "The transaction wasn't correctly executed, the stored objects does not store the correct amount of fields it should be storing after execution of the transaction, seed was "
                        + seed
                        + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                nrOfFieldsInChangedObject, nrOfFieldsInObjectSnapshot);
        
        for(XId fieldId : changedObject) {
            assertTrue(
                    "The stored object does not contain a field it should contain after the transaction was executed, seed was "
                            + seed
                            + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                    objectSnapshot.hasField(fieldId));
            
            XReadableField changedField = changedObject.getField(fieldId);
            XReadableField fieldSnapshot = objectSnapshot.getField(fieldId);
            
            assertEquals(
                    "One of the stored fields does not contain the value it should contain after the transaction was executed, seed was "
                            + seed
                            + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                    changedField.getValue(), fieldSnapshot.getValue());
        }
        
    }
    
    private void testExecuteCommandSucceedingObjectTransactionFindFailingSubTxn(int maxNrOfFields,
            long seed) {
        
        String modelIdString = "FindFailingSubTxn-Model_seed" + seed;
        String objectIdString = "FindFailingSubTxn-Object_seed" + seed;
        
        XTransaction txn = findFailingSubtransactionInSucceedingObjectTransaction(modelIdString,
                objectIdString, seed, maxNrOfFields);
        
        if(txn != null) {
            XId modelId = X.getIDProvider().fromString(modelIdString);
            XId objectId = X.getIDProvider().fromString(objectIdString);
            
            XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId,
                    false);
            // add a model on which an object can be created first
            long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
            
            assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
            
            XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                    XX.resolveModel(this.repoId, modelId), objectId, false);
            // add an object on which the txn can be executed
            revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
            
            assertTrue("Object could not be added, test cannot be executed.", revNr >= 0);
            
            revNr = this.persistence.executeCommand(this.actorId, txn);
            
            assertTrue("Transaction failed, should've succeeded.", revNr >= 0);
        }
    }
    
    @Test
    public void testExecuteCommandSucceedingObjectTransactionFindFailingSubTxn_seed2745963502525881193() {
        int maxNrOfFields = 10;
        
        long seed = 2745963502525881193l;
        
        testExecuteCommandSucceedingObjectTransactionFindFailingSubTxn(maxNrOfFields, seed);
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
    
    private boolean constructFieldCommandForFaultyTransaction(XAddress failFieldAddress,
            XAddress succFieldAddress, XTransactionBuilder failTxnBuilder,
            XTransactionBuilder succTxnBuilder, boolean failBecauseOfFaultyFieldCommand,
            long fieldRevNr, Random rand) {
        
        if(failBecauseOfFaultyFieldCommand) {
            int reason = rand.nextInt(3);
            
            if(reason == 0) {
                // fail because we try to remove a not existing
                // value
                XCommand failRemoveCom = this.comFactory.createRemoveValueCommand(failFieldAddress,
                        fieldRevNr, true);
                
                failTxnBuilder.addCommand(failRemoveCom);
                
                System.out
                        .println("Transaction will fail because of a faulty FieldCommand of remove type.");
            } else if(reason == 1) {
                // fail because we try to add a value to a field
                // which value is already set
                
                XValue value1 = createRandomValue(rand);
                XValue value2 = createRandomValue(rand);
                XCommand failAddCom1 = this.comFactory.createAddValueCommand(failFieldAddress,
                        fieldRevNr, value1, false);
                XCommand failAddCom2 = this.comFactory.createAddValueCommand(failFieldAddress,
                        fieldRevNr, value2, false);
                
                failTxnBuilder.addCommand(failAddCom1);
                failTxnBuilder.addCommand(failAddCom2);
                
                System.out
                        .println("Transaction will fail because of a faulty FieldCommand of add type.");
            } else {
                assert reason == 2;
                // fail because we try to change the value of a
                // field which value isn't set
                XValue value = createRandomValue(rand);
                XCommand failChangeCom = this.comFactory.createChangeValueCommand(failFieldAddress,
                        fieldRevNr, value, false);
                
                failTxnBuilder.addCommand(failChangeCom);
                
                System.out
                        .println("Transaction will fail because of a faulty FieldCommand of change type.");
            }
            
            /*
             * faulty command was added, stop construction
             */
            return true;
            
        } else {
            XValue value = X.getValueFactory().createStringValue("" + rand.nextInt());
            
            XCommand failAddValueCom = this.comFactory.createAddValueCommand(failFieldAddress,
                    fieldRevNr, value, true);
            XCommand succAddValueCom = this.comFactory.createAddValueCommand(succFieldAddress,
                    fieldRevNr, value, true);
            
            failTxnBuilder.addCommand(failAddValueCom);
            succTxnBuilder.addCommand(succAddValueCom);
            
            return false;
        }
    }
    
    /**
     * Pseudorandomly generates a transaction which should fail on the given
     * empty model (failModel). Also creates a transaction which does not
     * contain the event which makes the transaction fail (i.e. this transaction
     * is supposed to succeed) for the given empty model succModel.
     * 
     * The seed determines how the random number generator generates its
     * pseudorandom output. Using the same seed twice will result in
     * deterministically generating the same transaction again, which can be
     * useful when executing a transaction in a test fails and the failed
     * transaction needs to be reconstructed.
     */
    private Pair<XTransaction,XTransaction> createRandomFailingModelTransaction(
            XWritableModel failModel, XWritableModel succModel, long seed) {
        assertTrue("This test only works with empty models.", failModel.isEmpty());
        assertTrue("This test only works with empty models.", succModel.isEmpty());
        
        Random rand = new Random(seed);
        
        /*
         * we create two transactions that basically do the same, but one of
         * them does not contain the command which is supposed to let the
         * execution of the transaction fail. By executing both transactions on
         * different models (which are in the same state), we'll be able to
         * ensure that the failing transaction actually fails because of the
         * specific command we added to let it fail and not because of another
         * event which execution should succeed.
         */
        XTransactionBuilder failTxnBuilder = new XTransactionBuilder(failModel.getAddress());
        XTransactionBuilder succTxnBuilder = new XTransactionBuilder(succModel.getAddress());
        
        // create random amount of objects
        int nrOfObjects = 0;
        
        nrOfObjects = 2 + rand.nextInt(10);
        
        /*
         * determines whether the constructed transaction's execution should
         * fail because of a command which tries to add or remove an object, but
         * fails.
         */
        boolean failBecauseOfAddOrRemoveObjectCommand = rand.nextBoolean();
        int faultyAddOrRemoveObjectCommand = -1;
        
        if(failBecauseOfAddOrRemoveObjectCommand) {
            
            faultyAddOrRemoveObjectCommand = 1 + rand.nextInt(nrOfObjects - 1);
            
            /*
             * add at least one succeeding command so that succTxnBuilder does
             * not throw an exception because of an empty command list. The
             * range is from 0 to nrOfOBjects - 1 because of this too (this
             * ensures that the random value is between 1 and nrOfObjects-1, if
             * we wouldn't do this, the variable might be set to nrOfObjects,
             * which obviously can never be reached, because i only iterates
             * from 0 to nrOfObject-1).
             */
        }
        
        /*
         * begin adding commands until the command which should let the
         * execution fail is added.
         */
        boolean faultyCommandAdded = false;
        
        for(int i = 0; i < nrOfObjects && !faultyCommandAdded; i++) {
            XId objectId = X.getIDProvider().fromString("object" + i);
            XAddress failObjectAddress = XX.resolveObject(failModel.getAddress(), objectId);
            XAddress succObjectAddress = XX.resolveObject(succModel.getAddress(), objectId);
            
            if(i == faultyAddOrRemoveObjectCommand) {
                /*
                 * The transaction's execution is supposed to fail because of a
                 * ModelCommand. The next random boolean determines whether it
                 * will fail because we'll try to add an already existing object
                 * or because we'll try to remove a not existing object.
                 */
                boolean failBecauseOfFalseRemove = rand.nextBoolean();
                
                if(failBecauseOfFalseRemove) {
                    // fail because we try to remove a not existing object
                    XCommand removeCom = this.comFactory.createRemoveObjectCommand(
                            failObjectAddress, failModel.getRevisionNumber(), false);
                    
                    failTxnBuilder.addCommand(removeCom);
                    System.out
                            .println("Transaction will fail because of a faulty ModelCommand of remove type.");
                    
                } else {
                    // fail because we try to add an already existing object
                    XCommand addCom = this.comFactory.createAddObjectCommand(
                            failModel.getAddress(), objectId, false);
                    
                    // we need to add it twice for this use-case
                    failTxnBuilder.addCommand(addCom);
                    failTxnBuilder.addCommand(addCom);
                    
                    System.out
                            .println("Transaction will fail because of a faulty ModelCommand of add type.");
                }
                
                /*
                 * faulty command was added, stop adding more elements
                 */
                faultyCommandAdded = true;
                
            } else {
                XCommand failAddCom = this.comFactory.createAddObjectCommand(
                        failModel.getAddress(), objectId, false);
                
                XCommand succAddCom = this.comFactory.createAddObjectCommand(
                        succModel.getAddress(), objectId, false);
                
                failTxnBuilder.addCommand(failAddCom);
                succTxnBuilder.addCommand(succAddCom);
            }
            
            // add fields
            
            int nrOfFields = 0;
            
            nrOfFields = 1 + rand.nextInt(10);
            // add at least one field
            
            /*
             * if the transaction isn't supposed to fail because of a faulty
             * ModelCommand, randomly determine whether it should fail because
             * of a faulty ObjectCommand.
             */
            boolean failBecauseOfAddOrRemoveFieldCommand = false;
            int faultyAddOrRemoveFieldCommand = -1;
            if(!failBecauseOfAddOrRemoveObjectCommand) {
                failBecauseOfAddOrRemoveFieldCommand = rand.nextBoolean();
                
                if(!failBecauseOfAddOrRemoveFieldCommand && i + 1 == nrOfObjects) {
                    /*
                     * if the transaction wasn't supposed to fail because of a
                     * ModelCommand and the current object is the last, make
                     * sure that the transaction will actually fail by enforcing
                     * that it will fail because of an ObjectCommand.
                     */
                    failBecauseOfAddOrRemoveFieldCommand = true;
                }
                
                if(failBecauseOfAddOrRemoveFieldCommand) {
                    faultyAddOrRemoveFieldCommand = rand.nextInt(nrOfFields);
                }
            }
            
            for(int j = 0; j < nrOfFields && !faultyCommandAdded; j++) {
                XId fieldId = X.getIDProvider().fromString("field" + j);
                
                if(j == faultyAddOrRemoveFieldCommand) {
                    /*
                     * The transaction's execution is supposed to fail because
                     * of an ObjectCommand. The next random boolean determines
                     * whether it will fail because we'll try to add an already
                     * existing field or because we'll try to remove a not
                     * existing field.
                     */
                    
                    boolean failBecauseOfFalseRemove = rand.nextBoolean();
                    
                    if(failBecauseOfFalseRemove) {
                        // fail because we try to remove a not existing field
                        XAddress fieldAddress = XX.resolveField(failModel.getAddress(), objectId,
                                fieldId);
                        
                        XCommand removeCom = this.comFactory.createRemoveFieldCommand(fieldAddress,
                                failModel.getRevisionNumber(), false);
                        
                        failTxnBuilder.addCommand(removeCom);
                        System.out
                                .println("Transaction will fail because of a faulty ObjectCommand of remove type.");
                        
                    } else {
                        // fail because we try to add an already existing field
                        XCommand addCom = this.comFactory.createAddFieldCommand(failObjectAddress,
                                fieldId, false);
                        
                        // we need to add it twice for this use-case
                        failTxnBuilder.addCommand(addCom);
                        failTxnBuilder.addCommand(addCom);
                        System.out
                                .println("Transaction will fail because of a faulty ObjectCommand of add type.");
                    }
                    
                    /*
                     * faulty command was added, stop the construction of the
                     * transaction
                     */
                    faultyCommandAdded = true;
                    
                } else {
                    XCommand failAddCom = this.comFactory.createAddFieldCommand(failObjectAddress,
                            fieldId, false);
                    
                    XCommand succAddCom = this.comFactory.createAddFieldCommand(succObjectAddress,
                            fieldId, false);
                    
                    failTxnBuilder.addCommand(failAddCom);
                    succTxnBuilder.addCommand(succAddCom);
                    
                    /*
                     * randomly decide whether the field has a value or not
                     */
                    boolean addValue = rand.nextBoolean();
                    if(addValue) {
                        /*
                         * the field didn't exist before the transaction, so its
                         * revision number is non-existant.
                         */
                        long fieldRevNr = XCommand.SAFE_STATE_BOUND;
                        
                        boolean failBecauseOfFaultyFieldCommand = false;
                        if(!failBecauseOfAddOrRemoveFieldCommand
                                && !failBecauseOfAddOrRemoveObjectCommand) {
                            failBecauseOfFaultyFieldCommand = rand.nextBoolean();
                        }
                        
                        XAddress failFieldAddress = XX.resolveField(failModel.getAddress(),
                                objectId, fieldId);
                        XAddress succFieldAddress = XX.resolveField(succModel.getAddress(),
                                objectId, fieldId);
                        
                        boolean temp = constructFieldCommandForFaultyTransaction(failFieldAddress,
                                succFieldAddress, failTxnBuilder, succTxnBuilder,
                                failBecauseOfFaultyFieldCommand, fieldRevNr, rand);
                        
                        if(temp) {
                            /*
                             * we're using a temporary variable here because
                             * faultyCommandAdded might already be set to true
                             * and the method might return false, thereby
                             * wrongly setting the faultyCommandAdd to false if
                             * we'd just assign the return value of the method
                             * to faultyCommandAdded.
                             */
                            
                            faultyCommandAdded = true;
                        }
                        
                    }
                }
            }
        }
        
        /*
         * TODO add some commands after the failed command to check that this is
         * no problem
         */
        
        XTransaction failTxn = failTxnBuilder.build();
        XTransaction succTxn = succTxnBuilder.build();
        
        assertTrue(
                "There seems to be a bug in the code of the test, since no faulty command was added to the transaction which' execution is supposed to fail, which results in a transaction which' execution would succeed.",
                faultyCommandAdded);
        Pair<XTransaction,XTransaction> pair = new Pair<XTransaction,XTransaction>(failTxn, succTxn);
        return pair;
        
    }
    
    /**
     * Pseudorandomly generates a transaction which should fail on the given
     * empty object (failObject). Also creates a transaction which does not
     * contain the event which makes the transaction fail (i.e. this transaction
     * is supposed to succeed) for the given empty object (succObject).
     * 
     * The seed determines how the random number generator generates its
     * pseudorandom output. Using the same seed twice will result in
     * deterministically generating the same transaction again, which can be
     * useful when executing a transaction in a test fails and the failed
     * transaction needs to be reconstructed.
     */
    private Pair<XTransaction,XTransaction> createRandomFailingObjectTransaction(
            XWritableObject failObject, XWritableObject succObject, long seed) {
        assertTrue("This test only works with empty objects.", failObject.isEmpty());
        assertTrue("This test only works with empty objects", succObject.isEmpty());
        
        Random rand = new Random(seed);
        
        /*
         * we create two transactions that basically do the same, but one of
         * them does not contain the command which is supposed to let the
         * execution of the transaction fail. By executing both transactions on
         * different models (which are in the same state), we'll be able to
         * ensure that the failing transaction actually fails because of the
         * specific command we added to let it fail and not because of another
         * event which execution should succeed.
         */
        XTransactionBuilder failTxnBuilder = new XTransactionBuilder(failObject.getAddress());
        XTransactionBuilder succTxnBuilder = new XTransactionBuilder(succObject.getAddress());
        
        /*
         * begin adding commands until the command which should let the
         * execution fail is added.
         */
        boolean faultyCommandAdded = false;
        
        // add fields
        
        int nrOfFields = 0;
        
        nrOfFields = 2 + rand.nextInt(10);
        // add at least two fields
        
        /*
         * randomly determine whether the transaction should fail because of a
         * faulty ObjectCommand.
         */
        boolean failBecauseOfAddOrRemoveFieldCommand = false;
        int faultyAddOrRemoveFieldCommand = -1;
        
        failBecauseOfAddOrRemoveFieldCommand = rand.nextBoolean();
        
        if(failBecauseOfAddOrRemoveFieldCommand) {
            faultyAddOrRemoveFieldCommand = 1 + rand.nextInt(nrOfFields - 1);
        }
        
        for(int j = 0; j < nrOfFields && !faultyCommandAdded; j++) {
            XId fieldId = X.getIDProvider().fromString("field" + j);
            
            if(j == faultyAddOrRemoveFieldCommand) {
                /*
                 * The transaction's execution is supposed to fail because of an
                 * ObjectCommand. The next random boolean determines whether it
                 * will fail because we'll try to add an already existing field
                 * or because we'll try to remove a not existing field.
                 */
                
                boolean failBecauseOfFalseRemove = rand.nextBoolean();
                
                if(failBecauseOfFalseRemove) {
                    // fail because we try to remove a not existing field
                    XAddress fieldAddress = XX.resolveField(failObject.getAddress(), fieldId);
                    
                    XCommand removeCom = this.comFactory.createRemoveFieldCommand(fieldAddress,
                            XCommand.SAFE_STATE_BOUND
                            // failObject.getRevisionNumber()
                            , false);
                    
                    failTxnBuilder.addCommand(removeCom);
                    System.out
                            .println("Transaction will fail because of a faulty ObjectCommand of remove type.");
                    
                } else {
                    // fail because we try to add an already existing field
                    XCommand addCom = this.comFactory.createAddFieldCommand(
                            failObject.getAddress(), fieldId, false);
                    
                    // we need to add it twice for this use-case
                    failTxnBuilder.addCommand(addCom);
                    failTxnBuilder.addCommand(addCom);
                    System.out
                            .println("Transaction will fail because of a faulty ObjectCommand of add type.");
                }
                
                /*
                 * faulty command was added, stop the construction of the
                 * transaction
                 */
                faultyCommandAdded = true;
                
            } else {
                XCommand failAddCom = this.comFactory.createAddFieldCommand(
                        failObject.getAddress(), fieldId, true);
                
                XCommand succAddCom = this.comFactory.createAddFieldCommand(
                        succObject.getAddress(), fieldId, true);
                
                failTxnBuilder.addCommand(failAddCom);
                succTxnBuilder.addCommand(succAddCom);
                
                /*
                 * randomly decide whether the field has a value or not
                 */
                
                boolean addValue = rand.nextBoolean();
                if(!faultyCommandAdded && j + 1 == nrOfFields) {
                    /*
                     * assures that a faulty field command will be added if no
                     * faulty command was added up till now and this is the last
                     * iteration through the loop which creates commands.
                     * 
                     * If we wouldn't set addValue to true here, the following
                     * might occur: No faulty command was added until the last
                     * iteration, addValue is set to false and therefore no
                     * faulty field command might be added, which will result in
                     * a transaction without a faulty command, i.e. a
                     * transaction which' execution should succeed. If we set
                     * addValue to true in this case, the code after
                     * "if(addValue)..." will ensure that a faulty field command
                     * will be added.
                     */
                    addValue = true;
                }
                if(addValue) {
                    /*
                     * the field didn't exist before the transaction, so its
                     * revision number is non-existant.
                     * 
                     * Impl note: Was reported as 0 in earlier implementations.
                     */
                    long fieldRevNr = XCommand.FORCED;
                    
                    boolean failBecauseOfFaultyFieldCommand = false;
                    if(!failBecauseOfAddOrRemoveFieldCommand) {
                        failBecauseOfFaultyFieldCommand = rand.nextBoolean();
                        
                        if(!failBecauseOfFaultyFieldCommand && j + 1 == nrOfFields) {
                            /*
                             * make sure that the transaction will fail if this
                             * is the last command which will be added but no
                             * faulty command was added until now
                             */
                            failBecauseOfFaultyFieldCommand = true;
                        }
                    }
                    
                    XAddress failFieldAddress = XX.resolveField(failObject.getAddress(), fieldId);
                    XAddress succFieldAddress = XX.resolveField(succObject.getAddress(), fieldId);
                    
                    boolean temp = constructFieldCommandForFaultyTransaction(failFieldAddress,
                            succFieldAddress, failTxnBuilder, succTxnBuilder,
                            failBecauseOfFaultyFieldCommand, fieldRevNr, rand);
                    
                    if(temp) {
                        /*
                         * we're using a temporary variable here because
                         * faultyCommandAdded might already be set to true and
                         * the method might return false, thereby wrongly
                         * setting the faultyCommandAdd to false if we'd just
                         * assign the return value of the method to
                         * faultyCommandAdded.
                         */
                        
                        faultyCommandAdded = true;
                    }
                }
            }
        }
        
        /*
         * TODO add some commands after the failed command to check that this is
         * no problem
         */
        
        XTransaction failTxn = failTxnBuilder.build();
        XTransaction succTxn = succTxnBuilder.build();
        
        assertTrue(
                "There seems to be a bug in the code of the test, since no faulty command was added to the transaction which' execution is supposed to fail, which results in a transaction which' execution would succeed.",
                faultyCommandAdded);
        Pair<XTransaction,XTransaction> pair = new Pair<XTransaction,XTransaction>(failTxn, succTxn);
        return pair;
        
    }
    
    /**
     * Pseudorandomly generates a transaction which should succeed on the given
     * empty model. The seed determines how the random number generator
     * generates its pseudorandom output.
     * 
     * Using the same seed twice will result in deterministically generating the
     * same transaction again, which can be useful when executing a transaction
     * in a test fails and the failed transaction needs to be reconstructed.
     */
    private Pair<ChangedModel,XTransaction> createRandomSucceedingModelTransaction(
            XWritableModel model, long seed, int maxNrOfObjects, int maxNrOfFields) {
        assertTrue("This method only works with empty models.", model.isEmpty());
        assertTrue(maxNrOfObjects > 0);
        assertTrue(maxNrOfFields >= 0);
        
        Random rand = new Random(seed);
        XAddress modelAddress = model.getAddress();
        
        XTransactionBuilder txBuilder = new XTransactionBuilder(modelAddress);
        ChangedModel changedModel = new ChangedModel(model);
        
        // create random amount of objects
        int nrOfObjects = 0;
        
        if(maxNrOfObjects == 1) {
            nrOfObjects = 1;
        } else {
            nrOfObjects = 1 + rand.nextInt(maxNrOfObjects - 1);
        }
        
        // add at least one object
        
        for(int i = 0; i < nrOfObjects; i++) {
            XId objectId = X.getIDProvider().fromString("object" + i);
            
            changedModel.createObject(objectId);
            
            assertTrue(changedModel.hasObject(objectId));
            
            XCommand addObjectCommand = this.comFactory.createAddObjectCommand(modelAddress,
                    objectId, false);
            
            txBuilder.addCommand(addObjectCommand);
        }
        
        List<XId> toBeRemovedObjects = new LinkedList<XId>();
        
        XId firstObjectId = X.getIDProvider().fromString("object" + 0);
        XId secondObjectId = X.getIDProvider().fromString("object" + 1);
        
        // add fields and values to the object
        for(XId objectId : changedModel) {
            XWritableObject changedObject = changedModel.getObject(objectId);
            XAddress objectAddress = XX.resolveObject(modelAddress, objectId);
            
            int nrOfFields = rand.nextInt(maxNrOfFields);
            for(int i = 0; i < nrOfFields; i++) {
                XId fieldId = X.getIDProvider().fromString(objectId + "field" + i);
                XAddress fieldAddress = XX.resolveField(objectAddress, fieldId);
                
                XWritableField field = changedObject.createField(fieldId);
                
                assertTrue(changedObject.hasField(fieldId));
                
                XCommand addFieldCommand = this.comFactory.createAddFieldCommand(objectAddress,
                        fieldId, false);
                
                txBuilder.addCommand(addFieldCommand);
                
                boolean hasValue = rand.nextBoolean();
                
                if(hasValue) {
                    
                    XValue value = createRandomValue(rand);
                    
                    field.setValue(value);
                    
                    assertEquals(value, field.getValue());
                    
                    XCommand addValueCommand = this.comFactory.createAddValueCommand(fieldAddress,
                            XCommand.SAFE_STATE_BOUND, value, false);
                    
                    txBuilder.addCommand(addValueCommand);
                }
            }
            
            /*
             * randomly change fields, i.e. randomly remove or change some
             * values and randomly remove some fields
             */
            randomlyChangeFields(rand, changedObject, txBuilder);
            
            if(!objectId.equals(firstObjectId) && !objectId.equals(secondObjectId)) {
                
                /*
                 * randomly determine if the object should be removed in the
                 * transaction.
                 * 
                 * Never remove the first and second object that was added so
                 * that at least two objects will be added and we're actually
                 * building a transaction (and not just a single command or a
                 * transaction which execution would result in NOCHANGE)
                 */
                boolean removeObject = rand.nextBoolean();
                if(removeObject) {
                    
                    toBeRemovedObjects.add(objectId);
                    
                    /*
                     * object was added in the transaction, so its revision
                     * number should be 0.
                     */
                    XCommand removeObjectCommand = this.comFactory.createRemoveObjectCommand(
                            objectAddress, XCommand.SAFE_STATE_BOUND, false);
                    
                    txBuilder.addCommand(removeObjectCommand);
                }
            }
            
        }
        
        /*
         * we need to remove the objects in a separate loop because modifying
         * the set of objects of the changedModel while we iterate over it
         * results in ConcurrentModificationExceptions
         */
        for(XId objectId : toBeRemovedObjects) {
            changedModel.removeObject(objectId);
            
            assertFalse(changedModel.hasObject(objectId));
        }
        
        XTransaction txn = txBuilder.build();
        
        return new Pair<ChangedModel,XTransaction>(changedModel, txn);
    }
    
    /**
     * Pseudorandomly generates a transaction which should succeed on the given
     * empty object. The seed determines how the random number generator
     * generates its pseudorandom output.
     * 
     * Using the same seed twice will result in deterministically generating the
     * same transaction again, which can be useful when executing a transaction
     * in a test fails and the failed transaction needs to be reconstructed.
     */
    private Pair<ChangedObject,XTransaction> createRandomSucceedingObjectTransaction(
            XWritableObject object, long seed, int maxNrOfFields) {
        assertTrue("This method only works with empty objects.", object.isEmpty());
        
        Random rand = new Random(seed);
        XId objectId = object.getId();
        XAddress objectAddress = object.getAddress();
        
        XTransactionBuilder txBuilder = new XTransactionBuilder(object.getAddress());
        ChangedObject changedObject = new ChangedObject(object);
        
        int nrOfFields;
        if(maxNrOfFields == 1) {
            nrOfFields = 1;
        } else {
            nrOfFields = 1 + rand.nextInt(maxNrOfFields - 1);
        }
        // add at least one field.
        
        for(int i = 0; i < nrOfFields; i++) {
            XId fieldId = X.getIDProvider().fromString(objectId + "field" + i);
            XAddress fieldAddress = XX.resolveField(objectAddress, fieldId);
            
            XWritableField field = changedObject.createField(fieldId);
            XCommand addFieldCommand = this.comFactory.createAddFieldCommand(objectAddress,
                    fieldId, false);
            txBuilder.addCommand(addFieldCommand);
            
            boolean hasValue = rand.nextBoolean();
            
            if(hasValue) {
                
                XValue value = createRandomValue(rand);
                
                field.setValue(value);
                
                XCommand addValueCommand = this.comFactory.createAddValueCommand(fieldAddress,
                        XCommand.SAFE_STATE_BOUND, value, false);
                txBuilder.addCommand(addValueCommand);
            }
        }
        
        /*
         * randomly change fields, i.e. randomly remove or change some values
         * and randomly remove some fields
         */
        randomlyChangeFields(rand, changedObject, txBuilder);
        
        /*
         * The code above might construct transactions which add some fields,
         * but immediately removes these fields again, so the execution of this
         * transaction would return "No change" or it might create a transaction
         * which only contains one command which actually changes one thing (for
         * example when nrOfFields equals 1, but we don't add a value to the
         * field or we add a value, but remove it again). But we want to create
         * a succeeding transaction, which actually changes something, which is
         * the reason why another field is added here to be on the safe side. We
         * also need to add a value, since a transaction should be made up of at
         * least 2 commands which actually change something.
         */
        
        XId fieldId = X.getIDProvider().fromString(objectId + "field" + nrOfFields + 1);
        XAddress fieldAddress = XX.resolveField(objectAddress, fieldId);
        
        XWritableField field = changedObject.createField(fieldId);
        XCommand addFieldCommand = this.comFactory.createAddFieldCommand(objectAddress, fieldId,
                false);
        txBuilder.addCommand(addFieldCommand);
        
        XValue value = createRandomValue(rand);
        
        field.setValue(value);
        
        XCommand addValueCommand = this.comFactory.createAddValueCommand(fieldAddress,
                XCommand.SAFE_STATE_BOUND, value, false);
        txBuilder.addCommand(addValueCommand);
        
        XTransaction txn = txBuilder.build();
        
        return new Pair<ChangedObject,XTransaction>(changedObject, txn);
    }
    
    /*
     * TODO check if all types of forced commands work correctly with arbitrary
     * revision numbers (as they should)
     */
    
    private XTransaction findFailingSubtransactionInSucceedingModelTransaction(
            String modelIdString, long seed, int maxNrOfObjects, int maxNrOfFields) {
        XId modelId = X.getIDProvider().fromString(modelIdString);
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        
        GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
        XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
        // add a model on which an object can be created first
        long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
        
        assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
        
        XWritableModel modelSnapshot = this.persistence.getModelSnapshot(modelAdrRequest);
        
        log.info("Creating transaction with seed " + seed + ".");
        Pair<ChangedModel,XTransaction> pair = createRandomSucceedingModelTransaction(
                modelSnapshot, seed, maxNrOfObjects, maxNrOfFields);
        XTransaction txn = pair.getSecond();
        
        revNr = 0;
        
        XTransaction testTxn = null;
        
        for(int i = 1; i < txn.size() && revNr != XCommand.FAILED; i++) {
            /*
             * reset state, so that we try to execute the transaction part we're
             * now checking on the old state
             */
            XCommand removeModelCommand = this.comFactory.createRemoveModelCommand(modelAddress, 0,
                    true);
            
            long removeRevNr = this.persistence.executeCommand(this.actorId, removeModelCommand);
            
            assertTrue(
                    "Model couldn't be removed, state couldn't be reset, therefore method cannot be executed correctly.",
                    removeRevNr >= 0);
            
            // add model again
            long addRevNr = this.persistence.executeCommand(this.actorId, addModelCom);
            
            assertTrue("Model could not be added, test cannot be executed.", addRevNr >= 0);
            
            /*
             * build sub transaction
             */
            
            XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
            
            for(int j = 0; j < i; j++) {
                txnBuilder.addCommand(txn.getCommand(j));
            }
            
            testTxn = txnBuilder.build();
            
            revNr = this.persistence.executeCommand(this.actorId, testTxn);
            
            if(revNr == XCommand.FAILED) {
                // last command is the one that fails
                XCommand failCmd = testTxn.getCommand(testTxn.size() - 1);
                
                // assertTrue(failCmd.getChangeType() == ChangeType.REMOVE);
                
                XId failId;
                if(failCmd instanceof XModelCommand) {
                    failId = failCmd.getChangedEntity().getObject();
                } else {
                    failId = failCmd.getTarget().getObject();
                }
                
                XTransactionBuilder txnBuilder2 = new XTransactionBuilder(modelAddress);
                
                for(XCommand cmd : testTxn) {
                    if(failId.equals(cmd.getTarget().getObject())
                            || failId.equals(cmd.getChangedEntity().getObject())) {
                        txnBuilder2.addCommand(cmd);
                    }
                }
                
                /*
                 * reset state, so that we try to execute the transaction part
                 * we're now checking on the old state
                 */
                removeModelCommand = this.comFactory
                        .createRemoveModelCommand(modelAddress, 0, true);
                
                removeRevNr = this.persistence.executeCommand(this.actorId, removeModelCommand);
                
                assertTrue(
                        "Model couldn't be removed, state couldn't be reset, therefore method cannot be executed correctly.",
                        removeRevNr >= 0);
                
                // add model again
                addRevNr = this.persistence.executeCommand(this.actorId, addModelCom);
                
                assertTrue("Model could not be added, test cannot be executed.", addRevNr >= 0);
                
                XTransaction testTxn2 = txnBuilder2.build();
                
                long testRevNr = this.persistence.executeCommand(this.actorId, testTxn2);
                
                if(testRevNr == XCommand.FAILED) {
                    testTxn = testTxn2;
                }
            }
            
        }
        
        /*
         * reset state, so that we try to execute the transaction part we're now
         * checking on the old state
         */
        XCommand removeModelCommand = this.comFactory.createRemoveModelCommand(modelAddress, 0,
                true);
        
        long removeRevNr = this.persistence.executeCommand(this.actorId, removeModelCommand);
        
        assertTrue(
                "Model couldn't be removed, state couldn't be reset, therefore method cannot be executed correctly.",
                removeRevNr >= 0);
        
        return testTxn;
    }
    
    private XTransaction findFailingSubtransactionInSucceedingObjectTransaction(
            String modelIdString, String objectIdString, long seed, int maxNrOfFields) {
        XId modelId = X.getIDProvider().fromString(modelIdString);
        XId objectId = X.getIDProvider().fromString(objectIdString);
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
        
        GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
        
        XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
        // add a model on which an object can be created first
        long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
        
        assertTrue("Model could not be added, test cannot be executed.", revNr >= 0);
        
        XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, false);
        // add an object on which the txn can be executed
        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
        
        assertTrue("Object could not be added, test cannot be executed.", revNr >= 0);
        
        XWritableObject objectSnapshot = this.persistence.getObjectSnapshot(objectAdrRequest);
        
        log.info("Creating transaction with seed " + seed + ".");
        Pair<ChangedObject,XTransaction> pair = createRandomSucceedingObjectTransaction(
                objectSnapshot, seed, maxNrOfFields);
        XTransaction txn = pair.getSecond();
        
        revNr = 0;
        
        XTransaction testTxn = null;
        
        for(int i = 1; i < txn.size() && revNr != XCommand.FAILED; i++) {
            /*
             * reset state, so that we try to execute the transaction part we're
             * now checking on the old state
             */
            XCommand removeModelCommand = this.comFactory.createRemoveModelCommand(modelAddress, 0,
                    true);
            
            long removeRevNr = this.persistence.executeCommand(this.actorId, removeModelCommand);
            
            assertTrue(
                    "Model couldn't be removed, state couldn't be reset, therefore method cannot be executed correctly.",
                    removeRevNr >= 0);
            
            // add model and object again
            long addRevNr = this.persistence.executeCommand(this.actorId, addModelCom);
            
            assertTrue("Model could not be added, test cannot be executed.", addRevNr >= 0);
            
            addRevNr = this.persistence.executeCommand(this.actorId, addObjectCom);
            
            assertTrue("Object could not be added, test cannot be executed.", addRevNr >= 0);
            
            /*
             * build sub transaction
             */
            
            XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
            
            for(int j = 0; j < i; j++) {
                txnBuilder.addCommand(txn.getCommand(j));
            }
            
            testTxn = txnBuilder.build();
            
            revNr = this.persistence.executeCommand(this.actorId, testTxn);
            
            if(revNr == XCommand.FAILED) {
                // last command is the one that fails
                XCommand failCmd = testTxn.getCommand(testTxn.size() - 1);
                
                // assertTrue(failCmd.getChangeType() == ChangeType.REMOVE);
                
                XId failId;
                if(failCmd instanceof XObjectCommand) {
                    failId = failCmd.getChangedEntity().getField();
                } else {
                    failId = failCmd.getTarget().getField();
                }
                
                XTransactionBuilder txnBuilder2 = new XTransactionBuilder(modelAddress);
                
                for(XCommand cmd : testTxn) {
                    if(failId.equals(cmd.getTarget().getField())
                            || failId.equals(cmd.getChangedEntity().getField())) {
                        txnBuilder2.addCommand(cmd);
                    }
                }
                
                /*
                 * reset state, so that we try to execute the transaction part
                 * we're now checking on the old state
                 */
                removeModelCommand = this.comFactory
                        .createRemoveModelCommand(modelAddress, 0, true);
                removeRevNr = this.persistence.executeCommand(this.actorId, removeModelCommand);
                assertTrue(
                        "Model couldn't be removed, state couldn't be reset, therefore method cannot be executed correctly.",
                        removeRevNr >= 0);
                
                // add model and object again
                addRevNr = this.persistence.executeCommand(this.actorId, addModelCom);
                
                assertTrue("Model could not be added, test cannot be executed.", addRevNr >= 0);
                
                addRevNr = this.persistence.executeCommand(this.actorId, addObjectCom);
                
                assertTrue("Object could not be added, test cannot be executed.", addRevNr >= 0);
                
                XTransaction testTxn2 = txnBuilder2.build();
                
                long testRevNr = this.persistence.executeCommand(this.actorId, testTxn2);
                
                if(testRevNr == XCommand.FAILED) {
                    testTxn = testTxn2;
                }
            }
            
        }
        
        /*
         * reset state, so that we try to execute the transaction part we're now
         * checking on the old state
         */
        XCommand removeModelCommand = this.comFactory.createRemoveModelCommand(modelAddress, 0,
                true);
        
        long removeRevNr = this.persistence.executeCommand(this.actorId, removeModelCommand);
        
        assertTrue(
                "Model couldn't be removed, state couldn't be reset, therefore method cannot be executed correctly.",
                removeRevNr >= 0);
        
        return testTxn;
    }
    
    /**
     * This test randomly creates model transactions which execution is supposed
     * to fail. It uses {@link java.util.Random} to create random transactions.
     * We set the seed manually and always print it on the screen.
     * 
     * If the test fails, simply copy the seed which created the transaction
     * which was the cause of the failure and set the seed to this value
     * (instead of using a random seed, as the test normally does). This makes
     * the test deterministic and enables debugging.
     */
    @Test
    public void testExecuteCommandFailingModelTransactions() {
        SecureRandom seedGen = new SecureRandom();
        
        for(int i = 0; i <= this.nrOfIterationsForTxnTests; i++) {
            testExecuteCommandFailingModelTransactionWithSeed(i, seedGen.nextLong());
        }
    }
    
    private void testExecuteCommandFailingModelTransactionWithSeed(int i, long seed) {
        
        XId failModelId = X.getIDProvider().fromString(
                "testExecuteCommandFailingModelTransactionFailModel" + i);
        XAddress failModelAddress = XX.resolveModel(this.repoId, failModelId);
        
        GetWithAddressRequest failModelAdrRequest = new GetWithAddressRequest(failModelAddress);
        XCommand addFailModelCom = this.comFactory.createAddModelCommand(this.repoId, failModelId,
                false);
        
        XId succModelId = X.getIDProvider().fromString(
                "testExecuteCommandFailingModelTranscationSuccModel" + i);
        XAddress succModelAddress = XX.resolveModel(this.repoId, succModelId);
        
        GetWithAddressRequest succModelAdrRequest = new GetWithAddressRequest(succModelAddress);
        XCommand addSuccModelCom = this.comFactory.createAddModelCommand(this.repoId, succModelId,
                false);
        
        /*
         * We use two model instance, which basically represent the same model.
         * One will be used to execute the succeeding transaction and the other
         * one for the transaction which is supposed to fail. This makes testing
         * easier and more flexible.
         */
        
        // add a model on which an object can be created first
        long failRevNr = this.persistence.executeCommand(this.actorId, addFailModelCom);
        long succRevNr = this.persistence.executeCommand(this.actorId, addSuccModelCom);
        
        assertTrue("Model for the failing transaction could not be added, test cannot be executed",
                failRevNr >= 0);
        assertTrue(
                "Model for the succeeding transaction could not be added, test cannot be executed",
                succRevNr >= 0);
        
        XWritableModel failModelSnapshot = this.persistence.getModelSnapshot(failModelAdrRequest);
        XWritableModel succModelSnapshot = this.persistence.getModelSnapshot(succModelAdrRequest);
        
        /*
         * Info: if the test fails, do the following for deterministic
         * debugging: Set the seed to the value which caused the test to fail.
         * This makes the test deterministic.
         */
        log.info("Creating transaction pair " + i + " with seed " + seed + ".");
        Pair<XTransaction,XTransaction> pair = createRandomFailingModelTransaction(
                failModelSnapshot, succModelSnapshot, seed);
        
        XTransaction failTxn = pair.getFirst();
        XTransaction succTxn = pair.getSecond();
        
        succRevNr = this.persistence.executeCommand(this.actorId, succTxn);
        assertTrue(
                "Model Transaction failed, should succeed, since this was the transaction that does not contain the command which should cause the transaction to fail, seed was: "
                        + seed
                        + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                succRevNr >= 0);
        
        failRevNr = this.persistence.executeCommand(this.actorId, failTxn);
        assertEquals(
                "Model Transaction succeeded, should fail, seed was: "
                        + seed
                        + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                XCommand.FAILED, failRevNr);
        
        /*
         * Make sure that the changes actually weren't executed. Since the model
         * was empty before the execution of the faulty transaction, we just
         * need to check if it's still empty. If this is not the case, this
         * implies that some of the commands in the faulty transaction were
         * executed, although the transaction's execution failed.
         */
        
        failModelSnapshot = this.persistence.getModelSnapshot(failModelAdrRequest);
        assertTrue(
                "Since the model was empty before the execution of the faulty transaction, it  should be empty. Since it is not empty, some commands of the transaction must've been executed, although the transaction failed."
                        + " Seed was: "
                        + seed
                        + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                failModelSnapshot.isEmpty());
    }
    
    /**
     * This test randomly creates object transactions which execution is
     * supposed to fail. It uses {@link java.util.Random} to create random
     * transactions. We set the seed manually and always print it on the screen.
     * 
     * If the test fails, simply copy the seed which created the transaction
     * which was the cause of the failure and set the seed to this value
     * (instead of using random seeds, as the test normally does). This makes
     * the test deterministic and enables debugging.
     */
    @Test
    public void testExecuteCommandFailingObjectTransactions() {
        SecureRandom seedGen = new SecureRandom();
        
        for(int i = 0; i <= this.nrOfIterationsForTxnTests; i++) {
            testExecuteCommandFailingObjectTransactionWithSeed(i, seedGen.nextLong());
        }
    }
    
    @Test
    public void testExecuteCommandFailingObjectTransactions_7741171529824133452() {
        testExecuteCommandFailingObjectTransactionWithSeed(1, 7741171529824133452l);
    }
    
    private void testExecuteCommandFailingObjectTransactionWithSeed(int i, long seed) {
        
        XId failModelId = X.getIDProvider().fromString(
                "testExecuteCommandFailingObjectTransactionFailModel" + i);
        
        XCommand addFailModelCom = this.comFactory.createAddModelCommand(this.repoId, failModelId,
                false);
        
        XId succModelId = X.getIDProvider().fromString(
                "testExecuteCommandFailingObjectTransactionSuccModel" + i);
        
        XCommand addSuccModelCom = this.comFactory.createAddModelCommand(this.repoId, succModelId,
                false);
        
        /*
         * We use two model instances, which basically represent the same model.
         * One will be used to hold the object on which we'll execute the
         * succeeding transaction and the other one for the object on which
         * we'll execute the transaction which is supposed to fail. This makes
         * testing easier and more flexible.
         */
        
        // add a model on which an object can be created first
        long failRevNr = this.persistence.executeCommand(this.actorId, addFailModelCom);
        long succRevNr = this.persistence.executeCommand(this.actorId, addSuccModelCom);
        
        assertTrue("One of the models could not be added, test cannot be executed.", failRevNr >= 0
                && succRevNr >= 0);
        
        XId failObjectId = X.getIDProvider().fromString(
                "testExecuteCommandFailingObjectTransactionFailObject" + i);
        XAddress failObjectAddress = XX.resolveObject(this.repoId, failModelId, failObjectId);
        
        GetWithAddressRequest failObjectAdrRequest = new GetWithAddressRequest(failObjectAddress);
        XCommand addFailObjectCom = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, failModelId), failObjectId, false);
        
        XId succObjectId = X.getIDProvider().fromString(
                "testExecuteCommandFailingObjectTransactionSuccObject" + i);
        XAddress succObjectAddress = XX.resolveObject(this.repoId, succModelId, succObjectId);
        
        GetWithAddressRequest succObjectAdrRequest = new GetWithAddressRequest(succObjectAddress);
        XCommand addSuccObjectCom = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, succModelId), succObjectId, false);
        
        /*
         * We use two object instances, which basically represent the same
         * object. One will be used to execute the succeeding transaction and
         * the other one for the transaction which is supposed to fail. This
         * makes testing easier and more flexible.
         */
        
        // create the objects on which the transactions will be executed
        failRevNr = this.persistence.executeCommand(this.actorId, addFailObjectCom);
        succRevNr = this.persistence.executeCommand(this.actorId, addSuccObjectCom);
        
        assertTrue("The object for the failing transaction could not be added.", failRevNr >= 0);
        assertTrue("The object for the succeeding transaction could not be added.", succRevNr >= 0);
        
        XWritableObject failObjectSnapshot = this.persistence
                .getObjectSnapshot(failObjectAdrRequest);
        XWritableObject succObjectSnapshot = this.persistence
                .getObjectSnapshot(succObjectAdrRequest);
        
        /*
         * Info: if the test fails, do the following for deterministic
         * debugging: Set the seed to the value which caused the test to fail.
         * This makes the test deterministic.
         */
        System.out.println("Creating object transaction pair " + i + " with seed " + seed + ".");
        Pair<XTransaction,XTransaction> pair = createRandomFailingObjectTransaction(
                failObjectSnapshot, succObjectSnapshot, seed);
        
        XTransaction failTxn = pair.getFirst();
        XTransaction succTxn = pair.getSecond();
        
        succRevNr = this.persistence.executeCommand(this.actorId, succTxn);
        assertTrue(
                "Object Transaction failed ("
                        + succRevNr
                        + "), should succeed, since this was the transaction that does not contain the command which should cause the transaction to fail, seed was: "
                        + seed
                        + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu). Txn was:\n"
                        + succTxn, succRevNr >= 0);
        
        failRevNr = this.persistence.executeCommand(this.actorId, failTxn);
        assertEquals(
                "Object Transaction succeeded, should fail, seed was: "
                        + seed
                        + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                XCommand.FAILED, failRevNr);
        
        /*
         * Make sure that the changes actually weren't executed. Since the
         * object was empty before we tried to execute the faulty transaction,
         * we just have to assert that it is still empty. This implies that no
         * commands of the transaction were executed.
         */
        failObjectSnapshot = this.persistence.getObjectSnapshot(failObjectAdrRequest);
        assertTrue(
                "Object was empty before we tried to execute the faulty transaction, but has fields now, which means that some of the commands of the faulty transaction must've been executed, although its execution failed."
                        + " Seed was: "
                        + seed
                        + " (please see the documentation about the use of the seed, it is needed for debugging, do not discard it before you've read the docu).",
                failObjectSnapshot.isEmpty());
        
    }
    
    @Test
    public void testGetEventsModelTransactions() {
        SecureRandom seedGen = new SecureRandom();
        
        for(int i = 0; i < this.nrOfIterationsForTxnTests; i++) {
            testGetEventsModelTransactionWithSeed(i, seedGen.nextLong());
        }
    }
    
    private void testGetEventsModelTransactionWithSeed(int i, long seed) {
        
        XId modelId = XX.toId("testGetEventsTransactionsModel" + i);
        XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
        XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
        assertTrue("The model wasn't correctly added, test cannot be executed", revNr >= 0);
        
        GetWithAddressRequest modelAdrRequest = new GetWithAddressRequest(modelAddress);
        XWritableModel model = this.persistence.getModelSnapshot(modelAdrRequest);
        
        /*
         * Info: if the test fails, do the following for deterministic
         * debugging: Set the seed to the value which caused the test to fail.
         * This makes the test deterministic.
         */
        log.info("Used seed: " + seed + ".");
        Pair<ChangedModel,XTransaction> pair = createRandomSucceedingModelTransaction(model, seed,
                10, 10);
        XTransaction txn = pair.getSecond();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        assertTrue("Transaction did not succeed, seed was " + seed, revNr >= 0);
        
        List<XEvent> events = this.persistence.getEvents(modelAddress, revNr, revNr);
        assertEquals(
                "The list of events should contain one Transaction Event, but actually contains multiple events, seed was "
                        + seed, 1, events.size());
        
        XEvent event = events.get(0);
        assertTrue("The returned event should be a TransactionEvent, seed was " + seed,
                event instanceof XTransactionEvent);
        
        XTransactionEvent txnEvent = (XTransactionEvent)event;
        
        assertEquals("The event didn't refer to the correct old revision number, seed was " + seed,
                0, txnEvent.getOldModelRevision());
        assertEquals("The event didn't refer to the correct revision number, seed was " + seed,
                revNr, txnEvent.getRevisionNumber());
        assertEquals("Event doesn't refer to the correct target, seed was " + seed, modelAddress,
                txnEvent.getTarget());
        assertEquals("Event doesn't refer to the correct changed entity, seed was " + seed,
                modelAddress, txnEvent.getChangedEntity());
        assertEquals("The actor of the event is not correct, seed was " + seed, this.actorId,
                txnEvent.getActor());
        assertFalse("The event is wrongly marked as implied, seed was " + seed,
                txnEvent.isImplied());
        assertFalse("the event is wrongly marked as being part of a transaction, seed was " + seed,
                txnEvent.inTransaction());
        
        /*
         * TODO check the events that make up the transaction!
         */
        
        /*
         * TODO document why there are no "removedObjectEvents" (etc.) lists
         */
        Map<XAddress,XEvent> addedObjectEvents = new HashMap<XAddress,XEvent>();
        Map<XAddress,XEvent> addedFieldEvents = new HashMap<XAddress,XEvent>();
        Map<XAddress,XEvent> addedValueEvents = new HashMap<XAddress,XEvent>();
        
        for(XEvent ev : txnEvent) {
            /*
             * TODO Notice that this might change in the future and the randomly
             * constructed succeeding transaction might also contain events of
             * other types.
             * 
             * Check why the transaction events only contain ADD type events,
             * although the transactions themselves also contain changes and
             * removes. Does the TransactionBuilder or the system which creates
             * the transaction event already fine tune the result so that
             * changes and removes which occur on objects/fields which were
             * added during the transaction aren't even shown?
             */
            assertTrue("The transaction should only contain events of the Add-type, seed was "
                    + seed, ev.getChangeType() == ChangeType.ADD);
            assertTrue("Event is wrongly marked as not being part of a transaction, seed was "
                    + seed, ev.inTransaction());
            assertEquals("The event doesn't refer to the correct model, seed was " + seed, modelId,
                    ev.getTarget().getModel());
            assertFalse("The event is wrongly marked as being implied, seed was " + seed,
                    event.isImplied());
            assertEquals("The actor of the event is not correct.", this.actorId, event.getActor());
            assertEquals("The event didn't refer to the correct revision number, seed was " + seed,
                    revNr, ev.getRevisionNumber());
            
            if(ev.getChangedEntity().getAddressedType() == XType.XOBJECT) {
                
                addedObjectEvents.put(ev.getChangedEntity(), ev);
            } else {
                assertEquals(
                        "A model transaction should only contain commands that target objects or fields, seed was "
                                + seed, ev.getChangedEntity().getAddressedType(), XType.XFIELD);
                
                if(ev instanceof XObjectEvent) {
                    addedFieldEvents.put(ev.getChangedEntity(), ev);
                } else {
                    assertTrue(ev instanceof XFieldEvent);
                    addedValueEvents.put(ev.getChangedEntity(), ev);
                }
            }
        }
        
        model = this.persistence.getModelSnapshot(modelAdrRequest);
        for(XId objectId : model) {
            XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
            
            assertTrue(
                    "Since the model was emtpy before the transaction, there should be a fitting add-event for the object with XId "
                            + objectId + ", seed was " + seed,
                    addedObjectEvents.containsKey(objectAddress));
            XReadableObject object = model.getObject(objectId);
            
            for(XId fieldId : object) {
                XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
                
                assertTrue(
                        "Since the model was emtpy before the transaction, there should be a fitting add-event for the field with XId "
                                + fieldId + ", seed was " + seed,
                        addedFieldEvents.containsKey(fieldAddress));
                XReadableField field = object.getField(fieldId);
                if(field.getValue() != null) {
                    assertTrue(
                            "Since the model was emtpy before the transaction, there should be a fitting add-event for the value in the field with XId "
                                    + fieldId + ", seed was " + seed,
                            addedValueEvents.containsKey(fieldAddress));
                }
            }
        }
        
    }
    
    @Test
    public void testGetEventsObjectTransactions() {
        SecureRandom seedGen = new SecureRandom();
        
        for(int i = 0; i < this.nrOfIterationsForTxnTests; i++) {
            testGetEventsObjectTransactionsWithSeed(i, seedGen.nextLong());
        }
    }
    
    private void testGetEventsObjectTransactionsWithSeed(int i, long seed) {
        
        XId modelId = XX.toId("tgeotwsModel" + i);
        XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
        
        XId objectId = XX.toId("object" + i);
        XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
        XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, false);
        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
        assertTrue("The object wasn't correctly added, test cannot be executed.", revNr >= 0);
        assertEquals("new object has revNr 1", 1, revNr);
        
        GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
        XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
        assertEquals(1, object.getRevisionNumber());
        long objectRevNr = object.getRevisionNumber();
        
        /*
         * Info: if the test fails, do the following for deterministic
         * debugging: Set the seed to the value which caused the test to fail.
         * This makes the test deterministic.
         */
        log.info("Used seed: " + seed + ".");
        Pair<ChangedObject,XTransaction> pair = createRandomSucceedingObjectTransaction(object,
                seed, 10);
        XTransaction txn = pair.getSecond();
        
        revNr = this.persistence.executeCommand(this.actorId, txn);
        assertTrue("Transaction did not succeed, seed was " + seed, revNr >= 0);
        
        List<XEvent> events = this.persistence.getEvents(objectAddress, revNr, revNr);
        assertEquals(
                "The list of events should contain one Transaction Event, but actually contains zero or multiple events, seed was "
                        + seed, 1, events.size());
        
        XEvent event = events.get(0);
        assertTrue("The returned event should be a TransactionEvent.",
                event instanceof XTransactionEvent);
        
        XTransactionEvent txnEvent = (XTransactionEvent)event;
        
        /**
         * TODO test txn event as such
         */
        
        /*
         * TODO check the other events that make up the transaction!
         */
        assertEquals("The event didn't refer to the correct old revision number, seed was " + seed,
                objectRevNr, txnEvent.getEvent(0).getOldObjectRevision());
        assertEquals("The event didn't refer to the correct revision number, seed was " + seed,
                revNr, txnEvent.getEvent(0).getRevisionNumber());
        assertEquals("Event doesn't refer to the correct target, seed was " + seed, objectAddress,
                txnEvent.getEvent(0).getTarget());
        assertEquals("Event doesn't refer to the correct changed entity, seed was " + seed,
                objectAddress, txnEvent.getEvent(0).getChangedEntity().getParent());
        assertEquals("The actor of the event is not correct, seed was " + seed, this.actorId,
                txnEvent.getEvent(0).getActor());
        assertFalse("The event is wrongly marked as implied, seed was " + seed, txnEvent
                .getEvent(0).isImplied());
        assertTrue("the event is wrongly marked as being part of a transaction, seed was " + seed,
                txnEvent.getEvent(0).inTransaction());
        
        /*
         * TODO document why there are no "removedFieldEvents" (etc.) lists
         */
        Map<XAddress,XEvent> addedFieldEvents = new HashMap<XAddress,XEvent>();
        Map<XAddress,XEvent> addedValueEvents = new HashMap<XAddress,XEvent>();
        
        for(XEvent ev : txnEvent) {
            /*
             * TODO Notice that this might change in the future and the randomly
             * constructed succeeding transaction might also contain events of
             * other types.
             */
            assertTrue("The transaction should only contain events of the Add-type, seed was "
                    + seed, ev.getChangeType() == ChangeType.ADD);
            assertTrue("Event is wrongly marked as not being part of a transaction, seed was "
                    + seed, ev.inTransaction());
            assertEquals("The event doesn't refer to the correct model, seed was " + seed, modelId,
                    ev.getTarget().getModel());
            assertEquals("The event doesn't refer to the correct object, seed was " + seed,
                    objectId, ev.getTarget().getObject());
            assertFalse("The event is wrongly marked as being implied, seed was " + seed,
                    event.isImplied());
            assertEquals("The actor of the event is not correct, seed was " + seed, this.actorId,
                    event.getActor());
            assertEquals("The event didn't refer to the correct revision number, seed was " + seed,
                    revNr, ev.getRevisionNumber());
            
            assertEquals(
                    "A object transaction should only contain commands that target fields, seed was "
                            + seed, ev.getChangedEntity().getAddressedType(), XType.XFIELD);
            
            if(ev instanceof XObjectEvent) {
                addedFieldEvents.put(ev.getChangedEntity(), ev);
            } else {
                assertTrue(ev instanceof XFieldEvent);
                addedValueEvents.put(ev.getChangedEntity(), ev);
            }
        }
        
        object = this.persistence.getObjectSnapshot(objectAdrRequest);
        for(XId fieldId : object) {
            XAddress fieldAddress = XX.resolveField(this.repoId, modelId, objectId, fieldId);
            
            assertTrue(
                    "Since the object was emtpy before the transaction, there should be a fitting add-event for the field with XId "
                            + fieldId + ", seed was " + seed,
                    addedFieldEvents.containsKey(fieldAddress));
            XReadableField field = object.getField(fieldId);
            if(field.getValue() != null) {
                assertTrue(
                        "Since the object was emtpy before the transaction, there should be a fitting add-event for the value in the field with XId "
                                + fieldId + ", seed was " + seed,
                        addedValueEvents.containsKey(fieldAddress));
            }
        }
        
    }
    
    @Test
    public void testGetEventsObjectTransactionWithSeedMinus602254616775376772_A() {
        testGetEventsObjectTransactionsWithSeed(0, -602254616775376772l);
    }
    
    @Test
    public void testGetEventsObjectTransactionWithSeedMinus602254616775376772_B() {
        XId modelId = XX.toId("modelm602254616775376772l");
        XCommand addModelCom = this.comFactory.createAddModelCommand(this.repoId, modelId, false);
        long revNr = this.persistence.executeCommand(this.actorId, addModelCom);
        assertTrue("The model wasn't correctly added, test cannot be executed.", revNr >= 0);
        
        XId objectId = XX.toId("object1");
        XAddress objectAddress = XX.resolveObject(this.repoId, modelId, objectId);
        XCommand addObjectCom = this.comFactory.createAddObjectCommand(
                XX.resolveModel(this.repoId, modelId), objectId, false);
        revNr = this.persistence.executeCommand(this.actorId, addObjectCom);
        assertTrue("The object wasn't correctly added, test cannot be executed.", revNr >= 0);
        assertEquals("new object has revNr 1", 1, revNr);
        
        GetWithAddressRequest objectAdrRequest = new GetWithAddressRequest(objectAddress);
        XWritableObject object = this.persistence.getObjectSnapshot(objectAdrRequest);
        assertEquals(1, object.getRevisionNumber());
        
        XTransactionBuilder builder2 = new XTransactionBuilder(objectAddress);
        builder2.addField(objectAddress, XCommand.SAFE_STATE_BOUND, XX.toId("object0field0"));
        builder2.addField(objectAddress, XCommand.SAFE_STATE_BOUND, XX.toId("object0field1"));
        builder2.addField(objectAddress, XCommand.SAFE_STATE_BOUND, XX.toId("object0field21"));
        // FIXME was revision 0 instead of SAFE
        builder2.addValue(XX.resolveField(objectAddress, XX.toId("object0field21")),
                XCommand.SAFE_STATE_BOUND, XV.toValue(false));
        XTransaction txn2 = builder2.build();
        revNr = this.persistence.executeCommand(this.actorId, txn2);
        
        assertTrue("Transaction did not succeed", revNr >= 0);
    }
    
}
