package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;
import org.xydra.store.impl.delegate.XydraPersistence;


public abstract class AbstractSingleCommandTransactionTest {
	static {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
	}
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractPersistenceTest.class);
	
	public XydraPersistence persistence;
	
	public XCommandFactory comFactory;
	
	public XID repoId = X.getIDProvider().fromString("testRepo");
	public XAddress repoAddress = XX.resolveRepository(this.repoId);
	public XID actorId = X.getIDProvider().fromString("testActor");
	
	@Test
	public void testSingleCommandTransactionExceptingTransactionEvent() {
		
		XID modelId = X.getIDProvider().fromString("singleCommandTransactionModel");
		XAddress modelAddress = XX.resolveModel(this.repoId, modelId);
		
		XCommand addModelCommand =
		        this.comFactory.createAddModelCommand(this.repoId, modelId, false);
		
		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);
		
		assertTrue("Model couldn't be added, test cannot be executed", revNr >= 0);
		
		XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		XID objectId = X.getIDProvider().fromString("singleCommandTransactionObject");
		XCommand addObjectCommand =
		        this.comFactory.createAddObjectCommand(this.repoId, modelId, objectId, false);
		
		txnBuilder.addCommand(addObjectCommand);
		XTransaction txn = txnBuilder.build();
		
		revNr = this.persistence.executeCommand(this.actorId, txn);
		
		assertTrue("Transaction failed.", revNr > 0);
		
		List<XEvent> events = this.persistence.getEvents(modelAddress, revNr, revNr);
		
		assertEquals("There should only be one event.", 1, events.size());
		assertTrue("The event should be a TransactionEvent.",
		        events.get(0) instanceof XTransactionEvent);
	}
}
