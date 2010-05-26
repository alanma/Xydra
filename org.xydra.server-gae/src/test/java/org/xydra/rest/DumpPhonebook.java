package org.xydra.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.core.util.SimpleSyntaxUtils;



public class DumpPhonebook {
	
	@Test
	@Ignore
	// not urgently needed, concept not ready yet
	public void testSimpleSyntax() {
		XRepository repository = X.createMemoryRepository();
		DemoModelUtil.addPhonebookModel(repository);
		XModel phonebook = repository.getModel(DemoModelUtil.PHONEBOOK_ID);
		String simpleSyntax = SimpleSyntaxUtils.toSimpleSyntax(phonebook);
		
		// FIXME
		System.out.println(simpleSyntax);
		
		XModel phonebookParsed = SimpleSyntaxUtils
		        .toModel(DemoModelUtil.PHONEBOOK_ID, simpleSyntax);
		assertEquals(phonebook, phonebookParsed);
	}
	
	public static void main(String[] args) {
		XRepository repository = X.createMemoryRepository();
		DemoModelUtil.addPhonebookModel(repository);
		XModel phonebook = repository.getModel(DemoModelUtil.PHONEBOOK_ID);
		String syntax = SimpleSyntaxUtils.toSimpleSyntax(phonebook);
		System.out.println(syntax);
	}
	
}
