package org.xydra.base.change.impl.memory;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.value.XValue;


/**
 * A factory class providing helpful methods for creating various types of
 * {@link XCommand XCommands}.
 *
 * @author kaidel
 *
 */

public class MemoryCommandFactory implements XCommandFactory {

    /**
     * Creates a new {@link XObjectCommand} of the add-type for adding a new
     * field to an object.
     *
     * @param objectId The {@link XId} of the object to which the field is to be
     *            added.
     * @param fieldId The {@link XId} for the new field.
     * @return a new {@link XObjectCommand} of the add-type
     */
    public XObjectCommand createAddFieldCommand(final XId objectId, final XId fieldId) {
        return createAddFieldCommand(Base.resolveObject((XId)null, null, objectId), fieldId, false);
    }

    /**
     * Creates a new {@link XObjectCommand} of the add-type for adding a new
     * field to an object.
     *
     * @param objectId The {@link XId} of the object to which the field is to be
     *            added.
     * @param fieldId The {@link XId} for the new field.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return a new {@link XObjectCommand} of the add-type
     */
    public XObjectCommand createAddFieldCommand(final XId objectId, final XId fieldId, final boolean isForced) {
        return createAddFieldCommand(Base.resolveObject((XId)null, (XId)null, objectId), fieldId,
                isForced);
    }

    /**
     * Creates a new {@link XObjectCommand} of the add-type for adding a new
     * field to an object.
     *
     * @param modelId The {@link XId} of the model containing the object to
     *            which the field is too be added.
     * @param objectId The {@link XId} of the object to which the field is to be
     *            added.
     * @param fieldId The {@link XId} for the new field.
     * @return a new {@link XObjectCommand} of the add-type
     */
    public XObjectCommand createAddFieldCommand(final XId modelId, final XId objectId, final XId fieldId) {
        return createAddFieldCommand(Base.resolveObject((XId)null, modelId, objectId), fieldId,
                false);
    }

    /**
     * Creates a new {@link XObjectCommand} of the add-type for adding a new
     * field to an object.
     *
     * @param modelId The {@link XId} of the model containing the object to
     *            which the field is too be added.
     * @param objectId The {@link XId} of the object to which the field is to be
     *            added.
     * @param fieldId The {@link XId} for the new field.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return a new {@link XObjectCommand} of the add-type
     */
    public XObjectCommand createAddFieldCommand(final XId modelId, final XId objectId, final XId fieldId,
            final boolean isForced) {
        return createAddFieldCommand(Base.resolveObject((XId)null, modelId, objectId), fieldId,
                isForced);
    }

