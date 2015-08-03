package org.xydra.core.model.delta;

import java.util.ArrayList;
import java.util.List;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand.Intent;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.change.impl.memory.RevisionConstants;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.XExistsReadableModel;
import org.xydra.base.rmof.impl.XExistsWritableModel;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.value.XValue;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

/**
 * Helper class for executing commands and generating matching events.
 *
 * @author dscharrer
 */
public abstract class DeltaUtils {

	/**
	 * A description of what happened to the model itself.
	 *
	 * Changes to individual objects and fields are described by
	 * {@link ChangedModel}.
	 */
	@Deprecated
	public enum ModelChange {
		CREATED, NOCHANGE, REMOVED
	}

	private static final Logger log = LoggerFactory.getLogger(DeltaUtils.class);

	/**
	 * Apply the given changes to a {@link XRevWritableModel}.
	 *
	 * @param modelAddr
	 *            The address of the model to change. This is used if the model
	 *            needs to be created first (modelToChange is null).
	 * @param modelToChange
	 *            The model to change. This may be null if the model currently
	 *            exists.
	 * @param changedModel
	 *            The changes to apply as returned by
	 *            {@link #executeCommand(XExistsReadableModel, XCommand)}. @NeverNull
	 * @param rev
	 *            The revision number of the change.
	 * @return a model with the changes applied or null if model has been
	 *         removed by the changes.
	 */
	public static XRevWritableModel applyChanges(final XAddress modelAddr,
			final XRevWritableModel modelToChange, @NeverNull final ChangedModel changedModel, final long rev) {

		final XRevWritableModel model = modelToChange;

		if (changedModel.modelWasRemoved()) {
			return null;
		} else if (changedModel.modelWasCreated()) {
			assert model != null;
			assert !model.exists();
			model.setRevisionNumber(rev);
			if (model instanceof XExistsWritableModel) {
				((XExistsWritableModel) model).setExists(true);
			}
		}

		XyAssert.xyAssert(model != null);
		assert model != null;
		applyChanges(model, changedModel, rev);

		return model;
	}

	private static void applyChanges(final XRevWritableModel model, final ChangedModel changedModel, final long rev) {

		for (final XId objectId : changedModel.getRemovedObjects()) {
			XyAssert.xyAssert(model.hasObject(objectId));
			model.removeObject(objectId);
		}

		for (final XReadableObject object : changedModel.getNewObjects()) {
			XyAssert.xyAssert(!model.hasObject(object.getId()));
			final XRevWritableObject newObject = model.createObject(object.getId());
			for (final XId fieldId : object) {
				applyChanges(newObject, object.getField(fieldId), rev);
			}
			newObject.setRevisionNumber(rev);
		}

		for (final ChangedObject changedObject : changedModel.getChangedObjects()) {

			boolean objectChanged = false;

			final XRevWritableObject object = model.getObject(changedObject.getId());
			XyAssert.xyAssert(object != null);
			assert object != null;

			for (final XId fieldId : changedObject.getRemovedFields()) {
				XyAssert.xyAssert(object.hasField(fieldId));
				object.removeField(fieldId);
				objectChanged = true;
			}

			for (final XReadableField field : changedObject.getNewFields()) {
				applyChanges(object, field, rev);
				objectChanged = true;
			}

			for (final ChangedField changedField : changedObject.getChangedFields()) {
				if (changedField.isChanged()) {
					final XRevWritableField field = object.getField(changedField.getId());
					XyAssert.xyAssert(field != null);
					assert field != null;
					final boolean valueChanged = field.setValue(changedField.getValue());
					XyAssert.xyAssert(valueChanged);
					field.setRevisionNumber(rev);
					objectChanged = true;
				}
			}

			if (objectChanged) {
				object.setRevisionNumber(rev);
			}

		}

		model.setRevisionNumber(rev);

	}

