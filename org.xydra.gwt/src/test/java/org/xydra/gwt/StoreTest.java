package org.xydra.gwt;

import org.junit.Test;
import org.xydra.core.model.serialize.gwt.GwtXmlParser;
import org.xydra.core.serialize.XydraParser;

import com.google.gwt.junit.client.GWTTestCase;


public class StoreTest extends GWTTestCase {
	
	@Override
	public String getModuleName() {
		return "org.xydra.client.gwt.editor.XydraEditor";
	}
	
	@Test
	public void test() {
		
		XydraParser parser = new GwtXmlParser();
		assertNotNull(parser);
		
	}
	
}
