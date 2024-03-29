package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.persistence.XydraPersistence;


public abstract class AbstractSingleCommandTransactionTest {
	static {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
	}

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractPersistenceTestForTransactions.class);

	public XydraPersistence persistence;

	public XCommandFactory comFactory;

	public XId repoId = X.getIDProvider().fromString("testRepo");
	public XAddress repoAddress = XX.resolveRepository(this.repoId);
	public XId actorId = X.getIDProvider().fromString("testActor");

	@Test
	public void testSingleCommandTransactionExceptingTransactionEvent() {

		final XId modelId = BaseRuntime.getIDProvider().fromString("singleCommandTransactionModel");
		final XAddress modelAddress = Base.resolveModel(this.repoId, modelId);

		final XCommand addModelCommand = this.comFactory.createAddModelCommand(this.repoId, modelId,
		        false);

		long revNr = this.persistence.executeCommand(this.actorId, addModelCommand);

		assertTrue("Model couldn't be added, test cannot be executed", revNr >= 0);

		final XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
		final XId objectId = BaseRuntime.getIDProvider().fromString("singleCommandTransactionObject");
		final XCommand addObjectCommand = this.comFactory.createAddObjectCommand(Base.resolveModel(this.repoId, modelId),
		        objectId, false);

		txnBuilder.addCommand(addObjectCommand);
		final XTransaction txn = txnBuilder.build();

		revNr = this.persistence.executeCommand(this.actorId, txn);

		assertTrue("Transaction failed.", revNr > 0);

		final List<XEvent> events = this.persistence.getEvents(modelAddress, revNr, revNr);

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
