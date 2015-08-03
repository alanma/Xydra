package org.xydra.store.access.impl.delegate;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.BaseRuntime;
import org.xydra.base.WritableUtils;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.value.XIdSetValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.XGroupEvent;
import org.xydra.store.access.XGroupListener;


/**
 * Delegate all reads and writes to a {@link XWritableModel}.
 *
 * <h4>Data modelling</h4> Group membership (group->actors)
 *
 * <pre>
 * objectId | fieldId     | value
 * ---------+-------------+----------------------------
 * groupId  | "hasMember" | {@link XIdSetValue} actors
 * </pre>
 *
 * @author xamde
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public class PartialGroupDatabaseOnWritableModel implements XGroupListener {

	public static final XId hasMember = XX.toId("hasMember");

	/**
	 * Apply those events with group-actor change semantics.
	 *
	 * @param events The events to apply.
	 * @param fastDatabase The database to apply the events to.
	 */
	public static void applyEventsTo(final List<XEvent> events, final XGroupDatabaseWithListeners fastDatabase) {
		/* apply events */
		for(final XEvent event : events) {
			applyEventTo(event, fastDatabase);
		}
	}

	/**
	 * Translate from RMOF to group actions.
	 *
	 * @param event
	 * @param fastDatabase
	 */
	private static void applyEventTo(final XEvent event, final XGroupDatabaseWithListeners fastDatabase) {
		if(event.getChangeType() == ChangeType.TRANSACTION) {
			final XTransactionEvent txn = (XTransactionEvent)event;
			for(final XEvent atomicEvent : txn) {
				applyEventTo(atomicEvent, fastDatabase);
			}
			return;
		}

		XyAssert.xyAssert(event instanceof XAtomicEvent);

		/* We care for: {group}.hasMember = {actor, actor, ...} */
		final XAddress target = event.getTarget();
		switch(target.getAddressedType()) {
		case XMODEL:
			// if REMOVE {model}.{group}: remove group in index
			XyAssert.xyAssert(event instanceof XModelEvent);
			if(event.getChangeType() == ChangeType.REMOVE) {
				fastDatabase.removeGroup(event.getChangedEntity().getObject());
			}
			break;
		case XOBJECT: {
			// if REMOVE {model}.{group}.hasMember: remove group in index
			if(event.getChangeType() == ChangeType.REMOVE
			        && event.getChangedEntity().getObject().equals(hasMember)) {
				fastDatabase.removeGroup(event.getChangedEntity().getObject());
			}
		}
			break;
		case XFIELD: {
			final XFieldEvent fieldEvent = (XFieldEvent)event;
			final XId group = event.getChangedEntity().getObject();
			// {model}.{group}.hasMember.{value}
			final Set<XId> currentMembers = fastDatabase.getMembersOf(group);
			final Set<XId> nextMembers = XV.toIdSet(fieldEvent.getNewValue());

			for(final XId currentMember : currentMembers) {
				if(!nextMembers.contains(currentMember)) {
					fastDatabase.removeFromGroup(currentMember, group);
				}
			}
			for(final XId nextMember : nextMembers) {
				if(!currentMembers.contains(nextMember)) {
					fastDatabase.addToGroup(nextMember, group);
				}
			}
		}
			break;
		default:
			// ignore repository events
			break;
		}
	}

	protected XWritableModel groupModel;

	private boolean listeningToEvents;

	/**
	 *
	 * @param groupModel used to read and write account management data.
	 */
	public PartialGroupDatabaseOnWritableModel(final XWritableModel groupModel) {
		this.groupModel = groupModel;
	}

	@ModificationOperation
	public void addToGroup(final XId actorId, final XId groupId) {
		XIdSetValue members = (XIdSetValue)WritableUtils.getValue(this.groupModel, groupId,
		        hasMember);
		if(members == null) {
			members = BaseRuntime.getValueFactory().createIdSetValue(new XId[] { actorId });
		} else {
			members = members.add(groupId);
		}
		WritableUtils.setValue(this.groupModel, groupId, hasMember, members);
	}

	public void clear() {
		// delete all objects in account model
		WritableUtils.deleteAllObjects(this.groupModel);
	}

	public Set<XId> getGroups() {
		final Set<XId> result = new HashSet<XId>();
		for(final XId xid : this.groupModel) {
			result.add(xid);
		}
		return result;
	}

	public Set<XId> getMembersOf(final XId group) {
		final XValue value = WritableUtils.getValue(this.groupModel, group, hasMember);
		if(value == null) {
			return Collections.emptySet();
		} else {
			return ((XIdSetValue)value).toSet();
		}
	}

	public void loadInto(final XGroupDatabaseWithListeners groupDatabase) {
		for(final XId group : getGroups()) {
			for(final XId member : getMembersOf(group)) {
				groupDatabase.addToGroup(member, group);
			}
		}
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onGroupEvent(final XGroupEvent event) {
		if(!this.listeningToEvents) {
			return;
		}
		switch(event.getChangeType()) {
		case ADD:
			addToGroup(event.getActor(), event.getGroup());
			break;
		case REMOVE:
			removeFromGroup(event.getActor(), event.getGroup());
			break;
		}
	}

	@ModificationOperation
	public void removeFromGroup(final XId actorId, final XId groupId) {
		XIdSetValue members = (XIdSetValue)WritableUtils.getValue(this.groupModel, groupId,
		        hasMember);
		if(members == null) {
			members = BaseRuntime.getValueFactory().createIdSetValue(new XId[] { actorId });
		} else {
			members = members.remove(groupId);
		}
		WritableUtils.setValue(this.groupModel, groupId, hasMember, members);
	}

	public void setEventListening(final boolean enabled) {
		this.listeningToEvents = enabled;
	}

}
