package org.xydra.core.access.impl.memory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.model.XWritableObject;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XValue;
import org.xydra.index.impl.FastEntrySetFactory;
import org.xydra.index.impl.MapSetIndex;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.MAXTodo;
import org.xydra.store.XydraStore;
import org.xydra.store.access.XAccountDatabase;
import org.xydra.store.access.XidSetValueUtils;
import org.xydra.store.access.impl.delegate.Documentation_AccountModel;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * Wraps a {@link XydraStore} model to store accounts with passwords and groups
 * and their members. This is usually one special model per store.
 * 
 * See {@link Documentation_AccountModel} for the mapping from internal data
 * structures to Xydras repo/model/object/field layout.
 * 
 * 
 * FIXME This impl does not write changes back to a {@link XydraPersistence}
 * because {@link XWritableModel} is just an im-memory snapshot (at least the
 * ones we have).
 * 
 * @author voelkel
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
@MAXTodo
public class AccountModelWrapper implements XAccountDatabase {
	
	private static final Logger log = LoggerFactory.getLogger(AccountModelWrapper.class);
	
	public static final XID hasMember = XX.toId("hasMember");
	public static final XID isMemberOf = XX.toId("isMemberOf");
	public static final XID hasPasswordHash = XX.toId("hasPasswordHash");
	public static final XID hasFailedLoginAttempts = XX.toId("hasFailedLoginAttempts");
	
	private static final long serialVersionUID = 3858107275113200924L;
	
	private XWritableModel wrappedAccountModel;
	
	private MapSetIndex<XID,XID> actor2groups = null;
	
	// /**
	// * @param credentials to authenticate and authorise to store
	// * @param store
	// */
	// public AccountModelWrapper(Credentials credentials, XydraStore store) {
	// XWritableRepository repository = new WritableRepository(credentials,
	// store);
	// this.wrappedAccountModel =
	// repository.createModel(NamingUtils.ID_ACCOUNT_MODEL);
	// }
	//
	// /**
	// * To test
	// *
	// * @param repository
	// * @param modelId
	// */
	// public AccountModelWrapper(XRepository repository, XID modelId) {
	// this.wrappedAccountModel = repository.createModel(modelId);
	// }
	
	public AccountModelWrapper(XWritableModel accountModel) {
		if(accountModel == null) {
			throw new IllegalArgumentException("accountModel may not be null");
		}
		this.wrappedAccountModel = accountModel;
	}
	
	@Override
	public void addToGroup(XID actorId, XID groupId) {
		XidSetValueUtils.addToXIDSetValueInObject(this.wrappedAccountModel, groupId, hasMember,
		        actorId);
		
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
		return XidSetValueUtils.getXIDSetValue(this.wrappedAccountModel, group, hasMember);
	}
	
	/* direct */
	@Override
	public Set<XID> getGroups() {
		Iterator<XID> it = this.wrappedAccountModel.iterator();
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
		XidSetValueUtils.removeFromXIDSetValueInObject(this.wrappedAccountModel, groupId,
		        hasMember, actorId);
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
		XWritableObject actor = this.wrappedAccountModel.getObject(actorId);
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
		assert this.wrappedAccountModel != null;
		XWritableObject actor = this.wrappedAccountModel.getObject(actorId);
		if(actor == null) {
			actor = this.wrappedAccountModel.createObject(actorId);
		}
		XWritableField field = actor.getField(hasPasswordHash);
		if(field == null) {
			field = actor.createField(hasPasswordHash);
		}
		field.setValue(X.getValueFactory().createStringValue(passwordHash));
	}
	
	@Override
	public int incrementFailedLoginAttempts(XID actorId) {
		XWritableObject actor = this.wrappedAccountModel.getObject(actorId);
		if(actor == null) {
			actor = this.wrappedAccountModel.createObject(actorId);
		}
		XWritableField field = actor.getField(hasFailedLoginAttempts);
		if(field == null) {
			field = actor.createField(hasFailedLoginAttempts);
			field.setValue(X.getValueFactory().createIntegerValue(1));
			return 1;
		} else {
			// increment
			int failedAttempts = ((XIntegerValue)field.getValue()).contents();
			failedAttempts++;
			field.setValue(X.getValueFactory().createIntegerValue(failedAttempts));
			return failedAttempts;
		}
	}
	
	@Override
	public void resetFailedLoginAttempts(XID actorId) {
		XWritableObject actor = this.wrappedAccountModel.getObject(actorId);
		if(actor == null) {
			return;
		}
		XWritableField field = actor.getField(hasFailedLoginAttempts);
		if(field == null) {
			return;
		} else {
			actor.removeField(hasFailedLoginAttempts);
		}
	}
	
	@Override
	public int getFailedLoginAttempts(XID actorId) {
		if(actorId == null) {
			throw new IllegalArgumentException("actorId must not be null");
		}
		XWritableObject actor = this.wrappedAccountModel.getObject(actorId);
		if(actor == null) {
			return 0;
		}
		XWritableField field = actor.getField(hasFailedLoginAttempts);
		if(field == null) {
			return 0;
		}
		XValue value = field.getValue();
		if(value == null) {
			return 0;
		}
		assert value instanceof XIntegerValue : "type is " + value.getClass();
		return ((XIntegerValue)value).contents();
	}
	
	@Override
	public boolean isValidLogin(XID actorId, String passwordHash) {
		if(actorId == null || passwordHash == null) {
			throw new IllegalArgumentException("actorId and passwordHash must not be null");
		}
		
		String storedPasswordHash = getPasswordHash(actorId);
		return passwordHash.equals(storedPasswordHash);
	}
	
	@Override
	public String getPasswordHash(XID actorId) {
		if(actorId == null) {
			throw new IllegalArgumentException("actorId must not be null");
		}
		XWritableObject actor = this.wrappedAccountModel.getObject(actorId);
		if(actor == null) {
			return null;
		}
		XWritableField field = actor.getField(hasPasswordHash);
		if(field == null) {
			return null;
		}
		XValue value = field.getValue();
		if(value == null) {
			return null;
		}
		return ((XStringValue)value).contents();
	}
}
