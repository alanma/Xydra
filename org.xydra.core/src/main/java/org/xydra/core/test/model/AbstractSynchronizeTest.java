package org.xydra.core.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xydra.core.XCompareUtils;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.impl.memory.MemoryFieldCommand;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.LocalChange;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.test.ChangeRecorder;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.core.test.HasChanged;
import org.xydra.core.value.XV;
import org.xydra.core.value.XValue;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlCommand;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;


abstract public class AbstractSynchronizeTest {
	
	private XModel remoteModel;
	private XModel localModel;
	
	private XID actorId = XX.toId("AbstractSynchronizeTest");
	private String password = null; // TODO where to get this?
	
	@Before
	public void setUp() {
		
		// create two identical phonebook models
		XRepository remoteRepo = new MemoryRepository(this.actorId, this.password, XX
		        .toId("remoteRepo"));
		DemoModelUtil.addPhonebookModel(remoteRepo);
		this.remoteModel = remoteRepo.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		// TODO allow to select the state backend
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
	
	private void makeAdditionalChanges(XModel model) {
		
		assertNotNull(model.createObject(XX.createUniqueID()));
		
		assertTrue(model.removeObject(DemoModelUtil.JOHN_ID));
		
		assertNotNull(model.getObject(DemoModelUtil.PETER_ID).createField(XX.createUniqueID()));
		
		XTransactionBuilder tb = new XTransactionBuilder(model.getAddress());
		XID objId = XX.createUniqueID();
		tb.addObject(model.getAddress(), XCommand.SAFE, objId);
		XAddress objAddr = XX.resolveObject(model.getAddress(), objId);
		tb.addField(objAddr, XCommand.SAFE, XX.createUniqueID());
		assertTrue(model.executeCommand(tb.build()) >= 0);
		
		assertTrue(model.removeObject(DemoModelUtil.CLAUDIA_ID));
		
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
		XModelCommand createObject = MemoryModelCommand.createAddCommand(this.localModel
		        .getAddress(), false, newObjectId);
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
		XModelCommand removeJohnSafe = MemoryModelCommand.createRemoveCommand(this.localModel
		        .getAddress(), john.getRevisionNumber(), john.getID());
		XModelCommand removeJohnForced = MemoryModelCommand.createRemoveCommand(this.localModel
		        .getAddress(), XCommand.FORCED, john.getID());
		
		// IMPROVE test callbacks
		List<XCommand> localChanges = new ArrayList<XCommand>();
		localChanges.add(createObject); // 0
		localChanges.add(createField); // 1
		localChanges.add(setValue1); // 2
		localChanges.add(setValue2); // 3
		localChanges.add(removeField); // 4
		localChanges.add(removePeter); // 5
		localChanges.add(removeJohnSafe); // 6
		localChanges.add(removeJohnForced); // 7
		
		// create a model identical to localModel to check events sent on sync
		XModel checkModel = XCopyUtils.copyModel(this.actorId, this.password, this.localModel);
		
		// apply the commands locally
		for(XCommand command : localChanges) {
			long result = 0;
			result = checkModel.executeCommand(command);
			assertTrue("command: " + fix(command), result >= 0 || result == XCommand.NOCHANGE);
			// TODO test callbacks
			result = this.localModel.executeCommand(command);
			assertTrue("command: " + command, result >= 0 || result == XCommand.NOCHANGE);
		}
		
		// setup listeners
		List<XEvent> events = ChangeRecorder.record(this.localModel);
		HasChanged hc = new HasChanged();
		XObject newObject = this.localModel.getObject(newObjectId);
		newObject.addListenerForFieldEvents(hc);
		newObject.addListenerForObjectEvents(hc);
		
		// synchronize the remoteChanges into localModel
		XEvent[] remoteEvents = remoteChanges.toArray(new XEvent[remoteChanges.size()]);
		
		long[] results = this.localModel.synchronize(remoteEvents, lastRevision);
		
		// FIXME this is a hack;
		List<LocalChange> lc = this.localModel.getLocalChanges();
		
		// check results
		assertEquals(7, lc.size());
		assertEquals(lc.size(), results.length);
		assertTrue(results[0] >= 0); // createObject
		assertTrue(results[1] >= 0); // createField
		assertTrue(results[2] >= 0); // setValue1
		assertTrue(results[3] >= 0); // setValue2
		assertTrue(results[4] >= 0); // removeField
		assertEquals(XCommand.FAILED, results[5]); // removePeter
		assertEquals(XCommand.FAILED, results[6]); // removeJohnSafe
		
		// check that commands have been properly modified
		assertEquals(createObject, lc.get(0).command);
		assertEquals(createField, lc.get(1).command);
		assertEquals(setValue1.getRevisionNumber() + remoteChanges.size(), ((XFieldCommand)lc
		        .get(2).command).getRevisionNumber());
		assertEquals(setValue2, lc.get(3).command);
		assertEquals(removeField.getRevisionNumber() + remoteChanges.size(), ((XObjectCommand)lc
		        .get(4).command).getRevisionNumber());
		assertEquals(removePeter, lc.get(5).command);
		assertEquals(removeJohnSafe, lc.get(6).command);
		
		// apply the commands remotely
		assertTrue(this.remoteModel.executeCommand(fix(lc.get(0).command)) >= 0);
		assertTrue(this.remoteModel.executeCommand(fix(lc.get(1).command)) >= 0);
		assertTrue(this.remoteModel.executeCommand(fix(lc.get(2).command)) >= 0);
		assertTrue(this.remoteModel.executeCommand(fix(lc.get(3).command)) >= 0);
		assertTrue(this.remoteModel.executeCommand(fix(lc.get(4).command)) >= 0);
		assertEquals(XCommand.FAILED, this.remoteModel.executeCommand(fix(lc.get(5).command)));
		assertEquals(XCommand.FAILED, this.remoteModel.executeCommand(fix(lc.get(6).command)));
		// removeJohnForced not sent as it was NOCHANGE already
		
		assertTrue(XCompareUtils.equalState(this.remoteModel, this.localModel));
		
		// check that there are enough but no redundant events sent
		for(XEvent event : events) {
			
			assertFalse(johnAddr.equalsOrContains(event.getChangedEntity()));
			assertFalse(newObjectAddr.equalsOrContains(event.getChangedEntity()));
			
			if(event instanceof XModelEvent) {
				XModelEvent me = (XModelEvent)event;
				if(me.getChangeType() == ChangeType.ADD) {
					assertFalse(checkModel.hasObject(me.getObjectID()));
					checkModel.createObject(me.getObjectID());
				} else {
					assertTrue(checkModel.hasObject(me.getObjectID()));
					assertTrue(checkModel.getObject(me.getObjectID()).isEmpty());
					checkModel.removeObject(me.getObjectID());
				}
			} else if(event instanceof XObjectEvent) {
				XObjectEvent oe = (XObjectEvent)event;
				XObject obj = checkModel.getObject(oe.getObjectID());
				assertNotNull(obj);
				if(oe.getChangeType() == ChangeType.ADD) {
					assertFalse(obj.hasField(oe.getFieldID()));
					obj.createField(oe.getFieldID());
				} else {
					assertTrue(obj.hasField(oe.getFieldID()));
					assertTrue(obj.getField(oe.getFieldID()).isEmpty());
					obj.removeField(oe.getFieldID());
				}
			} else if(event instanceof XFieldEvent) {
				XFieldEvent fe = (XFieldEvent)event;
				XObject obj = checkModel.getObject(fe.getObjectID());
				assertNotNull(obj);
				XField fld = obj.getField(fe.getFieldID());
				assertNotNull(fld);
				assertEquals(fld.getValue(), fe.getOldValue());
				fld.setValue(fe.getNewValue());
			}
			
		}
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
	
	private XEvent fix(XEvent event) {
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlEvent.toXml(event, out, this.remoteModel.getAddress());
		
		MiniElement e = new MiniXMLParserImpl().parseXml(out.getXml());
		return XmlEvent.toEvent(e, this.localModel.getAddress());
		
	}
	
	private XCommand fix(XCommand command) {
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlCommand.toXml(command, out, this.localModel.getAddress());
		
		MiniElement e = new MiniXMLParserImpl().parseXml(out.getXml());
		return XmlCommand.toCommand(e, this.remoteModel.getAddress());
		
	}
	
}
