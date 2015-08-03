package org.xydra.core.change;

import java.util.LinkedList;
import java.util.List;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.XExistsWritableModel;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.model.XModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

/**
 * Note: This is similar to {@link DeltaUtils}.
 *
 * FIXME DOCU heavily document this
 *
 * @author xamde
 */
public class EventUtils {

	private static final Logger log = LoggerFactory.getLogger(EventUtils.class);

	private static final long MODEL_DOES_NOT_EXIST = -1;

	/**
	 * @param object @CanBeNull
	 * @param txnEvent
	 * @param inTxn
	 * @param partialModel
	 */
	private static void applyAtomicEvent(final XRevWritableObject object, final XAtomicEvent atomicEvent,
			final boolean inTxn, final boolean partialModel) {
		if (object == null) {
			/*
			 * we are working on a partial snapshot where this event is just
			 * irrelevant.
			 */
			if (log.isTraceEnabled()) {
				log.trace("Snapshot partial?" + partialModel + " and event " + atomicEvent
						+ " found not the matching object in snapshot. event ignored.");
			}
		} else {
			final XAddress modelAddress = object.getAddress().getParent();
			assert object != null;
			object.setRevisionNumber(atomicEvent.getRevisionNumber());
			if (atomicEvent instanceof XObjectEvent) {
				XyAssert.xyAssert(atomicEvent.getTarget().getParent().equals(modelAddress),
						"targetParent=" + atomicEvent.getTarget().getParent() + " vs. model="
								+ modelAddress);
				applyObjectEvent(object, (XObjectEvent) atomicEvent);
			} else {
				XyAssert.xyAssert(atomicEvent instanceof XFieldEvent);
				XyAssert.xyAssert(atomicEvent.getTarget().getParent().getParent()
						.equals(modelAddress));
				final XFieldEvent fieldEvent = (XFieldEvent) atomicEvent;
				final XRevWritableField field = object.getField(fieldEvent.getFieldId());
				if (field == null) {
					/* working on a partial snapshot ... */
				} else {
					assert field != null : "field is null";
					field.setRevisionNumber(atomicEvent.getRevisionNumber());
					applyFieldEvent(field, fieldEvent);
				}
			}
		}
	}

	/**
	 * @param model @NeverNull
	 * @param atomicEvent @NeverNull
	 * @param inTxn
	 * @param partialModel if true, the model can be a partial model. Events
	 *            addressing missing parts are silently dropped, if this flag is
	 *            true.
	 */
	private static void applyAtomicEvent(@NeverNull final XRevWritableModel model,
			@NeverNull final XAtomicEvent atomicEvent, final boolean inTxn, final boolean partialModel) {
		XyAssert.xyAssert(model != null);
		assert model != null;
		XyAssert.xyAssert(atomicEvent != null);
		assert atomicEvent != null;
		XyAssert.xyAssert(atomicEvent.getChangedEntity() != null);
		assert atomicEvent.getChangedEntity() != null;

		if (atomicEvent instanceof XRepositoryEvent) {
			XyAssert.xyAssert(atomicEvent.getChangedEntity().equals(model.getAddress()));
			applyRepositoryEvent(model, (XRepositoryEvent) atomicEvent);
			return;
		}
		XyAssert.xyAssert(model.getRevisionNumber() >= 0, "Should be >= 0 but is %s",
				model.getRevisionNumber());
		if (atomicEvent instanceof XModelEvent) {
			XyAssert.xyAssert(atomicEvent.getTarget().equals(model.getAddress()));
			applyModelEvent(model, (XModelEvent) atomicEvent);
		} else {
			// object & field events
			final XRevWritableObject object = model.getObject(atomicEvent.getTarget().getObject());
			applyAtomicEvent(object, atomicEvent, inTxn, partialModel);

		}
	}

