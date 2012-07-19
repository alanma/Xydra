package org.xydra.store.impl.gae;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


public class TestSyncDatastore {
	
	@Test
	public void testBatchPut() {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		ArrayList<Entity> list = new ArrayList<Entity>();
		list.add(new Entity("kind1", "key1"));
		list.add(new Entity("kind1", "key2"));
		list.add(new Entity("kind1", "key3"));
		
		SyncDatastore.putEntities(list);
		
		Map<Key,Entity> map = SyncDatastore.getEntities(Arrays.asList(
		        KeyFactory.createKey("kind1", "key1"), KeyFactory.createKey("kind1", "key2")));
		for(Entry<Key,Entity> a : map.entrySet()) {
			System.out.println(a.getKey() + "=" + a.getValue());
		}
	}
	
}
