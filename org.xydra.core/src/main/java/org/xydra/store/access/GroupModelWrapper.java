package org.xydra.store.access;

import java.util.Iterator;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableObject;
import org.xydra.core.value.XIDSetValue;
import org.xydra.core.value.XValue;
import org.xydra.store.XydraStore;
import org.xydra.store.base.Credentials;
import org.xydra.store.base.WritableModel;


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
public class GroupModelWrapper implements XGroupDatabase {
	
	public static final XID hasMember = XX.toId("hasMember");
	public static final XID hasTransitiveMember = XX.toId("hasTransitiveMember");
	public static final XID isMemberOf = XX.toId("isMemberOf");
	public static final XID isTransitiveMemberOf = XX.toId("isMemberOfTransitive");
	
	private static final long serialVersionUID = 3858107275113200924L;
	
	private static void addToXIDSetValueInObject(WritableModel model, XID objectId, XID fieldId,
	        XID addedValue) {
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
	
	private static Iterator<XID> getXIDSetValueIterator(WritableModel model, XID objectId,
	        XID fieldId) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			return null;
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return null;
		}
		XValue value = field.getValue();
		return ((XIDSetValue)value).iterator();
	}
	
	private static boolean valueContainsId(WritableModel model, XID objectId, XID fieldId,
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
	
	private WritableModel dataModel, indexByActor;
	
	public GroupModelWrapper(XydraStore store, XID repositoryId, XID modelId) {
		this.dataModel = new WritableModel(this.credentials, store, X.getIDProvider()
		        .fromComponents(repositoryId, modelId, null, null));
		this.indexByActor = new WritableModel(this.credentials, store, X.getIDProvider()
		        .fromComponents(repositoryId, XX.toId(modelId + "-index-by-actor"), null, null));
	}
	
	@Override
	public void addToGroup(XID actorOrGroupId, XID groupId) throws CycleException {
		addToGroupNonTransitive(actorOrGroupId, groupId);
		/*
		 * more updates if we just added a group-subgroup link = all members of
		 * actorOrGroupId are now also members of groupId
		 */
		Iterator<XID> transitiveMemberIt = this.getAllMembers(actorOrGroupId);
		while(transitiveMemberIt.hasNext()) {
			XID transitiveMemberId = transitiveMemberIt.next();
			this.addToGroupNonTransitive(transitiveMemberId, groupId);
		}
		Iterator<XID> transitiveGroupsIt = this.getAllGroups(actorOrGroupId);
		while(transitiveGroupsIt.hasNext()) {
			XID transitiveGroupId = transitiveGroupsIt.next();
			this.addToGroupNonTransitive(transitiveGroupId, groupId);
		}
		
	}
	
	private void addToGroupNonTransitive(XID actorOrGroupId, XID groupId) {
		addToXIDSetValueInObject(this.dataModel, groupId, hasMember, actorOrGroupId);
		// update indexes
		addToXIDSetValueInObject(this.indexByActor, actorOrGroupId, isMemberOf, groupId);
		// update transitive indexes
		addToXIDSetValueInObject(this.indexByActor, actorOrGroupId, isTransitiveMemberOf, groupId);
		addToXIDSetValueInObject(this.indexByActor, groupId, hasTransitiveMember, actorOrGroupId);
	}
	
	/* transitive */
	@Override
	public Iterator<XID> getAllGroups(XID actor) {
		XWritableObject actorObject = this.indexByActor.getObject(actor);
		if(actorObject == null) {
			return null;
		} else {
			return ((XIDSetValue)actorObject.getField(isMemberOf).getValue()).iterator();
		}
	}
	
	/* transitive */
	@Override
	public Iterator<XID> getAllMembers(XID group) {
		return getXIDSetValueIterator(this.dataModel, group, hasTransitiveMember);
	}
	
	/* direct */
	@Override
	public Iterator<XID> getDirectGroups() {
		return this.dataModel.iterator();
	}
	
	/* direct */
	@Override
	public Iterator<XID> getDirectGroups(XID actor) {
		return getXIDSetValueIterator(this.indexByActor, actor, isMemberOf);
	}
	
	/* direct */
	@Override
	public Iterator<XID> getDirectMembers(XID group) {
		return getXIDSetValueIterator(this.dataModel, group, hasMember);
	}
	
	@Override
	public boolean hasDirectGroup(XID actor, XID group) {
		return valueContainsId(this.dataModel, actor, isMemberOf, group);
	}
	
	/* transitive */
	@Override
	public boolean hasGroup(XID actor, XID group) {
		return valueContainsId(this.indexByActor, actor, isTransitiveMemberOf, group);
	}
	
	@Override
	public void removeFromGroup(XID actor, XID group) {
		XWritableObject groupObject = this.dataModel.getObject(group);
		if(groupObject == null) {
			return;
		}
		XWritableField actorsInGroupField = groupObject.getField(hasMember);
		if(actorsInGroupField == null) {
			return;
		}
		XValue actorsInGroupValue = actorsInGroupField.getValue();
		if(actorsInGroupValue == null) {
			return;
		} else {
			actorsInGroupValue = ((XIDSetValue)actorsInGroupValue).remove(actor);
			actorsInGroupField.setValue(actorsInGroupValue);
		}
	}
	
}