	/**
	 * Worker method.
	 *
	 * @param model
	 * @param atomicEvent
	 * @param inTxn
	 */
	private static void applyAtomicEventIgnoreRev_worker(@NeverNull final XModel model,
			@NeverNull final XAtomicEvent atomicEvent, final boolean inTxn) {
		XyAssert.xyAssert(model != null);
		assert model != null;
		XyAssert.xyAssert(atomicEvent != null);
		assert atomicEvent != null;
		XyAssert.xyAssert(atomicEvent.getChangedEntity() != null);
		assert atomicEvent.getChangedEntity() != null;

		if (atomicEvent instanceof XRepositoryEvent) {
			XyAssert.xyAssert(atomicEvent.getChangedEntity().equals(model.getAddress()));
			applyRepositoryEventIgnoreRev(model, (XRepositoryEvent) atomicEvent);
			return;
		}
		assert model.getRevisionNumber() >= 0 : model.getRevisionNumber();
		if (atomicEvent instanceof XModelEvent) {
			XyAssert.xyAssert(atomicEvent.getTarget().equals(model.getAddress()));
			applyModelEventIgnoreRev(model, (XModelEvent) atomicEvent);
		} else {
			// object & field events
			final XWritableObject object = model.getObject(atomicEvent.getTarget().getObject());
			XyAssert.xyAssert(object != null, "object null for event %s", atomicEvent);
			assert object != null;
			if (atomicEvent instanceof XObjectEvent) {
				XyAssert.xyAssert(atomicEvent.getTarget().getParent().equals(model.getAddress()));
				applyObjectEventIgnoreRev(object, (XObjectEvent) atomicEvent);
			} else {
				XyAssert.xyAssert(atomicEvent instanceof XFieldEvent);
				XyAssert.xyAssert(atomicEvent.getTarget().getParent().getParent()
						.equals(model.getAddress()));
				final XFieldEvent fieldEvent = (XFieldEvent) atomicEvent;
				final XWritableField field = object.getField(fieldEvent.getFieldId());
				XyAssert.xyAssert(field != null, "field is null");
				assert field != null;
				applyFieldEventIgnoreRev(field, fieldEvent);
			}
		}
	}

	/**
	 * @param reference @CanBeNull
	 * @param model @NeverNull FIXME DOCU ### must contain the entities
	 *            addressed in event -- Is this OLD or NEW docu? Unclear!
	 * @param atomicEvent @NeverNull
	 * @param inTxn
	 * @param partialSnapshot if true, model can be a partial snapshot. Events
	 *            addressing missing parts are silently ignored.
	 */
	private static void applyAtomicEventNonDestructive(@CanBeNull final XReadableModel reference,
			@NeverNull final XRevWritableModel model, @NeverNull final XAtomicEvent atomicEvent, final boolean inTxn,
			final boolean partialSnapshot) {
		XyAssert.xyAssert(atomicEvent != null);
		assert atomicEvent != null;
		XyAssert.xyAssert(model != null);
		assert model != null;

		final XId objectId = atomicEvent.getTarget().getObject();
		if (objectId != null) {
			// => its a field or object event
			XRevWritableObject object = model.getObject(objectId);
			if (object == null) {
				XyAssert.xyAssert(
						partialSnapshot,
						"Must be a partial snapshot. "
								+ "This is an attempt to execute an event on a non-existing (non-relevant) part of the model. We would just ignore it.");
			} else {
				final XReadableObject referenceObject = reference == null ? null : reference
						.getObject(objectId);
				if (object == referenceObject) {
					object = SimpleObject.shallowCopy(object);
					model.addObject(object);
				}
				final XId fieldId = atomicEvent.getTarget().getField();
				if (fieldId != null) {
					// => its a field event
					final XRevWritableField field = object.getField(fieldId);
					if (field == null) {
						XyAssert.xyAssert(partialSnapshot, "field '" + atomicEvent.getTarget()
								+ "' is null, but snapshot is not partial.");
					} else {
						final XReadableField referenceField = referenceObject == null ? null
								: referenceObject.getField(fieldId);
						if (field == referenceField) {
							object.addField(new SimpleField(referenceField.getAddress(),
									referenceField.getRevisionNumber(), referenceField.getValue()));
						}
					}
				}
			}
		}

		applyAtomicEvent(model, atomicEvent, inTxn, partialSnapshot);
		XyAssert.xyAssert(reference != model, "just applied event " + atomicEvent + " inTxn?"
				+ inTxn);
	}

	/**
	 * Applies an event to a model. Event must be addressed to the model.
	 *
	 * Sets the revision number of the model.
	 *
	 * It is the responsibility of the caller to ensure that the provided model
	 * is at a state where the given event applies.
	 *
	 * @param model to which the event is applied. No copy is created. @NeverNull
	 * @param event to be applied
	 */
	public static void applyEvent(@NeverNull final XRevWritableModel model, @NeverNull final XEvent event) {
		XyAssert.xyAssert(model != null, "model may not be null");
		assert model != null;
		XyAssert.xyAssert(event != null, "event may not be null");
		assert event != null;

		if (event instanceof XTransactionEvent) {
			for (final XEvent txnEvent : (XTransactionEvent) event) {
				if (!txnEvent.isImplied()) {
					applyAtomicEvent(model, (XAtomicEvent) txnEvent, true, false);
				}
			}
		} else {
			XyAssert.xyAssert(event instanceof XAtomicEvent, event.getClass().getName());
			applyAtomicEvent(model, (XAtomicEvent) event, false, false);
		}
		/*
		 * As Model.ADD and REMOVE events are technically only managed within
		 * the change log of the model itself, we must even then record the
		 * changed revision number
		 */
		model.setRevisionNumber(event.getRevisionNumber());
	}

