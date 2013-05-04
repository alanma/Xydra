package org.xydra.core.serialize.xml;

import java.io.IOException;

import org.xydra.core.serialize.AbstractSerializedModelTest;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;


public class XmlModelTest extends AbstractSerializedModelTest {
	
	@Override
	protected XydraParser getParser() {
		return new XmlParser();
	}
	
	@Override
	protected XydraSerializer getSerializer() {
		return new XmlSerializer();
	}
	
	@Override
	protected String getFileLocation() throws IOException {
		String fileLocation = "C:/Users/Andi/workspace_421/core-newmax/src/test/resources/serializedfiles/Phonebook_oldLog.xml";
		return fileLocation;
	}
}
