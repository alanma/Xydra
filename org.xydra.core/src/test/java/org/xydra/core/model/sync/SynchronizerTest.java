package org.xydra.core.model.sync;

import junit.framework.TestCase;

import org.junit.Test;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XSynchronizationCallback;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.memory.AllowAllStore;
import org.xydra.store.impl.memory.MemoryNoAccessRightsNoBatchNoAsyncStore;
import org.xydra.store.impl.memory.XydraNoAccessRightsNoBatchNoAsyncStore;


/**
 * Test for {@link XSynchronizer}.
 * 
 * @author dscharrer
 * 
 *         TODO remove this once the store-based implementation in core works
 */
public class SynchronizerTest extends TestCase {
	
	protected static final XID ACTOR_TESTER = XX.toId("tester");
	protected static final String PSW_TESTER = "password"; // TODO where to get
														   // this?
	
	private final XydraNoAccessRightsNoBatchNoAsyncStore bs;
	private final XydraStore store;
	
	public SynchronizerTest() {
		
		this.bs = new MemoryNoAccessRightsNoBatchNoAsyncStore(XX.toId("remote"));
		
		this.store = new AllowAllStore(this.bs);
		
		XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(
		        XX.toAddress(this.bs.getRepositoryId(), null, null, null), XCommand.SAFE,
		        DemoModelUtil.PHONEBOOK_ID);
		assertTrue(this.bs.executeCommand(ACTOR_TESTER, createCommand) >= 0);
		XAddress modelAddr = createCommand.getChangedEntity();
		XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
		DemoModelUtil.setupPhonebook(modelAddr, tb);
		XTransaction trans = tb.build();
		// Apply events individually so there is something in the change log to
		// test
		for(XAtomicCommand ac : trans) {
			assertTrue(this.bs.executeCommand(ACTOR_TESTER, ac) >= 0);
		}
		
	}
	
	static class TestCallback implements XSynchronizationCallback {
		
		boolean applied = false;
		boolean failed = false;
		
		synchronized public void failed() {
			
			assertFalse("double fail detected", this.failed);
			assertFalse("fail after apply detected", this.applied);
			
			this.failed = true;
			notify();
		}
		
		synchronized public void applied(long revision) {
			
			assertFalse("double apply detected", this.applied);
			assertFalse("apply after fail detected", this.failed);
			
			this.applied = true;
			notify();
		}
	}
	
	@Test
	public void testSynchronizer() {
		
		XBaseModel modelSnapshot = this.bs.getModelSnapshot(XX.toAddress(this.bs.getRepositoryId(),
		        DemoModelUtil.PHONEBOOK_ID, null, null));
		assertNotNull(modelSnapshot);
		// TODO there should be a better way to get a proper XModel from an
		// XBaseModel
		XModel model = XCopyUtils.copyModel(ACTOR_TESTER, PSW_TESTER, modelSnapshot);
		
		XSynchronizer sync = new XSynchronizer(model, this.store);
		
		// don't write model directly or changes will not be
		// synchronized, so only pass the XModel as read-only XBaseModel
		testCommands(sync, model);
		
	}
	
	private void testCommands(XSynchronizer sync, XModel model) {
		
		final TestCallback c1 = new TestCallback();
		final TestCallback c2 = new TestCallback();
		
		// Create a command manually.
		XCommand command = MemoryModelCommand.createAddCommand(model.getAddress(), false,
		        XX.toId("Frank"));
		
		// Apply the command locally.
		model.executeCommand(command, c1);
		
		// Now synchronize with the server.
		sync.synchronize();
		
		// command may not be applied remotely yet!
		
		// We don't have to create all commands manually but can use a
		// ChangedModel.
		// TODO this is problematic with multithreading!
		ChangedModel writeable = new ChangedModel(model);
		
		// Make modifications to the changed model.
		writeable.getObject(DemoModelUtil.JOHN_ID).createField(XX.toId("newField"));
		writeable.removeObject(DemoModelUtil.PETER_ID);
		
		// Create the command(s) describing the changes made to the
		// ChangedModel.
		XTransactionBuilder tb = new XTransactionBuilder(model.getAddress());
		tb.applyChanges(writeable);
		XCommand autoCommand = tb.buildCommand();
		
		// Now apply the command locally. It should be automatically
		// sent to the server.
		model.executeCommand(autoCommand, c2);
		
		sync.synchronize();
		
		// both commands may still not be applied remotely
		
		waitForSuccess(c1);
		waitForSuccess(c2);
		
	}
	
	private void waitForSuccess(final TestCallback c1) {
		
		long time = System.currentTimeMillis();
		synchronized(c1) {
			while(!c1.applied) {
				
				if(c1.failed) {
					fail("a command failed to apply remotely");
				}
				
				assertFalse("timeout waiting for command to apply", System.currentTimeMillis()
				        - time > 1000);
				try {
					c1.wait(1100);
				} catch(InterruptedException e) {
					// ignore
				}
			}
		}
	}
	
}
