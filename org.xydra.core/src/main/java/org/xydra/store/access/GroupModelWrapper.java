package org.xydra.store.access;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.model.XWritableObject;
import org.xydra.core.model.XWritableRepository;
import org.xydra.core.value.XIDSetValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XValue;
import org.xydra.index.impl.FastEntrySetFactory;
import org.xydra.index.impl.MapSetIndex;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.NamingUtils;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.base.Credentials;
import org.xydra.store.base.HashUtils;
import org.xydra.store.base.WritableRepository;


/**
 * Wraps a XydraStore model to model groups and their members.
 * 
 * 
 * <h3>Implementation Note</h3>
 * 
 * <h4>Data modelling</h4> Group membership (group->actors)
 * 
 * <pre>
 * objectId | fieldId     | value
 * ---------+-------------+----------------------------
 * groupId  | "hasMember" | {@link XIDSetValue} actors
 * </pre>
 * 
 * Passwords
 * 
 * <pre>
 * objectId | fieldId           | value
 * ---------+-------------------+----------------------------
 * actorId  | "hasPasswordHash" | the password hash (see {@link HashUtils})
 * </pre>
 * 
 * 
 * <h3>FUTURE IMPL -- currently index is in memory only</h3> <h4>Indexes for
 * faster access</h4> Group membership (actor->groups)
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
	
	private XWritableModel dataModel;
	
	private MapSetIndex<XID,XID> actor2groups = null;
	
	/**
	 * @param credentials to authenticate and authorise to store
	 * @param store
	 */
	public GroupModelWrapper(XydraStore store) {
		Credentials credentials;
		XydraStoreAdmin admin = store.getXydraStoreAdmin();
		if(admin != null) {
			// use XydraAdmin account
			credentials = new Credentials(XydraStoreAdmin.XYDRA_ADMIN_ID,
			        admin.getXydraAdminPasswordHash());
		} else {
			// use a bogus account and hope the store has an allow-all policy
			credentials = new Credentials(XX.toId("GroupModelWrapper"),
			        HashUtils.getXydraPasswordHash("secret"));
		}
		XWritableRepository repository = new WritableRepository(credentials, store);
		this.dataModel = repository.createModel(NamingUtils.ID_ACCOUNT_MODEL);
	}
	
	/**
	 * To test
	 * 
	 * @param repository
	 * @param modelId
	 */
	protected GroupModelWrapper(XRepository repository, XID modelId) {
		this.dataModel = repository.createModel(modelId);
	}
	
	@Override
	public void addToGroup(XID actorId, XID groupId) {
		XidSetValueUtils.addToXIDSetValueInObject(this.dataModel, groupId, hasMember, actorId);
		
		ensureIndexIsInitialised();
		this.actor2groups.index(actorId, groupId);
	}
	
	private void ensureIndexIsInitialised() {
		if(this.actor2groups == null) {
			this.actor2groups = new MapSetIndex<XID,XID>(new FastEntrySetFactory<XID>());
		}
	}
	
	@Override
	public Set<XID> getGroupsOf(XID actor) {
		ensureIndexIsInitialised();
		Iterator<XID> it = this.actor2groups.constraintIterator(new EqualsConstraint<XID>(actor));
		Set<XID> result = new HashSet<XID>();
		while(it.hasNext()) {
			XID xid = it.next();
			result.add(xid);
		}
		return result;
	}
	
	/* transitive */
	@Override
	public Set<XID> getMembersOf(XID group) {
		return XidSetValueUtils.getXIDSetValue(this.dataModel, group, hasMember);
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
		ensureIndexIsInitialised();
		return this.actor2groups.contains(new EqualsConstraint<XID>(actor),
		        new EqualsConstraint<XID>(group));
	}
	
	@Override
	public void removeFromGroup(XID actorId, XID groupId) {
		XidSetValueUtils.removeFromXIDSetValueInObject(this.dataModel, groupId, hasMember, actorId);
		ensureIndexIsInitialised();
		this.actor2groups.deIndex(actorId, groupId);
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
