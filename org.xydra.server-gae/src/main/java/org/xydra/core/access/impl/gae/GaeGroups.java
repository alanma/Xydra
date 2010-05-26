package org.xydra.core.access.impl.gae;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.X;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.XGroupEvent;
import org.xydra.core.access.XGroupListener;
import org.xydra.core.access.impl.memory.MemoryGroupDatabase;
import org.xydra.core.change.ChangeType;
import org.xydra.core.model.XID;
import org.xydra.server.gae.GaeTestfixer;
import org.xydra.server.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;



public class GaeGroups {
	
	private static final String NAME_GROUPDB = "groups";
	private static final String KIND_GROUPDB = "groupdb";
	private static final String PROP_GROUPS = "groups";
	private static final String KIND_GROUP = "group";
	private static final String PREFIX_GROUP = NAME_GROUPDB + "/";
	private static final String PROP_ACTORS = "actors";
	
	@SuppressWarnings("unchecked")
	public static XGroupDatabase loadGroups() {
		
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Key groupdbKey = getGroupDBKey();
		
		Entity groupdb = GaeUtils.getEntity(groupdbKey);
		
		XGroupDatabase groups = new MemoryGroupDatabase();
		groups.addListener(new Persister(groups));
		
		if(groupdb == null) {
			// new group database
			return groups;
		}
		
		List<String> groupIdStrs = (List<String>)groupdb.getProperty(PROP_GROUPS);
		
		for(String groupIdStr : groupIdStrs) {
			
			XID groupId = X.getIDProvider().fromString(groupIdStr);
			
			Key groupKey = getGroupKey(groupId);
			
			Entity group = GaeUtils.getEntity(groupKey);
			
			List<String> actorIdStrs = (List<String>)group.getProperty(PROP_ACTORS);
			
			for(String actorIdStr : actorIdStrs) {
				
				XID actorId = actorIdStr == null ? null : X.getIDProvider().fromString(actorIdStr);
				
				groups.addToGroup(actorId, groupId);
				
			}
			
		}
		
		return groups;
	}
	
	private static Key getGroupDBKey() {
		return KeyFactory.createKey(KIND_GROUPDB, NAME_GROUPDB);
	}
	
	private static Key getGroupKey(XID groupId) {
		return KeyFactory.createKey(KIND_GROUP, PREFIX_GROUP + groupId.toString());
	}
	
	private static void saveGroups(Iterator<XID> groups) {
		
		Key key = getGroupDBKey();
		
		Entity e = new Entity(key);
		
		e.setUnindexedProperty(PROP_GROUPS, asStringList(groups));
		
		GaeUtils.putEntity(e);
	}
	
	private static List<String> asStringList(Iterator<XID> entries) {
		List<String> list = new ArrayList<String>();
		while(entries.hasNext()) {
			XID entry = entries.next();
			list.add(entry == null ? null : entry.toString());
		}
		return list;
	}
	
	private static void saveGroup(XID groupId, Iterator<XID> actors) {
		
		Key key = getGroupKey(groupId);
		
		Entity e = new Entity(key);
		
		e.setUnindexedProperty(PROP_GROUPS, asStringList(actors));
		
		GaeUtils.putEntity(e);
	}
	
	private static void deleteGroup(XID groupId) {
		GaeUtils.deleteEntity(getGroupKey(groupId));
	}
	
	private static class Persister implements XGroupListener {
		
		private final XGroupDatabase groups;
		
		public Persister(XGroupDatabase groups) {
			this.groups = groups;
		}
		
		public void onGroupEvent(XGroupEvent event) {
			XID groupId = event.getGroup();
			Iterator<XID> it = this.groups.getMembers(groupId);
			
			// update the group
			if(it.hasNext()) {
				saveGroup(groupId, it);
			} else {
				deleteGroup(groupId);
			}
			
			if(event.getChangeType() == ChangeType.ADD) {
				it = this.groups.getMembers(groupId);
				it.next();
				if(!it.hasNext()) {
					// this is the only and first element, add group to DB
					saveGroups(this.groups.getGroups());
				}
			}
		}
	}
	
}
