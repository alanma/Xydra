package org.xydra.core.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.value.XV;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XCompareUtils;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.serialize.MiniElement;
import org.xydra.core.serialize.XmlModel;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.MiniParserXml;
import org.xydra.core.serialize.xml.XydraOutXml;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Test serializing {@link XReadableRepository}, {@link XReadableModel},
 * {@link XReadableObject} and {@link XReadableField} types to/from XML.
 * 
 * @author dscharrer
 * 
 */
public class XmlModelTest {
	
	private static final Logger log = getLogger();
	
	private static Logger getLogger() {
		LoggerTestHelper.init();
		return LoggerFactory.getLogger(XmlModelTest.class);
	}
	
	private XID actorId = XX.toId("a-test-user");
	
	void checkNoRevisions(XReadableField field) {
		assertEquals(XmlModel.NO_REVISION, field.getRevisionNumber());
	}
	
	void checkNoRevisions(XReadableModel model) {
		assertEquals(XmlModel.NO_REVISION, model.getRevisionNumber());
		for(XID objectId : model) {
			checkNoRevisions(model.getObject(objectId));
		}
	}
	
	void checkNoRevisions(XReadableObject object) {
		assertEquals(XmlModel.NO_REVISION, object.getRevisionNumber());
		for(XID fieldId : object) {
			checkNoRevisions(object.getField(fieldId));
		}
	}
	
	void checkNoRevisions(XReadableRepository repo) {
		for(XID modelId : repo) {
			checkNoRevisions(repo.getModel(modelId));
		}
	}
	
	@Test
	public void testEmptyField() {
		testField(new MemoryField(this.actorId, DemoModelUtil.ALIASES_ID));
	}
	
	@Test
	public void testEmptyModel() {
		testModel(new MemoryModel(this.actorId, null, DemoModelUtil.PHONEBOOK_ID));
	}
	
	@Test
	public void testEmptyObject() {
		testObject(new MemoryObject(this.actorId, null, DemoModelUtil.JOHN_ID));
	}
	
	@Test
	public void testEmptyRepository() {
		testRepository(new MemoryRepository(this.actorId, null, XX.toId("repo")));
	}
	
	private void testField(XReadableField field) {
		
		// test serializing with revisions
		XydraOut out = new XydraOutXml();
		XmlModel.toXml(field, out);
		assertTrue(out.isClosed());
		String xml = out.getData();
		log.info(xml);
		MiniElement e = new MiniParserXml().parse(xml);
		XField fieldAgain = XmlModel.toField(this.actorId, e);
		assertTrue(XCompareUtils.equalState(field, fieldAgain));
		
		// test serializing without revisions
		out = new XydraOutXml();
		XmlModel.toXml(field, out, false);
		assertTrue(out.isClosed());
		xml = out.getData();
		log.info(xml);
		e = new MiniParserXml().parse(xml);
		fieldAgain = XmlModel.toField(this.actorId, e);
		assertTrue(XCompareUtils.equalTree(field, fieldAgain));
		checkNoRevisions(fieldAgain);
		
	}
	
	@Test
	public void testFullField() {
		XField field = new MemoryField(this.actorId, DemoModelUtil.ALIASES_ID);
		field.setValue(XV.toStringSetValue(new String[] { "Cookie Monster" }));
		testField(field);
	}
	
	@Test
	public void testFullModel() {
		XModel model = new MemoryModel(this.actorId, null, DemoModelUtil.PHONEBOOK_ID);
		DemoModelUtil.setupPhonebook(model);
		testModel(model);
	}
	
	@Test
	public void testFullObject() {
		XObject object = new MemoryObject(this.actorId, null, DemoModelUtil.JOHN_ID);
		DemoModelUtil.setupJohn(object);
		testObject(object);
	}
	
	@Test
	public void testFullRepository() {
		XRepository repo = new MemoryRepository(this.actorId, null, XX.toId("repo"));
		DemoModelUtil.addPhonebookModel(repo);
		testRepository(repo);
	}
	
	private void testModel(XReadableModel model) {
		
		// test serializing with revisions
		XydraOut out = new XydraOutXml();
		XmlModel.toXml(model, out);
		assertTrue(out.isClosed());
		String xml = out.getData();
		log.info(xml);
		MiniElement e = new MiniParserXml().parse(xml);
		XModel modelAgain = XmlModel.toModel(this.actorId, null, e);
		assertTrue(XCompareUtils.equalState(model, modelAgain));
		
		// check that there is a change log
		XChangeLog changeLog = modelAgain.getChangeLog();
		assertNotNull(changeLog);
		
		// test serializing without revisions
		out = new XydraOutXml();
		XmlModel.toXml(model, out, false, true, false);
		assertTrue(out.isClosed());
		xml = out.getData();
		log.info(xml);
		e = new MiniParserXml().parse(xml);
		modelAgain = XmlModel.toModel(this.actorId, null, e);
		assertTrue(XCompareUtils.equalTree(model, modelAgain));
		checkNoRevisions(modelAgain);
		
	}
	
	private void testObject(XReadableObject object) {
		
		// test serializing with revisions
		XydraOut out = new XydraOutXml();
		XmlModel.toXml(object, out);
		assertTrue(out.isClosed());
		String xml = out.getData();
		log.info(xml);
		MiniElement e = new MiniParserXml().parse(xml);
		XObject objectAgain = XmlModel.toObject(this.actorId, null, e);
		assertTrue(XCompareUtils.equalState(object, objectAgain));
		
		// check that there is a change log
		XChangeLog changeLog = objectAgain.getChangeLog();
		assertNotNull(changeLog);
		
		// test serializing without revisions
		out = new XydraOutXml();
		XmlModel.toXml(object, out, false, true, false);
		assertTrue(out.isClosed());
		xml = out.getData();
		log.info(xml);
		e = new MiniParserXml().parse(xml);
		objectAgain = XmlModel.toObject(this.actorId, null, e);
		assertTrue(XCompareUtils.equalTree(object, objectAgain));
		checkNoRevisions(objectAgain);
		
	}
	
	private void testRepository(XReadableRepository repo) {
		
		// test serializing with revisions
		XydraOut out = new XydraOutXml();
		XmlModel.toXml(repo, out);
		assertTrue(out.isClosed());
		String xml = out.getData();
		log.info(xml);
		MiniElement e = new MiniParserXml().parse(xml);
		XRepository repoAgain = XmlModel.toRepository(this.actorId, null, e);
		assertTrue(XCompareUtils.equalState(repo, repoAgain));
		
		// test serializing without revisions
		out = new XydraOutXml();
		XmlModel.toXml(repo, out, false, true, false);
		assertTrue(out.isClosed());
		xml = out.getData();
		log.info(xml);
		e = new MiniParserXml().parse(xml);
		repoAgain = XmlModel.toRepository(this.actorId, null, e);
		assertTrue(XCompareUtils.equalTree(repo, repoAgain));
		checkNoRevisions(repoAgain);
		
	}
	
}
