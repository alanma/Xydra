package org.xydra.client.test;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.client.Callback;
import org.xydra.client.XChangesService;
import org.xydra.client.XDataService;
import org.xydra.client.sync.XCommandCallback;
import org.xydra.client.sync.XSynchronizer;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XModel;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.test.DemoModelUtil;


/**
 * Abstract test for {@link XSynchronizer} and thereby implicitly also
 * {@link XChangesService} and the server. Should be overwritten for each
 * {@link XChangesService} / {@link XDataService} implementation.
 * 
 * @author dscharrer
 * 
 */
public abstract class SynchronizerTest {
	
	private static final String PREFIX_DATA = "/data";
	private static final String PREFIX_CHANGES = "/changes";
	
	private static XDataService dataService;
	private static XChangesService changesService;
	
	@BeforeClass
	public static void init() {
		
		// TODO start server and add phonebook model
		
	}
	
	@Before
	public void setUp() {
		
		String serverAddress = "http://localhost:8888/xydra";
		dataService = getDataService(serverAddress + PREFIX_DATA);
		changesService = getChangesService(serverAddress + PREFIX_CHANGES);
		
	}
	
	abstract XChangesService getChangesService(String address);
	
	abstract XDataService getDataService(String address);
	
	@Test
	public void testSynchronizer() {
		
		final XCommandCallback cc = new XCommandCallback() {
			
			protected int appliedCounter = 0;
			
			public void failed() {
				fail("a command failed to apply remotely");
			}
			
			public void failedPost() {
				fail("should never be reached as we die in failed() already");
			}
			
			public void applied(long revision) {
				// command applied remotely with the given revision
				this.appliedCounter++;
			}
		};
		
		Callback<XModel> loadCallback = new Callback<XModel>() {
			
			public void onSuccess(XModel model) {
				
				XSynchronizer sync = new XSynchronizer(model, changesService);
				
				// don't write model directly or changes will not be
				// synchronized, so only pass the XModel as read-only XBaseModel
				testCommands(sync, model);
				
			}
			
			private void testCommands(XSynchronizer sync, XBaseModel model) {
				
				// Create a command manually.
				XCommand command = MemoryModelCommand.createAddCommand(model.getAddress(), false,
				        XX.toId("Frank"));
				
				// Apply the command locally.
				sync.executeCommand(command, cc);
				
				// Now synchronize with the server.
				sync.synchronize();
				
				// command may not be applied yet!
				
				// Manually synchronizing is tedious, so send all commands
				// immediately from now on.
				sync.setAutomaticSynchronize(true);
				
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
				sync.executeCommand(autoCommand, cc);
				
			}
			
			public void onFailure(Throwable error) {
				fail("could not load the initial model: " + error);
			}
			
		};
		
		dataService.getModel(DemoModelUtil.PHONEBOOK_ID, loadCallback);
		
		// TODO check for cc.appliedCounter == 2 after timeout
		
	}
}
