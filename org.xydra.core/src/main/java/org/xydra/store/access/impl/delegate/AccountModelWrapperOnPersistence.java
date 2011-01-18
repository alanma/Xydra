package org.xydra.store.access.impl.delegate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.value.XIDSetValue;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XStringValue;
import org.xydra.index.IEntrySet;
import org.xydra.index.impl.FastEntrySetFactory;
import org.xydra.index.impl.MapIndex;
import org.xydra.index.impl.MapSetIndex;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.MAXTodo;
import org.xydra.store.NamingUtils;
import org.xydra.store.XydraStore;
import org.xydra.store.access.XAccountDatabase;
import org.xydra.store.access.XidSetValueUtils;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * Wraps a {@link XydraStore} model to store accounts with passwords and groups
 * and their members. This is usually one special model per store.
 * 
 * See {@link Documentation_AccountModel} for the mapping from internal data
 * structures to Xydras repo/model/object/field layout.
 * 
 * @author voelkel
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
@MAXTodo
public class AccountModelWrapperOnPersistence implements XAccountDatabase {
	
	private static final Logger log = LoggerFactory
	        .getLogger(AccountModelWrapperOnPersistence.class);
	
	public static final XID hasMember = XX.toId("hasMember");
	public static final XID isMemberOf = XX.toId("isMemberOf");
	public static final XID hasPasswordHash = XX.toId("hasPasswordHash");
	public static final XID hasFailedLoginAttempts = XX.toId("hasFailedLoginAttempts");
	
	private static final long serialVersionUID = 3858107275113200924L;
	
	private transient MapSetIndex<XID,XID> actor2groups__index = null;
	private MapSetIndex<XID,XID> group_hasMember__actor = null;
	private MapIndex<XID,String> actor_hasPasswordHash__string = null;
	private MapIndex<XID,Integer> actor_hasFailedLoginAttempts__int = null;
	
	private XydraPersistence persistence;
	
	private XID executingActorId;
	
	private long currentModelRev;
	
	public AccountModelWrapperOnPersistence(XydraPersistence persistence, XID executingActorId) {
		this.persistence = persistence;
		this.executingActorId = executingActorId;
		initialiseFromRemoteSnapshot();
	}
	
	private void initialiseFromRemoteSnapshot() {
		XAddress accountModelAddress = X.getIDProvider().fromComponents(
		        this.persistence.getRepositoryId(), NamingUtils.ID_ACCOUNT_MODEL, null, null);
		XWritableModel accountModelSnapshot = this.persistence
		        .getModelSnapshot(accountModelAddress);
		// initialize internal data structures
		this.currentModelRev = accountModelSnapshot.getRevisionNumber();
		this.group_hasMember__actor = new MapSetIndex<XID,XID>(new FastEntrySetFactory<XID>());
		this.actor_hasPasswordHash__string = new MapIndex<XID,String>();
		this.actor_hasFailedLoginAttempts__int = new MapIndex<XID,Integer>();
		for(XID objectId : accountModelSnapshot) {
			XBaseObject object = accountModelSnapshot.getObject(objectId);
			for(XID fieldId : object) {
				// actorId | "hasPasswordHash" | the password hash (see {@link
				// HashUtils})
				if(fieldId.equals(hasPasswordHash)) {
					XBaseField field = object.getField(fieldId);
					this.actor_hasPasswordHash__string.index(objectId,
					        ((XStringValue)field.getValue()).contents());
				} else
				// actorId | "hasFailedLoginAttempts" | if present: number of
				// failed login attempts
				if(fieldId.equals(hasFailedLoginAttempts)) {
					XBaseField field = object.getField(fieldId);
					this.actor_hasFailedLoginAttempts__int.index(objectId,
					        ((XIntegerValue)field.getValue()).contents());
				} else
				// groupId | "hasMember" | {@link XIDSetValue} actors
				if(fieldId.equals(hasMember)) {
					XBaseField field = object.getField(fieldId);
					XIDSetValue members = (XIDSetValue)field.getValue();
					for(XID member : members) {
						this.group_hasMember__actor.index(objectId, member);
					}
				}
			}
		}
	}
	
	@Override
	@ModificationOperation
	public void addToGroup(XID actorId, XID groupId) {
		// in-memory state
		this.group_hasMember__actor.index(groupId, actorId);
		// in-memory index
		ensureIndexIsInitialised();
		this.actor2groups__index.index(actorId, groupId);
		// remote state
		Set<XID> members = entrySetToJavaSet(this.group_hasMember__actor.lookup(groupId));
		members.add(actorId);
		XIDSetValue membersValue = X.getValueFactory().createIDSetValue(members);
		this.currentModelRev = XidSetValueUtils.setValueInObject(this.persistence,
		        this.currentModelRev, this.executingActorId, groupId, hasMember, membersValue);
	}
	
	/**
	 * Initialise lazily.
	 */
	private void ensureIndexIsInitialised() {
		if(this.actor2groups__index == null) {
			this.actor2groups__index = new MapSetIndex<XID,XID>(new FastEntrySetFactory<XID>());
		}
	}
	
	@Override
	public Set<XID> getGroupsOf(XID actor) {
		checkRefreshFromPersistentData();
		ensureIndexIsInitialised();
		IEntrySet<XID> entrySet = this.actor2groups__index.lookup(actor);
		return entrySetToJavaSet(entrySet);
	}
	
