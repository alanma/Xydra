package org.xydra.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.XCompareUtils;
import org.xydra.core.XX;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.core.value.XV;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.XmlOutStringBuffer;


/**
 * Test for /data REST API
 * 
 * @author dscharrer
 * 
 */
public abstract class DataApiTest extends AbstractRestApiTest {
	
	private static final XID JANE_ID = XX.toId("jane");
	
	@Test
	public void testPing() throws IOException {
		int result = getPing();
		assertEquals("Is the Test-server running?", 200, result);
	}
	
	@Test
	public void testGetModelTrivial() throws IOException {
		XModel model = getRemoteModel(X.getIDProvider().createUniqueID());
		assertNull(model);
		
		model = getRemoteModel(DemoModelUtil.PHONEBOOK_ID);
		
		assertNotNull(model);
	}
	
	@Test
	public void testGetModel() throws IOException {
		
		XModel retrievedModel = getRemoteModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(retrievedModel);
		// try to read children
		Iterator<XID> objIt = retrievedModel.iterator();
		while(objIt.hasNext()) {
			XObject object = retrievedModel.getObject(objIt.next());
			assertNotNull(object);
		}
		
		XModel originalModel = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(originalModel);
		
		assertTrue(XCompareUtils.equalState(originalModel, retrievedModel));
	}
	
	@Test
	public void testGetObject() throws IOException {
		
		URL objectUrl = dataapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI() + "/").resolve(
		        DemoModelUtil.JOHN_ID.toURI()).toURL();
		
		MiniElement objectElement = loadXml(objectUrl);
		assertNotNull(objectElement);
		
		XObject retrievedObject;
		try {
			retrievedObject = XmlModel.toObject(JANE_ID, null, objectElement);
		} catch(IllegalArgumentException iae) {
			fail(iae.getMessage());
			throw new RuntimeException();
		}
		
		XObject originalObject = repo.getModel(DemoModelUtil.PHONEBOOK_ID).getObject(
		        DemoModelUtil.JOHN_ID);
		
