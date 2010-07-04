package org.xydra.rest.changes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.impl.memory.MemoryFieldCommand;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlCommand;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.rest.AbstractRestApiTest;


/**
 * Test for implementations of {@link IGroupDatabase}.
 * 
 * @author dscharrer
 * 
 */
public class ChangesApiTest extends AbstractRestApiTest {
	
	@BeforeClass
	public static void init() {
		AbstractRestApiTest.init();
		changesapi = apiprefix.resolve("changes/");
	}
	
	private static URI changesapi;
	
	private static enum CommandResult {
		FAILED, NOCHANGE, EXECUTED
	}
	
	private static class CommandResponse {
		
		private final CommandResult result;
		private final List<XEvent> events;
		
		public CommandResponse(CommandResult result, List<XEvent> events) {
			this.result = result;
			this.events = events;
		}
		
	}
	
	private CommandResponse sendCommand(XCommand command, long since) throws IOException {
		
		XAddress target = command.getTarget();
		
		String name = "";
		if(target.getModel() != null) {
			name += target.getModel().toString();
		}
		if(since != Long.MAX_VALUE) {
			name += "?since=" + since;
		}
		
		URL targetUrl = name.equals("") ? changesapi.toURL() : changesapi.resolve(name).toURL();
		
		XAddress context = target;
		if(target.getObject() != null) {
			context = X.getIDProvider().fromComponents(target.getRepository(), target.getModel(),
			        null, null);
		}
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlCommand.toXml(command, out, context);
		
		HttpURLConnection c = (HttpURLConnection)targetUrl.openConnection();
		setLoginDetails(c);
		c.setDoOutput(true);
		c.setRequestMethod("POST");
		c.setRequestProperty("Content-Type", "application/xml");
		Writer w = new OutputStreamWriter(c.getOutputStream(), "UTF-8");
		w.write(out.getXml());
		w.flush();
		c.connect();
		
		int response = c.getResponseCode();
		
		if(response == HttpURLConnection.HTTP_NOT_FOUND) {
			return null;
		}
		
		assertTrue(response == HttpURLConnection.HTTP_CONFLICT
		        || response == HttpURLConnection.HTTP_CREATED
		        || response == (target.getModel() == null ? HttpURLConnection.HTTP_NO_CONTENT
		                : HttpURLConnection.HTTP_OK));
		
		CommandResult result = CommandResult.NOCHANGE;
		if(response == HttpURLConnection.HTTP_CONFLICT) {
			result = CommandResult.FAILED;
		} else if(response == HttpURLConnection.HTTP_CREATED) {
			result = CommandResult.EXECUTED;
		}
		
		List<XEvent> events = null;
		if(target.getModel() != null
		        && (response == HttpURLConnection.HTTP_CREATED || since != Long.MAX_VALUE)) {
			
			assertEquals("application/xml", c.getContentType());
			
			String data = readAll((InputStream)c.getContent());
			
			try {
				MiniElement eventsElement = new MiniXMLParserImpl().parseXml(data);
				events = XmlEvent.toEventList(eventsElement, context);
			} catch(IllegalArgumentException iae) {
				fail(iae.getMessage());
				throw new RuntimeException();
			}
			
		} else {
			
			assertTrue(c.getContentLength() <= 0);
			
		}
		
		return new CommandResponse(result, events);
	}
	
	private List<XEvent> getEvents(XAddress addr, long since, long until) throws IOException {
		
		String name = addr.getModel().toString();
		if(addr.getObject() != null) {
			name += "/" + addr.getObject().toString();
		}
		if(since > 0) {
			name += "?since=" + since;
			if(until != Long.MAX_VALUE) {
				name += "&until=" + until;
			}
		} else if(until != Long.MAX_VALUE) {
			name += "?until=" + until;
		}
		
		URL modelUrl = changesapi.resolve(name).toURL();
		
		MiniElement eventsElement = loadXml(modelUrl);
		if(eventsElement == null) {
			return null;
		}
		
		try {
			return XmlEvent.toEventList(eventsElement, addr);
		} catch(IllegalArgumentException iae) {
			fail(iae.getMessage());
			throw new RuntimeException();
		}
		
	}
	
