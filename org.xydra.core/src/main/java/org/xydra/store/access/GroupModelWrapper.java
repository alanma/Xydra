package org.xydra.store.access;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.model.XWritableObject;
import org.xydra.core.value.XIDSetValue;
import org.xydra.core.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
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
	
	private static final Logger log = LoggerFactory.getLogger(GroupModelWrapper.class);
	
	public static final XID hasMember = XX.toId("hasMember");
	public static final XID hasTransitiveMember = XX.toId("hasTransitiveMember");
	public static final XID isMemberOf = XX.toId("isMemberOf");
	public static final XID isTransitiveMemberOf = XX.toId("isTransitiveMemberOf");
	
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
		this.dataModel = new WritableModel(this.credentials, store, X.getIDProvider()
		        .fromComponents(repositoryId, modelId, null, null));
		this.indexModel = new WritableModel(this.credentials, store, X.getIDProvider()
		        .fromComponents(repositoryId, XX.toId(modelId + "-index-by-actor"), null, null));
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
	public void addToGroup(XID actorOrGroupId, XID groupId) throws CycleException {
		log.trace("=== " + actorOrGroupId + " subGroupOf " + groupId);
		
		/**
		 * In general, we have the following situation:
		 * 
		 * <pre>
		 * A -> B -> C ... D -> E -> F
		 * </pre>
		 * 
		 * where '->' denotes subGroup and '...' is the link we are just
		 * creating.
		 */
		
		/**
		 * for clarity of the algorithm, we rename the inputs. Instead of
		 * 'isMemberOf' we says 'subGroup' and instead of 'group' we say
		 * 'superGroup'.
		 */
		XID c = actorOrGroupId;
		XID d = groupId;
		
		/**
		 * (1) The direct links and its inverse:
		 * 
		 * <pre>
		 * C.directSupergroup -> ADD D 
		 * D.directSubGroup   -> ADD C
		 * </pre>
		 */
		addToXIDSetValueInObject(this.indexModel, c, isMemberOf, d);
		addToXIDSetValueInObject(this.dataModel, d, hasMember, c);
		
		/**
		 * (2) add direct links additionally as transitive links
		 * 
		 * <pre>
		 * C.transitiveSupergroup -> ADD D.transitiveSupergroup 
		 * D.transitiveSubgroup   -> ADD C.transitiveSubgroup
		 * </pre>
		 */
		addToXIDSetValueInObject(this.indexModel, c, isTransitiveMemberOf, d);
		addToXIDSetValueInObject(this.indexModel, d, hasTransitiveMember, c);
		
		/**
		 * (3a) Each transitive superGroup of D is a transitive superGroup of [C
		 * and all it transitive subGroups].
		 * 
		 * <pre>
		 * C.transitiveSuperGroup -> ADD D.transitiveSuperGroups
		 * for all A in C.transitiveSubGroup: 
		 *   A.transitiveSuperGroup -> ADD D.transitiveSuperGroups
		 * </pre>
		 */
		for(XID f : getAllGroups(d)) {
			log.trace(":: " + f + " hasSuperGroup " + f);
			addToXIDSetValueInObject(this.indexModel, f, hasTransitiveMember, c);
			addToXIDSetValueInObject(this.indexModel, c, isTransitiveMemberOf, f);
			for(XID a : this.getAllMembers(c)) {
				addToXIDSetValueInObject(this.indexModel, d, hasTransitiveMember, a);
				addToXIDSetValueInObject(this.indexModel, a, isTransitiveMemberOf, d);
				addToXIDSetValueInObject(this.indexModel, f, hasTransitiveMember, a);
				addToXIDSetValueInObject(this.indexModel, a, isTransitiveMemberOf, f);
			}
		}
		
		/**
		 * (3b) Each transitive subGroup of C is a transitive subGroup of [D and
		 * all its transitive superGroups].
		 * 
		 * <pre>
		 * D.transitiveSubGroup -> ADD C.transitiveSubGroup
		 * for all F in D.transitiveSuperGroup: 
		 *   F.transitiveSubGroup: ADD C.transitiveSubGroup
		 * </pre>
		 */
		for(XID a : getAllMembers(c)) {
			log.trace(":: " + c + " hasSubGroup " + a);
			addToXIDSetValueInObject(this.indexModel, a, isTransitiveMemberOf, d);
			addToXIDSetValueInObject(this.indexModel, d, hasTransitiveMember, a);
			for(XID f : this.getAllGroups(d)) {
				addToXIDSetValueInObject(this.indexModel, c, isTransitiveMemberOf, f);
				addToXIDSetValueInObject(this.indexModel, f, hasTransitiveMember, c);
				addToXIDSetValueInObject(this.indexModel, a, isTransitiveMemberOf, f);
				addToXIDSetValueInObject(this.indexModel, f, hasTransitiveMember, a);
			}
		}
		
	}
	
	/* transitive */
	@Override
	public Set<XID> getAllGroups(XID actor) {
		XWritableObject actorObject = this.indexModel.getObject(actor);
		if(actorObject == null) {
			return Collections.emptySet();
		}
		
		XWritableField field = actorObject.getField(isTransitiveMemberOf);
		if(field == null) {
			return Collections.emptySet();
		}
		
		XValue value = field.getValue();
		if(value == null) {
			return Collections.emptySet();
		}
		
		return ((XIDSetValue)value).toSet();
	}
	
	/* transitive */
	@Override
	public Set<XID> getAllMembers(XID group) {
		return getXIDSetValue(this.indexModel, group, hasTransitiveMember);
	}
	
	/* direct */
	@Override
	public Set<XID> getDirectGroups() {
		Iterator<XID> it = this.dataModel.iterator();
		Set<XID> set = new HashSet<XID>();
		while(it.hasNext()) {
			XID xid = it.next();
			set.add(xid);
		}
		return set;
	}
	
	/* direct */
	@Override
	public Set<XID> getDirectGroups(XID actorOrGroupId) {
		return getXIDSetValue(this.indexModel, actorOrGroupId, isMemberOf);
	}
	
	/* direct */
	@Override
	public Set<XID> getDirectMembers(XID group) {
		return getXIDSetValue(this.dataModel, group, hasMember);
	}
	
	@Override
	public boolean hasDirectGroup(XID actor, XID group) {
		return valueContainsId(this.dataModel, actor, isMemberOf, group);
	}
	
	/* transitive */
	@Override
	public boolean hasGroup(XID actor, XID group) {
		return valueContainsId(this.indexModel, actor, isTransitiveMemberOf, group);
	}
	
	private void removeFromGroupNonTransitive(XID actorOrGroupId, XID groupId) {
		removeFromXIDSetValueInObject(this.dataModel, groupId, hasMember, actorOrGroupId);
		// update indexes
		removeFromXIDSetValueInObject(this.indexModel, actorOrGroupId, isMemberOf, groupId);
		// update transitive indexes
		removeFromXIDSetValueInObject(this.indexModel, actorOrGroupId, isTransitiveMemberOf,
		        groupId);
		removeFromXIDSetValueInObject(this.indexModel, groupId, hasTransitiveMember, actorOrGroupId);
	}
	
	@Override
	public void removeFromGroup(XID actorOrGroupId, XID groupId) {
		/**
		 * In general, we have the following situation:
		 * 
		 * <pre>
		 * A -> B -> C ... D -> E -> F
		 * </pre>
		 * 
		 * where '->' denotes subGroup and '...' is the link we are just
		 * removing.
		 */
		
		/**
		 * for clarity of the algorithm, we rename the inputs. Instead of
		 * 'isMemberOf' we says 'subGroup' and instead of 'group' we say
		 * 'superGroup'.
		 */
		XID c = actorOrGroupId;
		XID d = groupId;
		
		/* (1) removed direct links */
		removeFromXIDSetValueInObject(this.indexModel, c, isMemberOf, d);
		removeFromXIDSetValueInObject(this.dataModel, d, hasMember, c);
		
		/*
		 * 
		 * removeFromGroupNonTransitive(actorOrGroupId, groupId); // TODO fix
		 * transitive sets if we just removed a group-group link
		 * 
		 * /* re-build transitive sets
		 */

	}
	
	public void dump() {
		System.out.println("All groups:");
		for(XID groupId : getDirectGroups()) {
			dumpGroupId(groupId);
		}
	}
	
	public void dumpGroupId(XID groupId) {
		System.out.println("=== " + groupId);
		System.out.println("*     All groups: " + getAllGroups(groupId));
		System.out.println("*    All members: " + getAllMembers(groupId));
		System.out.println("*  Direct groups: " + getDirectGroups(groupId));
		System.out.println("* Direct members: " + getDirectMembers(groupId));
	}
	
}
