package org.xydra.core.test.model;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.impl.memory.MemoryFieldEvent;
import org.xydra.core.change.impl.memory.MemoryModelEvent;
import org.xydra.core.change.impl.memory.MemoryObjectEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XExecutesTransactions;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.test.ChangeRecorder;
import org.xydra.core.test.HasChanged;
import org.xydra.core.value.XValue;


/**
 * Tests for implementations of {@link XExecutesTransactions}.
 * 
 * @author dscharrer
 */
abstract public class AbstractTransactionTest extends TestCase {
	
	private static final XValue JOHN_ALIAS = X.getValueFactory()
	        .createStringValue("Cookie Monster");
	private static final XValue PETER_PHONE = X.getValueFactory().createStringValue("934-253-2");
	private static final XValue JOHN_PHONE = X.getValueFactory().createStringValue("87589-876");
	
	private static final XID MODEL_ID = X.getIDProvider().fromString("model");
	private static final XID ACTOR_ID = X.getIDProvider().fromString("actor");
	private static final XID JOHN_ID = X.getIDProvider().fromString("john");
	private static final XID PETER_ID = X.getIDProvider().fromString("peter");
	private static final XID PHONE_ID = X.getIDProvider().fromString("phone");
	private static final XID ALIAS_ID = X.getIDProvider().fromString("alias");
	
	private XRepository repo;
	protected XModel model;
	protected XObject john;
	protected XObject peter;
	
	private void equalsIterator(Iterator<XEvent> a, Iterator<XAtomicEvent> b) {
		while(a.hasNext()) {
			assertTrue(b.hasNext());
			assertEquals(a.next(), b.next());
		}
		assertFalse(b.hasNext());
	}
	
	@Override
	@Before
	public void setUp() {
		
		this.repo = X.createMemoryRepository();
		this.model = this.repo.createModel(ACTOR_ID, MODEL_ID);
		this.john = this.model.createObject(ACTOR_ID, JOHN_ID);
		XField johnsPhone = this.john.createField(ACTOR_ID, PHONE_ID);
		johnsPhone.setValue(ACTOR_ID, JOHN_PHONE);
		this.peter = this.model.createObject(ACTOR_ID, PETER_ID);
		XField petersPhone = this.peter.createField(ACTOR_ID, PHONE_ID);
		petersPhone.setValue(ACTOR_ID, PETER_PHONE);
		
	}
	
	@Override
	@After
	public void tearDown() {
		this.repo.removeModel(ACTOR_ID, MODEL_ID);
	}
	
