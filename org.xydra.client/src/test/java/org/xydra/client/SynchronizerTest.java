package org.xydra.client;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.client.impl.direct.DirectChangesService;
import org.xydra.client.impl.direct.DirectDataService;
import org.xydra.client.sync.XSynchronizer;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.model.session.impl.arm.ArmProtectedRepository;
import org.xydra.store.XydraStore;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;
import org.xydra.store.access.impl.memory.MemoryGroupDatabase;


/**
 * Test for {@link XSynchronizer}.
 * 
 * TODO remove this once the store-based implementation in core works and there
 * is a {@link XydraStore} network implementation
 * 
 * @author dscharrer
 * 
 */
@Deprecated
@Ignore
public class SynchronizerTest extends TestCase {
	
	private XDataService dataService;
	private XChangesService changesService;
	
	public SynchronizerTest() {
		
		// Setup a "server" repository.
		XID actorId = XX.toId("TestActor");
		XRepository repo = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo);
		XGroupDatabaseWithListeners groups = new MemoryGroupDatabase();
		
		XAuthorisationManager arm = new MemoryAuthorisationManager(groups);
		arm.getAuthorisationDatabase().setAccess(actorId, repo.getAddress(), XA.ACCESS_READ, true);
		arm.getAuthorisationDatabase().setAccess(actorId, repo.getAddress(), XA.ACCESS_WRITE, true);
		XProtectedRepository pr = new ArmProtectedRepository(repo, arm, actorId);
		this.dataService = new DirectDataService(pr);
		this.changesService = new DirectChangesService(pr);
		
	}
	
	static class TestCallback implements XLocalChangeCallback {
		
		boolean applied = false;
		
		@Override
        public void onFailure() {
			fail("a command failed to apply remotely");
		}
		
		public void failedPost() {
			fail("should never be reached as we die in failed() already");
		}
		
		@Override
        synchronized public void onSuccess(long revision) {
			
			assertFalse("double apply detected", this.applied);
			
			this.applied = true;
			notify();
		}
	}
	
	@Test
	public void testSynchronizer() {
		
		final TestCallback c1 = new TestCallback();
		final TestCallback c2 = new TestCallback();
		
		Callback<XModel> loadCallback = new Callback<XModel>() {
			
			@Override
            public void onSuccess(XModel model) {
				
				XSynchronizer sync = new XSynchronizer(model, SynchronizerTest.this.changesService);
				
				// don't write model directly or changes will not be
				// synchronized, so only pass the XModel as read-only XBaseModel
				testCommands(sync, model);
				
			}
			
			private void testCommands(XSynchronizer sync, XModel model) {
				
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
				
			}
			
			@Override
            public void onFailure(Throwable error) {
				fail("could not load the initial model: " + error);
			}
			
		};
		
		this.dataService.getModel(DemoModelUtil.PHONEBOOK_ID, loadCallback);
		
		waitForSuccess(c1);
		waitForSuccess(c2);
		
	}
	
	private void waitForSuccess(final TestCallback c1) {
		
		long time = System.currentTimeMillis();
		synchronized(c1) {
			while(!c1.applied) {
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