		assertTrue(XCompareUtils.equalState(originalObject, retrievedObject));
		
	}
	
	@Test
	public void testGetField() throws IOException {
		
		URL fieldUrl = dataapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI() + "/").resolve(
		        DemoModelUtil.JOHN_ID.toURI() + "/").resolve(DemoModelUtil.PHONE_ID.toURI())
		        .toURL();
		
		MiniElement fieldElement = loadXml(fieldUrl);
		assertNotNull(fieldElement);
		
		XField retrievedField;
		try {
			retrievedField = XmlModel.toField(JANE_ID, fieldElement);
		} catch(IllegalArgumentException iae) {
			fail(iae.getMessage());
			throw new RuntimeException();
		}
		
		assert repo != null;
		XModel originalModel = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(originalModel);
		XObject originalObject = originalModel.getObject(DemoModelUtil.JOHN_ID);
		assertNotNull(originalObject);
		XField originalField = originalObject.getField(DemoModelUtil.PHONE_ID);
		
		assertTrue(XCompareUtils.equalState(originalField, retrievedField));
		
	}
	
	@Test
	public void testGetBadModelId() throws IOException {
		
		URL url = dataapi.resolve(AbstractRestApiTest.BAD_ID).toURL();
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, c.getResponseCode());
		
	}
	
	@Test
	public void testGetBadObjectId() throws IOException {
		
		URL url = dataapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI() + "/").resolve(
		        AbstractRestApiTest.BAD_ID).toURL();
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, c.getResponseCode());
		
	}
	
	@Test
	public void testGetBadFieldId() throws IOException {
		
		URL url = dataapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI() + "/").resolve(
		        DemoModelUtil.JOHN_ID.toURI() + "/").resolve(AbstractRestApiTest.BAD_ID).toURL();
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, c.getResponseCode());
		
	}
	
	@Test
	public void testGetMoreComponentsUrl() throws IOException {
		
		URL url = dataapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI() + "/").resolve(
		        DemoModelUtil.JOHN_ID.toURI() + "/").resolve(DemoModelUtil.PHONE_ID + "/").resolve(
		        AbstractRestApiTest.BAD_ID).toURL();
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, c.getResponseCode());
		
	}
	
	@Test
	public void testGetMissingModel() throws IOException {
		
		URL url = dataapi.resolve(AbstractRestApiTest.MISSING_ID).toURL();
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, c.getResponseCode());
		
	}
	
	@Test
	public void testGetMissingObject() throws IOException {
		
		URL url = dataapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI() + "/").resolve(
		        AbstractRestApiTest.MISSING_ID).toURL();
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, c.getResponseCode());
		
	}
	
	@Test
	public void testGetMissingField() throws IOException {
		
		URL url = dataapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI() + "/").resolve(
		        DemoModelUtil.JOHN_ID.toURI() + "/").resolve(AbstractRestApiTest.MISSING_ID)
		        .toURL();
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, c.getResponseCode());
		
	}
	
	/**
	 * @param url
	 * @param data to be posted
	 * @throws IOException
	 */
	private void postDataAndExpectHttpCreatedResponse(URL url, String data) throws IOException {
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.setDoOutput(true);
		c.setRequestMethod("POST");
		c.setRequestProperty("Content-Type", "application/xml");
		Writer w = new OutputStreamWriter(c.getOutputStream(), "UTF-8");
		w.write(data);
		w.flush();
		assertEquals(HttpURLConnection.HTTP_CREATED, c.getResponseCode());
	}
	
	@Test
	public void testPostModel() throws IOException {
		
		XModel model = getRemoteModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(model);
		
		long newRev = model.getRevisionNumber() + 1;
		assertNotNull(model.getObject(DemoModelUtil.CLAUDIA_ID));
		long claudiaRev = model.getObject(DemoModelUtil.CLAUDIA_ID).getRevisionNumber();
		
		XObject john = model.getObject(DemoModelUtil.JOHN_ID);
		long titleRev = john.getField(DemoModelUtil.TITLE_ID).getRevisionNumber();
		XField phone = john.getField(DemoModelUtil.PHONE_ID);
		
		// change model
		john.removeField(DemoModelUtil.ALIASES_ID);
		john.createField(AbstractRestApiTest.NEW_ID);
		phone.setValue(XV.toValue("342-170984-7892"));
		model.createObject(JANE_ID);
		model.removeObject(DemoModelUtil.PETER_ID);
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(model, out, false, false, false);
		postDataAndExpectHttpCreatedResponse(dataapi.toURL(), out.getXml());
		
		XModel updatedModel = getRemoteModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(updatedModel);
		
		assertTrue(XCompareUtils.equalTree(model, updatedModel));
		
		assertEquals(newRev, updatedModel.getRevisionNumber());
		
		XObject updatedJohn = updatedModel.getObject(DemoModelUtil.JOHN_ID);
		assertEquals(newRev, updatedJohn.getRevisionNumber());
		assertEquals(newRev, updatedJohn.getField(DemoModelUtil.PHONE_ID).getRevisionNumber());
		assertEquals(newRev, updatedJohn.getField(AbstractRestApiTest.NEW_ID).getRevisionNumber());
		
		assertEquals(newRev, updatedModel.getObject(JANE_ID).getRevisionNumber());
		
		assertEquals(claudiaRev, updatedModel.getObject(DemoModelUtil.CLAUDIA_ID)
		        .getRevisionNumber());
		assertEquals(titleRev, updatedJohn.getField(DemoModelUtil.TITLE_ID).getRevisionNumber());
		
	}
	
	@Test
	public void testPostObject() throws IOException {
		
		XModel model = getRemoteModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(model);
		
		long newRev = model.getRevisionNumber() + 1;
		long claudiaRev = model.getObject(DemoModelUtil.CLAUDIA_ID).getRevisionNumber();
		
		XObject john = model.getObject(DemoModelUtil.JOHN_ID);
		long titleRev = john.getField(DemoModelUtil.TITLE_ID).getRevisionNumber();
		XField phone = john.getField(DemoModelUtil.PHONE_ID);
		
		// change object
		john.removeField(DemoModelUtil.ALIASES_ID);
		john.createField(AbstractRestApiTest.NEW_ID);
		phone.setValue(XV.toValue("342-170984-7892"));
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(john, out, false, false, false);
		postDataAndExpectHttpCreatedResponse(dataapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI())
		        .toURL(), out.getXml());
		
		XModel updatedModel = getRemoteModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(updatedModel);
		
		assertTrue(XCompareUtils.equalTree(model, updatedModel));
		
		assertEquals(newRev, updatedModel.getRevisionNumber());
		
		XObject updatedJohn = updatedModel.getObject(DemoModelUtil.JOHN_ID);
		assertEquals(newRev, updatedJohn.getRevisionNumber());
		assertEquals(newRev, updatedJohn.getField(DemoModelUtil.PHONE_ID).getRevisionNumber());
		assertEquals(newRev, updatedJohn.getField(AbstractRestApiTest.NEW_ID).getRevisionNumber());
		
		assertEquals(claudiaRev, updatedModel.getObject(DemoModelUtil.CLAUDIA_ID)
		        .getRevisionNumber());
		assertEquals(titleRev, updatedJohn.getField(DemoModelUtil.TITLE_ID).getRevisionNumber());
		
	}
	
	@Test
	public void testPostField() throws IOException {
		
		XModel model = getRemoteModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(model);
		
		long newRev = model.getRevisionNumber() + 1;
		long claudiaRev = model.getObject(DemoModelUtil.CLAUDIA_ID).getRevisionNumber();
		
		XObject john = model.getObject(DemoModelUtil.JOHN_ID);
		long titleRev = john.getField(DemoModelUtil.TITLE_ID).getRevisionNumber();
		XField phone = john.getField(DemoModelUtil.PHONE_ID);
		phone.setValue(XV.toValue("342-170984-7892"));
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(phone, out, false);
		postDataAndExpectHttpCreatedResponse(dataapi.resolve(
		        DemoModelUtil.PHONEBOOK_ID.toURI() + "/").resolve(DemoModelUtil.JOHN_ID.toURI())
		        .toURL(), out.getXml());
		
		XModel updatedModel = getRemoteModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(updatedModel);
		
		assertTrue(XCompareUtils.equalTree(model, updatedModel));
		
		assertEquals(newRev, updatedModel.getRevisionNumber());
		
		XObject updatedJohn = updatedModel.getObject(DemoModelUtil.JOHN_ID);
		assertEquals(newRev, updatedJohn.getRevisionNumber());
		assertEquals(newRev, updatedJohn.getField(DemoModelUtil.PHONE_ID).getRevisionNumber());
		
		assertEquals(claudiaRev, updatedModel.getObject(DemoModelUtil.CLAUDIA_ID)
		        .getRevisionNumber());
		assertEquals(titleRev, updatedJohn.getField(DemoModelUtil.TITLE_ID).getRevisionNumber());
		
	}
	
	@Test
	public void testDeleteModel() throws IOException {
		
		URL url = dataapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI()).toURL();
		
		deleteResource(url);
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, c.getResponseCode());
		
	}
	
	@Test
	public void testDeleteObject() throws IOException {
		
		XModel oldModel = getRemoteModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(oldModel);
		
		URL url = dataapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI() + "/").resolve(
		        DemoModelUtil.JOHN_ID.toURI()).toURL();
		
		deleteResource(url);
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, c.getResponseCode());
		
		XModel model = getRemoteModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(model);
		
		assertFalse(model.hasObject(DemoModelUtil.JOHN_ID));
		assertEquals(oldModel.getRevisionNumber() + 1, model.getRevisionNumber());
		
		assertEquals(oldModel.getObject(DemoModelUtil.PETER_ID).getRevisionNumber(), model
		        .getObject(DemoModelUtil.PETER_ID).getRevisionNumber());
		
	}
	
	@Test
	public void testDeleteField() throws IOException {
		
		XModel oldModel = getRemoteModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(oldModel);
		
		URL url = dataapi.resolve(DemoModelUtil.PHONEBOOK_ID.toURI() + "/").resolve(
		        DemoModelUtil.JOHN_ID.toURI() + "/").resolve(DemoModelUtil.ALIASES_ID.toURI())
		        .toURL();
		
		deleteResource(url);
		
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_NOT_FOUND, c.getResponseCode());
		
		XModel model = getRemoteModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(model);
		
		XObject john = model.getObject(DemoModelUtil.JOHN_ID);
		assertFalse(john.hasField(DemoModelUtil.ALIASES_ID));
		long newRev = oldModel.getRevisionNumber() + 1;
		assertEquals(newRev, model.getRevisionNumber());
		assertEquals(newRev, john.getRevisionNumber());
		
		assertEquals(oldModel.getObject(DemoModelUtil.PETER_ID).getRevisionNumber(), model
		        .getObject(DemoModelUtil.PETER_ID).getRevisionNumber());
		assertEquals(oldModel.getObject(DemoModelUtil.JOHN_ID).getField(DemoModelUtil.PHONE_ID)
		        .getRevisionNumber(), john.getField(DemoModelUtil.PHONE_ID).getRevisionNumber());
		
	}
	
}
