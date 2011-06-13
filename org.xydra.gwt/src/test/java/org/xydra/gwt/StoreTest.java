package org.xydra.gwt;

import org.junit.Test;
import org.xydra.core.serialize.XydraParser;
import org.xydra.gwt.xml.GwtXmlParser;

import com.google.gwt.junit.client.GWTTestCase;


public class StoreTest extends GWTTestCase {
	
	@Override
	public String getModuleName() {
		return "org.xydra.gwt.XydraGwtTest";
	}
	
	@Test
	public void test() {
		
		XydraParser parser = new GwtXmlParser();
		assertNotNull(parser);
		
	}
	
}
