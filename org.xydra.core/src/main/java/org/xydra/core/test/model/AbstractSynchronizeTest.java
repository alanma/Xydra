package org.xydra.core.test.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xydra.core.X;
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
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.impl.memory.MemoryFieldCommand;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.test.ChangeRecorder;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.core.value.XValue;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlCommand;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;


abstract public class AbstractSynchronizeTest extends TestCase {
	
	private static final XID ACTOR_ID = X.getIDProvider().fromString("tester");
	
	private XRepository localRepo;
	private XModel remoteModel;
	private XModel localModel;
	
	@Override
	@Before
	public void setUp() {
		
		this.localRepo = X.createMemoryRepository();
		
		assertFalse(this.localRepo.hasModel(DemoModelUtil.PHONEBOOK_ID));
		
		// create two identical phonebook models
		this.remoteModel = new MemoryModel(DemoModelUtil.PHONEBOOK_ID);
		DemoModelUtil.setupPhonebook(this.remoteModel);
		DemoModelUtil.addPhonebookModel(this.localRepo);
		this.localModel = this.localRepo.getModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(this.localModel);
		
		assertTrue(XX.equalState(this.localModel, this.remoteModel));
		
	}
	
	@Override
	@After
	public void tearDown() {
		this.localRepo.removeModel(ACTOR_ID, DemoModelUtil.PHONEBOOK_ID);
	}
	
	@Test
	public void testModelRollback() {
		
		// make some additional changes to the local model
		makeAdditionalChanges(this.localModel);
		
		// now rollback the second model and compare the models
		this.localModel.rollback(this.remoteModel.getRevisionNumber());
		
		assertEquals(this.remoteModel.getRevisionNumber(), this.localModel.getRevisionNumber());
		
		assertTrue(XX.equalState(this.remoteModel, this.localModel));
		
	}
	
	private void makeAdditionalChanges(XModel model) {
		
		assertNotNull(model.createObject(ACTOR_ID, X.getIDProvider().createUniqueID()));
		
		assertTrue(model.removeObject(ACTOR_ID, DemoModelUtil.JOHN_ID));
		
		assertNotNull(model.getObject(DemoModelUtil.PETER_ID).createField(ACTOR_ID,
		        X.getIDProvider().createUniqueID()));
		
		XTransactionBuilder tb = new XTransactionBuilder(model.getAddress());
		XID objId = X.getIDProvider().createUniqueID();
		tb.addObject(model.getAddress(), XCommand.SAFE, objId);
		XAddress objAddr = XX.resolveObject(model.getAddress(), objId);
		tb.addField(objAddr, XCommand.SAFE, X.getIDProvider().createUniqueID());
		assertTrue(model.executeTransaction(ACTOR_ID, tb.build()) >= 0);
		
		assertTrue(model.removeObject(ACTOR_ID, DemoModelUtil.CLAUDIA_ID));
		
	}
	
