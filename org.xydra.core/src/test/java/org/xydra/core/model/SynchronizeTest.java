package org.xydra.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.ChangeRecorder;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.HasChanged;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XCompareUtils;
import org.xydra.core.XCopyUtils;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.model.impl.memory.SynchronizesChangesImpl;
import org.xydra.core.serialize.SerializedCommand;
import org.xydra.core.serialize.SerializedEvent;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.core.serialize.xml.XmlParser;


/**
 * Test for {@link SynchronizesChangesImpl} ({@link MemoryModel})
 * 
 * @author dscharrer
 * 
 */
public class SynchronizeTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}
	
	protected static void replaySyncEvents(XModel checkModel, List<XEvent> events) {
		
		for(XEvent event : events) {
			
			if(event instanceof XModelEvent) {
				XModelEvent me = (XModelEvent)event;
				if(me.getChangeType() == ChangeType.ADD) {
					assertFalse(checkModel.hasObject(me.getObjectId()));
					checkModel.createObject(me.getObjectId());
				} else {
					assertTrue(checkModel.hasObject(me.getObjectId()));
					assertTrue(checkModel.getObject(me.getObjectId()).isEmpty());
					checkModel.removeObject(me.getObjectId());
				}
			} else if(event instanceof XObjectEvent) {
				XObjectEvent oe = (XObjectEvent)event;
				XObject obj = checkModel.getObject(oe.getObjectId());
				assertNotNull(obj);
				if(oe.getChangeType() == ChangeType.ADD) {
					assertFalse(obj.hasField(oe.getFieldId()));
					obj.createField(oe.getFieldId());
				} else {
					assertTrue(obj.hasField(oe.getFieldId()));
					assertTrue(obj.getField(oe.getFieldId()).isEmpty());
					obj.removeField(oe.getFieldId());
				}
			} else if(event instanceof XReversibleFieldEvent) {
				XReversibleFieldEvent rfe = (XReversibleFieldEvent)event;
				XObject obj = checkModel.getObject(rfe.getObjectId());
				assertNotNull(obj);
				XField fld = obj.getField(rfe.getFieldId());
				assertNotNull(fld);
				assertEquals(fld.getValue(), rfe.getOldValue());
				fld.setValue(rfe.getNewValue());
			} else if(event instanceof XFieldEvent) {
				XFieldEvent fe = (XFieldEvent)event;
				XObject obj = checkModel.getObject(fe.getObjectId());
				assertNotNull(obj);
				XField fld = obj.getField(fe.getFieldId());
				assertNotNull(fld);
				fld.setValue(fe.getNewValue());
			}
			
		}
	}
	
	private XID actorId = XX.toId("AbstractSynchronizeTest");
	private XModel localModel;
	
	private String password = null; // TODO auth: where to get this?
	private XModel remoteModel;
	
	{
		LoggerTestHelper.init();
	}
	
	private XCommand fix(XCommand command) {
		
		XydraOut out = new XmlOut();
		SerializedCommand.serialize(command, out, this.localModel.getAddress());
		
		XydraElement e = new XmlParser().parse(out.getData());
		return SerializedCommand.toCommand(e, this.remoteModel.getAddress());
		
	}
	
	private XEvent fix(XEvent event) {
		
		XydraOut out = new XmlOut();
		SerializedEvent.serialize(event, out, this.remoteModel.getAddress());
		
		XydraElement e = new XmlParser().parse(out.getData());
		return SerializedEvent.toEvent(e, this.localModel.getAddress());
		
	}
	
	private static void makeAdditionalChanges(XModel model) {
		
		assertNotNull(model.createObject(XX.createUniqueId()));
		
		assertTrue(model.removeObject(DemoModelUtil.JOHN_ID));
		
		assertNotNull(model.getObject(DemoModelUtil.PETER_ID).createField(XX.createUniqueId()));
		
		XTransactionBuilder tb = new XTransactionBuilder(model.getAddress());
		XID objId = XX.createUniqueId();
		tb.addObject(model.getAddress(), XCommand.SAFE, objId);
		XAddress objAddr = XX.resolveObject(model.getAddress(), objId);
		tb.addField(objAddr, XCommand.SAFE, XX.createUniqueId());
		assertTrue(model.executeCommand(tb.build()) >= 0);
		
		assertTrue(model.removeObject(DemoModelUtil.CLAUDIA_ID));
		
	}
	
	@Before
	public void setUp() {
		
		// create two identical phonebook models
		XRepository remoteRepo = new MemoryRepository(this.actorId, this.password,
		        XX.toId("remoteRepo"));
		DemoModelUtil.addPhonebookModel(remoteRepo);
		this.remoteModel = remoteRepo.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		// TODO sync: allow to select the state backend
		this.localModel = XCopyUtils.copyModel(this.actorId, this.password, this.remoteModel);
		
		assertTrue(XCompareUtils.equalState(this.localModel, this.remoteModel));
		
	}
	
	@After
	public void tearDown() {
	}
	
	@Test
	public void testModelRollback() {
		
		// make some additional changes to the local model
		makeAdditionalChanges(this.localModel);
		
		// now rollback the second model and compare the models
		this.localModel.rollback(this.remoteModel.getRevisionNumber());
		
		assertEquals(this.remoteModel.getRevisionNumber(), this.localModel.getRevisionNumber());
		
		assertTrue(XCompareUtils.equalState(this.remoteModel, this.localModel));
		
	}
	
	@Test
	public void testModelSynchronize() {
		
		XAddress johnAddr = XX.resolveObject(this.localModel.getAddress(), DemoModelUtil.JOHN_ID);
		
		long lastRevision = this.localModel.getRevisionNumber();
		assertEquals(lastRevision, this.remoteModel.getRevisionNumber());
		
		assertTrue(XCompareUtils.equalState(this.remoteModel, this.localModel));
		
		// add some remote changes
		makeAdditionalChanges(this.remoteModel);
		
		// get the remote changes
		List<XEvent> remoteChanges = new ArrayList<XEvent>();
		Iterator<XEvent> rCIt = this.remoteModel.getChangeLog().getEventsSince(lastRevision + 1);
		while(rCIt.hasNext()) {
			remoteChanges.add(fix(rCIt.next()));
		}
		
		// create a set of local changes
		XID newObjectId = XX.toId("cookiemonster");
		XID newFieldId = XX.toId("cookies");
		XAddress newObjectAddr = XX.resolveObject(this.localModel.getAddress(), newObjectId);
		XAddress newFieldAddr = XX.resolveField(newObjectAddr, newFieldId);
		XValue newValue1 = XV.toValue("chocolate chip");
		XValue newValue2 = XV.toValue("almond");
		XModelCommand createObject = MemoryModelCommand.createAddCommand(
		        this.localModel.getAddress(), false, newObjectId);
		XObjectCommand createField = MemoryObjectCommand.createAddCommand(newObjectAddr, false,
		        newFieldId);
		XFieldCommand setValue1 = MemoryFieldCommand.createAddCommand(newFieldAddr,
		        lastRevision + 2, newValue1);
		XFieldCommand setValue2 = MemoryFieldCommand.createAddCommand(newFieldAddr,
		        XCommand.FORCED, newValue2);
		XObjectCommand removeField = MemoryObjectCommand.createRemoveCommand(newObjectAddr,
		        lastRevision + 4, newFieldId);
		
		XModelCommand removePeter = MemoryModelCommand.createRemoveCommand(this.localModel
		        .getAddress(), this.localModel.getObject(DemoModelUtil.PETER_ID)
		        .getRevisionNumber(), DemoModelUtil.PETER_ID);
		
		XObject john = this.localModel.getObject(DemoModelUtil.JOHN_ID);
		XModelCommand removeJohnSafe = MemoryModelCommand.createRemoveCommand(
		        this.localModel.getAddress(), john.getRevisionNumber(), john.getId());
		XModelCommand removeJohnForced = MemoryModelCommand.createRemoveCommand(
		        this.localModel.getAddress(), XCommand.FORCED, john.getId());
		
		List<XCommand> localChanges = new ArrayList<XCommand>();
		localChanges.add(createObject); // 0
		localChanges.add(createField); // 1
		localChanges.add(setValue1); // 2
		localChanges.add(setValue2); // 3
		localChanges.add(removeField); // 4
		localChanges.add(removePeter); // 5
		localChanges.add(removeJohnSafe); // 6
		localChanges.add(removeJohnForced); // 7
		
		ForTestLocalChangeCallback[] lcc = new ForTestLocalChangeCallback[localChanges.size()];
		
		// create a model identical to localModel to check events sent on sync
		XModel checkModel = XCopyUtils.copyModel(this.actorId, this.password, this.localModel);
		
		// apply the commands locally
		int i = 0;
		for(XCommand command : localChanges) {
			long result = 0;
			result = checkModel.executeCommand(command);
			assertTrue("command: " + fix(command), result >= 0 || result == XCommand.NOCHANGE);
			lcc[i] = new ForTestLocalChangeCallback();
			result = this.localModel.executeCommand(command, lcc[i]);
			assertTrue("command: " + command, result >= 0 || result == XCommand.NOCHANGE);
			i++;
		}
		
		assertEquals(XCommand.NOCHANGE, lcc[7].waitForResult()); // removeJohnForced
		
		// setup listeners
		List<XEvent> events = ChangeRecorder.record(this.localModel);
		HasChanged hc = new HasChanged();
		XObject newObject = this.localModel.getObject(newObjectId);
		newObject.addListenerForFieldEvents(hc);
		newObject.addListenerForObjectEvents(hc);
		
		// synchronize the remoteChanges into localModel
		XEvent[] remoteEvents = remoteChanges.toArray(new XEvent[remoteChanges.size()]);
		
		assert lastRevision == this.localModel.getSynchronizedRevision();
		boolean success = this.localModel.synchronize(remoteEvents);
		assertTrue(success);
		
		XLocalChange[] lc = this.localModel.getLocalChanges();
		
		// check results
		assertEquals(5, lc.length);
		
		assertFalse(lcc[0].hasBeenCalled()); // createObject
		assertFalse(lcc[1].hasBeenCalled()); // createField
		assertFalse(lcc[2].hasBeenCalled()); // setValue1
		assertFalse(lcc[3].hasBeenCalled()); // setValue2
		assertFalse(lcc[4].hasBeenCalled()); // removeField
		assertEquals(XCommand.FAILED, lcc[5].waitForResult()); // removePeter
		assertEquals(XCommand.FAILED, lcc[6].waitForResult()); // removeJohnSafe
		
		// check that commands have been properly modified
		assertEquals(createObject, lc[0].getCommand());
		assertEquals(createField, lc[1].getCommand());
		assertEquals(setValue1.getRevisionNumber() + remoteChanges.size(),
		        ((XFieldCommand)lc[2].getCommand()).getRevisionNumber());
		assertEquals(setValue2, lc[3].getCommand());
		assertEquals(removeField.getRevisionNumber() + remoteChanges.size(),
		        ((XObjectCommand)lc[4].getCommand()).getRevisionNumber());
		
		// apply the commands remotely
		assertTrue(this.remoteModel.executeCommand(fix(lc[0].getCommand())) >= 0);
		assertTrue(this.remoteModel.executeCommand(fix(lc[1].getCommand())) >= 0);
		assertTrue(this.remoteModel.executeCommand(fix(lc[2].getCommand())) >= 0);
		assertTrue(this.remoteModel.executeCommand(fix(lc[3].getCommand())) >= 0);
		assertTrue(this.remoteModel.executeCommand(fix(lc[4].getCommand())) >= 0);
		
		assertTrue(XCompareUtils.equalState(this.remoteModel, this.localModel));
		
		// check that there are enough but no redundant events sent
		for(XEvent event : events) {
			assertFalse(johnAddr.equalsOrContains(event.getChangedEntity()));
			assertFalse(newObjectAddr.equalsOrContains(event.getChangedEntity()));
		}
		replaySyncEvents(checkModel, events);
		assertTrue(XCompareUtils.equalTree(this.localModel, checkModel));
		
		// check the change log
		Iterator<XEvent> remoteHistory = this.remoteModel.getChangeLog().getEventsSince(
		        lastRevision + 1);
		Iterator<XEvent> localHistory = this.localModel.getChangeLog().getEventsSince(
		        lastRevision + 1);
		
		assertEquals(this.remoteModel.getChangeLog().getCurrentRevisionNumber(), this.localModel
		        .getChangeLog().getCurrentRevisionNumber());
		
		while(remoteHistory.hasNext()) {
			assertTrue(localHistory.hasNext());
			XEvent remote = fix(remoteHistory.next());
			XEvent local = localHistory.next();
			assertEquals(remote, local);
		}
		assertFalse(localHistory.hasNext());
		
		// check that listeners are still there
		assertFalse(hc.eventsReceived);
		this.localModel.getObject(newObjectId).createField(newFieldId);
		assertTrue(hc.eventsReceived);
		
	}
	
}