	@Test
	public void testModelTransactionSuccess() {
		
		final long modelRev = this.model.getRevisionNumber();
		final long johnRev = this.john.getRevisionNumber();
		final long johnPhoneRev = this.john.getField(PHONE_ID).getRevisionNumber();
		final long peterRev = this.peter.getRevisionNumber();
		final long peterPhoneRev = this.peter.getField(PHONE_ID).getRevisionNumber();
		
		XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
		// should succeed and remove peter
		XAddress modelAddr = this.model.getAddress();
		tb.removeObject(modelAddr, peterRev, PETER_ID);
		// should succeed and do nothing (peter is already removed)
		tb.removeObject(modelAddr, XCommand.FORCED, PETER_ID);
		// should succeed and add john/alias
		XAddress johnAddr = this.john.getAddress();
		tb.addField(johnAddr, XCommand.SAFE, ALIAS_ID);
		// should succeed and do nothing (john/alias is already there)
		tb.addField(johnAddr, XCommand.FORCED, ALIAS_ID);
		// should succeed and set john/alias to JOHN_ALIAS
		XAddress aliasAddr = XX.resolveField(johnAddr, ALIAS_ID);
		tb.addValue(aliasAddr, XCommand.NEW, JOHN_ALIAS);
		
		// record any changes and check that everything has been changed
		// correctly when the first event is executed
		List<XEvent> received = ChangeRecorder.record(this.model);
		List<XEvent> trans = ChangeRecorder.recordTransactions(this.model);
		
		long result = this.model.executeTransaction(ACTOR_ID, tb.build());
		
		assertEquals(modelRev, result);
		
		// check that everything is correct after the transaction has executed.
		
		assertEquals(modelRev + 1, this.model.getRevisionNumber());
		assertEquals(modelRev + 1, this.john.getRevisionNumber());
		
		assertFalse(this.model.hasObject(PETER_ID));
		
		assertTrue(this.john.hasField(ALIAS_ID));
		assertEquals(modelRev + 1, this.john.getField(ALIAS_ID).getRevisionNumber());
		
		assertEquals(johnPhoneRev, this.john.getField(PHONE_ID).getRevisionNumber());
		
		assertEquals(JOHN_ALIAS, this.john.getField(ALIAS_ID).getValue());
		
		/*-- check received events --*/

		/* check events received for removing peter */

		XAddress peterAddr = XX.resolveObject(modelAddr, PETER_ID);
		XAddress peterPhoneAddr = XX.resolveField(peterAddr, PHONE_ID);
		XEvent removePeterPhoneValue = MemoryFieldEvent.createRemoveEvent(ACTOR_ID, peterPhoneAddr,
		        PETER_PHONE, modelRev, peterRev, peterPhoneRev, true);
		XEvent removePeterPhone = MemoryObjectEvent.createRemoveEvent(ACTOR_ID, peterAddr,
		        PHONE_ID, modelRev, peterRev, peterPhoneRev, true);
		XEvent removePeter = MemoryModelEvent.createRemoveEvent(ACTOR_ID, modelAddr, PETER_ID,
		        modelRev, peterRev, true);
		
		int x1 = received.indexOf(removePeterPhoneValue);
		assertTrue(x1 >= 0);
		int x2 = received.indexOf(removePeterPhone);
		assertTrue(x2 >= 0);
		int x3 = received.indexOf(removePeter);
		assertTrue(x3 >= 0);
		
		assertTrue(x1 < x2);
		assertTrue(x2 < x3);
		
		/* check events received for adding alias to john */

		XEvent addJohnAlias = MemoryObjectEvent.createAddEvent(ACTOR_ID, johnAddr, ALIAS_ID,
		        modelRev, johnRev, true);
		
		int x4 = received.indexOf(addJohnAlias);
		assertTrue(x4 >= 0);
		
		/* check events received for setting john/alias */

		XEvent addJohnAliasValue = MemoryFieldEvent.createAddEvent(ACTOR_ID, aliasAddr, JOHN_ALIAS,
		        modelRev, johnRev, XCommand.NEW, true);
		
		int x5 = received.indexOf(addJohnAliasValue);
		assertTrue(x5 >= 0);
		
		assertTrue(x4 < x5);
		
		// check that there were no more events
		assertEquals(5, received.size());
		
		/* check that a transaction event was sent */

		assertEquals(1, trans.size());
		XTransactionEvent te = (XTransactionEvent)trans.get(0);
		
		assertEquals(received.size(), te.size());
		
		equalsIterator(received.iterator(), te.iterator());
		
	}
	
	@Test
	public void testModelTransactionNoChange() {
		
		final long modelRev = this.model.getRevisionNumber();
		final long johnRev = this.john.getRevisionNumber();
		
		XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
		// should succeed and add john/alias
		XAddress johnAddr = this.john.getAddress();
		tb.addField(johnAddr, XCommand.SAFE, ALIAS_ID);
		// should succeed and do nothing (john/alias is already there)
		tb.addField(johnAddr, XCommand.FORCED, ALIAS_ID);
		// should succeed and set john/alias to JOHN_ALIAS
		XAddress aliasAddr = XX.resolveField(johnAddr, ALIAS_ID);
		tb.addValue(aliasAddr, XCommand.NEW, JOHN_ALIAS);
		// should succeed and reset everything
		tb.removeField(johnAddr, XCommand.NEW, ALIAS_ID);
		
		// record any changes and check that everything has been changed
		// correctly when the first event is executed
		HasChanged hc = HasChanged.listen(this.model);
		
		long result = this.model.executeTransaction(ACTOR_ID, tb.build());
		
		assertEquals(XCommand.NOCHANGE, result);
		
		// check that everything is correct after the transaction has executed.
		
		assertEquals(modelRev, this.model.getRevisionNumber());
		assertEquals(johnRev, this.john.getRevisionNumber());
		
		assertFalse(hc.eventsReceived);
		
	}
	
