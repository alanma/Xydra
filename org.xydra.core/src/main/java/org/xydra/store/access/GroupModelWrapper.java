package org.xydra.store.access;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.model.XWritableObject;
import org.xydra.core.model.XWritableRepository;
import org.xydra.core.value.XIDSetValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraStore;
import org.xydra.store.base.Credentials;
import org.xydra.store.base.WritableRepository;


/**
 * Wraps a XydraStore model to model groups and their members.
 * 
 * Data modelling:
 * 
 * <pre>
 * objectId | fieldId     | value
 * ---------+-------------+----------------------------
 * groupId  | "hasMember" | {@link XIDSetValue} actors
 * </pre>
 * 
 * Indexes:
 * 
 * <pre>
 * objectId | fieldId     | value
 * ---------+-------------+----------------------------
 * actorId  | "isMemberOf" | {@link XIDSetValue} groupIds
 * </pre>
 * 
 * @author voelkel
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
public class GroupModelWrapper implements XGroupDatabase, XPasswordDatabase {
	
	private static final Logger log = LoggerFactory.getLogger(GroupModelWrapper.class);
	
	public static final XID hasMember = XX.toId("hasMember");
	public static final XID isMemberOf = XX.toId("isMemberOf");
	public static final XID hasPasswordHash = XX.toId("hasPasswordHash");
	
	private static final long serialVersionUID = 3858107275113200924L;
	
	private static void addToXIDSetValueInObject(XWritableModel model, XID objectId, XID fieldId,
	        XID addedValue) {
		log.trace(objectId + " " + fieldId + " " + addedValue + " .");
		
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			object = model.createObject(objectId);
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			field = object.createField(fieldId);
		}
		XValue currentValue = field.getValue();
		if(currentValue == null) {
			currentValue = X.getValueFactory().createIDSetValue(new XID[] { addedValue });
		} else {
			currentValue = ((XIDSetValue)currentValue).add(addedValue);
		}
		field.setValue(currentValue);
	}
	
	private static void removeFromXIDSetValueInObject(XWritableModel model, XID objectId,
	        XID fieldId, XID removedValue) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			return;
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return;
		}
		XValue currentValue = field.getValue();
		if(currentValue == null) {
			return;
		} else {
			currentValue = ((XIDSetValue)currentValue).remove(removedValue);
		}
		field.setValue(currentValue);
	}
	
	private static Set<XID> getXIDSetValue(XWritableModel model, XID objectId, XID fieldId) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			return Collections.emptySet();
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return Collections.emptySet();
		}
		XValue value = field.getValue();
		return ((XIDSetValue)value).toSet();
	}
	
	private static boolean valueContainsId(XWritableModel model, XID objectId, XID fieldId,
	        XID valueId) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			throw new IllegalArgumentException("Object " + objectId + " not found");
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return false;
		}
		XValue value = field.getValue();
		return ((XIDSetValue)value).contains(valueId);
	}
	
	// FIXME get credentials from config settings
	private Credentials credentials = new Credentials(XX.toId("__accessManager"), "TODO");
	
	private XWritableModel dataModel, indexModel;
	
	public GroupModelWrapper(XydraStore store, XID repositoryId, XID modelId) {
		XAddress repoAddr = XX.toAddress(repositoryId, null, null, null);
		XWritableRepository repo = new WritableRepository(this.credentials, store, repoAddr);
		this.dataModel = repo.createModel(modelId);
		this.indexModel = repo.createModel(XX.toId(modelId + "-index-by-actor"));
		// FIXME a malicious user might be able to overwrite this index
	}
	
	/**
	 * To test
	 * 
	 * @param repository
	 * @param modelId
	 */
	protected GroupModelWrapper(XRepository repository, XID modelId) {
		this.dataModel = repository.createModel(modelId);
		this.indexModel = repository.createModel(XX.toId(modelId + "-index-by-actor"));
	}
	
	@Override
	public void addToGroup(XID actorId, XID groupId) {
		addToXIDSetValueInObject(this.dataModel, groupId, hasMember, actorId);
		addToXIDSetValueInObject(this.indexModel, actorId, isMemberOf, groupId);
	}
	
	@Override
	public Set<XID> getGroupsOf(XID actor) {
		return getXIDSetValue(this.indexModel, actor, isMemberOf);
	}
	
	/* transitive */
	@Override
	public Set<XID> getMembersOf(XID group) {
		return getXIDSetValue(this.dataModel, group, hasMember);
	}
	
	/* direct */
	@Override
	public Set<XID> getGroups() {
		Iterator<XID> it = this.dataModel.iterator();
		Set<XID> set = new HashSet<XID>();
		while(it.hasNext()) {
			XID xid = it.next();
			set.add(xid);
		}
		return set;
	}
	
	@Override
	public boolean hasGroup(XID actor, XID group) {
		return valueContainsId(this.indexModel, actor, isMemberOf, group);
	}
	
	@Override
	public void removeFromGroup(XID actorId, XID groupId) {
		removeFromXIDSetValueInObject(this.dataModel, groupId, hasMember, actorId);
		removeFromXIDSetValueInObject(this.indexModel, actorId, isMemberOf, groupId);
	}
	
	public void dump() {
		log.info("All groups:");
		for(XID groupId : getGroups()) {
			dumpGroupId(groupId);
		}
	}
	
	public void dumpGroupId(XID groupId) {
		log.info("=== " + groupId);
		log.info("*     All groups: " + getGroupsOf(groupId));
		log.info("*    All members: " + getMembersOf(groupId));
	}
	
	@Override
	public void removePasswordHash(XID actorId) {
		XWritableObject actor = this.dataModel.getObject(actorId);
		if(actor == null) {
			return;
		}
		XWritableField field = actor.getField(hasPasswordHash);
		if(field == null) {
			return;
		}
		field.setValue(null);
	}
	
	@Override
	public void setPasswordHash(XID actorId, String passwordHash) {
		XWritableObject actor = this.dataModel.getObject(actorId);
		if(actor == null) {
			actor = this.dataModel.createObject(actorId);
		}
		XWritableField field = actor.getField(hasPasswordHash);
		if(field == null) {
			field = actor.createField(hasPasswordHash);
		}
		field.setValue(X.getValueFactory().createStringValue(passwordHash));
	}
	
	@Override
	public boolean isValidLogin(XID actorId, String passwordHash) {
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorId and passwordHash must not be null");
		}
		XWritableObject actor = this.dataModel.getObject(actorId);
		if(actor == null) {
			return false;
		}
		XWritableField field = actor.getField(hasPasswordHash);
		if(field == null) {
			return false;
		}
		XValue value = field.getValue();
		if(value == null) {
			return false;
		}
		return ((XStringValue)value).contents().equals(passwordHash);
	}
	
}