	public void testGetChanges(final XAddress addr, long since, long until) throws IOException {
		
		List<XEvent> retrievedEvents = getEvents(addr, since, until);
		assertNotNull(retrievedEvents);
		// try to read events
		Iterator<XEvent> it = retrievedEvents.iterator();
		while(it.hasNext()) {
			XEvent event = it.next();
			if(addr.getObject() == null) {
				assertNotNull(event);
			}
		}
		
		XModel originalModel = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(originalModel);
		
		Iterator<XEvent> originalEventIt = originalModel.getChangeLog().getEventsBetween(since,
		        until);
		Iterator<XEvent> retrievedEventIt = retrievedEvents.iterator();
		
		if(addr.getObject() != null) {
			originalEventIt = new AbstractTransformingIterator<XEvent,XEvent>(originalEventIt) {
				@Override
				public XEvent transform(XEvent in) {
					if(!XX.equalsOrContains(addr, in.getTarget())) {
						return null;
					}
					return in;
				}
			};
		}
		
		checkEvents(originalEventIt, retrievedEventIt);
		
	}
	
	private XAddress model() {
		return X.getIDProvider().fromComponents(repo.getID(), DemoModelUtil.PHONEBOOK_ID, null,
		        null);
	}
	
	private XAddress object() {
		return X.getIDProvider().fromComponents(repo.getID(), DemoModelUtil.PHONEBOOK_ID,
		        DemoModelUtil.JOHN_ID, null);
	}
	
	@Test
	public void testGetAllChanges() throws IOException {
		
		testGetChanges(model(), 0, Long.MAX_VALUE);
		
	}
	
	@Test
	public void testGetChangesSince() throws IOException {
		
		testGetChanges(model(), 3, Long.MAX_VALUE);
		
	}
	
	@Test
	public void testGetChangesUntil() throws IOException {
		
		long now = repo.getModel(DemoModelUtil.PHONEBOOK_ID).getRevisionNumber();
		testGetChanges(model(), 0, now - 3);
		
	}
	
	@Test
	public void testGetChangesBetween() throws IOException {
		
		long now = repo.getModel(DemoModelUtil.PHONEBOOK_ID).getRevisionNumber();
		testGetChanges(model(), 3, now - 3);
		
	}
	
	@Test
	public void testGetAllChangesObject() throws IOException {
		
		testGetChanges(object(), 0, Long.MAX_VALUE);
		
	}
	
	@Test
	public void testGetChangesSinceObject() throws IOException {
		
		testGetChanges(object(), 3, Long.MAX_VALUE);
		
	}
	
	@Test
	public void testGetChangesUntilObject() throws IOException {
		
		long now = repo.getModel(DemoModelUtil.PHONEBOOK_ID).getRevisionNumber();
		testGetChanges(object(), 0, now - 3);
		
	}
	
	@Test
	public void testGetChangesBetweenObject() throws IOException {
		
		long now = repo.getModel(DemoModelUtil.PHONEBOOK_ID).getRevisionNumber();
		testGetChanges(object(), 3, now - 3);
		
	}
	
	private void checkEvents(Iterator<XEvent> originalEventIt, Iterator<XEvent> retrievedEventIt) {
		
		while(originalEventIt.hasNext()) {
			
			XEvent originalEvent = originalEventIt.next();
			
			assertTrue("expected event: " + originalEvent, retrievedEventIt.hasNext());
			
			XEvent retrievedEvent = retrievedEventIt.next();
			
			assertEquals(originalEvent, retrievedEvent);
			
		}
		
		if(retrievedEventIt.hasNext()) {
			fail("unexpected event: " + retrievedEventIt.next());
		}
		
	}
	