	@Test
	public void testModelTransactionSingleChange() {
		
		final long modelRev = this.model.getRevisionNumber();
		final long johnRev = this.john.getRevisionNumber();
		
		XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
		// should succeed and add john/alias
		XAddress johnAddr = this.john.getAddress();
		tb.addField(johnAddr, XCommand.SAFE, ALIAS_ID);
		// should succeed and do nothing (john/alias is already there)
		tb.addField(johnAddr, XCommand.FORCED, ALIAS_ID);
		// should succeed and set john/alias to JOHN_ALIAS
		XAddress aliasAddr = XX.resolveField(johnAddr, ALIAS_ID);
		tb.addValue(aliasAddr, XCommand.NEW, JOHN_ALIAS);
		// should succeed and set john/alias to null
		tb.removeValue(aliasAddr, XCommand.NEW);
		
		// record any changes and check that everything has been changed
		// correctly when the first event is executed
		List<XEvent> received = ChangeRecorder.record(this.model);
		List<XEvent> trans = ChangeRecorder.recordTransactions(this.model);
		
		long result = this.model.executeTransaction(ACTOR_ID, tb.build());
		
		assertEquals(modelRev, result);
		
		// check that everything is correct after the transaction has executed.
		
		assertEquals(modelRev + 1, this.model.getRevisionNumber());
		assertEquals(modelRev + 1, this.john.getRevisionNumber());
		
		assertTrue(this.john.hasField(ALIAS_ID));
		assertEquals(modelRev + 1, this.john.getField(ALIAS_ID).getRevisionNumber());
		
		assertEquals(null, this.john.getField(ALIAS_ID).getValue());
		
		/*-- check received events --*/

		/* check events received for adding alias to john */

		XEvent addJohnAlias = MemoryObjectEvent.createAddEvent(ACTOR_ID, johnAddr, ALIAS_ID,
		        modelRev, johnRev, false);
		
		int x4 = received.indexOf(addJohnAlias);
		assertTrue(x4 >= 0);
		
		// check that there were no more events
		assertEquals(1, received.size());
		
		/* check that no transaction event was sent */

		assertTrue(trans.isEmpty());
		
	}
	
	@Test
	public void testModelTransactionFailure() {
		
		long modelRev = this.model.getRevisionNumber();
		long johnRev = this.john.getRevisionNumber();
		long johnsPhoneRev = this.john.getField(PHONE_ID).getRevisionNumber();
		long peterRev = this.model.getObject(PETER_ID).getRevisionNumber();
		long petersPhoneRev = this.model.getObject(PETER_ID).getField(PHONE_ID).getRevisionNumber();
		
		XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
		XAddress modelAddr = this.model.getAddress();
		tb.removeObject(modelAddr, peterRev, PETER_ID);
		XAddress johnAddr = this.john.getAddress();
		tb.addField(johnAddr, XCommand.SAFE, ALIAS_ID);
		XAddress aliasAddr = XX.resolveField(johnAddr, ALIAS_ID);
		tb.addValue(aliasAddr, XCommand.NEW, JOHN_ALIAS);
		// should fail to execute
		tb.removeObject(modelAddr, johnRev - 1, JOHN_ID);
		
		// register listeners so we can check if rollback restores the correct
		// listeners
		HasChanged peterObjectListener = new HasChanged();
		HasChanged peterFieldListener = new HasChanged();
		this.peter.addListenerForObjectEvents(peterObjectListener);
		this.peter.addListenerForFieldEvents(peterFieldListener);
		
		HasChanged hc = HasChanged.listen(this.model);
		
		long result = this.model.executeTransaction(ACTOR_ID, tb.build());
		
		assertEquals(XCommand.FAILED, result);
		
		assertFalse(hc.eventsReceived);
		
		// check that the model is in the same state as before
		
		assertEquals(modelRev, this.model.getRevisionNumber());
		assertEquals(johnRev, this.john.getRevisionNumber());
		
		assertTrue(this.model.hasObject(PETER_ID));
		assertEquals(peterRev, this.model.getObject(PETER_ID).getRevisionNumber());
		XField petersPhone = this.model.getObject(PETER_ID).getField(PHONE_ID);
		assertNotNull(petersPhone);
		assertEquals(petersPhoneRev, petersPhone.getRevisionNumber());
		assertEquals(PETER_PHONE, petersPhone.getValue());
		
		assertFalse(this.john.hasField(ALIAS_ID));
		assertEquals(johnsPhoneRev, this.john.getField(PHONE_ID).getRevisionNumber());
		
		// test if the event listeners are still there
		assertFalse(peterObjectListener.eventsReceived);
		assertFalse(peterFieldListener.eventsReceived);
		XField peterAlias = this.model.getObject(PETER_ID).createField(ACTOR_ID, ALIAS_ID);
		peterAlias.setValue(ACTOR_ID, X.getValueFactory().createStringValue("nomnomnom"));
		assertTrue(peterObjectListener.eventsReceived);
		assertTrue(peterFieldListener.eventsReceived);
		
		// check that the peter we have now and the peter we had before are the
		// same or at least modifications to one affect the other.
		assertTrue(this.peter.hasField(ALIAS_ID));
		this.peter.removeField(ACTOR_ID, ALIAS_ID);
		assertFalse(this.model.getObject(PETER_ID).hasField(ALIAS_ID));
		
	}
	
