package org.xydra.store.impl.gae;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;



public class TestSyncDatastore {
    
    @Test
    public void testBatchPut() {
        ArrayList<SEntity> list = new ArrayList<SEntity>();
        list.add(XGae.get().datastore().createEntity(XGae.get().datastore().createKey("kind1", "key1")));
        list.add(XGae.get().datastore().createEntity(XGae.get().datastore().createKey("kind1", "key2")));
        list.add(XGae.get().datastore().createEntity(XGae.get().datastore().createKey("kind1", "key3")));
        
        XGae.get().datastore().sync().putEntities(list);
        
        Map<SKey,SEntity> map = XGae.get().datastore().sync().getEntities(Arrays.asList(
        
        XGae.get().datastore().createKey("kind1", "key1"), XGae.get().datastore().createKey("kind1", "key2")
        
        ));
        for(Entry<SKey,SEntity> a : map.entrySet()) {
            System.out.println(a.getKey() + "=" + a.getValue());
        }
    }
    
}
