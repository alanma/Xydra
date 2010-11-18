package org.xydra.schema;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.XFile;
import org.xydra.core.XX;
import org.xydra.core.model.XRepository;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.schema.model.SObject;
import org.xydra.schema.parser.ParseException;
import org.xydra.schema.parser.XSchemaParser;


public class ParseTest {
	
	static String objectDef = "person={Boolean nerd=true;Integer age=23;}";
	
	@Test
	public void testGeneratedParser() throws ParseException {
		StringReader sr = new StringReader(objectDef);
		XSchemaParser parser = new XSchemaParser(sr);
		parser.enable_tracing();
		// Token t = parser.getNextToken();
		// while(t.kind != XSchemaParserConstants.EOF) {
		// System.out.println(t.toString());
		// t = parser.getNextToken();
		// }
		
		SObject parsedObject = parser.object();
		
		StringBuffer buf = new StringBuffer();
		parsedObject.toSyntax(buf);
		
		assertEquals(objectDef, buf.toString());
	}
	
	@Test
	public void testParseModelFile() throws IOException {
		// File f = new File("./src/test/resources/model.xy");
		XRepository repository = X.createMemoryRepository(XX.toId("ParseTest"));
		DemoModelUtil.addPhonebookModel(repository);
		
		String fileName = "./src/test/resources/model.xy";
		XFile.saveRepository(repository, fileName);
	}
	
}
