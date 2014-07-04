package org.xydra.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XX;
import org.xydra.core.model.XModel;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class SimpleSyntaxUtilsTest {
	
	private static final Logger log = getLogger();
	
	static final XId PHONEBOOK = XX.toId("phonebook");
	
	static String test = "# declares the XObject 'hans' and the property 'phone', sets value of hans.phone to '123'.\n"
	        + "hans.phone=123\n"
	        + "hans.email=hans@example.com\n"
	        + "hans.knows=[peter,john,dirk]\n"
	        + "# declares peter as an object\n"
	        + "peter\n"
	        + "john.phone=1234\n";
	
	public static final void dump(XReadableModel model) {
		log.debug(toXml(model));
	}
	
	public static final String toXml(XReadableModel model) {
		XydraOut out = new XmlOut();
		SerializedModel.serialize(model, out, true, false, true);
		return out.getData();
	}
	
	private static Logger getLogger() {
		LoggerTestHelper.init();
		return LoggerFactory.getLogger(SimpleSyntaxUtilsTest.class);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testError1() {
		SimpleSyntaxUtils.toModel(PHONEBOOK, "hans.likes=ice\nhans.likes=fruit");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testError2() {
		SimpleSyntaxUtils.toModel(PHONEBOOK, "hans=somevalue");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testError3() {
		dump(SimpleSyntaxUtils.toModel(PHONEBOOK, "hans with space=somevalue"));
	}
	
	@Test
	public void testParsing() {
		XModel model = SimpleSyntaxUtils.toModel(PHONEBOOK, test);
		assertTrue(model.hasObject(XX.toId("hans")));
		assertTrue(model.hasObject(XX.toId("peter")));
	}
	
	@Test
	public void testParsing2() {
		XModel model = SimpleSyntaxUtils.toModel(PHONEBOOK, test);
		String syntax = SimpleSyntaxUtils.toSimpleSyntax(model);
		XModel model2 = SimpleSyntaxUtils.toModel(PHONEBOOK, syntax);
		assertEquals(model, model2);
		assertEquals(syntax, SimpleSyntaxUtils.toSimpleSyntax(model2));
	}
	
}