	private static void applyChanges(final XRevWritableObject object, final XReadableField field, final long rev) {
		XyAssert.xyAssert(!object.hasField(field.getId()));
		final XRevWritableField newField = object.createField(field.getId());
		newField.setValue(field.getValue());
		newField.setRevisionNumber(rev);
	}

	/**
	 * Calculated the events describing the given change.
	 *
	 * @param modelAddr
	 *            The model the change applies to.
	 * @param changedModel
	 *            A change as created by
	 *            {@link #executeCommand(XExistsReadableModel, XCommand)}. @NeverNull
	 * @param actorId
	 *            The actor that initiated the change.
	 * @param rev
	 *            The revision number of the change.
	 * @param forceTxnEvent
	 *            if true, a txn is created even if there is only 1 change and
	 *            thus no transaction necessary
	 * @return the appropriate events for the change (as returned by
	 *         {@link #executeCommand(XExistsReadableModel, XCommand)}
	 */
	public static List<XAtomicEvent> createEvents(final XAddress modelAddr, final ChangedModel changedModel,
			final XId actorId, final long rev, final boolean forceTxnEvent) {
		XyAssert.xyAssert(changedModel != null);
		assert changedModel != null;

		/* we count only 0, 1 or 2 = many */
		final int nChanges = changedModel.countCommandsNeeded(2);

		final List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();

		if (nChanges == 0) {
			return events;
		}

		XyAssert.xyAssert(nChanges > 0);

		/*
		 * A transaction command always creates a transaction event; a single
		 * command might create a transaction, i.e. when there are more than 1
		 * resulting event
		 */
		final boolean inTxn = nChanges > 1 || forceTxnEvent;

		XyAssert.xyAssert(changedModel.getAddress().equals(modelAddr));

		createEventsForChangedModel(events, actorId, changedModel, inTxn);

		assert nChanges == 1 ? events.size() == 1 :

		events.size() >= 2 :

		"1 change must result in 1 event, more changes result in more events. Got changes="
				+ nChanges + " events=" + events.size();

		return events;
	}

	public static void createEventsForChangedField(final List<XAtomicEvent> events, final long currentModelRev,
			final XId actorId, final long currentObjectRev, final ChangedField field, final boolean inTransaction) {
		if (field.isChanged()) {
			// IMPROVE we only need to know if the old value exists
			final XValue oldValue = field.getOldValue();
			final XValue newValue = field.getValue();
			final XAddress target = field.getAddress();
			final long currentFieldRev = field.getRevisionNumber();
			if (newValue == null) {
				XyAssert.xyAssert(oldValue != null);
				assert oldValue != null;
				events.add(MemoryFieldEvent.createRemoveEvent(actorId, target, currentModelRev,
						currentObjectRev, currentFieldRev, inTransaction, false));
			} else if (oldValue == null) {
				events.add(MemoryFieldEvent.createAddEvent(actorId, target, newValue,
						currentModelRev, currentObjectRev, currentFieldRev, inTransaction));
			} else {
				events.add(MemoryFieldEvent.createChangeEvent(actorId, target, newValue,
						currentModelRev, currentObjectRev, currentFieldRev, inTransaction));
			}
		}
	}

