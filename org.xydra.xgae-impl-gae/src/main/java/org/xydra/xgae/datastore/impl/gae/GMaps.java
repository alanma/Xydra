package org.xydra.xgae.datastore.impl.gae;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class GMaps {

	public static Map<SKey, SEntity> wrap(Map<Key, Entity> raw) {
		if (raw == null)
			return null;

		Map<SKey, SEntity> map = new HashMap<SKey, SEntity>(raw.size());
		for (Entry<Key, Entity> e : raw.entrySet()) {

			GKey gkey = GKey.wrap(e.getKey());
			GEntity gentity = GEntity.wrap(e.getValue());

			map.put(gkey, gentity);
		}

		return map;
	}

}
