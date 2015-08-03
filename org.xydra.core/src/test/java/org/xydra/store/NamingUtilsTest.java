package org.xydra.store;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.core.XX;


public class NamingUtilsTest {

    @Test
    public void testEncoding() {
        final XId modelId = Base.toId("my-model_.id_here");
        final String indexName = "hello_.wo--rld";
        final XId indexModelId = NamingUtils.getIndexModelId(modelId, indexName);

        final XId modelId2 = NamingUtils.getBaseModelIdForIndexModelId(indexModelId);
        assertEquals(modelId, modelId2);

        final String indexName2 = NamingUtils.getIndexNameForIndexModelId(indexModelId);
        assertEquals(indexName, indexName2);
    }

}