	public static void applyEvent(final XRevWritableObject objectState, final XEvent event) {
		XyAssert.xyAssert(objectState != null);
		assert objectState != null;
		XyAssert.xyAssert(event != null);
		assert event != null;

		if (event instanceof XTransactionEvent) {
			for (final XEvent txnEvent : (XTransactionEvent) event) {
				if (!txnEvent.isImplied()) {
					applyAtomicEvent(objectState, (XAtomicEvent) txnEvent, true, false);
				}
			}
		} else {
			XyAssert.xyAssert(event instanceof XAtomicEvent, event.getClass().getName());
			applyAtomicEvent(objectState, (XAtomicEvent) event, false, false);
		}
		objectState.setRevisionNumber(event.getRevisionNumber());
	}

	public static void applyEvent(final XRevWritableField fieldState, final XEvent event) {
		assert !(event instanceof XTransactionEvent);
		applyFieldEvent(fieldState, (XFieldEvent) event);
	}

	/**
	 * Note: Looks unused as of 2012-05
	 *
	 * @param model
	 * @param event
	 */
	@Deprecated
	public static void applyEventIgnoreRev(@NeverNull final XModel model, @NeverNull final XEvent event) {
		XyAssert.xyAssert(model != null);
		assert model != null;
		XyAssert.xyAssert(event != null);
		assert event != null;

		if (event instanceof XTransactionEvent) {
			for (final XEvent txnEvent : (XTransactionEvent) event) {
				if (!txnEvent.isImplied()) {
					applyAtomicEventIgnoreRev_worker(model, (XAtomicEvent) txnEvent, true);
				}
			}
		} else {
			XyAssert.xyAssert(event instanceof XAtomicEvent, event.getClass().getName());
			applyAtomicEventIgnoreRev_worker(model, (XAtomicEvent) event, false);
		}
	}

	/**
	 * Calculate the result of applying events to a model without changing the
	 * original model but copying as little as possible.
	 *
	 * @param reference The original model. Nothing contained in this model will
	 *            be changed, even if those parts are also contained in model.
	 * @param model The model to be modified. Any parts that need to be modified
	 *            but are also in the reference model are copied first. Never
	 *            null.
	 * @param event The events to apply.
	 * @param partialSnapshot if true, either reference or model might be
	 *            partial snapshots
	 * @return The result after applying the events. This model may share object
	 *         and/or fields with the original model, in which case modifying
	 *         them will change both models.
	 */
	public static XRevWritableModel applyEventNonDestructive(@CanBeNull final XReadableModel reference,
			@NeverNull final XRevWritableModel model, @NeverNull final XEvent event, final boolean partialSnapshot) {
		XyAssert.xyAssert(event != null);
		assert event != null;
		XyAssert.xyAssert(model != null);
		assert model != null;

		if (log.isTraceEnabled()) {
			log.trace("Apply event " + event);
		}

		XRevWritableModel result = model;
		if (event instanceof XTransactionEvent) {
			final XTransactionEvent trans = (XTransactionEvent) event;

			// If the whole model was removed, there is nothing else to process.
			final XAtomicEvent last = trans.getLastEvent();
			if (last instanceof XRepositoryEvent && last.getChangeType() == ChangeType.REMOVE) {
				result = new SimpleModel(model.getAddress(), MODEL_DOES_NOT_EXIST);
			} else {

				if (result == reference) {
					result = SimpleModel.shallowCopy(result);
					XyAssert.xyAssert(result != model, "copied");
				}

				for (final XAtomicEvent atomicEvent : (XTransactionEvent) event) {
					if (!atomicEvent.isImplied()) {
						applyAtomicEventNonDestructive(reference, result, atomicEvent, true,
								partialSnapshot);
					}
				}
			}
		} else {
			XyAssert.xyAssert(event instanceof XAtomicEvent, event.getClass().getName());

			if (result == reference) {
				result = SimpleModel.shallowCopy(result);
			}

			applyAtomicEventNonDestructive(reference, result, (XAtomicEvent) event, false,
					partialSnapshot);
		}

		result.setRevisionNumber(event.getRevisionNumber());
		return result;
	}

