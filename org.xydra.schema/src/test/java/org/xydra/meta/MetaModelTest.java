package org.xydra.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


public class MetaModelTest {
	
	private MetaModel metamodel;
	
	@Before
	public void init() {
		XID actor = X.getIDProvider().fromString("actor");
		XRepository repo = X.createMemoryRepository(actor);
		XModel baseModel = repo.createModel(X.getIDProvider().fromString("basemodel"));
		this.metamodel = new MetaModel(repo, baseModel);
	}
	
	@Test
	public void testGetNamespaceExpansion() {
		this.metamodel.setNamespaceExpansion("foaf", "http://example.com/foaf/");
		assertEquals("http://example.com/foaf/", this.metamodel.getNamespaceExpansion("foaf"));
	}
	
	@Test
	public void testGetPrefix() {
		assertEquals("abc", MetaModel.getPrefix("abc--defg"));
	}
	
	@Test
	public void testGetLocalName() {
		assertEquals("defg", MetaModel.getLocalName("abc--defg"));
	}
	
	@Test
	public void testToURI() {
		String uri = this.metamodel.toURI("rdf--subClassOf");
		assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#subClassOf", uri);
	}
	
	@Test
	public void testGetOrCreateAnnotationForModel() {
		XObject modelAnnotation = this.metamodel.getOrCreateAnnotationForModel(false);
		assertNull(modelAnnotation);
		modelAnnotation = this.metamodel.getOrCreateAnnotationForModel(true);
		assertNotNull(modelAnnotation);
		modelAnnotation = this.metamodel.getOrCreateAnnotationForModel(false);
		assertNotNull(modelAnnotation);
	}
	
	@Test
	public void testEscapeDots() {
		String test = "ab.c..d";
		assertEquals(test, MetaModel.unescapeDots(MetaModel.escapeDots(test)));
		assertEquals("ab..c....d", MetaModel.escapeDots(test));
	}
	
}
