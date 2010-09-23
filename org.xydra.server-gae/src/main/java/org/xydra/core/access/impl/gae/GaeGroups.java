package org.xydra.core.access.impl.gae;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.XGroupEvent;
import org.xydra.core.access.XGroupListener;
import org.xydra.core.access.impl.memory.MemoryGroupDatabase;
import org.xydra.core.change.ChangeType;
import org.xydra.core.model.XID;
import org.xydra.server.impl.gae.GaeTestfixer;
import org.xydra.server.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * Utility that can persist and load an {@link XGroupDatabase} in the GAE
 * datastore.
 * 
 * The XGroupDatabase is represented by a root entity containing the {@link XID}
 * s of all known groups. There is also one entity for each group containing the
 * {@link XID}s of the actors in that group. The group/actor lists are both
 * saved as an unindexed property containing a String List.
 * 
 * IMPROVE create a real GAE XGroupDatabase implementation to lower startup
 * costs
 * 
 * 
 * @author dscharrer
 * 
 */
public class GaeGroups {
	
	private static final String NAME_GROUPDB = "groups";
	private static final String KIND_GROUPDB = "groupdb";
	private static final String PROP_GROUPS = "groups";
	private static final String KIND_GROUP = "group";
	private static final String PREFIX_GROUP = NAME_GROUPDB + "/";
	private static final String PROP_ACTORS = "actors";
	
	/**
	 * Load the whole group membership database from the GAE datastore into
	 * memory. Changes to the returned {@link XGroupDatabase} are persisted.
	 */
	@SuppressWarnings("unchecked")
	public static XGroupDatabase loadGroups() {
		
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// Load the root entity of the group database.
		Key groupdbKey = getGroupDBKey();
		Entity groupdb = GaeUtils.getEntity(groupdbKey);
		XGroupDatabase groups = new MemoryGroupDatabase();
		
		if(groupdb != null) {
			
			// For each known group load the list of actors and record the
			// membership.
			List<String> groupIdStrs = (List<String>)groupdb.getProperty(PROP_GROUPS);
			for(String groupIdStr : groupIdStrs) {
				XID groupId = XX.toId(groupIdStr);
				Key groupKey = getGroupKey(groupId);
				Entity group = GaeUtils.getEntity(groupKey);
				List<String> actorIdStrs = (List<String>)group.getProperty(PROP_ACTORS);
				for(String actorIdStr : actorIdStrs) {
					XID actorId = actorIdStr == null ? null : XX.toId(actorIdStr);
					groups.addToGroup(actorId, groupId);
				}
			}
			
		} else {
			// There was no group db in the state store, so just return an
			// empty one.
			// The entity will be created by the Persister when adding groups.
		}
		
		// Listen to changes made so they can be persisted.
		groups.addListener(new Persister(groups));
		
		return groups;
	}
	
	private static Key getGroupDBKey() {
		return KeyFactory.createKey(KIND_GROUPDB, NAME_GROUPDB);
	}
	
	private static Key getGroupKey(XID groupId) {
		return KeyFactory.createKey(KIND_GROUP, PREFIX_GROUP + groupId.toString());
	}
	
	/**
	 * Save the given list of known groups in the root entity.
	 */
	private static void saveGroupList(Iterator<XID> groups) {
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
	
	/**
	 * Save the given list of actors in the entity for the given group.
	 */
	private static void saveGroup(XID groupId, Iterator<XID> actors) {
		Key key = getGroupKey(groupId);
		Entity e = new Entity(key);
		e.setUnindexedProperty(PROP_GROUPS, asStringList(actors));
		GaeUtils.putEntity(e);
	}
	
	private static void deleteGroup(XID groupId) {
		GaeUtils.deleteEntity(getGroupKey(groupId));
	}
	
	/**
	 * Listen to {@link XGroupEvent}s and persist in data store immediately.
	 * 
	 * @author dscharrer
	 */
	private static class Persister implements XGroupListener {
		
		private final XGroupDatabase groups;
		
		public Persister(XGroupDatabase groups) {
			this.groups = groups;
		}
		
		public void onGroupEvent(XGroupEvent event) {
			
			// Get the actors that are now in the changed group.
			XID groupId = event.getGroup();
			Iterator<XID> it = this.groups.getMembers(groupId);
			
			// FIXME handle concurrency
			
			// Update the group.
			if(it.hasNext()) {
				saveGroup(groupId, it);
				
				// If this is an add event and the group has only one actor, the
				// group must be new, so add it to the group list.
				if(event.getChangeType() == ChangeType.ADD) {
					it = this.groups.getMembers(groupId);
					it.next();
					if(!it.hasNext()) {
						saveGroupList(this.groups.getGroups());
					}
				}
				
			} else {
				deleteGroup(groupId);
				saveGroupList(this.groups.getGroups());
			}
			
		}
	}
	
}
