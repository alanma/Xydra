package org.xydra.schema.parser;

import org.xydra.schema.model.SObject;


public class XSchemaParserOld {
	
	public static SObject parseAsObject(String objectDef) {
		return SObject.parse(objectDef);
	}
	
}