	@Test
	public void testObjectTransactionSuccess() {
		
		final long modelRev = this.model.getRevisionNumber();
		final long johnRev = this.john.getRevisionNumber();
		final long phoneRev = this.john.getField(PHONE_ID).getRevisionNumber();
		
		XTransactionBuilder tb = new XTransactionBuilder(this.john.getAddress());
		XAddress johnAddr = this.john.getAddress();
		tb.addField(johnAddr, XCommand.SAFE, ALIAS_ID);
		XAddress aliasAddr = XX.resolveField(johnAddr, ALIAS_ID);
		tb.addValue(aliasAddr, XCommand.NEW, JOHN_ALIAS);
		
		// record any changes and check that everything has been changed
		// correctly when the first event is executed
		List<XEvent> received = ChangeRecorder.record(this.model);
		List<XEvent> receivedFromObject = ChangeRecorder.record(this.john);
		List<XEvent> trans = ChangeRecorder.recordTransactions(this.model);
		List<XEvent> transFromObject = ChangeRecorder.recordTransactions(this.john);
		
		long result = this.john.executeTransaction(ACTOR_ID, tb.build());
		
		assertEquals(modelRev, result);
		
		// check that everything is correct after the transaction has executed.
		
		assertEquals(modelRev + 1, this.model.getRevisionNumber());
		assertEquals(modelRev + 1, this.john.getRevisionNumber());
		
		assertTrue(this.john.hasField(ALIAS_ID));
		assertEquals(modelRev + 1, this.john.getField(ALIAS_ID).getRevisionNumber());
		
		assertEquals(phoneRev, this.john.getField(PHONE_ID).getRevisionNumber());
		
		assertEquals(JOHN_ALIAS, this.john.getField(ALIAS_ID).getValue());
		
		/*-- check received events --*/

		/* check events received for adding alias to john */

		XEvent addJohnAlias = MemoryObjectEvent.createAddEvent(ACTOR_ID, johnAddr, ALIAS_ID,
		        modelRev, johnRev, true);
		
		int x1 = received.indexOf(addJohnAlias);
		assertTrue(x1 >= 0);
		
		/* check events received for setting john/alias */

		XEvent addJohnAliasValue = MemoryFieldEvent.createAddEvent(ACTOR_ID, aliasAddr, JOHN_ALIAS,
		        modelRev, johnRev, XCommand.NEW, true);
		
		int x2 = received.indexOf(addJohnAliasValue);
		assertTrue(x2 >= 0);
		
		assertTrue(x1 < x2);
		
		// check that there were no more events
		assertEquals(2, received.size());
		
		/* check that a transaction event was sent */

		assertEquals(1, trans.size());
		XTransactionEvent te = (XTransactionEvent)trans.get(0);
		
		assertEquals(received.size(), te.size());
		
		equalsIterator(received.iterator(), te.iterator());
		
		/* check events received from object */

		assertEquals(received, receivedFromObject);
		assertEquals(trans, transFromObject);
		
	}
	
	@Test
	public void testObjectTransactionNoChange() {
		
		final long modelRev = this.model.getRevisionNumber();
		final long johnRev = this.john.getRevisionNumber();
		
		XAddress johnAddr = this.john.getAddress();
		XTransactionBuilder tb = new XTransactionBuilder(johnAddr);
		// should succeed and add john/alias
		tb.addField(johnAddr, XCommand.SAFE, ALIAS_ID);
		// should succeed and do nothing (john/alias is already there)
		tb.addField(johnAddr, XCommand.FORCED, ALIAS_ID);
		// should succeed and set john/alias to JOHN_ALIAS
		XAddress aliasAddr = XX.resolveField(johnAddr, ALIAS_ID);
		tb.addValue(aliasAddr, XCommand.NEW, JOHN_ALIAS);
		// should succeed and reset everything
		tb.removeField(johnAddr, XCommand.NEW, ALIAS_ID);
		
		// record any changes and check that everything has been changed
		// correctly when the first event is executed
		HasChanged hc = HasChanged.listen(this.model);
		
		long result = this.john.executeTransaction(ACTOR_ID, tb.build());
		
		assertEquals(XCommand.NOCHANGE, result);
		
		// check that everything is correct after the transaction has executed.
		
		assertEquals(modelRev, this.model.getRevisionNumber());
		assertEquals(johnRev, this.john.getRevisionNumber());
		
		assertFalse(hc.eventsReceived);
		
	}
	