	@Test
	public void testModelSynchronize() {
		
		XAddress johnAddr = XX.resolveObject(this.localModel.getAddress(), DemoModelUtil.JOHN_ID);
		
		long lastRevision = this.localModel.getRevisionNumber();
		assertEquals(lastRevision, this.remoteModel.getRevisionNumber());
		
		assertTrue(XX.equalTree(this.remoteModel, this.localModel));
		
		// Add some remote changes.
		makeAdditionalChanges(this.remoteModel);
		
		List<XEvent> remoteChanges = this.remoteModel.getChangeLog()
		        .getAllEventsAfter(lastRevision);
		
		XID newObjectId = X.getIDProvider().fromString("cookiemonster");
		XID newFieldId = X.getIDProvider().fromString("cookies");
		XAddress newObjectAddr = XX.resolveObject(this.localModel.getAddress(), newObjectId);
		XAddress newFieldAddr = XX.resolveField(newObjectAddr, newFieldId);
		XValue newValue1 = X.getValueFactory().createStringValue("chocolate chip");
		XValue newValue2 = X.getValueFactory().createStringValue("almond");
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
		
		List<XCommand> localChanges = new ArrayList<XCommand>();
		localChanges.add(createObject);
		localChanges.add(createField);
		localChanges.add(setValue1);
		localChanges.add(setValue2);
		localChanges.add(removeField);
		localChanges.add(removePeter);
		localChanges.add(removeJohnSafe);
		localChanges.add(removeJohnForced);
		
		XModel checkModel = new MemoryModel(DemoModelUtil.PHONEBOOK_ID);
		DemoModelUtil.setupPhonebook(checkModel);
		
		// apply the commands locally
		for(XCommand command : localChanges) {
			long result = 0;
			checkModel.executeCommand(ACTOR_ID, fix(command));
			assertTrue("command: " + fix(command), result >= 0 || result == XCommand.NOCHANGE);
			result = this.localModel.executeCommand(ACTOR_ID, command);
			assertTrue("command: " + command, result >= 0 || result == XCommand.NOCHANGE);
		}
		
		List<XEvent> events = ChangeRecorder.record(this.localModel);
		
		for(int i = 0; i < remoteChanges.size(); i++) {
			remoteChanges.set(i, fix(remoteChanges.get(i)));
		}
		
		long[] results = this.localModel.synchronize(remoteChanges, lastRevision, ACTOR_ID,
		        localChanges);
		
		assertEquals(localChanges.size(), results.length);
		assertTrue(results[0] >= 0); // createObject
		assertTrue(results[1] >= 0); // createField
		assertTrue(results[2] >= 0); // setValue1
		assertTrue(results[3] >= 0); // setValue2
		assertTrue(results[4] >= 0); // removeField
		assertEquals(XCommand.FAILED, results[5]); // removePeter
		assertEquals(XCommand.FAILED, results[6]); // removeJohnSafe
		assertEquals(XCommand.NOCHANGE, results[7]); // removeJohnForced
		
		assertEquals(createObject, localChanges.get(0));
		assertEquals(createField, localChanges.get(1));
		assertEquals(setValue1.getRevisionNumber() + remoteChanges.size(),
		        ((XFieldCommand)localChanges.get(2)).getRevisionNumber());
		assertEquals(setValue2, localChanges.get(3));
		assertEquals(removeField.getRevisionNumber() + remoteChanges.size(),
		        ((XObjectCommand)localChanges.get(4)).getRevisionNumber());
		assertEquals(removePeter, localChanges.get(5));
		assertEquals(removeJohnSafe, localChanges.get(6));
		assertEquals(removeJohnForced, localChanges.get(7));
		
		// apply the commands remotely
		assertTrue(this.remoteModel.executeCommand(ACTOR_ID, fix(localChanges.get(0))) >= 0);
		assertTrue(this.remoteModel.executeCommand(ACTOR_ID, fix(localChanges.get(1))) >= 0);
		assertTrue(this.remoteModel.executeCommand(ACTOR_ID, fix(localChanges.get(2))) >= 0);
		assertTrue(this.remoteModel.executeCommand(ACTOR_ID, fix(localChanges.get(3))) >= 0);
		assertTrue(this.remoteModel.executeCommand(ACTOR_ID, fix(localChanges.get(4))) >= 0);
		assertEquals(XCommand.FAILED, this.remoteModel.executeCommand(ACTOR_ID, fix(localChanges
		        .get(5))));
		assertEquals(XCommand.FAILED, this.remoteModel.executeCommand(ACTOR_ID, fix(localChanges
		        .get(6))));
		// removeJohnForced not sent as it was NOCHANGE already
		
		assertTrue(XX.equalState(this.remoteModel, this.localModel));
		
		// check that there are enough but no redundant events sent
		for(XEvent event : events) {
			
			assertFalse(XX.equalsOrContains(johnAddr, event.getChangedEntity()));
			assertFalse(XX.equalsOrContains(newObjectAddr, event.getChangedEntity()));
			
			if(event instanceof XModelEvent) {
				XModelEvent me = (XModelEvent)event;
				if(me.getChangeType() == ChangeType.ADD) {
					assertFalse(checkModel.hasObject(me.getObjectID()));
					checkModel.createObject(ACTOR_ID, me.getObjectID());
				} else {
					assertTrue(checkModel.hasObject(me.getObjectID()));
					assertTrue(checkModel.getObject(me.getObjectID()).isEmpty());
					checkModel.removeObject(ACTOR_ID, me.getObjectID());
				}
			} else if(event instanceof XObjectEvent) {
				XObjectEvent oe = (XObjectEvent)event;
				XObject obj = checkModel.getObject(oe.getObjectID());
				assertNotNull(obj);
				if(oe.getChangeType() == ChangeType.ADD) {
					assertFalse(obj.hasField(oe.getFieldID()));
					obj.createField(ACTOR_ID, oe.getFieldID());
				} else {
					assertTrue(obj.hasField(oe.getFieldID()));
					assertTrue(obj.getField(oe.getFieldID()).isEmpty());
					obj.removeField(ACTOR_ID, oe.getFieldID());
				}
			} else if(event instanceof XFieldEvent) {
				XFieldEvent fe = (XFieldEvent)event;
				XObject obj = checkModel.getObject(fe.getObjectID());
				assertNotNull(obj);
				XField fld = obj.getField(fe.getFieldID());
				assertNotNull(fld);
				assertEquals(fld.getValue(), fe.getOldValue());
				fld.setValue(ACTOR_ID, fe.getNewValue());
			}
			
		}
		assertTrue(XX.equalTree(this.localModel, checkModel));
		
		// check the change log
		List<XEvent> remoteHistory = this.remoteModel.getChangeLog().getAllEventsAfter(0);
		List<XEvent> localHistory = this.localModel.getChangeLog().getAllEventsAfter(0);
		
		assertEquals(remoteHistory.size(), localHistory.size());
		
		for(int i = 0; i < remoteHistory.size(); i++) {
			XEvent remote = fix(remoteHistory.get(i));
			XEvent local = localHistory.get(i);
			if(!(remote instanceof XTransactionEvent)) {
				assertEquals(remote, local);
			} else {
				// FIXME the order of events in a transaction may differ
			}
		}
		
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