	/**
	 * Calculate the result of applying events to a model without changing the
	 * original model but copying as little as possible.
	 *
	 * @param model The original model.
	 * @param event The events to apply.
	 *
	 * @return The result after applying the events. This model may share object
	 *         and/or fields with the original model, in which case modifying
	 *         them will change both models.
	 */
	public static XRevWritableModel applyEventNonDestructive(@NeverNull final XRevWritableModel model,
			@NeverNull final XEvent event) {
		XyAssert.xyAssert(model != null);
		XyAssert.xyAssert(event != null);

		return applyEventNonDestructive(model, model, event, false);
	}

	/**
	 * @param field to which the event is applied
	 * @param event to be applied
	 */
	private static void applyFieldEvent(final XRevWritableField field, @NeverNull final XFieldEvent event) {
		XyAssert.xyAssert(field != null);
		assert field != null;

		switch (event.getChangeType()) {
		case ADD:
			XyAssert.xyAssert(field.isEmpty(), field.getValue());
			field.setValue(event.getNewValue());
			break;
		case CHANGE:
			XyAssert.xyAssert(!field.isEmpty());
			field.setValue(event.getNewValue());
			break;
		case REMOVE:
			XyAssert.xyAssert(!field.isEmpty(), "cannot remove value from empty field");
			field.setValue(null);
			break;
		case TRANSACTION:
			throw new IllegalStateException("XFieldEvent cannot be this " + event);
		}
		field.setRevisionNumber(event.getRevisionNumber());
	}

	/**
	 * @param field to which the event is applied
	 * @param event to be applied
	 */
	private static void applyFieldEventIgnoreRev(@NeverNull final XWritableField field, final XFieldEvent event) {
		XyAssert.xyAssert(field != null);
		assert field != null;

		switch (event.getChangeType()) {
		case ADD:
			// XyAssert.xyAssert(field.isEmpty(), field.getValue());
			field.setValue(event.getNewValue());
			break;
		case CHANGE:
			// XyAssert.xyAssert(!field.isEmpty());
			field.setValue(event.getNewValue());
			break;
		case REMOVE:
			XyAssert.xyAssert(!field.isEmpty(), "cannot remove value from empty field");
			field.setValue(null);
			break;
		case TRANSACTION:
			throw new IllegalStateException("XFieldEvent cannot be this " + event);
		}
	}

