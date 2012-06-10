package org.xydra.gae.datalogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.store.impl.gae.UniCache;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;


public class DataRecordEntryHandler implements UniCache.DatastoreEntryHandler<DataRecord> {
	
	@Override
	public Entity toEntity(final Key datastoreKey, final DataRecord entry) {
		Entity e = new Entity(datastoreKey);
		e.setUnindexedProperty(DataRecord.CREATION_DATE, entry.getCreationDate());
		for(Entry<String,String> pair : entry.map.entrySet()) {
			assert !pair.getKey().startsWith("_");
			if(pair.getKey().startsWith(DataRecord.INDEXED_PREFIX)) {
				e.setProperty(pair.getKey(), pair.getValue());
			} else {
				String s = pair.getValue();
				if(s == null) {
					continue;
				}
				if(s.length() > 400) {
					Text value = new Text(s);
					e.setUnindexedProperty(pair.getKey(), value);
				} else {
					e.setUnindexedProperty(pair.getKey(), s);
				}
			}
		}
		entry.key = datastoreKey.getName();
		return e;
	}
	
	@Override
	public DataRecord fromEntity(final Entity entity) {
		Map<String,String> map = new HashMap<String,String>();
		long creationDate = -1;
		
		for(Entry<String,Object> prop : entity.getProperties().entrySet()) {
			if(prop.getKey().equals(DataRecord.CREATION_DATE)) {
				creationDate = (Long)prop.getValue();
			} else {
				Object v = prop.getValue();
				String s;
				if(v instanceof Text) {
					s = ((Text)v).getValue();
				} else {
					s = v.toString();
				}
				map.put(prop.getKey(), s);
			}
		}
		DataRecord dr = new DataRecord(creationDate, map);
		dr.key = entity.getKey().getName();
		return dr;
	}
	
}
