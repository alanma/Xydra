package org.xydra.core.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.core.XX;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XBaseRepository;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.core.value.XV;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;


/**
 * Test serializing {@link XBaseRepository}, {@link XBaseModel},
 * {@link XBaseObject} and {@link XBaseField} types to/from XML.
 * 
 * @author dscharrer
 * 
 */
public class XmlModelTest {
	
	private void testRepository(XBaseRepository repo) {
		
		// test serializing with revisions
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(repo, out);
		assertEquals("", out.getOpentags());
		String xml = out.getXml();
		System.out.println(xml);
		MiniElement e = new MiniXMLParserImpl().parseXml(xml);
		XRepository repoAgain = XmlModel.toRepository(e);
		assertTrue(XX.equalState(repo, repoAgain));
		
		// test serializing without revisions
		out = new XmlOutStringBuffer();
		XmlModel.toXml(repo, out, false, true, true);
		assertEquals("", out.getOpentags());
		xml = out.getXml();
		System.out.println(xml);
		e = new MiniXMLParserImpl().parseXml(xml);
		repoAgain = XmlModel.toRepository(e);
		assertTrue(XX.equalTree(repo, repoAgain));
		checkNoRevisions(repoAgain);
		
	}
	
	private void testModel(XBaseModel model) {
		
		// test serializing with revisions
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(model, out);
		assertEquals("", out.getOpentags());
		String xml = out.getXml();
		System.out.println(xml);
		MiniElement e = new MiniXMLParserImpl().parseXml(xml);
		XModel modelAgain = XmlModel.toModel(e);
		assertTrue(XX.equalState(model, modelAgain));
		
		// check that there is a change log
		XChangeLog log = modelAgain.getChangeLog();
		assertNotNull(log);
		
		// test serializing without revisions
		out = new XmlOutStringBuffer();
		XmlModel.toXml(model, out, false, true, true);
		assertEquals("", out.getOpentags());
		xml = out.getXml();
		System.out.println(xml);
		e = new MiniXMLParserImpl().parseXml(xml);
		modelAgain = XmlModel.toModel(e);
		assertTrue(XX.equalTree(model, modelAgain));
		checkNoRevisions(modelAgain);
		
	}
	
	private void testObject(XBaseObject object) {
		
		// test serializing with revisions
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(object, out);
		assertEquals("", out.getOpentags());
		String xml = out.getXml();
		System.out.println(xml);
		MiniElement e = new MiniXMLParserImpl().parseXml(xml);
		XObject objectAgain = XmlModel.toObject(e);
		assertTrue(XX.equalState(object, objectAgain));
		
		// check that there is a change log
		XChangeLog log = objectAgain.getChangeLog();
		assertNotNull(log);
		
		// test serializing without revisions
		out = new XmlOutStringBuffer();
		XmlModel.toXml(object, out, false, true, true);
		assertEquals("", out.getOpentags());
		xml = out.getXml();
		System.out.println(xml);
		e = new MiniXMLParserImpl().parseXml(xml);
		objectAgain = XmlModel.toObject(e);
		assertTrue(XX.equalTree(object, objectAgain));
		checkNoRevisions(objectAgain);
		
	}
	
	private void testField(XBaseField field) {
		
		// test serializing with revisions
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(field, out);
		assertEquals("", out.getOpentags());
		String xml = out.getXml();
		System.out.println(xml);
		MiniElement e = new MiniXMLParserImpl().parseXml(xml);
		XField fieldAgain = XmlModel.toField(e);
		assertTrue(XX.equalState(field, fieldAgain));
		
		// test serializing without revisions
		out = new XmlOutStringBuffer();
		XmlModel.toXml(field, out, false);
		assertEquals("", out.getOpentags());
		xml = out.getXml();
		System.out.println(xml);
		e = new MiniXMLParserImpl().parseXml(xml);
		fieldAgain = XmlModel.toField(e);
		assertTrue(XX.equalTree(field, fieldAgain));
		checkNoRevisions(fieldAgain);
		
	}
	
	void checkNoRevisions(XBaseRepository repo) {
		for(XID modelId : repo) {
			checkNoRevisions(repo.getModel(modelId));
		}
	}
	
	void checkNoRevisions(XBaseModel model) {
		assertEquals(XmlModel.NO_REVISION, model.getRevisionNumber());
		for(XID objectId : model) {
			checkNoRevisions(model.getObject(objectId));
		}
	}
	
	void checkNoRevisions(XBaseObject object) {
		assertEquals(XmlModel.NO_REVISION, object.getRevisionNumber());
		for(XID fieldId : object) {
			checkNoRevisions(object.getField(fieldId));
		}
	}
	
	void checkNoRevisions(XBaseField field) {
		assertEquals(XmlModel.NO_REVISION, field.getRevisionNumber());
	}
	
	@Test
	public void testEmptyRepository() {
		testRepository(new MemoryRepository(XX.toId("repo")));
	}
	
	@Test
	public void testFullRepository() {
		XRepository repo = new MemoryRepository(XX.toId("repo"));
		DemoModelUtil.addPhonebookModel(repo);
		testRepository(repo);
	}
	
	@Test
	public void testEmptyModel() {
		testModel(new MemoryModel(DemoModelUtil.PHONEBOOK_ID));
	}
	
	@Test
	public void testFullModel() {
		XModel model = new MemoryModel(DemoModelUtil.PHONEBOOK_ID);
		DemoModelUtil.setupPhonebook(model);
		testModel(model);
	}
	
	@Test
	public void testEmptyObject() {
		testObject(new MemoryObject(DemoModelUtil.JOHN_ID));
	}
	
	@Test
	public void testFullObject() {
		XObject object = new MemoryObject(DemoModelUtil.JOHN_ID);
		DemoModelUtil.setupJohn(object);
		testObject(object);
	}
	
	@Test
	public void testEmptyField() {
		testField(new MemoryField(DemoModelUtil.ALIASES_ID));
	}
	
	@Test
	public void testFullField() {
		XField field = new MemoryField(DemoModelUtil.ALIASES_ID);
		field.setValue(DemoModelUtil.ACTOR_ID, XV
		        .toStringSetValue(new String[] { "Cookie Monster" }));
		testField(field);
	}
	
}
