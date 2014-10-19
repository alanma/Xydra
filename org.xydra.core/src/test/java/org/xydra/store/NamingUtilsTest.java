package org.xydra.store;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.base.XId;
import org.xydra.core.XX;


public class NamingUtilsTest {
    
    @Test
    public void testEncoding() {
        XId modelId = XX.toId("my-model_.id_here");
        String indexName = "hello_.wo--rld";
        XId indexModelId = NamingUtils.getIndexModelId(modelId, indexName);
        
        XId modelId2 = NamingUtils.getBaseModelIdForIndexModelId(indexModelId);
        assertEquals(modelId, modelId2);
        
        String indexName2 = NamingUtils.getIndexNameForIndexModelId(indexModelId);
        assertEquals(indexName, indexName2);
    }
    
}
