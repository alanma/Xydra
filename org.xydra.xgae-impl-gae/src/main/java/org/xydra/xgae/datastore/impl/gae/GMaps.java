package org.xydra.xgae.datastore.impl.gae;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class GMaps {

	public static Map<SKey, SEntity> wrap(final Map<Key, Entity> raw) {
		if (raw == null) {
			return null;
		}

		final Map<SKey, SEntity> map = new HashMap<SKey, SEntity>(raw.size());
		for (final Entry<Key, Entity> e : raw.entrySet()) {

			final GKey gkey = GKey.wrap(e.getKey());
			final GEntity gentity = GEntity.wrap(e.getValue());

			map.put(gkey, gentity);
		}

		return map;
	}

}