	@SuppressWarnings("unchecked")
	private <T> Set<T> entrySetToJavaSet(IEntrySet<T> entrySet) {
		if(entrySet instanceof Set) {
			/* if created with FastEntrySetFactory, this works */
			return (Set<T>)entrySet;
		} // else: convert
		Iterator<T> it = entrySet.iterator();
		Set<T> result = new HashSet<T>();
		while(it.hasNext()) {
			T xid = it.next();
			result.add(xid);
		}
		return result;
		
	}
	
	@Override
	public Set<XID> getMembersOf(XID group) {
		checkRefreshFromPersistentData();
		IEntrySet<XID> members = this.group_hasMember__actor.lookup(group);
		return entrySetToJavaSet(members);
	}
	
	@Override
	public Set<XID> getGroups() {
		checkRefreshFromPersistentData();
		return this.group_hasMember__actor.keySet();
	}
	
	@Override
	public boolean hasGroup(XID actor, XID group) {
		checkRefreshFromPersistentData();
		ensureIndexIsInitialised();
		return this.actor2groups__index.contains(new EqualsConstraint<XID>(actor),
		        new EqualsConstraint<XID>(group));
	}
	
	@Override
	@ModificationOperation
	public void removeFromGroup(XID actorId, XID groupId) {
		// local state
		this.group_hasMember__actor.deIndex(groupId, actorId);
		// local index
		ensureIndexIsInitialised();
		this.actor2groups__index.deIndex(actorId, groupId);
		// remote state
		Set<XID> members = entrySetToJavaSet(this.group_hasMember__actor.lookup(groupId));
		members.remove(actorId);
		XIDSetValue membersValue = X.getValueFactory().createIDSetValue(members);
		this.currentModelRev = XidSetValueUtils.setValueInObject(this.persistence,
		        this.currentModelRev, this.executingActorId, groupId, hasMember, membersValue);
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
	@ModificationOperation
	public void removePasswordHash(XID actorId) {
		// local state
		this.actor_hasPasswordHash__string.deIndex(actorId);
		// remote state
		this.currentModelRev = this.persistence.executeCommand(
		        this.executingActorId,
		        X.getCommandFactory().createRemoveFieldCommand(this.persistence.getRepositoryId(),
		                NamingUtils.ID_ACCOUNT_MODEL, actorId, hasPasswordHash,
		                this.currentModelRev, false));
	}
	
	@Override
	@ModificationOperation
	public void setPasswordHash(XID actorId, String passwordHash) {
		// local state
		this.actor_hasPasswordHash__string.index(actorId, passwordHash);
		// remote state
		this.currentModelRev = XidSetValueUtils.setValueInObject(this.persistence,
		        this.currentModelRev, this.executingActorId, actorId, hasPasswordHash, X
		                .getValueFactory().createStringValue(passwordHash));
		assert this.currentModelRev != XCommand.FAILED;
	}
	
	@Override
	@ModificationOperation
	public int incrementFailedLoginAttempts(XID actorId) {
		int failedLoginAttepts = getFailedLoginAttempts(actorId);
		failedLoginAttepts++;
		// locale state
		this.actor_hasFailedLoginAttempts__int.index(actorId, failedLoginAttepts);
		// remote state
		this.currentModelRev = XidSetValueUtils.setValueInObject(this.persistence,
		        this.currentModelRev, this.executingActorId, actorId, hasFailedLoginAttempts, X
		                .getValueFactory().createIntegerValue(failedLoginAttepts));
		
		return failedLoginAttepts;
	}
	
	@Override
	@ModificationOperation
	public void resetFailedLoginAttempts(XID actorId) {
		// local state
		this.actor_hasFailedLoginAttempts__int.deIndex(actorId);
		// remote state
		this.currentModelRev = XidSetValueUtils.setValueInObject(this.persistence,
		        this.currentModelRev, this.executingActorId, actorId, hasFailedLoginAttempts, X
		                .getValueFactory().createIntegerValue(0));
		this.currentModelRev = this.persistence.executeCommand(
		        this.executingActorId,
		        X.getCommandFactory().createRemoveFieldCommand(this.persistence.getRepositoryId(),
		                NamingUtils.ID_ACCOUNT_MODEL, actorId, hasFailedLoginAttempts,
		                this.currentModelRev, false));
	}
	
	@Override
	public int getFailedLoginAttempts(XID actorId) {
		assert this.actor_hasFailedLoginAttempts__int != null;
		assert actorId != null;
		checkRefreshFromPersistentData();
		Integer i = this.actor_hasFailedLoginAttempts__int.lookup(actorId);
		if(i == null) {
			return 0;
		} else
			return i;
	}
	
	@Override
	public boolean isValidLogin(XID actorId, String passwordHash) {
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorId and passwordHash must not be null");
		}
		checkRefreshFromPersistentData();
		
		String storedPasswordHash = getPasswordHash(actorId);
		return passwordHash.equals(storedPasswordHash);
	}
	
	@Override
	public String getPasswordHash(XID actorId) {
		checkRefreshFromPersistentData();
		return this.actor_hasPasswordHash__string.lookup(actorId);
	}
	
	private void checkRefreshFromPersistentData() {
		long removeRevNr = this.persistence.getModelRevision(X.getIDProvider().fromComponents(
		        this.persistence.getRepositoryId(), NamingUtils.ID_ACCOUNT_MODEL, null, null));
		if(removeRevNr > this.currentModelRev) {
			initialiseFromRemoteSnapshot();
		}
	}
}
