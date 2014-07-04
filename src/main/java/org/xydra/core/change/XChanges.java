package org.xydra.core.change;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryTransaction;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.index.XI;
import org.xydra.index.iterator.SingleValueIterator;
import org.xydra.sharedutils.XyAssert;


/**
 * Various helper methods for working with {@link XEvent XEvents} and
 * {@link XCommand XCommands}.
 * 
 * @author dscharrer
 * 
 */
public class XChanges {
	
	/**
	 * Create a forced {@link XAtomicCommand} that undoes the given
	 * {@link XAtomicEvent}.
	 * 
	 * Note: Be aware that the given command may still fail to apply if the
	 * target it operates upon no longer exists.
	 * 
	 * @param event The {@link XAtomicEvent} which inverse
	 *            {@link XAtomicCommand} is to be calculated
	 * @return the inverse of the given {@link XAtomicEvent}, executing it will
	 *         basically result in an undo operation
	 */
	static public XAtomicCommand createForcedUndoCommand(XAtomicEvent event) {
		
		if(event instanceof XFieldEvent)
			return createForcedUndoCommand((XReversibleFieldEvent)event);
		if(event instanceof XObjectEvent)
			return createForcedUndoCommand((XObjectEvent)event);
		if(event instanceof XModelEvent)
			return createForcedUndoCommand((XModelEvent)event);
		if(event instanceof XRepositoryEvent)
			return createForcedUndoCommand((XRepositoryEvent)event);
		
		throw new IllegalArgumentException("unknown command class: " + event);
	}
	
	/**
	 * Create a forced {@link XCommand} that undoes the given {@link XEvent}.
	 * 
	 * Note: Be aware that the given command may still fail to apply if the
	 * target it operates upon no longer exists.
	 * 
	 * @param event The {@link XEvent} which inverse {@link XCommand} is to be
	 *            calculated
	 * @return the inverse of the given {@link XEvent}, executing it will
	 *         basically result in an undo operation
	 */
	static public XCommand createForcedUndoCommand(XEvent event) {
		
		if(event instanceof XAtomicEvent)
			return createForcedUndoCommand((XAtomicEvent)event);
		if(event instanceof XTransactionEvent)
			return createForcedUndoCommand((XTransactionEvent)event);
		
		throw new IllegalArgumentException("unknown command class: " + event);
	}
	
	/**
	 * Create a forced {@link XFieldCommand} that undoes the given
	 * {@link XFieldEvent}.
	 * 
	 * Note: Be aware that the given command may still fail to apply if the
	 * field it operates upon no longer exists.
	 * 
	 * @param event The {@link XFieldEvent} which inverse {@link XFieldCommand}
	 *            is to be calculated.
	 * @return the inverse of the given {@link XFieldCommand}, executing it will
	 *         basically result in an undo operation
	 */
	static public XFieldCommand createForcedUndoCommand(XReversibleFieldEvent event) {
		
		switch(event.getChangeType()) {
		
		case REMOVE:
			return MemoryFieldCommand.createAddCommand(event.getTarget(), XCommand.FORCED,
			        event.getOldValue());
			
		case CHANGE:
			return MemoryFieldCommand.createChangeCommand(event.getTarget(), XCommand.FORCED,
			        event.getOldValue());
			
		case ADD:
			return MemoryFieldCommand.createRemoveCommand(event.getTarget(), XCommand.FORCED);
			
		default:
			throw new AssertionError("unexpected type for field events: " + event.getChangeType());
			
		}
		
	}
	