	@Test
	public void testObjectTransactionSingleChange() {
		
		final long modelRev = this.model.getRevisionNumber();
		final long johnRev = this.john.getRevisionNumber();
		
		XAddress johnAddr = this.john.getAddress();
		XTransactionBuilder tb = new XTransactionBuilder(johnAddr);
		// should succeed and add john/alias
		tb.addField(johnAddr, XCommand.SAFE, ALIAS_ID);
		// should succeed and do nothing (john/alias is already there)
		tb.addField(johnAddr, XCommand.FORCED, ALIAS_ID);
		// should succeed and set john/alias to JOHN_ALIAS
		XAddress aliasAddr = XX.resolveField(johnAddr, ALIAS_ID);
		tb.addValue(aliasAddr, XCommand.NEW, JOHN_ALIAS);
		// should succeed and set john/alias to null
		tb.removeValue(aliasAddr, XCommand.NEW);
		
		// record any changes and check that everything has been changed
		// correctly when the first event is executed
		List<XEvent> received = ChangeRecorder.record(this.model);
		List<XEvent> receivedFromObject = ChangeRecorder.record(this.john);
		List<XEvent> trans = ChangeRecorder.recordTransactions(this.model);
		List<XEvent> transFromObject = ChangeRecorder.recordTransactions(this.john);
		
		long result = this.john.executeTransaction(ACTOR_ID, tb.build());
		
		assertEquals(modelRev, result);
		
		// check that everything is correct after the transaction has executed.
		
		assertEquals(modelRev + 1, this.model.getRevisionNumber());
		assertEquals(modelRev + 1, this.john.getRevisionNumber());
		
		assertTrue(this.john.hasField(ALIAS_ID));
		assertEquals(modelRev + 1, this.john.getField(ALIAS_ID).getRevisionNumber());
		
		assertEquals(null, this.john.getField(ALIAS_ID).getValue());
		
		/*-- check received events --*/

		/* check events received for adding alias to john */

		XEvent addJohnAlias = MemoryObjectEvent.createAddEvent(ACTOR_ID, johnAddr, ALIAS_ID,
		        modelRev, johnRev, false);
		
		int x4 = received.indexOf(addJohnAlias);
		assertTrue(x4 >= 0);
		
		// check that there were no more events
		assertEquals(1, received.size());
		
		/* check that no transaction event was sent */

		assertTrue(trans.isEmpty());
		
		/* check events received from object */

		assertEquals(received, receivedFromObject);
		assertEquals(trans, transFromObject);
		
	}
	
