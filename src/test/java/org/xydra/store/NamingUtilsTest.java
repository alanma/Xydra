package org.xydra.store;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.xydra.base.XID;
import org.xydra.base.XX;


public class NamingUtilsTest {
	
	@Test
	public void testEncoding() {
		XID modelId = XX.toId("my-model_.id_here");
		String indexName = "hello_.wo--rld";
		XID indexModelId = NamingUtils.getIndexModelId(modelId, indexName);
		
		XID modelId2 = NamingUtils.getBaseModelIdForIndexModelId(indexModelId);
		assertEquals(modelId, modelId2);
		
		String indexName2 = NamingUtils.getIndexNameForIndexModelId(indexModelId);
		assertEquals(indexName, indexName2);
	}
	
}