	/**
	 * Create a forced {@link XModelCommand} that undoes the given
	 * {@link XModelEvent}.
	 * 
	 * Note: Be aware that the given command may still fail to apply if the
	 * model it operates upon no longer exists.
	 * 
	 * @param event The {@link XModelEvent} which inverse {@link XModelCommand}
	 *            is to be calculated.
	 * @return The inverse of the given {@link XModelEvent}, executing it will
	 *         basically result in an undo operation
	 */
	static public XModelCommand createForcedUndoCommand(XModelEvent event) {
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			return MemoryModelCommand.createAddCommand(event.getTarget(), XCommand.FORCED,
			        event.getObjectId());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for model events: "
			        + event.getChangeType();
			
			return MemoryModelCommand.createRemoveCommand(event.getTarget(), XCommand.FORCED,
			        event.getObjectId());
			
		}
		
	}
	
	/**
	 * Create a forced {@link XObjectCommand} that undoes the given
	 * {@link XObjectEvent}.
	 * 
	 * Note: Be aware that the given command may still fail to apply if the
	 * object it operates upon no longer exists.
	 * 
	 * @param event The {@link XObjectEvent} which inverse
	 *            {@link XObjectCommand} is to be calculated
	 * @return The inverse of the given {@link XObjectEvent}, executing it will
	 *         basically result in an undo operation
	 */
	static public XObjectCommand createForcedUndoCommand(XObjectEvent event) {
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			return MemoryObjectCommand.createAddCommand(event.getTarget(), XCommand.FORCED,
			        event.getFieldId());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for object events: "
			        + event.getChangeType();
			
			return MemoryObjectCommand.createRemoveCommand(event.getTarget(), XCommand.FORCED,
			        event.getFieldId());
			
		}
		
	}
	
	/**
	 * Create a forced {@link XRepositoryCommand} that undoes the given
	 * {@link XRepositoryEvent}.
	 * 
	 * @param event The {@link XRepositoryEvent} which inverse
	 *            {@link XRepositoryCommand} is to be calculated.
	 * @return The inverse of the given {@link XRepositoryEvent}, executing it
	 *         will basically result in an undo operation
	 */
	static public XRepositoryCommand createForcedUndoCommand(XRepositoryEvent event) {
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			return MemoryRepositoryCommand.createAddCommand(event.getTarget(), XCommand.FORCED,
			        event.getModelId());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for repository events: "
			        + event.getChangeType();
			
			return MemoryRepositoryCommand.createRemoveCommand(event.getTarget(), XCommand.FORCED,
			        event.getRepositoryId());
			
		}
		
	}
	
	/**
	 * Create a forced {@link XTransaction} that undoes the given
	 * {@link XTransactionEvent}.
	 * 
	 * Note: Be aware that the given command may still fail to apply if the
	 * targets it or only of the contained commands operate upon no longer
	 * exist.
	 * 
	 * @param transaction The {@link XTransactionEvent} which inverse
	 *            {@link XTransaction} is to be calculated
	 * @return the inverse of the given {@link XTransaction}, executing it will
	 *         basically result in an undo operation
	 */
	static public XTransaction createForcedUndoCommand(XTransactionEvent transaction) {
		
		XAtomicCommand[] result = new XAtomicCommand[transaction.size()];
		
		for(int i = 0, j = transaction.size() - 1; j >= 0; i++, j--) {
			result[i] = createForcedUndoCommand(transaction.getEvent(j));
		}
		
		return MemoryTransaction.createTransaction(transaction.getTarget(), result);
	}
	
	/**
	 * Create a {@link XAtomicCommand} that undoes the given
	 * {@link XAtomicEvent} but will fail if there have been any conflicting
	 * changes since then, even if they have also been undone as the revision
	 * number remains changed.
	 * 
	 * This should only be used for undoing the last event unless you know that
	 * there have been no conflicting events.
	 * 
	 * @param event The {@link XAtomicEvent} which inverse
	 *            {@link XAtomicCommand} is to be calculated
	 * @return The inverse of the given {@link XAtomicEvent}, successfully
	 *         executing it will basically result in an undo operation
	 */
	static public XAtomicCommand createImmediateUndoCommand(XAtomicEvent event) {
		
		if(event instanceof XFieldEvent)
			return createImmediateUndoCommand((XReversibleFieldEvent)event);
		if(event instanceof XObjectEvent)
			return createImmediateUndoCommand((XObjectEvent)event);
		if(event instanceof XModelEvent)
			return createImmediateUndoCommand((XModelEvent)event);
		if(event instanceof XRepositoryEvent)
			return createImmediateUndoCommand((XRepositoryEvent)event);
		
		throw new IllegalArgumentException("unknown command class: " + event);
	}
	
	/**
	 * Create a {@link XFieldCommand} that undoes the given {@link XFieldEvent}
	 * but will fail if there have been any conflicting changes since then, even
	 * if they have also been undone as the revision number remains changed.
	 * 
	 * This should only be used for undoing the last event unless you know that
	 * there have been no conflicting events.
	 * 
	 * @param event The {@link XFieldEvent} which inverse {@link XFieldCommand}
	 *            is to be calculated
	 * @return the inverse of the given {@link XFieldEvent}, successfully
	 *         executing it will basically result in an undo operation
	 */
	static public XFieldCommand createImmediateUndoCommand(XReversibleFieldEvent event) {
		
		long newRev = event.getOldModelRevision() + 1;
		
		switch(event.getChangeType()) {
		
		case REMOVE:
			return MemoryFieldCommand.createAddCommand(event.getTarget(), newRev,
			        event.getOldValue());
			
		case CHANGE:
			return MemoryFieldCommand.createChangeCommand(event.getTarget(), newRev,
			        event.getOldValue());
			
		case ADD:
			return MemoryFieldCommand.createRemoveCommand(event.getTarget(), newRev);
			
		default:
			throw new AssertionError("unexpected type for field events: " + event.getChangeType());
			
		}
		
	}
	
	/**
	 * Create a {@link XModelCommand} that undoes the given {@link XModelEvent}
	 * but will fail if there have been any conflicting changes since then, even
	 * if they have also been undone as the revision number remains changed.
	 * 
	 * This should only be used for undoing the last event unless you know that
	 * there have been no conflicting events.
	 * 
	 * @param event The {@link XModelEvent} which inverse {@link XModelCommand}
	 *            is to be calculated
	 * @return The inverse of the given {@link XModelEvent}, successfully
	 *         executing it will basically result in an undo operation
	 */
	static public XModelCommand createImmediateUndoCommand(XModelEvent event) {
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			return MemoryModelCommand.createAddCommand(event.getTarget(), XCommand.SAFE,
			        event.getObjectId());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for model events: "
			        + event.getChangeType();
			
			long newRev = event.getOldModelRevision() + 1;
			
			return MemoryModelCommand.createRemoveCommand(event.getTarget(), newRev,
			        event.getObjectId());
			
		}
		
	}
	
	/**
	 * Create a {@link XObjectCommand} that undoes the given
	 * {@link XObjectEvent} but will fail if there have been any conflicting
	 * changes since then, even if they have also been undone as the revision
	 * number remains changed.
	 * 
	 * This should only be used for undoing the last event unless you know that
	 * there have been no conflicting events.
	 * 
	 * @param event The {@link XObjectEvent} which inverse
	 *            {@link XObjectCommand} is to be calculated
	 * @return The inverse of the given {@link XObjectEvent}, successfully
	 *         executing it will basically result in an undo operation.
	 */
	static public XObjectCommand createImmediateUndoCommand(XObjectEvent event) {
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			return MemoryObjectCommand.createAddCommand(event.getTarget(), XCommand.SAFE,
			        event.getFieldId());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for object events: "
			        + event.getChangeType();
			
			long newRev = event.getOldModelRevision() + 1;
			
			return MemoryObjectCommand.createRemoveCommand(event.getTarget(), newRev,
			        event.getFieldId());
			
		}
		
	}
	
	/**
	 * Create a {@link XRepositoryCommand} that undoes the given
	 * {@link XRepositoryEvent} but will fail if there have been any conflicting
	 * changes since then, even if they have also been undone as the revision
	 * number remains changed.
	 * 
	 * This should only be used for undoing the last event unless you know that
	 * there have been no conflicting events.
	 * 
	 * @param event The {@link XRepositoryEvent} which inverse
	 *            {@link XRepositoryCommand} is to be calculated
	 * @return The inverse of the given {@link XRepositoryEvent}, successfully
	 *         executing it will basically result in an undo operation
	 */
	static public XRepositoryCommand createImmediateUndoCommand(XRepositoryEvent event) {
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			return MemoryRepositoryCommand.createAddCommand(event.getTarget(), XCommand.SAFE,
			        event.getModelId());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for repository events: "
			        + event.getChangeType();
			
			return MemoryRepositoryCommand.createRemoveCommand(event.getTarget(), 0,
			        event.getRepositoryId());
			
		}
		
	}
	
	/**
	 * Create a {@link XAtomicCommand} that would have caused the given
	 * {@link XAtomicEvent}
	 * 
	 * @param event The {@link XAtomicEvent} for which a corresponding
	 *            {@link XAtomicCommand} is to be created
	 * @return an {@link XAtomicCommand} which execution would've created the
	 *         given {@link XAtomicEvent}
	 */
	static public XAtomicCommand createReplayCommand(XAtomicEvent event) {
		
		if(event instanceof XFieldEvent)
			return createReplayCommand((XFieldEvent)event);
		if(event instanceof XObjectEvent)
			return createReplayCommand((XObjectEvent)event);
		if(event instanceof XModelEvent)
			return createReplayCommand((XModelEvent)event);
		if(event instanceof XRepositoryEvent)
			return createReplayCommand((XRepositoryEvent)event);
		
		throw new IllegalArgumentException("unknown command class: " + event);
	}
	
	/**
	 * Create a {@link XCommand} that would have caused the given {@link XEvent}
	 * 
	 * @param event The {@link XEvent} for which a corresponding
	 *            {@link XCommand} is to be created
	 * @return an {@link XCommand} which execution would've created the given
	 *         {@link XEvent}
	 */
	static public XCommand createReplayCommand(XEvent event) {
		
		if(event instanceof XAtomicEvent)
			return createReplayCommand((XAtomicEvent)event);
		if(event instanceof XTransactionEvent)
			return createReplayCommand((XTransactionEvent)event);
		
		throw new IllegalArgumentException("unknown command class: " + event);
	}
	
	/**
	 * Create a {@link XFieldCommand} that would have caused the given
	 * {@link XFieldEvent}
	 * 
	 * @param event The {@link XFieldEvent} for which a corresponding
	 *            {@link XFieldCommand} is to be created
	 * @return an {@link XFieldCommand} which execution would've created the
	 *         given {@link XFieldEvent}
	 */
	static public XFieldCommand createReplayCommand(XFieldEvent event) {
		
		switch(event.getChangeType()) {
		
		case ADD:
			return MemoryFieldCommand.createAddCommand(event.getTarget(),
			        event.getOldFieldRevision(), event.getNewValue());
			
		case CHANGE:
			return MemoryFieldCommand.createChangeCommand(event.getTarget(),
			        event.getOldFieldRevision(), event.getNewValue());
			
		case REMOVE:
			return MemoryFieldCommand.createRemoveCommand(event.getTarget(),
			        event.getOldFieldRevision());
			
		default:
			throw new AssertionError("unexpected type for field events: " + event.getChangeType());
			
		}
		
	}
	
	/**
	 * Create a {@link XModelCommand} that would have caused the given
	 * {@link XModelEvent}
	 * 
	 * @param event The {@link XModelEvent} for which a corresponding
	 *            {@link XModelCommand} is to be created
	 * @return an {@link XModelCommand} which execution would've created the
	 *         given {@link XModelEvent}
	 */
	static public XModelCommand createReplayCommand(XModelEvent event) {
		
		if(event.getChangeType() == ChangeType.ADD) {
			
			return MemoryModelCommand.createAddCommand(event.getTarget(), XCommand.SAFE,
			        event.getObjectId());
			
		} else {
			
			assert event.getChangeType() == ChangeType.REMOVE : "unexpected change type for model events: "
			        + event.getChangeType();
			
			return MemoryModelCommand.createRemoveCommand(event.getTarget(),
			        event.getOldObjectRevision(), event.getObjectId());
			
		}
		
	}
	
	/**
	 * Create a {@link XObjectCommand} that would have caused the given
	 * {@link XObjectEvent}
	 * 
	 * @param event The {@link XObjectEvent} for which a corresponding
	 *            {@link XObjectCommand} is to be created
	 * @return an {@link XObjectCommand} which execution would've created the
	 *         given {@link XObjectEvent}
	 */
	static public XObjectCommand createReplayCommand(XObjectEvent event) {
		
		if(event.getChangeType() == ChangeType.ADD) {
			
			return MemoryObjectCommand.createAddCommand(event.getTarget(), XCommand.SAFE,
			        event.getFieldId());
			
		} else {
			
			assert event.getChangeType() == ChangeType.REMOVE : "unexpected change type for object events: "
			        + event.getChangeType();
			
			return MemoryObjectCommand.createRemoveCommand(event.getTarget(),
			        event.getOldFieldRevision(), event.getFieldId());
			
		}
		
	}
	
	/**
	 * Create a {@link XRepositoryCommand} that would have caused the given
	 * {@link XRepositoryEvent}
	 * 
	 * @param event The {@link XRepositoryEvent} for which a corresponding
	 *            {@link XRepositoryCommand} is to be created
	 * @return an {@link XRepositoryCommand} which execution would've created
	 *         the given {@link XRepositoryEvent}
	 */
	static public XRepositoryCommand createReplayCommand(XRepositoryEvent event) {
		
		if(event.getChangeType() == ChangeType.ADD) {
			
			return MemoryRepositoryCommand.createAddCommand(event.getTarget(), XCommand.SAFE,
			        event.getModelId());
			
		} else {
			
			assert event.getChangeType() == ChangeType.REMOVE : "unexpected change type for repository events: "
			        + event.getChangeType();
			
			return MemoryRepositoryCommand.createRemoveCommand(event.getTarget(),
			        event.getOldModelRevision(), event.getModelId());
			
		}
		
	}
	
	/**
	 * Create a {@link XTransaction} that would have caused the given
	 * {@link XTransactionEvent}
	 * 
	 * @param trans The {@link XTransactionEvent} for which a corresponding
	 *            {@link XTransaction} is to be created
	 * @return an {@link XTransaction} or {@link XCommand} (if sufficient) which
	 *         execution would've created the given {@link XTransactionEvent}
	 */
	static public XCommand createReplayCommand(XTransactionEvent trans) {
		
		List<XAtomicCommand> result = new ArrayList<XAtomicCommand>();
		
		for(XAtomicEvent event : trans) {
			if(!event.isImplied()) {
				result.add(createReplayCommand(event));
			}
		}
		
		XyAssert.xyAssert(result.size() > 0);
		
		if(result.size() == 1) {
			return result.get(0);
		}
		
		return MemoryTransaction.createTransaction(trans.getTarget(), result);
	}
	
	/**
	 * Create a {@link XCommand} that undoes the given {@link XEvent}s in the
	 * order provided by the iterator but will fail if there have been any
	 * conflicting changes since then that have not been undone already.
	 * 
	 * The created command may be a transaction. However, if only one command is
	 * needed to undo the events, no transaction is created and the command is
	 * returned directly.
	 * 
	 * The relevant parts of the given {@link XModel} must be in the same state
	 * as they where directly after the event, only the revision numbers may
	 * differ.
	 * 
	 * @param base The {@link XReadableModel} on which the given {@link XEvent}s
	 *            happened.
	 * @param events an iterator over the {@link XEvent}s which inverse
	 *            {@link XCommand}s are to be calculated
	 * @return an {@link XTransaction} (as an {@link XCommand}), which will undo
	 *         the specified events if it can be successfully executed.
	 * 
	 * @throws IllegalStateException if the given {@link XModel} is in a
	 *             different state
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of any of the events or if any of the
	 *             events is a {@link XRepositoryEvent}
	 */
	public static XCommand createUndoCommand(XReadableModel base, Iterator<XEvent> events) {
		
		ChangedModel model = new ChangedModel(base);
		
		while(events.hasNext()) {
			undoChanges(model, events.next());
		}
		
		XTransactionBuilder builder = new XTransactionBuilder(base.getAddress());
		
		builder.applyChanges(model);
		
		if(builder.isEmpty()) {
			// no changes needed
			return null;
		}
		
		return builder.buildCommand();
	}
	
	/**
	 * Create a {@link XCommand} that undoes the given {@link XEvent} but will
	 * fail if there have been any conflicting changes since then that have not
	 * been undone already.
	 * 
	 * The relevant parts of the given {@link XReadableModel} must be in the
	 * same state as they where directly after the event, only the revision
	 * numbers may differ.
	 * 
	 * @param base The {@link XReadableModel} on which the given {@link XEvent}
	 *            happened
	 * @param event The {@link XEvent} which inverse {@link XCommand} is to be
	 *            calculated
	 * @return the inverse of the given {@link XEvent}, successfully executing
	 *         it will basically result in an undo operation
	 * 
	 * @throws IllegalStateException if the given {@link XReadableModel} is in a
	 *             different state
	 * @throws IllegalArgumentException if the given {@link XReadableModel}
	 *             doesn't contain the target of the given event or the event is
	 *             an {@link XRepositoryEvent}
	 */
	public static XCommand createUndoCommand(XReadableModel base, XEvent event) {
		return createUndoCommand(base, new SingleValueIterator<XEvent>(event));
	}
	
	/**
	 * Create a {@link XRepositoryCommand} that undoes the given
	 * {@link XRepositoryEvent} but will fail if there have been any conflicting
	 * changes since then that have not been undone already.
	 * 
	 * The relevant parts of the given {@link XRepository} must be in the same
	 * state as they where directly after the event, only the revision numbers
	 * may differ.
	 * 
	 * @param repo The {@link XRepository} on which the given
	 *            {@link XRepositoryEvent} happened
	 * @param event The {@link XRepository} which inverse
	 *            {@link XRepositoryEvent} is to be calculated
	 * @return The inverse of the given {@link XRepositoryEvent}, successfully
	 *         executing it will basically result in an undo operation
	 * 
	 * @throws IllegalStateException if the given {@link XRepository} is in a
	 *             different state
	 * @throws IllegalArgumentException if the given {@link XRepository} doesn't
	 *             contain the target of the event
	 */
	static public XRepositoryCommand createUndoCommand(XRepository repo, XRepositoryEvent event) {
		
		if(!repo.getAddress().equals(event.getTarget()))
			throw new IllegalArgumentException("repository and event don't match");
		
		XId modelId = event.getTarget().getModel();
		
		XyAssert.xyAssert(modelId != null); assert modelId != null;
		
		XModel model = repo.getModel(modelId);
		
		if(event.getChangeType() == ChangeType.REMOVE) {
			
			if(model != null) {
				throw new IllegalStateException("model already exists, cannot undo " + event);
			}
			
			return MemoryRepositoryCommand.createAddCommand(event.getTarget(), XCommand.SAFE,
			        event.getModelId());
			
		} else {
			
			assert event.getChangeType() == ChangeType.ADD : "unexpected change type for repository events: "
			        + event.getChangeType();
			
			if(model == null) {
				throw new IllegalStateException("model no longer exists, cannot undo " + event);
			}
			
			if(!model.isEmpty()) {
				throw new IllegalStateException("model should be empty, cannot undo " + event);
			}
			
			return MemoryRepositoryCommand.createRemoveCommand(event.getTarget(),
			        event.getOldModelRevision(), event.getRepositoryId());
			
		}
		
	}
	
	/**
	 * Undo the changes represented by the given {@link XAtomicEvent} on the
	 * given {@link XWritableModel}.
	 * 
	 * @param model The {@link XWritableModel} on which the given
	 *            {@link XAtomicEvent} happened
	 * @param event The {@link XAtomicEvent} which is to be undone
	 * 
	 * @throws IllegalStateException if the are conflicting changes between the
	 *             state the model was in after the event and now.
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of the event or if the events is a
	 *             {@link XRepositoryEvent}
	 */
	public static void undoChanges(XWritableModel model, XAtomicEvent event) {
		if(event instanceof XModelEvent) {
			undoChanges(model, (XModelEvent)event);
			return;
		} else if(event instanceof XObjectEvent) {
			undoChanges(model, (XObjectEvent)event);
			return;
		} else if(event instanceof XReversibleFieldEvent) {
			undoChanges(model, (XReversibleFieldEvent)event);
			return;
		} else if(event instanceof XRepositoryEvent) {
			throw new IllegalArgumentException("need repository to undo repository changes");
		} else if(event instanceof XFieldEvent) {
			throw new IllegalArgumentException("the given fieldEvent is not reversible");
		}
		throw new AssertionError("unknown event class: " + event);
	}
	
	/**
	 * Undo the changes represented by the given {@link XTransactionEvent} on
	 * the given {@link XWritableModel}.
	 * 
	 * @param model The {@link XWritableModel} on which the events of the given
	 *            {@link XTransactionEvent} happened
	 * @param event The {@link XTransactionEvent} which changes are to be undone
	 * 
	 * @throws IllegalStateException if the are conflicting changes between the
	 *             state the model was in after the event and now.
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of any of the events
	 */
	public static void undoChanges(XWritableModel model, XEvent event) {
		if(event instanceof XTransactionEvent) {
			undoChanges(model, (XTransactionEvent)event);
		} else if(event instanceof XAtomicEvent) {
			undoChanges(model, (XAtomicEvent)event);
		} else {
			assert false : "events are either transactions or atomic";
		}
	}
	
	/**
	 * Undo the changes represented by the given {@link XFieldEvent} on the
	 * given {@link XWritableModel}.
	 * 
	 * @param model The {@link XWritableModel} on which the given
	 *            {@link XObjectEvent} happened
	 * @param event The {@link XObjectEvent} which is to be undone
	 * 
	 * @throws IllegalStateException if the are conflicting changes between the
	 *             state the model was in after the event and now.
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of the event
	 */
	public static void undoChanges(XWritableModel model, XReversibleFieldEvent event) {
		
		if(!model.getAddress().contains(event.getTarget())) {
			throw new IllegalArgumentException();
		}
		
		XId objectId = event.getObjectId();
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			throw new IllegalStateException();
		}
		
		XId fieldId = event.getFieldId();
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			throw new IllegalStateException();
		}
		
		if(!XI.equals(field.getValue(), event.getNewValue())) {
			throw new IllegalStateException();
		}
		
		field.setValue(event.getOldValue());
	}
	
	/**
	 * Undo the changes represented by the given {@link XModelEvent} on the
	 * given {@link XWritableModel}.
	 * 
	 * @param model The {@link XWritableModel} on which the given
	 *            {@link XModelEvent} happened
	 * @param event The {@link XModelEvent} which is to be undone
	 * 
	 * @throws IllegalStateException if the are conflicting changes between the
	 *             state the model was in after the event and now.
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of the event
	 */
	public static void undoChanges(XWritableModel model, XModelEvent event) {
		
		if(!XI.equals(model.getAddress(), event.getTarget())) {
			throw new IllegalArgumentException();
		}
		
		XId objectId = event.getObjectId();
		
		switch(event.getChangeType()) {
		
		case ADD:
			if(!model.hasObject(objectId)) {
				throw new IllegalStateException();
			}
			model.removeObject(objectId);
			break;
		
		case REMOVE:
			if(model.hasObject(objectId)) {
				throw new IllegalStateException();
			}
			model.createObject(objectId);
			break;
		
		default:
			throw new AssertionError("impossible type for model commands");
		}
		
	}
	
	/**
	 * Undo the changes represented by the given {@link XObjectEvent} on the
	 * given {@link XWritableModel}.
	 * 
	 * @param model The {@link XWritableModel} on which the given
	 *            {@link XObjectEvent} happened
	 * @param event The {@link XObjectEvent} which is to be undone
	 * 
	 * @throws IllegalStateException if the are conflicting changes between the
	 *             state the model was in after the event and now.
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of the event
	 */
	public static void undoChanges(XWritableModel model, XObjectEvent event) {
		
		if(!model.getAddress().contains(event.getTarget())) {
			throw new IllegalArgumentException();
		}
		
		XId objectId = event.getObjectId();
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			throw new IllegalStateException();
		}
		
		XId fieldId = event.getFieldId();
		
		switch(event.getChangeType()) {
		
		case ADD:
			if(!object.hasField(fieldId)) {
				throw new IllegalStateException();
			}
			object.removeField(fieldId);
			break;
		
		case REMOVE:
			if(object.hasField(fieldId)) {
				throw new IllegalStateException();
			}
			object.createField(fieldId);
			break;
		
		default:
			throw new AssertionError("impossible type for object commands");
		}
		
	}
	
	/**
	 * Undo the changes represented by the given {@link XTransactionEvent} on
	 * the given {@link XWritableModel}.
	 * 
	 * @param model The {@link XWritableModel} on which the events of the given
	 *            {@link XTransactionEvent} happened
	 * @param event The {@link XTransactionEvent} which changes are to be undone
	 * 
	 * @throws IllegalStateException if the are conflicting changes between the
	 *             state the model was in after the event and now.
	 * @throws IllegalArgumentException if the given {@link XModel} doesn't
	 *             contain the target of any of the events
	 */
	public static void undoChanges(XWritableModel model, XTransactionEvent event) {
		for(int i = event.size(); i >= 0; i--) {
			undoChanges(model, event.getEvent(i));
		}
	}
	
}
