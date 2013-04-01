package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;
import org.xydra.persistence.XydraPersistence;


public abstract class AbstractSingleCommandTransactionTest {
	static {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
	}
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractPersistenceTest.class);
	
	public XydraPersistence persistence;
	
	public XCommandFactory comFactory;
	
	public XId repoId = X.getIDProvider().fromString("testRepo");
	public XAddress repoAddress = XX.resolveRepository(this.repoId);
	public XId actorId = X.getIDProvider().fromString("testActor");
	
	@Test
	public void testSingleCommandTransactionExceptingTransactionEvent() {
		
		XId modelId = X.getIDProvider().fromString("singleCommandTransactionModel");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
		        false);
		
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model couldn't be added, test cannot be executed", revNr >= 0);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		XId objectId = X.getIDProvider().fromString("singleCommandTransactionObject");
		XCommand addObjectCommand = this.comFactory.createAddObjectCommand(this.repoId, modelId,
		        objectId, false);
		
		txnBuilder.addCommand(addObjectCommand);
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertTrue("Transaction failed.", revNr > 0);
		
		List<XEvent> events = this.persistence.getEvents(modelAddress, revNr, revNr);
		
		assertEquals("There should only be one event.", 1, events.size());
		/*
		 * implementations are free to replace a txn with a single command with
		 * a standalone single command
		 */
		// assertTrue("The event should be a TransactionEvent was " +
		// events.get(0).getClass(), events.get(0) instanceof
		// XTransactionEvent);
	}
}
