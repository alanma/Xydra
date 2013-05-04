package org.xydra.core.serialize.json;

import java.io.IOException;

import org.xydra.core.serialize.AbstractSerializedModelTest;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;


public class JsonModelTest extends AbstractSerializedModelTest {
	
	@Override
	protected XydraParser getParser() {
		return new JsonParser();
	}
	
	@Override
	protected XydraSerializer getSerializer() {
		return new JsonSerializer();
	}
	
	@Override
	protected String getFileLocation() throws IOException {
		String fileLocation = "C:/Users/Andi/workspace_421/core-newmax/src/test/resources/serializedfiles/Phonebook_oldLog.json";
		return fileLocation;
	}
	
}