	/**
	 * @param events
	 * @param actorId
	 * @param changedModel
	 * @param forceTransaction
	 *            should always be true if there are more than 1 events
	 */
	public static void createEventsForChangedModel(final List<XAtomicEvent> events, final XId actorId,
			final ChangedModel changedModel, final boolean forceTransaction) {

		final boolean implied = changedModel.modelWasRemoved();
		final long rev = changedModel.getRevisionNumber();
		final int nChanges = changedModel.countEventsNeeded(2);
		final boolean inTransaction = forceTransaction || nChanges > 1;

		/* Repository ADD commands handled first */
		if (changedModel.modelWasCreated()) {
			final XAddress target = changedModel.getAddress().getParent();
			final XRepositoryEvent repositoryEvent = MemoryRepositoryEvent.createAddEvent(actorId,
					target, changedModel.getId(), rev, inTransaction);
			events.add(repositoryEvent);
		}

		for (final XId objectId : changedModel.getRemovedObjects()) {
			final XReadableObject removedObject = changedModel.getOldObject(objectId);
			DeltaUtils.createEventsForRemovedObject(events, rev, actorId, removedObject,
					inTransaction, implied);
		}

		for (final XReadableObject object : changedModel.getNewObjects()) {
			events.add(MemoryModelEvent.createAddEvent(actorId, changedModel.getAddress(),
					object.getId(), rev, inTransaction));
			for (final XId fieldId : object) {
				DeltaUtils.createEventsForNewField(events, rev, actorId, object,
						object.getField(fieldId), inTransaction);
			}
		}

		for (final ChangedObject object : changedModel.getChangedObjects()) {
			createEventsForChangedObject(events, actorId, object, forceTransaction, rev);
		}

		/* Repository REMOVE commands handled last */
		if (changedModel.modelWasRemoved()) {
			final XAddress target = changedModel.getAddress().getParent();
			final XRepositoryEvent repositoryEvent = MemoryRepositoryEvent.createRemoveEvent(actorId,
					target, changedModel.getId(), rev, inTransaction);
			events.add(repositoryEvent);
		}

	}

	/**
	 * @param events
	 * @param actorId
	 * @param object
	 * @param forceTransaction
	 * @param currentModelRev
	 *            the new resulting model revision
	 */
	public static void createEventsForChangedObject(final List<XAtomicEvent> events, final XId actorId,
			final ChangedObject object, final boolean forceTransaction, final long currentModelRev) {
		final boolean inTransaction = forceTransaction || object.countCommandsNeeded(2) > 1;

		for (final XId fieldId : object.getRemovedFields()) {
			DeltaUtils.createEventsForRemovedField(events, currentModelRev, actorId, object,
					object.getOldField(fieldId), inTransaction, false);
		}

		for (final XReadableField field : object.getNewFields()) {
			DeltaUtils.createEventsForNewField(events, currentModelRev, actorId, object, field,
					inTransaction);
		}

		for (final ChangedField field : object.getChangedFields()) {
			final long objectRev = object.getRevisionNumber();
			DeltaUtils.createEventsForChangedField(events, currentModelRev, actorId, objectRev,
					field, inTransaction);
		}
	}

	private static void createEventsForNewField(final List<XAtomicEvent> events, final long rev, final XId actorId,
			final XReadableObject object, final XReadableField field, final boolean inTransaction) {
		final long objectRev = object.getRevisionNumber();
		events.add(MemoryObjectEvent.createAddEvent(actorId, object.getAddress(), field.getId(),
				rev, objectRev, inTransaction));
		if (!field.isEmpty()) {
			events.add(MemoryFieldEvent.createAddEvent(actorId, field.getAddress(),
					field.getValue(), rev, objectRev, field.getRevisionNumber(), inTransaction));
		}
	}

	private static void createEventsForRemovedField(final List<XAtomicEvent> events, final long modelRev,
			final XId actorId, final XReadableObject object, final XReadableField field, final boolean inTransaction,
			final boolean implied) {
		final long objectRev = object.getRevisionNumber();
		final long fieldRev = field.getRevisionNumber();
		if (!field.isEmpty()) {
			events.add(MemoryFieldEvent.createRemoveEvent(actorId, field.getAddress(), modelRev,
					objectRev, fieldRev, inTransaction, true));
		}
		events.add(MemoryObjectEvent.createRemoveEvent(actorId, object.getAddress(), field.getId(),
				modelRev, objectRev, fieldRev, inTransaction, implied));
	}

	private static void createEventsForRemovedObject(final List<XAtomicEvent> events, final long modelRev,
			final XId actorId, final XReadableObject object, final boolean inTransaction, final boolean implied) {
		for (final XId fieldId : object) {
			DeltaUtils.createEventsForRemovedField(events, modelRev, actorId, object,
					object.getField(fieldId), inTransaction, true);
		}
		events.add(MemoryModelEvent.createRemoveEvent(actorId, object.getAddress().getParent(),
				object.getId(), modelRev, object.getRevisionNumber(), inTransaction, implied));
	}

