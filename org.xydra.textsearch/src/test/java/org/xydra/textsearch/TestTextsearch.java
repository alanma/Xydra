package org.xydra.textsearch;

import org.junit.Test;
import org.xydra.textsearch.PragmaticTextSearch.Normaliser;
import org.xydra.textsearch.impl.PTSImpl;



public class TestTextsearch {
	
	@Test
	public void testBasicUsage() {
		
		PTSImpl<PayloadObject> pts = new PTSImpl<PayloadObject>();
		pts.configure(" ", " ", new Normaliser() {
			
			public String normalise(String raw) {
				String n = raw.toLowerCase();
				n.replace("é", "e");
				return n;
			}
		});
		
		PayloadObject p1 = new PayloadObject();
		p1.payload = "p1";
		pts.index(p1, "1");
		
		PayloadObject p2 = new PayloadObject();
		p2.payload = "p2";
		pts.index(p2, "2");
		
		pts.search("1");
		
	}
	
	static class PayloadObject {
		
		String payload;
		
	}
	
}