    /**
     * Creates a new {@link XObjectCommand} of the add-type for adding a new
     * field to an object.
     *
     * @param repositoryId The {@link XId} of the repository holding the the
     *            model which is holding the object to which the field is to be
     *            added.
     * @param modelId The {@link XId} of the model containing the object to
     *            which the field is too be added.
     * @param objectId The {@link XId} of the object to which the field is to be
     *            added.
     * @param fieldId The {@link XId} for the new field.
     * @return a new {@link XObjectCommand} of the add-type
     */
    public XObjectCommand createAddFieldCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId) {
        return createAddFieldCommand(Base.resolveObject(repositoryId, modelId, objectId), fieldId,
                false);
    }

    private static XObjectCommand createAddFieldCommand(final XId repositoryId, final XId modelId,
            final XId objectId, final XId fieldId, final boolean isForced) {
        final long revNr = isForced ? XCommand.FORCED : XCommand.SAFE_STATE_BOUND;

        final XAddress target = Base.toAddress(repositoryId, modelId, objectId, null);

        return MemoryObjectCommand.createAddCommand(target, revNr, fieldId);
    }

    /**
     * Creates a new {@link XRepositoryCommand} of the add-type for adding a new
     * model to an repository
     *
     * @param repositoryId The {@link XId} of the repository to which the new
     *            model is to be added.
     * @param modelId The {@link XId} for the new model.
     * @return A new {@link XRepositoryCommand} of the add-type.
     */
    public XRepositoryCommand createAddModelCommand(final XId repositoryId, final XId modelId) {
        return createAddModelCommand(repositoryId, modelId, false);
    }

    @Override
    public XRepositoryCommand createAddModelCommand(final XId repositoryId, final XId modelId, final boolean isForced) {
        final long revNr = isForced ? XCommand.FORCED : XCommand.SAFE_STATE_BOUND;

        final XAddress target = Base.toAddress(repositoryId, null, null, null);

        return MemoryRepositoryCommand.createAddCommand(target, revNr, modelId);
    }

    /**
     * Creates a new {@link XModelCommand} of the add-type for adding a new
     * object to an model.
     *
     * @param modelId The {@link XId} of the model to which the new object is to
     *            be added.
     * @param objectId The {@link XId} for the new object.
     * @return A new {@link XModelCommand} of the add-type.
     */
    public XModelCommand createAddObjectCommand(final XId modelId, final XId objectId) {
        return createAddObjectCommand(Base.resolveModel((XId)null, modelId), objectId, false);
    }

    /**
     * Creates a new {@link XModelCommand} of the add-type for adding a new
     * object to an model.
     *
     * @param modelId The {@link XId} of the model to which the new object is to
     *            be added.
     * @param objectId The {@link XId} for the new object.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return A new {@link XModelCommand} of the add-type.
     */
    public XModelCommand createAddObjectCommand(final XId modelId, final XId objectId, final boolean isForced) {
        return createAddObjectCommand(Base.resolveModel((XId)null, modelId), objectId, isForced);
    }

    /**
     * Creates a new {@link XModelCommand} of the add-type for adding a new
     * object to an model.
     *
     * @param repositoryId The {@link XId} of the repository holding the model
     *            to which the new object is to be added.
     * @param modelId The {@link XId} of the model to which the new object is to
     *            be added.
     * @param objectId The {@link XId} for the new object.
     * @return A new {@link XModelCommand} of the add-type.
     */
    public XModelCommand createAddObjectCommand(final XId repositoryId, final XId modelId, final XId objectId) {
        return createAddObjectCommand(Base.resolveModel(repositoryId, modelId), objectId, false);
    }

    private static XModelCommand createAddObjectCommand(final XId repositoryId, final XId modelId,
            final XId objectId, final boolean isForced) {
        final long revNr = isForced ? XCommand.FORCED : XCommand.SAFE_STATE_BOUND;

        final XAddress target = Base.toAddress(repositoryId, modelId, null, null);

        return MemoryModelCommand.createAddCommand(target, revNr, objectId);
    }

    /**
     * Creates a new {@link XFieldCommand} of the add-type for adding an
     * {@link XValue} to an field.
     *
     * @param fieldId The {@link XId} of the field to which the {@link XValue}
     *            is to be added.
     * @param fieldRevision The current revision number of the field to which
     *            the {@link XValue} is to be added.
     * @param value the {@link XValue} which is to be added.
     * @return A new {@link XFieldCommand} of the add-type.
     */
    public XFieldCommand createAddValueCommand(final XId fieldId, final long fieldRevision, final XValue value) {
        return createAddValueCommand(Base.resolveField((XId)null, (XId)null, (XId)null, fieldId),
                fieldRevision, value, false);
    }

    /**
     * Creates a new {@link XFieldCommand} of the add-type for adding an
     * {@link XValue} to an field.
     *
     * @param fieldId The {@link XId} of the field to which the {@link XValue}
     *            is to be added.
     * @param fieldRevision The current revision number of the field to which
     *            the {@link XValue} is to be added.
     * @param value the {@link XValue} which is to be added.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return A new {@link XFieldCommand} of the add-type.
     */
    public XFieldCommand createAddValueCommand(final XId fieldId, final long fieldRevision, final XValue value,
            final boolean isForced) {
        return createAddValueCommand(Base.resolveField((XId)null, (XId)null, (XId)null, fieldId),
                fieldRevision, value, isForced);
    }

    /**
     * Creates a new {@link XFieldCommand} of the add-type for adding an
     * {@link XValue} to an field.
     *
     * @param objectId The {@link XId} of the object holding the field to which
     *            the {@link XValue} is to be added.
     * @param fieldId The {@link XId} of the field to which the {@link XValue}
     *            is to be added.
     * @param fieldRevision The current revision number of the field to which
     *            the {@link XValue} is to be added.
     * @param value the {@link XValue} which is to be added.
     * @return A new {@link XFieldCommand} of the add-type.
     */
    public XFieldCommand createAddValueCommand(final XId objectId, final XId fieldId, final long fieldRevision,
            final XValue value) {
        return createAddValueCommand(Base.resolveField((XId)null, (XId)null, objectId, fieldId),
                fieldRevision, value, false);
    }

    /**
     * Creates a new {@link XFieldCommand} of the add-type for adding an
     * {@link XValue} to an field.
     *
     * @param objectId The {@link XId} of the object holding the field to which
     *            the {@link XValue} is to be added.
     * @param fieldId The {@link XId} of the field to which the {@link XValue}
     *            is to be added.
     * @param fieldRevision The current revision number of the field to which
     *            the {@link XValue} is to be added.
     * @param value the {@link XValue} which is to be added.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return A new {@link XFieldCommand} of the add-type.
     */
    public XFieldCommand createAddValueCommand(final XId objectId, final XId fieldId, final long fieldRevision,
            final XValue value, final boolean isForced) {
        return createAddValueCommand(Base.resolveField((XId)null, (XId)null, objectId, fieldId),
                fieldRevision, value, isForced);
    }

    /**
     * Creates a new {@link XFieldCommand} of the add-type for adding an
     * {@link XValue} to an field.
     *
     * @param modelId The {@link XId} of the model holding the object holding
     *            the field to which the {@link XValue} is to be added.
     * @param objectId The {@link XId} of the object holding the field to which
     *            the {@link XValue} is to be added.
     * @param fieldId The {@link XId} of the field to which the {@link XValue}
     *            is to be added.
     * @param fieldRevision The current revision number of the field to which
     *            the {@link XValue} is to be added.
     * @param value the {@link XValue} which is to be added.
     * @return A new {@link XFieldCommand} of the add-type.
     */
    public XFieldCommand createAddValueCommand(final XId modelId, final XId objectId, final XId fieldId,
            final long fieldRevision, final XValue value) {
        return createAddValueCommand(Base.resolveField((XId)null, modelId, objectId, fieldId),
                fieldRevision, value, false);
    }

    /**
     * Creates a new {@link XFieldCommand} of the add-type for adding an
     * {@link XValue} to an field.
     *
     * @param modelId The {@link XId} of the model holding the object holding
     *            the field to which the {@link XValue} is to be added.
     * @param objectId The {@link XId} of the object holding the field to which
     *            the {@link XValue} is to be added.
     * @param fieldId The {@link XId} of the field to which the {@link XValue}
     *            is to be added.
     * @param fieldRevision The current revision number of the field to which
     *            the {@link XValue} is to be added.
     * @param value the {@link XValue} which is to be added.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return A new {@link XFieldCommand} of the add-type.
     */
    public XFieldCommand createAddValueCommand(final XId modelId, final XId objectId, final XId fieldId,
            final long fieldRevision, final XValue value, final boolean isForced) {
        return createAddValueCommand(Base.resolveField((XId)null, modelId, objectId, fieldId),
                fieldRevision, value, isForced);
    }

    /**
     * Creates a new {@link XFieldCommand} of the add-type for adding an
     * {@link XValue} to an field.
     *
     * @param repositoryId The {@link XId} of the model holding the object
     *            holding the field to which the {@link XValue} is to be added.
     * @param modelId The {@link XId} of the model holding the object holding
     *            the field to which the {@link XValue} is to be added.
     * @param objectId The {@link XId} of the object holding the field to which
     *            the {@link XValue} is to be added.
     * @param fieldId The {@link XId} of the field to which the {@link XValue}
     *            is to be added.
     * @param fieldRevision The current revision number of the field to which
     *            the {@link XValue} is to be added.
     * @param value the {@link XValue} which is to be added.
     *
     * @return A new {@link XFieldCommand} of the add-type.
     */
    public XFieldCommand createAddValueCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId, final long fieldRevision, final XValue value) {
        return createAddValueCommand(Base.resolveField(repositoryId, modelId, objectId, fieldId),
                fieldRevision, value, false);
    }

    private static XFieldCommand createAddValueCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId, final long fieldRevision, final XValue value, final boolean isForced) {
        if(fieldRevision == XCommand.FORCED && !isForced) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        long revNr = fieldRevision;

        if(isForced) {
            revNr = XCommand.FORCED;
        }

        final XAddress target = Base.toAddress(repositoryId, modelId, objectId, fieldId);

        return MemoryFieldCommand.createAddCommand(target, revNr, value);
    }

    /**
     * Creates a new {@link XFieldCommand} of the change-type for changing the
     * {@link XValue} of an field.
     *
     * @param fieldId The {@link XId} of the field of which the {@link XValue}
     *            is to be changed.
     * @param fieldRevision The current revision number of the field of which
     *            the {@link XValue} is to be changed.
     * @param value the new {@link XValue}.
     * @return A new {@link XFieldCommand} of the change-type.
     */

    public XFieldCommand createChangeValueCommand(final XId fieldId, final long fieldRevision, final XValue value) {
        return createChangeValueCommand(null, null, null, fieldId, fieldRevision, value, false);
    }

    /**
     * Creates a new {@link XFieldCommand} of the change-type for changing the
     * {@link XValue} of an field.
     *
     * @param fieldId The {@link XId} of the field of which the {@link XValue}
     *            is to be changed.
     * @param fieldRevision The current revision number of the field of which
     *            the {@link XValue} is to be changed.
     * @param value the new {@link XValue}.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return A new {@link XFieldCommand} of the change-type.
     */
    public XFieldCommand createChangeValueCommand(final XId fieldId, final long fieldRevision, final XValue value,
            final boolean isForced) {
        return createChangeValueCommand(null, null, null, fieldId, fieldRevision, value, isForced);
    }

    /**
     * Creates a new {@link XFieldCommand} of the change-type for changing the
     * {@link XValue} of an field.
     *
     * @param objectId The {@link XId} of the object holding the field of which
     *            the {@link XValue} is to be changed.
     * @param fieldId The {@link XId} of the field of which the {@link XValue}
     *            is to be changed.
     * @param fieldRevision The current revision number of the field of which
     *            the {@link XValue} is to be changed.
     * @param value the new {@link XValue}.
     * @return A new {@link XFieldCommand} of the change-type.
     */
    public XFieldCommand createChangeValueCommand(final XId objectId, final XId fieldId, final long fieldRevision,
            final XValue value) {
        return createChangeValueCommand(null, null, objectId, fieldId, fieldRevision, value, false);
    }

    /**
     * Creates a new {@link XFieldCommand} of the change-type for changing the
     * {@link XValue} of an field.
     *
     * @param objectId The {@link XId} of the object holding the field of which
     *            the {@link XValue} is to be changed.
     * @param fieldId The {@link XId} of the field of which the {@link XValue}
     *            is to be changed.
     * @param fieldRevision The current revision number of the field of which
     *            the {@link XValue} is to be changed.
     * @param value the new {@link XValue}.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return A new {@link XFieldCommand} of the change-type.
     */
    public XFieldCommand createChangeValueCommand(final XId objectId, final XId fieldId, final long fieldRevision,
            final XValue value, final boolean isForced) {
        return createChangeValueCommand(null, null, objectId, fieldId, fieldRevision, value,
                isForced);
    }

    /**
     * Creates a new {@link XFieldCommand} of the change-type for changing the
     * {@link XValue} of an field.
     *
     * @param modelId The {@link XId} of the model holding the object holding
     *            the field of which the {@link XValue} is to be changed.
     * @param objectId The {@link XId} of the object holding the field of which
     *            the {@link XValue} is to be changed.
     * @param fieldId The {@link XId} of the field of which the {@link XValue}
     *            is to be changed.
     * @param fieldRevision The current revision number of the field of which
     *            the {@link XValue} is to be changed.
     * @param value the new {@link XValue}.
     * @return A new {@link XFieldCommand} of the change-type.
     */
    public XFieldCommand createChangeValueCommand(final XId modelId, final XId objectId, final XId fieldId,
            final long fieldRevision, final XValue value) {
        return createChangeValueCommand(null, modelId, objectId, fieldId, fieldRevision, value,
                false);
    }

    /**
     * Creates a new {@link XFieldCommand} of the change-type for changing the
     * {@link XValue} of an field.
     *
     * @param modelId The {@link XId} of the model holding the object holding
     *            the field of which the {@link XValue} is to be changed.
     * @param objectId The {@link XId} of the object holding the field of which
     *            the {@link XValue} is to be changed.
     * @param fieldId The {@link XId} of the field of which the {@link XValue}
     *            is to be changed.
     * @param fieldRevision The current revision number of the field of which
     *            the {@link XValue} is to be changed.
     * @param value the new {@link XValue}.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return A new {@link XFieldCommand} of the change-type.
     */
    public XFieldCommand createChangeValueCommand(final XId modelId, final XId objectId, final XId fieldId,
            final long fieldRevision, final XValue value, final boolean isForced) {
        return createChangeValueCommand(null, modelId, objectId, fieldId, fieldRevision, value,
                isForced);
    }

    /**
     * Creates a new {@link XFieldCommand} of the change-type for changing the
     * {@link XValue} of an field.
     *
     * @param repositoryId The {@link XId} of the model holding the object
     *            holding the field of which the {@link XValue} is to be
     *            changed.
     * @param modelId The {@link XId} of the model holding the object holding
     *            the field of which the {@link XValue} is to be changed.
     * @param objectId The {@link XId} of the object holding the field of which
     *            the {@link XValue} is to be changed.
     * @param fieldId The {@link XId} of the field of which the {@link XValue}
     *            is to be changed.
     * @param fieldRevision The current revision number of the field of which
     *            the {@link XValue} is to be changed.
     * @param value the new {@link XValue}.
     * @return A new {@link XFieldCommand} of the change-type.
     */
    public XFieldCommand createChangeValueCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId, final long fieldRevision, final XValue value) {
        return createChangeValueCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
                value, false);
    }

    private static XFieldCommand createChangeValueCommand(final XId repositoryId, final XId modelId,
            final XId objectId, final XId fieldId, final long fieldRevision, final XValue value, final boolean isForced) {
        if(fieldRevision == XCommand.FORCED && !isForced) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        long revNr = fieldRevision;

        if(isForced) {
            revNr = XCommand.FORCED;
        }

        final XAddress target = Base.toAddress(repositoryId, modelId, objectId, fieldId);

        return MemoryFieldCommand.createChangeCommand(target, revNr, value);
    }

    /**
     * Creates a new {@link XObjectCommand} of the remove-type for removing an
     * field from an object.
     *
     * @param objectId The {@link XId} of the object from which the field is to
     *            be removed.
     * @param fieldId The {@link XId} for the field which is to be removed.
     * @param fieldRevision The current revision number of the field which is to
     *            be removed
     * @return a new {@link XObjectCommand} of the remove-type
     */
    public XObjectCommand createRemoveFieldCommand(final XId objectId, final XId fieldId, final long fieldRevision) {
        return createRemoveFieldCommand(null, null, objectId, fieldId, fieldRevision, false);
    }

    /**
     * Creates a new {@link XObjectCommand} of the remove-type for removing an
     * field from an object.
     *
     * @param objectId The {@link XId} of the object from which the field is to
     *            be removed.
     * @param fieldId The {@link XId} for the field which is to be removed.
     * @param fieldRevision The current revision number of the field which is to
     *            be removed
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return a new {@link XObjectCommand} of the remove-type
     */
    public XObjectCommand createRemoveFieldCommand(final XId objectId, final XId fieldId, final long fieldRevision,
            final boolean isForced) {
        return createRemoveFieldCommand(null, null, objectId, fieldId, fieldRevision, isForced);
    }

    /**
     * Creates a new {@link XObjectCommand} of the remove-type for removing an
     * field from an object.
     *
     * @param modelId the {@link XId} of the model holding the object from which
     *            the field is to be removed.
     * @param objectId The {@link XId} of the object from which the field is to
     *            be removed.
     * @param fieldId The {@link XId} for the field which is to be removed.
     * @param fieldRevision The current revision number of the field which is to
     *            be removed
     * @return a new {@link XObjectCommand} of the remove-type
     */
    public XObjectCommand createRemoveFieldCommand(final XId modelId, final XId objectId, final XId fieldId,
            final long fieldRevision) {
        return createRemoveFieldCommand(null, modelId, objectId, fieldId, fieldRevision, false);
    }

    /**
     * Creates a new {@link XObjectCommand} of the remove-type for removing an
     * field from an object.
     *
     * @param modelId the {@link XId} of the model holding the object from which
     *            the field is to be removed.
     * @param objectId The {@link XId} of the object from which the field is to
     *            be removed.
     * @param fieldId The {@link XId} for the field which is to be removed.
     * @param fieldRevision The current revision number of the field which is to
     *            be removed
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return a new {@link XObjectCommand} of the remove-type
     */
    public XObjectCommand createRemoveFieldCommand(final XId modelId, final XId objectId, final XId fieldId,
            final long fieldRevision, final boolean isForced) {
        return createRemoveFieldCommand(null, modelId, objectId, fieldId, fieldRevision, isForced);
    }

    /**
     * Creates a new {@link XObjectCommand} of the remove-type for removing an
     * field from an object.
     *
     * @param repositoryId the {@link XId} of the repository holding the model
     *            holding the object from which the field is to be removed.
     * @param modelId the {@link XId} of the model holding the object from which
     *            the field is to be removed.
     * @param objectId The {@link XId} of the object from which the field is to
     *            be removed.
     * @param fieldId The {@link XId} for the field which is to be removed.
     * @param fieldRevision The current revision number of the field which is to
     *            be removed
     * @return a new {@link XObjectCommand} of the remove-type
     */
    public XObjectCommand createRemoveFieldCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId, final long fieldRevision) {
        return createRemoveFieldCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
                false);
    }

    @Override
    public XObjectCommand createRemoveFieldCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId, final long fieldRevision, final boolean isForced) {
        if(fieldRevision == XCommand.FORCED && !isForced) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        long revNr = fieldRevision;

        if(isForced) {
            revNr = XCommand.FORCED;
        }

        final XAddress target = Base.toAddress(repositoryId, modelId, objectId, null);

        return MemoryObjectCommand.createRemoveCommand(target, revNr, fieldId);
    }

    /**
     * Creates a new {@link XRepositoryCommand} of the remove-type for removing
     * an model from an repository.
     *
     * @param repositoryId The {@link XId} of the repository from which the
     *            model is to be removed.
     * @param modelId The {@link XId} of the model which is to be removed.
     * @param modelRevision the current revision number of the model which is to
     *            be removed
     * @return A new {@link XRepositoryCommand} of the remove-type.
     */
    public XRepositoryCommand createRemoveModelCommand(final XId repositoryId, final XId modelId,
            final long modelRevision) {
        return createRemoveModelCommand(repositoryId, modelId, modelRevision, false);
    }

    @Override
    public XRepositoryCommand createRemoveModelCommand(final XId repositoryId, final XId modelId,
            final long modelRevision, final boolean isForced) {
        if(modelRevision == XCommand.FORCED && !isForced) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        long revNr = modelRevision;

        if(isForced) {
            revNr = XCommand.FORCED;
        }

        final XAddress target = Base.toAddress(repositoryId, null, null, null);

        return MemoryRepositoryCommand.createRemoveCommand(target, revNr, modelId);
    }

    /**
     * Creates a new {@link XModelCommand} of the remove-type for removing an
     * object from an model.
     *
     * @param modelId The {@link XId} of the model from which the object is to
     *            be removed.
     * @param objectId The {@link XId} of the object which is to be removed.
     * @param objectRevision The current revision number of the object which is
     *            to be removed.
     * @return A new {@link XModelCommand} of the remove-type.
     */
    public XModelCommand createRemoveObjectCommand(final XId modelId, final XId objectId, final long objectRevision) {
        return createRemoveObjectCommand(null, modelId, objectId, objectRevision, false);
    }

    /**
     * Creates a new {@link XModelCommand} of the remove-type for removing an
     * object from an model.
     *
     * @param modelId The {@link XId} of the model from which the object is to
     *            be removed.
     * @param objectId The {@link XId} of the object which is to be removed.
     * @param objectRevision The current revision number of the object which is
     *            to be removed.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return A new {@link XModelCommand} of the remove-type.
     */
    public XModelCommand createRemoveObjectCommand(final XId modelId, final XId objectId, final long objectRevision,
            final boolean isForced) {
        return createRemoveObjectCommand(null, modelId, objectId, objectRevision, isForced);
    }

    /**
     * Creates a new {@link XModelCommand} of the remove-type for removing an
     * object from an model.
     *
     * @param repositoryId The {@link XId} of the repository holding the model
     *            from which the object is to be removed.
     * @param modelId The {@link XId} of the model from which the object is to
     *            be removed.
     * @param objectId The {@link XId} of the object which is to be removed.
     * @param objectRevision The current revision number of the object which is
     *            to be removed.
     * @return A new {@link XModelCommand} of the remove-type.
     */
    public XModelCommand createRemoveObjectCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final long objectRevision) {
        return createRemoveObjectCommand(repositoryId, modelId, objectId, objectRevision, false);
    }

    @Override
    public XModelCommand createRemoveObjectCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final long objectRevision, final boolean isForced) {
        if(objectRevision == XCommand.FORCED && !isForced) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        long revNr = objectRevision;

        if(isForced) {
            revNr = XCommand.FORCED;
        }

        final XAddress target = Base.toAddress(repositoryId, modelId, null, null);

        return MemoryModelCommand.createRemoveCommand(target, revNr, objectId);
    }

    /**
     * Creates a new {@link XFieldCommand} of the remove-type for removing an
     * {@link XValue} from an field.
     *
     * @param fieldId The {@link XId} of the field from which the {@link XValue}
     *            is to be removed.
     * @param fieldRevision The current revision number of the field from which
     *            the {@link XValue} is to be removed.
     * @return A new {@link XFieldCommand} of the remove-type.
     */
    public XFieldCommand createRemoveValueCommand(final XId fieldId, final long fieldRevision) {
        return createRemoveValueCommand(null, null, null, fieldId, fieldRevision, false);
    }

    /**
     * Creates a new {@link XFieldCommand} of the remove-type for removing an
     * {@link XValue} from an field.
     *
     * @param fieldId The {@link XId} of the field from which the {@link XValue}
     *            is to be removed.
     * @param fieldRevision The current revision number of the field from which
     *            the {@link XValue} is to be removed.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return A new {@link XFieldCommand} of the remove-type.
     */
    public XFieldCommand createRemoveValueCommand(final XId fieldId, final long fieldRevision, final boolean isForced) {
        return createRemoveValueCommand(null, null, null, fieldId, fieldRevision, isForced);
    }

    /**
     * Creates a new {@link XFieldCommand} of the remove-type for removing an
     * {@link XValue} from an field.
     *
     * @param objectId The {@link XId} of the object holding the field to which
     *            the {@link XValue} is to be added.
     * @param fieldId The {@link XId} of the field from which the {@link XValue}
     *            is to be removed.
     * @param fieldRevision The current revision number of the field from which
     *            the {@link XValue} is to be removed.
     * @return A new {@link XFieldCommand} of the remove-type.
     */
    public XFieldCommand createRemoveValueCommand(final XId objectId, final XId fieldId, final long fieldRevision) {
        return createRemoveValueCommand(null, null, objectId, fieldId, fieldRevision, false);
    }

    /**
     * Creates a new {@link XFieldCommand} of the remove-type for removing an
     * {@link XValue} from an field.
     *
     * @param objectId The {@link XId} of the object holding the field to which
     *            the {@link XValue} is to be added.
     * @param fieldId The {@link XId} of the field from which the {@link XValue}
     *            is to be removed.
     * @param fieldRevision The current revision number of the field from which
     *            the {@link XValue} is to be removed.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return A new {@link XFieldCommand} of the remove-type.
     */
    public XFieldCommand createRemoveValueCommand(final XId objectId, final XId fieldId, final long fieldRevision,
            final boolean isForced) {
        return createRemoveValueCommand(null, null, objectId, fieldId, fieldRevision, isForced);
    }

    /**
     * Creates a new {@link XFieldCommand} of the remove-type for removing an
     * {@link XValue} from an field.
     *
     * @param modelId The {@link XId} of the model holding the object holding
     *            the field from which the {@link XValue} is to be removed.
     * @param objectId The {@link XId} of the object holding the field to which
     *            the {@link XValue} is to be added.
     * @param fieldId The {@link XId} of the field from which the {@link XValue}
     *            is to be removed.
     * @param fieldRevision The current revision number of the field from which
     *            the {@link XValue} is to be removed.
     * @return A new {@link XFieldCommand} of the remove-type.
     */
    public XFieldCommand createRemoveValueCommand(final XId modelId, final XId objectId, final XId fieldId,
            final long fieldRevision) {
        return createRemoveValueCommand(null, modelId, objectId, fieldId, fieldRevision, false);
    }

    /**
     * Creates a new {@link XFieldCommand} of the remove-type for removing an
     * {@link XValue} from an field.
     *
     * @param modelId The {@link XId} of the model holding the object holding
     *            the field from which the {@link XValue} is to be removed.
     * @param objectId The {@link XId} of the object holding the field to which
     *            the {@link XValue} is to be added.
     * @param fieldId The {@link XId} of the field from which the {@link XValue}
     *            is to be removed.
     * @param fieldRevision The current revision number of the field from which
     *            the {@link XValue} is to be removed.
     * @param isForced this parameter determines if the new command will be a
     *            forced command or not.
     * @return A new {@link XFieldCommand} of the remove-type.
     */
    public XFieldCommand createRemoveValueCommand(final XId modelId, final XId objectId, final XId fieldId,
            final long fieldRevision, final boolean isForced) {
        return createRemoveValueCommand(null, modelId, objectId, fieldId, fieldRevision, isForced);
    }

    /**
     * Creates a new {@link XFieldCommand} of the remove-type for removing an
     * {@link XValue} from an field.
     *
     * @param repositoryId The {@link XId} of the model holding the object
     *            holding the field from which the {@link XValue} is to be
     *            removed.
     * @param modelId The {@link XId} of the model holding the object holding
     *            the field from which the {@link XValue} is to be removed.
     * @param objectId The {@link XId} of the object holding the field to which
     *            the {@link XValue} is to be added.
     * @param fieldId The {@link XId} of the field from which the {@link XValue}
     *            is to be removed.
     * @param fieldRevision The current revision number of the field from which
     *            the {@link XValue} is to be removed.
     * @return A new {@link XFieldCommand} of the remove-type.
     */
    public XFieldCommand createRemoveValueCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId, final long fieldRevision) {
        return createRemoveValueCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
                false);
    }

    @Override
    public XFieldCommand createRemoveValueCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId, final long fieldRevision, final boolean isForced) {
        if(fieldRevision == XCommand.FORCED && !isForced) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        long revNr = fieldRevision;

        if(isForced) {
            revNr = XCommand.FORCED;
        }

        final XAddress target = Base.toAddress(repositoryId, modelId, objectId, fieldId);

        return MemoryFieldCommand.createRemoveCommand(target, revNr);
    }

    @Override
    public XObjectCommand createAddFieldCommand(final XAddress objectAddress, final XId fieldId,
            final boolean isForced) {
        return createAddFieldCommand(objectAddress.getRepository(), objectAddress.getModel(),
                objectAddress.getObject(), fieldId, isForced);
    }

    @Override
    public XModelCommand createAddObjectCommand(final XAddress modelAddress, final XId objectId,
            final boolean isForced) {
        return createAddObjectCommand(modelAddress.getRepository(), modelAddress.getModel(),
                objectId, isForced);
    }

    @Override
    public XFieldCommand createAddValueCommand(final XAddress fieldAddress, final long fieldRevision,
            final XValue value, final boolean isForced) {
        return createAddValueCommand(fieldAddress.getRepository(), fieldAddress.getModel(),
                fieldAddress.getObject(), fieldAddress.getField(), fieldRevision, value, isForced);
    }

    @Override
    public XFieldCommand createChangeValueCommand(final XAddress fieldAddress, final long fieldRevision,
            final XValue value, final boolean isForced) {
        return createChangeValueCommand(fieldAddress.getRepository(), fieldAddress.getModel(),
                fieldAddress.getObject(), fieldAddress.getField(), fieldRevision, value, isForced);
    }

    @Override
    public XObjectCommand createRemoveFieldCommand(final XAddress fieldAddress, final long fieldRevision,
            final boolean isForced) {
        return createRemoveFieldCommand(fieldAddress.getRepository(), fieldAddress.getModel(),
                fieldAddress.getObject(), fieldAddress.getField(), fieldRevision, isForced);
    }

    @Override
    public XRepositoryCommand createRemoveModelCommand(final XAddress modelAddress, final long modelRevision,
            final boolean isForced) {
        return createRemoveModelCommand(modelAddress.getRepository(), modelAddress.getModel(),
                modelRevision, isForced);
    }

    @Override
    public XModelCommand createRemoveObjectCommand(final XAddress objectAddress, final long objectRevision,
            final boolean isForced) {
        return createRemoveObjectCommand(objectAddress.getRepository(), objectAddress.getModel(),
                objectAddress.getObject(), objectRevision, isForced);
    }

    @Override
    public XFieldCommand createRemoveValueCommand(final XAddress fieldAddress, final long fieldRevision,
            final boolean isForced) {
        return createRemoveValueCommand(fieldAddress.getRepository(), fieldAddress.getModel(),
                fieldAddress.getObject(), fieldAddress.getField(), fieldRevision, isForced);
    }

    @Override
    public XObjectCommand createSafeAddFieldCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId) {
        return createAddFieldCommand(Base.resolveObject(repositoryId, modelId, objectId), fieldId,
                false);
    }

    @Override
    public XObjectCommand createForcedAddFieldCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId) {
        return createAddFieldCommand(Base.resolveObject(repositoryId, modelId, objectId), fieldId,
                true);
    }

    @Override
    public XObjectCommand createSafeAddFieldCommand(final XAddress objectAddress, final XId fieldId) {
        return createAddFieldCommand(objectAddress, fieldId, false);
    }

    @Override
    public XObjectCommand createForcedAddFieldCommand(final XAddress objectAddress, final XId fieldId) {
        return createAddFieldCommand(objectAddress, fieldId, true);
    }

    @Override
    public XRepositoryCommand createSafeAddModelCommand(final XId repositoryId, final XId modelId) {
        return createAddModelCommand(repositoryId, modelId, false);
    }

    @Override
    public XRepositoryCommand createForcedAddModelCommand(final XId repositoryId, final XId modelId) {
        return createAddModelCommand(repositoryId, modelId, true);
    }

    @Override
    public XModelCommand createSafeAddObjectCommand(final XAddress modelAddress, final XId objectId) {
        return createAddObjectCommand(modelAddress, objectId, false);
    }

    @Override
    public XModelCommand createForcedAddObjectCommand(final XAddress modelAddress, final XId objectId) {
        return createAddObjectCommand(modelAddress, objectId, true);
    }

    @Override
    public XFieldCommand createSafeAddValueCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId, final long fieldRevision, final XValue value) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        return createAddValueCommand(Base.resolveField(repositoryId, modelId, objectId, fieldId),
                fieldRevision, value, false);
    }

    @Override
    public XFieldCommand createForcedAddValueCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId, final XValue value) {
        return createAddValueCommand(Base.resolveField(repositoryId, modelId, objectId, fieldId),
                XCommand.FORCED, value, true);
    }

    @Override
    public XFieldCommand createSafeAddValueCommand(final XAddress fieldAddress, final long fieldRevision,
            final XValue value) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        return createAddValueCommand(fieldAddress, fieldRevision, value, false);
    }

    @Override
    public XFieldCommand createForcedAddValueCommand(final XAddress fieldAddress, final XValue value) {
        return createAddValueCommand(fieldAddress, XCommand.FORCED, value, true);
    }

    @Override
    public XFieldCommand createSafeChangeValueCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId, final long fieldRevision, final XValue value) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        return createChangeValueCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
                value, false);
    }

    @Override
    public XFieldCommand createForcedChangeValueCommand(final XId repositoryId, final XId modelId,
            final XId objectId, final XId fieldId, final XValue value) {
        return createChangeValueCommand(repositoryId, modelId, objectId, fieldId, XCommand.FORCED,
                value, true);
    }

    @Override
    public XFieldCommand createSafeChangeValueCommand(final XAddress fieldAddress, final long fieldRevision,
            final XValue value) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        return createChangeValueCommand(fieldAddress, fieldRevision, value, false);
    }

    @Override
    public XFieldCommand createForcedChangeValueCommand(final XAddress fieldAddress, final XValue value) {
        return createChangeValueCommand(fieldAddress, XCommand.FORCED, value, true);
    }

    @Override
    public XObjectCommand createSafeRemoveFieldCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId, final long fieldRevision) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        return createRemoveFieldCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
                false);
    }

    @Override
    public XObjectCommand createForcedRemoveFieldCommand(final XId repositoryId, final XId modelId,
            final XId objectId, final XId fieldId) {
        return createRemoveFieldCommand(repositoryId, modelId, objectId, fieldId, XCommand.FORCED,
                true);
    }

    @Override
    public XObjectCommand createSafeRemoveFieldCommand(final XAddress fieldAddress, final long fieldRevision) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        return createRemoveFieldCommand(fieldAddress, fieldRevision, false);
    }

    @Override
    public XObjectCommand createForcedRemoveFieldCommand(final XAddress fieldAddress) {
        return createRemoveFieldCommand(fieldAddress, XCommand.FORCED, true);
    }

    @Override
    public XRepositoryCommand createSafeRemoveModelCommand(final XId repositoryId, final XId modelId,
            final long modelRevision) {
        if(modelRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        return createRemoveModelCommand(repositoryId, modelId, modelRevision, false);
    }

    @Override
    public XRepositoryCommand createForcedRemoveModelCommand(final XId repositoryId, final XId modelId) {
        return createRemoveModelCommand(repositoryId, modelId, XCommand.FORCED, true);
    }

    @Override
    public XRepositoryCommand createSafeRemoveModelCommand(final XAddress modelAddress, final long modelRevision) {
        if(modelRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        return createRemoveModelCommand(modelAddress, modelRevision, false);
    }

    @Override
    public XRepositoryCommand createForcedRemoveModelCommand(final XAddress modelAddress) {
        return createRemoveModelCommand(modelAddress, XCommand.FORCED, true);
    }

    @Override
    public XModelCommand createSafeRemoveObjectCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final long objectRevision) {
        if(objectRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }

        return createRemoveObjectCommand(repositoryId, modelId, objectId, objectRevision, false);
    }

    @Override
    public XModelCommand createForcedRemoveObjectCommand(final XId repositoryId, final XId modelId, final XId objectId) {
        return createRemoveObjectCommand(repositoryId, modelId, objectId, XCommand.FORCED, true);
    }

    @Override
    public XModelCommand createSafeRemoveObjectCommand(final XAddress objectAddress, final long objectRevision) {
        if(objectRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        return createRemoveObjectCommand(objectAddress, objectRevision, false);
    }

    @Override
    public XModelCommand createForcedRemoveObjectCommand(final XAddress objectAddress) {
        return createRemoveObjectCommand(objectAddress, XCommand.FORCED, true);
    }

    @Override
    public XFieldCommand createSafeRemoveValueCommand(final XId repositoryId, final XId modelId, final XId objectId,
            final XId fieldId, final long fieldRevision) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        return createRemoveValueCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
                false);
    }

    @Override
    public XFieldCommand createForcedRemoveValueCommand(final XId repositoryId, final XId modelId,
            final XId objectId, final XId fieldId) {
        return createRemoveValueCommand(repositoryId, modelId, objectId, fieldId, XCommand.FORCED,
                true);
    }

    @Override
    public XFieldCommand createSafeRemoveValueCommand(final XAddress fieldAddress, final long fieldRevision) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        return createRemoveValueCommand(fieldAddress, fieldRevision, false);
    }

    @Override
    public XFieldCommand createForcedRemoveValueCommand(final XAddress fieldAddress) {
        return createRemoveValueCommand(fieldAddress, XCommand.FORCED, true);
    }

    @Override
    public XModelCommand createSafeAddObjectCommand(final XId repositoryId, final XId modelId, final XId objectId) {
        return createAddObjectCommand(Base.resolveModel(repositoryId, modelId), objectId, true);
    }

}