	@Test
	public void testObjectTransactionFailure() {
		
		long modelRev = this.model.getRevisionNumber();
		long johnRev = this.john.getRevisionNumber();
		long phoneRev = this.john.getField(PHONE_ID).getRevisionNumber();
		long peterRev = this.peter.getRevisionNumber();
		
		XField johnPhone = this.john.getField(PHONE_ID);
		
		XTransactionBuilder tb = new XTransactionBuilder(this.john.getAddress());
		
		XAddress johnAddr = this.john.getAddress();
		tb.addField(johnAddr, XCommand.SAFE, ALIAS_ID);
		tb.removeField(johnAddr, phoneRev, PHONE_ID);
		XAddress aliasAddr = XX.resolveField(johnAddr, ALIAS_ID);
		tb.addValue(aliasAddr, XCommand.NEW, JOHN_ALIAS);
		// should fail to execute
		tb.removeField(johnAddr, phoneRev, PHONE_ID);
		
		HasChanged hc = HasChanged.listen(this.model);
		
		HasChanged phoneListener = new HasChanged();
		johnPhone.addListenerForFieldEvents(phoneListener);
		
		long result = this.john.executeTransaction(ACTOR_ID, tb.build());
		
		assertEquals(XCommand.FAILED, result);
		
		assertFalse(hc.eventsReceived);
		
		// check that the model is in the same state as before
		
		assertEquals(modelRev, this.model.getRevisionNumber());
		assertEquals(johnRev, this.john.getRevisionNumber());
		
		assertTrue(this.model.hasObject(PETER_ID));
		assertEquals(peterRev, this.model.getObject(PETER_ID).getRevisionNumber());
		
		assertFalse(this.john.hasField(ALIAS_ID));
		
		assertTrue(this.john.hasField(PHONE_ID));
		
		XField phone = this.john.getField(PHONE_ID);
		assertNotNull(phone);
		assertEquals(phoneRev, phone.getRevisionNumber());
		assertEquals(JOHN_PHONE, phone.getValue());
		
		// test if the event listeners are still there
		assertFalse(phoneListener.eventsReceived);
		XValue newPhone = X.getValueFactory().createStringValue("0-NEW-PHONE");
		phone.setValue(ACTOR_ID, newPhone);
		assertTrue(phoneListener.eventsReceived);
		
		// check that the phone we have now and the phone we had before are the
		// same or at least modifications to one affect the other.
		assertEquals(newPhone, johnPhone.getValue());
		johnPhone.setValue(ACTOR_ID, null);
		assertNull(phone.getValue());
		
	}
	
	@Test
	public void testFailureDoubleRemove() {
		
		long modelRev = this.model.getRevisionNumber();
		long johnRev = this.john.getRevisionNumber();
		long peterRev = this.peter.getRevisionNumber();
		long petersPhoneRev = this.peter.getField(PHONE_ID).getRevisionNumber();
		
		XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
		XAddress modelAddr = this.model.getAddress();
		tb.removeObject(modelAddr, peterRev, PETER_ID);
		tb.addObject(modelAddr, XCommand.SAFE, PETER_ID);
		XAddress peterAddr = this.peter.getAddress();
		tb.addField(peterAddr, XCommand.SAFE, ALIAS_ID);
		tb.removeObject(modelAddr, XCommand.NEW, PETER_ID);
		// should fail to execute
		tb.removeObject(modelAddr, johnRev - 1, JOHN_ID);
		
		// register listeners so we can check if rollback restores the correct
		// listeners
		HasChanged peterObjectListener = new HasChanged();
		HasChanged peterFieldListener = new HasChanged();
		this.model.getObject(PETER_ID).addListenerForObjectEvents(peterObjectListener);
		this.model.getObject(PETER_ID).addListenerForFieldEvents(peterFieldListener);
		
		HasChanged hc = HasChanged.listen(this.model);
		
		long result = this.model.executeTransaction(ACTOR_ID, tb.build());
		
		assertEquals(XCommand.FAILED, result);
		
		assertFalse(hc.eventsReceived);
		
		// check that the model is in the same state as before
		
		assertEquals(modelRev, this.model.getRevisionNumber());
		assertEquals(johnRev, this.john.getRevisionNumber());
		
		assertTrue(this.model.hasObject(PETER_ID));
		assertEquals(peterRev, this.model.getObject(PETER_ID).getRevisionNumber());
		XField petersPhone = this.model.getObject(PETER_ID).getField(PHONE_ID);
		assertNotNull(petersPhone);
		assertEquals(petersPhoneRev, petersPhone.getRevisionNumber());
		assertEquals(PETER_PHONE, petersPhone.getValue());
		assertFalse(this.model.getObject(PETER_ID).hasField(ALIAS_ID));
		
		// test if the event listeners are still there
		assertFalse(peterObjectListener.eventsReceived);
		assertFalse(peterFieldListener.eventsReceived);
		XField peterAlias = this.model.getObject(PETER_ID).createField(ACTOR_ID, ALIAS_ID);
		peterAlias.setValue(ACTOR_ID, X.getValueFactory().createStringValue("nomnomnom"));
		assertTrue(peterObjectListener.eventsReceived);
		assertTrue(peterFieldListener.eventsReceived);
		
		// check that the peter we have now and the peter we had before are the
		// same or at least modifications to one affect the other.
		assertTrue(this.peter.hasField(ALIAS_ID));
		this.peter.removeField(ACTOR_ID, ALIAS_ID);
		assertFalse(this.model.getObject(PETER_ID).hasField(ALIAS_ID));
		
	}
	
}