	/**
	 * @param modelAddress
	 * @param model
	 * @CanBeNull
	 * @return the given model, if not null, or create a new one
	 */
	private static XReadableModel createNonExistingModel(final XAddress modelAddress, final XReadableModel model) {
		if (model != null) {
			return model;
		}

		final SimpleModel nonExistingModel = new SimpleModel(modelAddress);
		nonExistingModel.setExists(false);
		nonExistingModel.setRevisionNumber(RevisionConstants.NOT_EXISTING);
		return nonExistingModel;
	}

	/**
	 * Calculate the changes resulting from executing the given command on the
	 * given model.
	 *
	 * @param model
	 *            The model to modify. Null if the model currently doesn't
	 *            exist. This instance is modified. @CanBeNull
	 * @param command
	 * @return The changed model after executing the command. Returns null if
	 *         the command failed.
	 */
	public static ChangedModel executeCommand(final XExistsReadableModel model, final XCommand command) {

		final boolean modelExists = model != null && model.exists();

		if (command instanceof XRepositoryCommand) {
			final XRepositoryCommand rc = (XRepositoryCommand) command;

			switch (rc.getChangeType()) {
			case ADD:
				if (modelExists) {
					if (rc.getIntent() != Intent.Forced) {
						log.warn("Safe-X RepositoryCommand ADD failed; modelExists=" + modelExists
								+ " model==null?" + (model == null));
						return null;
					}
					log.info("Command is forced, but there is no change");
					return new ChangedModel(createNonExistingModel(rc.getChangedEntity(), model));
				} else {
					if (rc.getIntent() == Intent.SafeRevBound) {
						if (model != null) {
							final long currentModelRev = model.getRevisionNumber();
							if (((XRepositoryCommand) command).getRevisionNumber() != currentModelRev) {
								log.warn("Safe RepositoryCommand ADD failed; modelRev="
										+ currentModelRev + " command=" + command);
								return null;
							}
						} else {
							log.warn("SafeRevBound model-ADD, but we cannot check rev, model is null");
						}
					}
					final XAddress modelAddress = rc.getChangedEntity();
					final ChangedModel changedModel = new ChangedModel(createNonExistingModel(
							modelAddress, model));
					changedModel.setExists(true);
					return changedModel;
				}
			case REMOVE:
				if (!modelExists) {
					// which kind of SAFE command?
					if (rc.getIntent() == Intent.Forced) {
						log.info("There is no change");
						return new ChangedModel(
								createNonExistingModel(rc.getChangedEntity(), model));
					} else {
						log.warn("Safe-X RepositoryCommand REMOVE failed, model was already removed");
						return null;
					}
				} else {
					assert model != null;
					if (rc.getIntent() == Intent.SafeRevBound) {
						if (model.getRevisionNumber() != rc.getRevisionNumber()) {
							log.warn("SafeRevBound RepositoryCommand REMOVE failed. Reason: "
									+ "modelRevNr:" + model.getRevisionNumber() + " cmdRevNr:"
									+ rc.getRevisionNumber() + " intent:" + rc.getIntent());
							return null;
						}
					}
					// do change
					if (log.isDebugEnabled()) {
						log.debug("Removing model " + model.getAddress() + " "
								+ model.getRevisionNumber());
					}
					final ChangedModel changedModel = new ChangedModel(model);
					changedModel.setExists(false);
					return changedModel;
				}

			default:
				throw new AssertionError("XRepositoryCommand with unexpected type: " + rc);
			}

		} else {
			if (model == null) {
				log.warn("Safe Non-RepositoryCommand '" + command + "' failed on null-model");
				return null;
			}

			final ChangedModel changedModel = new ChangedModel(model);

			// apply changes to the delta-model
			if (!changedModel.executeCommand(command)) {
				log.info("Could not execute command on ChangedModel");
				return null;
			}

			return changedModel;
		}
	}
}