	/**
	 * @param model to which the event is applied
	 * @param event to be applied
	 */
	private static void applyModelEvent(final XRevWritableModel model, final XModelEvent event) {
		XyAssert.xyAssert(event.getTarget().equals(model.getAddress()));
		switch (event.getChangeType()) {
		case ADD: {
			XyAssert.xyAssert(!model.hasObject(event.getObjectId()),
					"Trying to add object '" + event.getObjectId()
							+ "' but its already present in model " + model.getAddress() + " rev:"
							+ model.getRevisionNumber());
			final XRevWritableObject object = model.createObject(event.getObjectId());
			object.setRevisionNumber(event.getRevisionNumber());
			break;
		}
		case REMOVE: {
			XyAssert.xyAssert(model.hasObject(event.getObjectId()));
			model.removeObject(event.getObjectId());
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("MovelEvents cannot be this " + event);
		}
	}

	/**
	 * @param model to which the event is applied
	 * @param event to be applied
	 */
	private static void applyModelEventIgnoreRev(final XModel model, final XModelEvent event) {
		XyAssert.xyAssert(event.getTarget().equals(model.getAddress()));
		switch (event.getChangeType()) {
		case ADD: {
			// XyAssert.xyAssert(!model.hasObject(event.getObjectId()),
			// "applying ADD event but model had already object " +
			// event.getObjectId());
			model.createObject(event.getObjectId());
			break;
		}
		case REMOVE: {
			XyAssert.xyAssert(model.hasObject(event.getObjectId()));
			model.removeObject(event.getObjectId());
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("MovelEvents cannot be this " + event);
		}
	}

	/**
	 * @param object to which the event is applied
	 * @param event to be applied
	 */
	private static void applyObjectEvent(@NeverNull final XRevWritableObject object, final XObjectEvent event) {
		XyAssert.xyAssert(object != null, "object was null");
		assert object != null;

		switch (event.getChangeType()) {
		case ADD: {
			XyAssert.xyAssert(!object.hasField(event.getFieldId()),
					"object %s had already field %s", object.getAddress(), event.getFieldId());
			final XRevWritableField field = object.createField(event.getFieldId());
			field.setRevisionNumber(event.getRevisionNumber());
			break;
		}
		case REMOVE: {
			XyAssert.xyAssert(object.hasField(event.getFieldId()),
					"object '%s' should have field '%s' in rev %s", object.getAddress(),
					event.getFieldId(), object.getRevisionNumber());
			object.removeField(event.getFieldId());
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("XObjectEvents cannot be this " + event);
		}
	}

	/**
	 * @param object to which the event is applied
	 * @param event to be applied
	 */
	private static void applyObjectEventIgnoreRev(@NeverNull final XWritableObject object,
			final XObjectEvent event) {
		XyAssert.xyAssert(object != null);
		assert object != null;

		switch (event.getChangeType()) {
		case ADD: {
			// XyAssert.xyAssert(!object.hasField(event.getFieldId()));
			object.createField(event.getFieldId());
			break;
		}
		case REMOVE: {
			XyAssert.xyAssert(object.hasField(event.getFieldId()), "object " + object.getAddress()
					+ " should have field " + event.getFieldId());
			object.removeField(event.getFieldId());
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("XObjectEvents cannot be this " + event);
		}
	}

	/**
	 * Apply the {@link XRepositoryEvent} to the given model.
	 *
	 * @param model to which the event is applied. No copy is created.
	 * @param event to be applied
	 */
	private static void applyRepositoryEvent(final XRevWritableModel model, final XRepositoryEvent event) {
		XyAssert.xyAssert(event.getChangedEntity().equals(model.getAddress()));
		switch (event.getChangeType()) {
		case ADD: {
			/*
			 * if this triggers a bug, repo.clear might have failed -- maybe a
			 * test-only problem
			 */
			if (!model.isEmpty()) {
				throw new IllegalStateException(
						"Cannot apply repository ADD event to non-empty model "
								+ model.getAddress() + " r" + model.getRevisionNumber() + " Event="
								+ event);
			}
			model.setRevisionNumber(event.getRevisionNumber());
			if (model instanceof XExistsWritableModel) {
				((XExistsWritableModel) model).setExists(true);
			}
			break;
		}
		case REMOVE: {
			XyAssert.xyAssert(model.getRevisionNumber() >= 0, model.getRevisionNumber());
			model.setRevisionNumber(MODEL_DOES_NOT_EXIST);
			// clear model
			final List<XId> ids = new LinkedList<XId>();
			for (final XId id : model) {
				ids.add(id);
			}
			for (final XId id : ids) {
				model.removeObject(id);
			}
			if (model instanceof XExistsWritableModel) {
				((XExistsWritableModel) model).setExists(false);
			}
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("XRepositoryEvent cannot be this " + event);
		}
	}

	/**
	 * Apply the {@link XRepositoryEvent} to the given model.
	 *
	 * @param model to which the event is applied. No copy is created.
	 * @param event to be applied
	 */
	private static void applyRepositoryEventIgnoreRev(final XModel model, final XRepositoryEvent event) {
		XyAssert.xyAssert(event.getChangedEntity().equals(model.getAddress()));
		switch (event.getChangeType()) {
		case ADD: {
			/*
			 * if this triggers a bug, repo.clear might have failed -- maybe a
			 * test -only problem
			 */
			if (!model.isEmpty()) {
				throw new IllegalStateException(
						"Cannot apply repository ADD event to non-empty model "
								+ model.getAddress() + " " + model.getRevisionNumber());
			}
			if (model instanceof XExistsWritableModel) {
				((XExistsWritableModel) model).setExists(true);
			}
			break;
		}
		case REMOVE: {
			XyAssert.xyAssert(model.getRevisionNumber() >= 0, model.getRevisionNumber());
			// clear model
			final List<XId> ids = new LinkedList<XId>();
			for (final XId id : model) {
				ids.add(id);
			}
			for (final XId id : ids) {
				model.removeObject(id);
			}
			if (model instanceof XExistsWritableModel) {
				((XExistsWritableModel) model).setExists(false);
			}
			break;
		}
		case CHANGE:
		case TRANSACTION:
			throw new IllegalStateException("XRepositoryEvent cannot be this " + event);
		}
	}

}
