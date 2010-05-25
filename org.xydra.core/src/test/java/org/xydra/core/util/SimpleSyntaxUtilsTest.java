package org.xydra.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.util.SimpleSyntaxUtils;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.XmlOutStringBuffer;



public class SimpleSyntaxUtilsTest {
	
	static final XID PHONEBOOK = X.getIDProvider().fromString("phonebook");
	
	static String test = "# declares the XObject 'hans' and the property 'phone', sets value of hans.phone to '123'.\n"
	        + "hans.phone=123\n"
	        + "hans.email=hans@example.com\n"
	        + "hans.knows=[peter,john,dirk]\n"
	        + "# declares peter as an object\n"
	        + "peter\n"
	        + "john.phone=1234\n";
	
	@Test
	public void testParsing() {
		XModel model = SimpleSyntaxUtils.toModel(PHONEBOOK, test);
		assertTrue(model.hasObject(X.getIDProvider().fromString("hans")));
		assertTrue(model.hasObject(X.getIDProvider().fromString("peter")));
	}
	
	@Test
	public void testParsing2() {
		XModel model = SimpleSyntaxUtils.toModel(PHONEBOOK, test);
		String syntax = SimpleSyntaxUtils.toSimpleSyntax(model);
		XModel model2 = SimpleSyntaxUtils.toModel(PHONEBOOK, syntax);
		assertEquals(model, model2);
		assertEquals(syntax, SimpleSyntaxUtils.toSimpleSyntax(model2));
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
	
	public static final void dump(XModel model) {
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(model, xo, true, false);
		System.out.println(xo.getXml());
	}
	
}
