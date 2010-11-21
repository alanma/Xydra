package org.xydra.server.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XRepository;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.server.IXydraServer;


/**
 * Simple test to see if anything works.
 * 
 */
public abstract class PreTest {
	
	XRepository repo;
	
	// to be initialized by subclasses
	protected static IXydraServer xydraServer;
	
	/**
	 * Expect a run without errors, thats all.
	 */
	@Test
	public void testTheSetupItself() {
		
		assertNotNull(xydraServer);
		
		// initialize XModel
		// TODO move command into transaction
		XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(xydraServer
		        .getRepositoryAddress(), XCommand.SAFE, DemoModelUtil.PHONEBOOK_ID);
		assertTrue(xydraServer.executeCommand(createCommand, null) >= 0);
		XAddress modelAddr = createCommand.getChangedEntity();
		XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
		DemoModelUtil.setupPhonebook(modelAddr, tb);
		xydraServer.executeCommand(tb.build(), null);
		
		assertNotNull(xydraServer.getModelSnapshot(DemoModelUtil.PHONEBOOK_ID));
		
	}
	
	@After
	public void tearDown() {
		XCommand removeCommand = MemoryRepositoryCommand.createRemoveCommand(xydraServer
		        .getRepositoryAddress(), XCommand.FORCED, DemoModelUtil.PHONEBOOK_ID);
		long result = xydraServer.executeCommand(removeCommand, null);
		assert result != XCommand.FAILED;
	}
	
}
