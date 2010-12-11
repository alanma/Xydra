package org.xydra.core.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.core.XCompareUtils;
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
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Test serializing {@link XBaseRepository}, {@link XBaseModel},
 * {@link XBaseObject} and {@link XBaseField} types to/from XML.
 * 
 * @author dscharrer
 * 
 */
public class XmlModelTest {
	
	private static final Logger log = LoggerFactory.getLogger(XmlModelTest.class);
	private XID actorId = XX.toId("a-test-user");
	
	private void testRepository(XBaseRepository repo) {
		
		// test serializing with revisions
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(repo, out);
		assertEquals("", out.getOpentags());
		String xml = out.getXml();
		log.debug(xml);
		MiniElement e = new MiniXMLParserImpl().parseXml(xml);
		XRepository repoAgain = XmlModel.toRepository(this.actorId, e);
		assertTrue(XCompareUtils.equalState(repo, repoAgain));
		
		// test serializing without revisions
		out = new XmlOutStringBuffer();
		XmlModel.toXml(repo, out, false, true, false);
		assertEquals("", out.getOpentags());
		xml = out.getXml();
		log.debug(xml);
		e = new MiniXMLParserImpl().parseXml(xml);
		repoAgain = XmlModel.toRepository(this.actorId, e);
		assertTrue(XCompareUtils.equalTree(repo, repoAgain));
		checkNoRevisions(repoAgain);
		
	}
	
	private void testModel(XBaseModel model) {
		
		// test serializing with revisions
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(model, out);
		assertEquals("", out.getOpentags());
		String xml = out.getXml();
		log.debug(xml);
		MiniElement e = new MiniXMLParserImpl().parseXml(xml);
		XModel modelAgain = XmlModel.toModel(this.actorId, e);
		assertTrue(XCompareUtils.equalState(model, modelAgain));
		
		// check that there is a change log
		XChangeLog changeLog = modelAgain.getChangeLog();
		assertNotNull(changeLog);
		
		// test serializing without revisions
		out = new XmlOutStringBuffer();
		XmlModel.toXml(model, out, false, true, false);
		assertEquals("", out.getOpentags());
		xml = out.getXml();
		log.debug(xml);
		e = new MiniXMLParserImpl().parseXml(xml);
		modelAgain = XmlModel.toModel(this.actorId, e);
		assertTrue(XCompareUtils.equalTree(model, modelAgain));
		checkNoRevisions(modelAgain);
		
	}
	
	private void testObject(XBaseObject object) {
		
		// test serializing with revisions
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(object, out);
		assertEquals("", out.getOpentags());
		String xml = out.getXml();
		log.debug(xml);
		MiniElement e = new MiniXMLParserImpl().parseXml(xml);
		XObject objectAgain = XmlModel.toObject(this.actorId, e);
		assertTrue(XCompareUtils.equalState(object, objectAgain));
		
		// check that there is a change log
		XChangeLog changeLog = objectAgain.getChangeLog();
		assertNotNull(changeLog);
		
		// test serializing without revisions
		out = new XmlOutStringBuffer();
		XmlModel.toXml(object, out, false, true, false);
		assertEquals("", out.getOpentags());
		xml = out.getXml();
		log.debug(xml);
		e = new MiniXMLParserImpl().parseXml(xml);
		objectAgain = XmlModel.toObject(this.actorId, e);
		assertTrue(XCompareUtils.equalTree(object, objectAgain));
		checkNoRevisions(objectAgain);
		
	}
	
	private void testField(XBaseField field) {
		
		// test serializing with revisions
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlModel.toXml(field, out);
		assertEquals("", out.getOpentags());
		String xml = out.getXml();
		log.debug(xml);
		MiniElement e = new MiniXMLParserImpl().parseXml(xml);
		XField fieldAgain = XmlModel.toField(this.actorId, e);
		assertTrue(XCompareUtils.equalState(field, fieldAgain));
		
		// test serializing without revisions
		out = new XmlOutStringBuffer();
		XmlModel.toXml(field, out, false);
		assertEquals("", out.getOpentags());
		xml = out.getXml();
		log.debug(xml);
		e = new MiniXMLParserImpl().parseXml(xml);
		fieldAgain = XmlModel.toField(this.actorId, e);
		assertTrue(XCompareUtils.equalTree(field, fieldAgain));
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
		testRepository(new MemoryRepository(this.actorId, XX.toId("repo")));
	}
	
	@Test
	public void testFullRepository() {
		XRepository repo = new MemoryRepository(this.actorId, XX.toId("repo"));
		DemoModelUtil.addPhonebookModel(repo);
		testRepository(repo);
	}
	
	@Test
	public void testEmptyModel() {
		testModel(new MemoryModel(this.actorId, DemoModelUtil.PHONEBOOK_ID));
	}
	
	@Test
	public void testFullModel() {
		XModel model = new MemoryModel(this.actorId, DemoModelUtil.PHONEBOOK_ID);
		DemoModelUtil.setupPhonebook(model);
		testModel(model);
	}
	
	@Test
	public void testEmptyObject() {
		testObject(new MemoryObject(this.actorId, DemoModelUtil.JOHN_ID));
	}
	
	@Test
	public void testFullObject() {
		XObject object = new MemoryObject(this.actorId, DemoModelUtil.JOHN_ID);
		DemoModelUtil.setupJohn(object);
		testObject(object);
	}
	
	@Test
	public void testEmptyField() {
		testField(new MemoryField(this.actorId, DemoModelUtil.ALIASES_ID));
	}
	
	@Test
	public void testFullField() {
		XField field = new MemoryField(this.actorId, DemoModelUtil.ALIASES_ID);
		field.setValue(XV.toStringSetValue(new String[] { "Cookie Monster" }));
		testField(field);
	}
	
}