	@Test
	public void testGetModelChangesBadId() throws IOException {
		
		URL url = changesapi.resolve(BAD_ID).toURL();
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, c.getResponseCode());
		
	}
	
	@Test
	public void testGetMoreComponentsUrl() throws IOException {
		
		URL url = changesapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI() + "/").resolve(
		        DemoModelUtil.JOHN_ID.toURI() + "/").resolve(DemoModelUtil.ALIASES_ID.toURI())
		        .toURL();
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, c.getResponseCode());
		
	}
	
	@Test
	public void testGetChangesMissingModel() throws IOException {
		
		URL url = changesapi.resolve(MISSING_ID).toURL();
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, c.getResponseCode());
		
	}
	
	@Test
	public void testGetChangesMissingObject() throws IOException {
		
		URL url = changesapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI() + "/").resolve(MISSING_ID)
		        .toURL();
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, c.getResponseCode());
		
	}
	
	/**
	 * Test sending a command by also executing it locally and comparing the
	 * local and remote results.
	 */
	public void testSendCommand(XCommand command) throws IOException {
		
		XAddress target = command.getTarget();
		CommandResponse resp = sendCommand(command, Long.MAX_VALUE);
		
		long result = repo.executeCommand(ACTOR_TESTER, command);
		
		if(result == XCommand.FAILED) {
			assertEquals(CommandResult.FAILED, resp.result);
		} else if(result == XCommand.NOCHANGE) {
			assertEquals(CommandResult.NOCHANGE, resp.result);
		} else {
			assertEquals(CommandResult.EXECUTED, resp.result);
		}
		
		if(target.getModel() != null) {
			
			if(result >= 0) {
				assertNotNull(resp.events);
				
				assertEquals(1, resp.events.size());
				
				XModel model = repo.getModel(target.getModel());
				assertNotNull(model);
				
				XEvent event = model.getChangeLog().getEventAt(result);
				assertNotNull(event);
				
				assertEquals(event, resp.events.get(0));
				
			} else {
				assertNull(resp.events);
			}
			
		} else {
			assertNull(resp.events);
		}
		
		XID modelId = command instanceof XRepositoryCommand ? ((XRepositoryCommand)command)
		        .getModelID() : target.getModel();
		
		XModel localModel = repo.getModel(modelId);
		XModel remoteModel = getRemoteModel(modelId);
		assertTrue(XX.equalState(localModel, remoteModel));
		
	}
	
	@Test
	public void testCreateModel() throws IOException {
		
		XCommand command = MemoryRepositoryCommand.createAddCommand(repo.getAddress(),
		        XCommand.SAFE, NEW_ID);
		
		testSendCommand(command);
		
		// cleanup
		deleteResource(dataapi.resolve(NEW_ID.toURI()).toURL());
		
	}
	
	@Test
	public void testCreateExistingModelSafe() throws IOException {
		
		XCommand command = MemoryRepositoryCommand.createAddCommand(repo.getAddress(),
		        XCommand.SAFE, DemoModelUtil.PHONEBOOK_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testCreateExistingModelForced() throws IOException {
		
		XCommand command = MemoryRepositoryCommand.createAddCommand(repo.getAddress(),
		        XCommand.FORCED, DemoModelUtil.PHONEBOOK_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testCreateObject() throws IOException {
		
		XAddress modelAddr = XX.resolveModel(repo.getAddress(), DemoModelUtil.PHONEBOOK_ID);
		
		XCommand command = MemoryModelCommand.createAddCommand(modelAddr, XCommand.SAFE, NEW_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testCreateObjectMissingModel() throws IOException {
		
		XAddress modelAddr = XX.resolveModel(repo.getAddress(), NEW_ID);
		
		XCommand command = MemoryModelCommand.createAddCommand(modelAddr, XCommand.SAFE, NEW_ID);
		
		assertNull(sendCommand(command, Long.MAX_VALUE));
		
	}
	
	@Test
	public void testCreateExistingObjectSafe() throws IOException {
		
		XAddress modelAddr = XX.resolveModel(repo.getAddress(), DemoModelUtil.PHONEBOOK_ID);
		
		XCommand command = MemoryModelCommand.createAddCommand(modelAddr, XCommand.SAFE,
		        DemoModelUtil.JOHN_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testCreateExistingObjectForced() throws IOException {
		
		XAddress modelAddr = XX.resolveModel(repo.getAddress(), DemoModelUtil.PHONEBOOK_ID);
		
		XCommand command = MemoryModelCommand.createAddCommand(modelAddr, XCommand.FORCED,
		        DemoModelUtil.JOHN_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testCreateField() throws IOException {
		
		XAddress modelAddr = XX.resolveModel(repo.getAddress(), DemoModelUtil.PHONEBOOK_ID);
		XAddress objectAddr = XX.resolveObject(modelAddr, DemoModelUtil.JOHN_ID);
		
		XCommand command = MemoryObjectCommand.createAddCommand(objectAddr, XCommand.SAFE, NEW_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testCreateExistingFieldSafe() throws IOException {
		
		XAddress modelAddr = XX.resolveModel(repo.getAddress(), DemoModelUtil.PHONEBOOK_ID);
		XAddress objectAddr = XX.resolveObject(modelAddr, DemoModelUtil.JOHN_ID);
		
		XCommand command = MemoryObjectCommand.createAddCommand(objectAddr, XCommand.SAFE,
		        DemoModelUtil.ALIASES_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testCreateExistingFieldForced() throws IOException {
		
		XAddress modelAddr = XX.resolveModel(repo.getAddress(), DemoModelUtil.PHONEBOOK_ID);
		XAddress objectAddr = XX.resolveObject(modelAddr, DemoModelUtil.JOHN_ID);
		
		XCommand command = MemoryObjectCommand.createAddCommand(objectAddr, XCommand.FORCED,
		        DemoModelUtil.ALIASES_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveModel() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		XCommand command = MemoryRepositoryCommand.createRemoveCommand(repo.getAddress(), model
		        .getRevisionNumber(), model.getID());
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveModelSafeWrongRevision() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		XCommand command = MemoryRepositoryCommand.createRemoveCommand(repo.getAddress(), model
		        .getRevisionNumber() - 1, model.getID());
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveModelSafeMissing() throws IOException {
		
		XCommand command = MemoryRepositoryCommand
		        .createRemoveCommand(repo.getAddress(), 0, NEW_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveModelForcedMissing() throws IOException {
		
		XCommand command = MemoryRepositoryCommand.createRemoveCommand(repo.getAddress(),
		        XCommand.FORCED, NEW_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveObject() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		
		XCommand command = MemoryModelCommand.createRemoveCommand(model.getAddress(), object
		        .getRevisionNumber(), object.getID());
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveObjectMissingModel() throws IOException {
		
		XAddress modelAddr = XX.resolveModel(repo.getAddress(), NEW_ID);
		
		XCommand command = MemoryModelCommand.createRemoveCommand(modelAddr, XCommand.FORCED,
		        DemoModelUtil.JOHN_ID);
		
		assertNull(sendCommand(command, Long.MAX_VALUE));
		
	}
	
	@Test
	public void testRemoveObjectSafeWrongRevision() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		
		XCommand command = MemoryModelCommand.createRemoveCommand(model.getAddress(), object
		        .getRevisionNumber() - 1, object.getID());
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveObjectSafeMissing() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		XCommand command = MemoryModelCommand.createRemoveCommand(model.getAddress(), 42, NEW_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveObjectForcedMissing() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		XCommand command = MemoryModelCommand.createRemoveCommand(model.getAddress(),
		        XCommand.FORCED, NEW_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveField() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		XField field = object.getField(DemoModelUtil.ALIASES_ID);
		
		XCommand command = MemoryObjectCommand.createRemoveCommand(object.getAddress(), field
		        .getRevisionNumber(), field.getID());
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveFieldSafeWrongRevision() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		XField field = object.getField(DemoModelUtil.ALIASES_ID);
		
		XCommand command = MemoryObjectCommand.createRemoveCommand(object.getAddress(), field
		        .getRevisionNumber() - 1, field.getID());
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveFieldSafeMissing() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		
		XCommand command = MemoryObjectCommand.createRemoveCommand(object.getAddress(), 42, NEW_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveFieldForcedMissing() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		
		XCommand command = MemoryObjectCommand.createRemoveCommand(object.getAddress(),
		        XCommand.FORCED, NEW_ID);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveValue() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		XField field = object.getField(DemoModelUtil.ALIASES_ID);
		
		XCommand command = MemoryFieldCommand.createRemoveCommand(field.getAddress(), field
		        .getRevisionNumber());
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveValueSafeMissing() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		XField field = object.getField(DemoModelUtil.EMPTYFIELD_ID);
		
		XCommand command = MemoryFieldCommand.createRemoveCommand(field.getAddress(), field
		        .getRevisionNumber());
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testRemoveValueForcedMissing() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		XField field = object.getField(DemoModelUtil.EMPTYFIELD_ID);
		
		XCommand command = MemoryFieldCommand.createRemoveCommand(field.getAddress(),
		        XCommand.FORCED);
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testAddValue() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		XField field = object.getField(DemoModelUtil.EMPTYFIELD_ID);
		
		XCommand command = MemoryFieldCommand.createAddCommand(field.getAddress(), field
		        .getRevisionNumber(), X.getValueFactory().createStringValue("Cookie!"));
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testAddValueSafeExisting() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		XField field = object.getField(DemoModelUtil.ALIASES_ID);
		
		XCommand command = MemoryFieldCommand.createAddCommand(field.getAddress(), field
		        .getRevisionNumber(), X.getValueFactory().createStringValue("Cookie!"));
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testAddValueForcedExisting() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		XField field = object.getField(DemoModelUtil.ALIASES_ID);
		
		XCommand command = MemoryFieldCommand.createAddCommand(field.getAddress(), XCommand.FORCED,
		        X.getValueFactory().createStringValue("Cookie!"));
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testChangeValue() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		XField field = object.getField(DemoModelUtil.ALIASES_ID);
		
		XCommand command = MemoryFieldCommand.createChangeCommand(field.getAddress(), field
		        .getRevisionNumber(), X.getValueFactory().createStringValue("Cookie!"));
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testChangeValueSafeMissing() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		XField field = object.getField(DemoModelUtil.EMPTYFIELD_ID);
		
		XCommand command = MemoryFieldCommand.createChangeCommand(field.getAddress(), field
		        .getRevisionNumber(), X.getValueFactory().createStringValue("Cookie!"));
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testChangeValueForcedMissing() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject object = model.getObject(DemoModelUtil.JOHN_ID);
		XField field = object.getField(DemoModelUtil.EMPTYFIELD_ID);
		
		XCommand command = MemoryFieldCommand.createChangeCommand(field.getAddress(),
		        XCommand.FORCED, X.getValueFactory().createStringValue("Cookie!"));
		
		testSendCommand(command);
		
	}
	
	@Test
	public void testTransactionSuccess() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XTransactionBuilder b = new XTransactionBuilder(model.getAddress());
		
		b.removeObject(model.getAddress(), XCommand.FORCED, DemoModelUtil.JOHN_ID);
		b.removeObject(model.getAddress(), XCommand.FORCED, DemoModelUtil.PETER_ID);
		b.addObject(model.getAddress(), XCommand.SAFE, NEW_ID);
		
		testSendCommand(b.build());
		
	}
	
	@Test
	public void testTransactionFailure() throws IOException {
		
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XTransactionBuilder b = new XTransactionBuilder(model.getAddress());
		
		b.removeObject(model.getAddress(), XCommand.FORCED, DemoModelUtil.JOHN_ID);
		b.addObject(model.getAddress(), XCommand.SAFE, NEW_ID);
		b.removeObject(model.getAddress(), 0, DemoModelUtil.PETER_ID);
		// fails (wrong revision)
		
		testSendCommand(b.build());
		
	}
	
}
