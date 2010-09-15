package org.xydra.schema;

import org.junit.Test;
import org.xydra.schema.model.SBoolean;
import org.xydra.schema.model.SField;
import org.xydra.schema.model.SName;
import org.xydra.schema.model.SNumber;
import org.xydra.schema.model.SObject;
import org.xydra.schema.model.SType;


public class ToSyntaxTest {
	
	@Test
	public void testObjectToSyntax() {
		SObject object = new SObject(new SName("person"));
		
		SField field1 = new SField(new SType("Boolean"), new SName("nerd"), new SBoolean(true));
		object.fields.add(field1);
		
		SField field2 = new SField(new SType("Integer"), new SName("age"), new SNumber(new Integer(
		        23)));
		object.fields.add(field2);
		
		StringBuffer buf = new StringBuffer();
		object.toSyntax(buf);
		System.out.println(buf.toString());
	}
	
}
