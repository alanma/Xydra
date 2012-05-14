package org.xydra.store;

import org.junit.Test;
import org.xydra.store.impl.delegate.XydraPersistence;


public abstract class AbstractPersistenceTest {
	/*
	 * needs to be instantiated in the @Before methd by implementations of this
	 * test
	 */
	private XydraPersistence persistence;
	
	@Test
	public void testExecuteCommandRepositoryCommandAddType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandRepositoryCommandAddRemove() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandModelCommandAddType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandModelCommandRemoveType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandObjectCommandAddType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandObjectCommandRemoveType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandFieldCommandAddType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandFieldCommandRemoveType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandFieldCommandChangeType() {
		// TODO write this test
	}
	
	@Test
	public void testExecuteCommandSimpleTransaction() {
		// TODO write this test
		// TODO write multiple tests for Transactions, since they are pretty
		// complex
	}
	
	@Test
	public void testGetEvents() {
		/*
		 * TODO write this test - execute some simple (and maybe some complex)
		 * commands & transactions and check if the returned events match
		 */
	}
	
	@Test
	public void testGetManagedModelIds() {
		/*
		 * TODO write this test - add some models and check whether the corret
		 * Ids are returned or not. (take into account that deleting a model
		 * does not necessarily remove its Id!)
		 */
	}
	
	@Test
	public void testGetModelRevision() {
		/*
		 * TODO write this test - add & change some models and check their
		 * revision numbers
		 */
	}
	
	@Test
	public void testGetModelSnapshot() {
		/*
		 * TODO write this test - create a model and execute some commands on
		 * it, manage a separate model in parallel, execute the same commands on
		 * that one and compare the returned model to the self-managed model
		 */
		// TODO check what a "tentative revision" is and write test accordingly
	}
	
	@Test
	public void testGetObjectSnapshot() {
		/*
		 * TODO write this test - just like testgetModelSnapshot, but with
		 * Objects
		 */
	}
	
	@Test
	public void testGetRepositoryId() {
		/*
		 * TODO write this test (is there anything that needs to be tested
		 * here?)
		 */
	}
	
	@Test
	public void testHasManagedModel() {
		// TODO write this test
	}
}
