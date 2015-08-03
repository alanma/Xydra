package org.xydra.core.change;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryTransaction;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.delta.ChangedField;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.sharedutils.XyAssert;


/**
 * An XTransactionBuilder provides methods for building {@link XTransaction
 * XTransactions} that change {@link XModel XModels}/{@link XObject XObjects}/
 * {@link XField XFields} according to the given {@link XCommand XCommands}.
 *
 * To build an {@link XTransaction} using an XTransactionBuilder, simply use the
 * constructor to create an XTransactionBuider referring to the {@link XModel}/
 * {@link XObject} you want to change, use the provided method to manipulate the
 * {@link XTransaction} you want to create. The resulting transaction can be
 * retrieved using the {@link #buildCommand()} or {@link #build()} methods.It is
 * possible to have multiple XTransactionBuilders referring to the same entity
 * at the same time.
 *
 * Every XTransactionBuilder should only be used to create one
 * {@link XTransaction} to avoid confusions. It is possible to create multiple
 * {@link XTransaction XTransactions} with only one XTransactionBuilder, but we
 * strongly encourage you to avoid this and look at each XTransactionBuilder
 * instance as an instance to create one specific {@link XTransaction}.
 *
 * @author dscharrer
 */
public class XTransactionBuilder implements Iterable<XAtomicCommand> {

    private final List<XAtomicCommand> commands;
    private final XAddress target;
    private final boolean forced;

    /**
     * Create a new transaction builder for constructing an {@link XTransaction}
     * that will operate on the given target.
     *
     * @param target The address of an {@link XModel} or an {@link XObject} to
     *            which all the commands in the {@link XTransaction} will apply.
     * @throws RuntimeException if the given {@link XAddress} is not an
     *             {@link XAddress} of an {@link XModel} or {@link XObject}
     */
    public XTransactionBuilder(final XAddress target) {
        this(target, false);
    }

    /**
     * Create a new transaction builder for constructing an {@link XTransaction}
     * that will operate on the given target. Used boolean flag forced when the
     * commands within the transaction should be forced.
     *
     * @param target The address of an {@link XModel} or an {@link XObject} to
     *            which all the commands in the {@link XTransaction} will apply.
     * @param forced - set to true when commands should be added as forced
     *            commands when invoking {@link
     *            XTransactionBuilder#applyChanges(...)}
     * @throws RuntimeException if the given {@link XAddress} is not an
     *             {@link XAddress} of an {@link XModel} or {@link XObject}
     */
    @SuppressWarnings("javadoc")
    public XTransactionBuilder(final XAddress target, final boolean forced) {
        if(target.getModel() == null && target.getObject() == null || target.getField() != null) {
			throw new RuntimeException("target must be a model or object, was:" + target);
		}

        this.target = target;
        this.commands = new ArrayList<XAtomicCommand>();
        this.forced = forced;
    }

    /**
     * Adds the given {@link XCommand} to this {@link XTransaction} at the given
     * index. If the given {@link XCommand} is an {@link XTransaction}, the
     * commands of this {@link XTransaction} will be added from index to
     * index+size of the transaction which is being built by this transaction
     * builder.
     *
     * @param index
     *
     * @param command The {@link XCommand} which is to be added.
     * @throws IllegalArgumentException if the command is not an
     *             {@link XTransaction}, {@link XFieldCommand},
     *             {@link XObjectCommand} (or {@link XModelCommand} for model
     *             transactions) that applies to the {@link XModel}/
     *             {@link XObject} the {@link XTransaction} which is being built
     *             will apply to
     * @throws NullPointerException if the given {@link XCommand} equals null
     * @throws IndexOutOfBoundsException if the given index is negative or
     *             greater than the current size of the {@link XTransaction}
     *             which is being built
     */
    public void addCommand(final int index, final XCommand command) throws IllegalArgumentException,
            NullPointerException, IndexOutOfBoundsException {

        assertCommandIsInTransactionScope(command);

        if(command instanceof XTransaction) {

            int idx = index;
            final XTransaction trans = (XTransaction)command;
            for(final XAtomicCommand atomicCommand : trans) {
                // commands have already been checked when added to the
                // transaction
                this.commands.add(idx, atomicCommand);
                idx++;
            }

            return;
        } else {
            this.commands.add(index, (XAtomicCommand)command);
        }
    }

    private void assertCommandIsInTransactionScope(final XCommand command) {
        if(command instanceof XRepositoryCommand
                && ((XRepositoryCommand)command).getRepositoryId().equals(
                        this.target.getRepository())
                && ((XRepositoryCommand)command).getModelId().equals(this.target.getModel())) {
            return;
        }

        if(!this.target.equalsOrContains(command.getTarget())) {
            throw new IllegalArgumentException(command.getTarget() + " is not contained-in or == "
                    + this.target + " And its not a repo-Command ins this scope either");
        }
    }

    /**
     * Adds the given {@link XCommand} to the end of the {@link XTransaction}
     * which is being built by this transaction builder.
     *
     * @param command The {@link XCommand} which is to be added
     * @throws IllegalArgumentException if the command is not an
     *             {@link XTransaction}, {@link XFieldCommand},
     *             {@link XObjectCommand} (or {@link XModelCommand} for model
     *             transactions) that applies to the {@link XModel}/
     *             {@link XObject} the {@link XTransaction} which is being built
     *             will apply to
     * @throws NullPointerException if the given {@link XCommand} equals null
     */
    public void addCommand(final XCommand command) throws IllegalArgumentException, NullPointerException {
        addCommand(size(), command);
    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will add and {@link XField} with the given {@link XId}.
     *
     * @param objectAddr The {@link XAddress} of the {@link XObject} to which
     *            the {@link XField} is to be added.
     * @param revision {@link XCommand#SAFE_STATE_BOUND} or
     *            {@link XCommand#FORCED}, to define whether the
     *            {@link XCommand} which is to be created shall be a forced or a
     *            safe event
     * @param fieldId The {@link XId} of the {@link XField} which will be added
     *
     * @throws IllegalArgumentException if this builders target doesn't contain
     *             the given {@link XAddress} or if the given revision number is
     *             neither {@link XCommand#SAFE_STATE_BOUND} or
     *             {@link XCommand#FORCED}.
     */
    public void addField(final XAddress objectAddr, final long revision, final XId fieldId) {
        if(revision != XCommand.SAFE_STATE_BOUND && revision != XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "given revision number needs to be XCommand.SAFE or XCommand.FORCED, was "
                            + revision);
        }

        addCommand(MemoryObjectCommand.createAddCommand(objectAddr, revision, fieldId));
    }

    public void addFieldForced(final XAddress objectAddr, final XReadableField newField) {
        addField(objectAddr, newField, true);
    }

    public void addFieldSafe(final XAddress objectAddr, final XReadableField newField) {
        addField(objectAddr, newField, false);
    }

    public void addField(final XAddress objectAddr, final XReadableField newField, final boolean forced) {
        assert createsNoTargetAddressTwice(newField.getAddress()) : newField.getAddress();
        addField(objectAddr, forced ? XCommand.FORCED : XCommand.SAFE_STATE_BOUND, newField.getId());
        assert createsNoTargetAddressTwice(newField.getAddress()) : newField.getAddress() + ""
                + this.commands;
        if(newField.getValue() != null) {
            final XAddress fieldAddr = Base.resolveField(objectAddr, newField.getId());
            // TODO was XCommand.NEW, why not SAFE?
            addValue(fieldAddr, forced ? XCommand.FORCED : XCommand.SAFE_STATE_BOUND,
                    newField.getValue());
        }
        assert createsNoTargetAddressTwice(newField.getAddress()) : newField.getAddress();
    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will will add an {@link XObject} with the given {@link XId}.
     *
     * @param modelAddr The {@link XAddress} of the {@link XModel} to which the
     *            {@link XObject} is to be added.
     * @param revision {@link XCommand#SAFE_STATE_BOUND} or
     *            {@link XCommand#FORCED}, to define whether the
     *            {@link XCommand} which is to be created shall be a forced or a
     *            safe event
     * @param objectId The {@link XId} for the {@link XObject} which will be
     *            added
     *
     * @throws IllegalArgumentException if this builders target doesn't contain
     *             the given {@link XAddress} or if the given revision number is
     *             neither {@link XCommand#SAFE_STATE_BOUND} or
     *             {@link XCommand#FORCED}.
     */
    public void addObject(final XAddress modelAddr, final long revision, final XId objectId) {
        if(revision != XCommand.SAFE_STATE_BOUND && revision != XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "given revision number needs to be XCommand.SAFE or XCommand.FORCED, was "
                            + revision);
        }

        addCommand(MemoryModelCommand.createAddCommand(modelAddr, revision, objectId));
    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will will add an {@link XModel} with the given {@link XId}.
     *
     * @param repoAddress The {@link XAddress} of the {@link XRepository} to
     *            which the {@link XModel} is to be added.
     * @param revision {@link XCommand#SAFE_STATE_BOUND} or
     *            {@link XCommand#FORCED}, to define whether the
     *            {@link XCommand} which is to be created shall be a forced or a
     *            safe event
     * @param modelId The {@link XId} for the {@link XModel} which will be added
     *
     * @throws IllegalArgumentException if this builders target doesn't contain
     *             the given {@link XAddress} or if the given revision number is
     *             neither {@link XCommand#SAFE_STATE_BOUND} or
     *             {@link XCommand#FORCED}.
     */
    public void addModel(final XAddress repoAddress, final long revision, final XId modelId) {
        assert repoAddress != null;
        assert repoAddress.getRepository() != null;
        if(revision != XCommand.SAFE_STATE_BOUND && revision != XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "given revision number needs to be XCommand.SAFE or XCommand.FORCED, was "
                            + revision);
        }

        addCommand(MemoryRepositoryCommand.createAddCommand(repoAddress, revision, modelId));
    }

    /**
     * Adds {@link XCommand XCommands} to the transaction which is being built
     * which will add the given {@link XObject} to the {@link XModel} with the
     * given {@link XAddress}.
     *
     * Creates a SAFE command within the transaction: The transaction will fail
     * if the object already existed and hasn't been removed before in the
     * transaction.
     *
     * Use {@link #setObject(XAddress, XReadableObject)} to create a command
     * that will always create an object with the specified state (removing any
     * existing object including fields).
     *
     * TODO How to create a forced command?
     *
     * @param modelAddr The {@link XAddress} of the {@link XModel} to which the
     *            given {@link XObject} is to be added by the {@link XCommand
     *            XCommands} that will be created
     * @param newObject The new {@link XObject} that is to be added.
     *
     * @throws IllegalArgumentException if this builders target is not the
     *             modelAddr.
     */
    public void addObjectSafe(final XAddress modelAddr, final XReadableObject newObject) {
        addObject(modelAddr, newObject, false);
    }

    public void addObjectForced(final XAddress modelAddr, final XReadableObject newObject) {
        addObject(modelAddr, newObject, true);
    }

    public void addObject(final XAddress modelAddr, final XReadableObject newObject, final boolean forced) {
        final XAddress objectAddr = Base.resolveObject(modelAddr, newObject.getId());
        addObject(modelAddr, forced ? XCommand.FORCED : XCommand.SAFE_STATE_BOUND,
                newObject.getId());
        assert createsNoTargetAddressTwice(newObject.getAddress()) : newObject.getAddress();
        for(final XId newFieldId : newObject) {
            addField(objectAddr, newObject.getField(newFieldId), forced);
            assert createsNoTargetAddressTwice(newObject.getAddress()) : newObject.getAddress();
        }
    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will add the given {@link XValue} to an existing {@link XField} specified
     * by the given {@link XAddress} which {@link XValue} is not yet set.
     *
     * @param fieldAddr The {@link XAddress} of the {@link XField} the
     *            {@link XValue} is to be added to.
     * @param revision The old revision number of the {@link XField} or
     *            {@link XCommand#FORCED}.
     * @param value The {@link XValue} which will be added.
     *
     * @throws IllegalArgumentException if this builders target doesn't contain
     *             the {@link XField} specified by the given {@link XAddress}.
     */
    public void addValue(final XAddress fieldAddr, final long revision, final XValue value) {
        addCommand(MemoryFieldCommand.createAddCommand(fieldAddr, revision, value));
    }

    /**
     * Adds {@link XCommand XCommands} to the transaction which is being built
     * which will apply the changes represented by the given
     * {@link ChangedField}.
     *
     * Creates a SAFE command within the transaction: The transaction will fail
     * if the value has been modified in the {@link ChangedField} and also in
     * the model the transaction is executed on. This only includes the current
     * diff specified by the {@link ChangedField} , not the complete history of
     * modifications made to the value in the {@link ChangedField}. Any changes
     * to parts of the model or object which are not touched by the given
     * {@link ChangedModel} will not impact the execution of this transaction.
     *
     * @param field
     */
    public void applyChanges(final ChangedField field) {
        final XAddress target = field.getAddress();
        final long revision = field.getRevisionNumber();
        if(this.forced) {
            changeValueForced(target, revision, field.getOldValue(), field.getValue());
        } else {
            changeValueSafe(target, revision, field.getOldValue(), field.getValue());
        }
    }

    /**
     * Adds {@link XCommand XCommands} to the transaction which is being built
     * which will apply the changes represented by the given
     * {@link ChangedObject}.
     *
     * Creates a SAFE command within the transaction: The transaction will fail
     * if any fields added in the {@link ChangedObject} already exist, if any
     * fields removed in the {@link ChangedObject} have already been removed or
     * if the fields containing values modified in the {@link ChangedObject}
     * have been modified. This only includes the current diff specified by the
     * {@link ChangedObject}, not the complete history of modifications made to
     * the {@link ChangedObject}. Any changes to parts of the model or object
     * which are not touched by the given {@link ChangedObject} will not impact
     * the execution of this transaction.
     *
     * @param object
     */
    public void applyChanges(final ChangedObject object) {

        XyAssert.xyAssert(createsNoTargetAddressTwice(object.getAddress()));

        for(final XId fieldId : object.getRemovedFields()) {
            final XReadableField field = object.getOldField(fieldId);
            if(this.forced) {
                removeFieldForced(field);
            } else {
                removeFieldSafe(field);
            }
        }

        XyAssert.xyAssert(createsNoTargetAddressTwice(object.getAddress()));

        for(final XReadableField field : object.getNewFields()) {
            addField(object.getAddress(), field, this.forced);
        }

        XyAssert.xyAssert(createsNoTargetAddressTwice(object.getAddress()));

        for(final ChangedField field : object.getChangedFields()) {
            applyChanges(field);
        }

        XyAssert.xyAssert(createsNoTargetAddressTwice(object.getAddress()));

    }

    /**
     * Adds {@link XCommand XCommands} to the transaction which is being built
     * which will apply the changes represented by the given
     * {@link ChangedModel}
     *
     * Creates a SAFE command within the transaction: The transaction will fail
     * if any objects or fields added in the {@link ChangedModel} already exist,
     * if any objects or fields removed in the {@link ChangedModel} have already
     * been removed or if the fields containing values modified in the
     * {@link ChangedModel} have been modified. This only includes the current
     * diff specified by the {@link ChangedModel}, not the complete history of
     * modifications made to the {@link ChangedModel}. Any changes to parts of
     * the model or object which are not touched by the given
     * {@link ChangedModel} will not impact the execution of this transaction.
     *
     * @param model
     */
    public void applyChanges(final ChangedModel model) {
        XyAssert.xyAssert(model.checkSetInvariants());
        XyAssert.xyAssert(createsNoTargetAddressTwice(model.getAddress()));

        for(final XId objectId : model.getRemovedObjects()) {
            final XReadableObject object = model.getOldObject(objectId);
            if(this.forced) {
                removeObjectForced(object);
            } else {
                removeObjectSafe(object);
            }
        }

        XyAssert.xyAssert(model.checkSetInvariants());
        XyAssert.xyAssert(createsNoTargetAddressTwice(model.getAddress()));

        for(final XReadableObject object : model.getNewObjects()) {
            addObject(model.getAddress(), object, this.forced);
        }

        XyAssert.xyAssert(model.checkSetInvariants());
        assert createsNoTargetAddressTwice(model.getAddress()) : model.getId();

        for(final ChangedObject object : model.getChangedObjects()) {
            applyChanges(object);
        }

        XyAssert.xyAssert(model.checkSetInvariants());
        XyAssert.xyAssert(createsNoTargetAddressTwice(model.getAddress()));

    }

    /**
     * Build a transaction from the added commands. This method will always
     * create a {@link XTransaction}, even if there is only one command. If this
     * is not desired, use {@link #buildCommand()}.
     *
     * @return an {@link XTransaction} with the current contents of this
     *         builder.
     */
    public XTransaction build() {

        // FIXME 2012-02 remove duplicate commands

        return MemoryTransaction.createTransaction(this.target, this.commands);
    }

    private boolean createsNoTargetAddressTwice(final XAddress targetAddress) {
        final Set<XAddress> toBeCreated = new HashSet<XAddress>();
        for(final XAtomicCommand cmd : this.commands) {
            if(cmd.getChangeType() == ChangeType.ADD && cmd.getTarget() != cmd.getChangedEntity()) {
                final XAddress t = cmd.getChangedEntity();
                assert !toBeCreated.contains(t) : targetAddress;
                toBeCreated.add(t);
            }
        }
        return true;
    }

    /**
     * @return If there is only one {@link XCommand} in the {@link XTransaction}
     *         which is being built, return that, otherwise return the built
     *         {@link XTransaction}.
     */
    public XCommand buildCommand() {
        if(size() == 1) {
            return getCommand(0);
        }
        return build();
    }

    /**
     * Adds {@link XCommand XCommands} to the transaction which is being built
     * which will change the given {@link XField} to the state of another.
     *
     * The revision number of the created {@link XCommand XCommands} will be set
     * so that the transaction will apply to the old {@link XField} in the state
     * it is in while this method is being executed. Revision numbers in the new
     * {@link XField} are ignored.
     *
     * The {@link XRepository}, {@link XModel} and {@link XObject} {@link XId
     * XIds} the created {@link XCommand XCommands} will refer to will be taken
     * from the old {@link XField}.
     *
     * @param oldField The old {@link XField} that is to be changed.
     * @param newField The {@link XField} to which state oldField will be
     *            changed to.
     *
     * @throws IllegalArgumentException if this builders target does not contain
     *             the oldField.
     */
    public void changeField(final XReadableField oldField, final XReadableField newField) {
        final XAddress target = oldField.getAddress();
        final long revision = oldField.getRevisionNumber();
        changeValueSafe(target, revision, oldField.getValue(), newField.getValue());
    }

    /**
     * Adds {@link XCommand XCommands} to the transaction which is being built,
     * that will change the state of one existing {@link XModel} into that of
     * another.
     *
     * The revision number of the created {@link XCommand XCommands} will be set
     * so that the transaction will apply to the old {@link XModel} in the state
     * it is in while this method is being executed. Revision numbers in the new
     * {@link XModel} are ignored.
     *
     * @param oldModel The old {@link XModel} that is to be changed.
     * @param newModel The new {@link XModel} that the old {@link XModel} will
     *            be changed to.
     *
     * @throws IllegalArgumentException if this builders target is not oldModel
     */
    /*
     * FIXME 2012-02 All of the change-Methods here have the same problem: It is
     * possible that someone will change the state of the given
     * model/object/etc. while the method is being executed, which may result in
     * incoherent transformations. We'll probably need a way to lock the
     * entities while these methods are being executed. Max: Yes.
     */
    public void changeModel(final XReadableModel oldModel, final XReadableModel newModel)
            throws IllegalArgumentException {
        for(final XId oldObjectId : oldModel) {
            if(!newModel.hasObject(oldObjectId)) {
                removeObjectSafe(oldModel.getObject(oldObjectId));
            }
        }
        for(final XId newObjectId : newModel) {
            setObject(oldModel, newModel.getObject(newObjectId));
        }
    }

    /**
     * Adds {@link XCommand XCommands} to the transaction which is being built
     * which will change an {@link XObject} to the state of another.
     *
     * The revision number of the created {@link XCommand XCommands} will be set
     * so that the transaction will apply to the old {@link XObject} in the
     * state it is in while this method is being executed. Revision numbers in
     * the new {@link XObject} are ignored.
     *
     * The {@link XRepository} and {@link XModel} {@link XId XIds} the created
     * {@link XCommand XCommands} will be taken from the old {@link XObject}.
     *
     * @param oldObject The old {@link XObject} that is to be changed.
     * @param newObject The {@link XObject} to which state the oldObject will
     *            being changed to by the created {@link XCommand XCommands}.
     *
     * @throws IllegalArgumentException if this builders target is not the
     *             oldObject or its parent {@link XModel}.
     */
    public void changeObject(final XReadableObject oldObject, final XReadableObject newObject) {
        for(final XId oldFieldId : oldObject) {
            if(!newObject.hasField(oldFieldId)) {
                removeFieldSafe(oldObject.getField(oldFieldId));
            }
        }
        for(final XId newFieldId : newObject) {
            setField(oldObject, newObject.getField(newFieldId));
        }
    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will change the {@link XValue} of the specified {@link XField} (which
     * {@link XValue} needs to be set!) to the given {@link XValue})
     *
     * Unless the provided revision is XCommand#FORCED, the created command will
     * fail if the field doesn't have a value.
     *
     * Use {@link #addValue(XAddress, long, XValue)} to add values to fields
     * that don't have an existing value or use
     * {@link #setValue(XAddress, XValue)} to always set the value no matter
     * what the field's current value is.
     *
     * @param fieldAddr The {@link XAddress} of the {@link XField} which
     *            {@link XValue} is to be changed.
     * @param revision The old revision number of the {@link XField} or
     *            {@link XCommand#FORCED}.
     * @param value The new {@link XValue} to which the current {@link XValue}
     *            of the specified {@link XField} is to be changed to.
     *
     * @throws IllegalArgumentException if this builders target doesn't contain
     *             the given {@link XField} specified by the given
     *             {@link XAddress}.
     */
    public void changeValue(final XAddress fieldAddr, final long revision, final XValue value) {
        addCommand(MemoryFieldCommand.createChangeCommand(fieldAddr, revision, value));
    }

    /**
     * Adds {@link XCommand XCommands} to the transaction which is being built
     * which will change an {@link XField} containing an {@link XValue} into one
     * containing another {@link XValue}.
     *
     * This will add a CHANGE, ADD or REMOVE command or no command at all
     * depending on how the newValue compares to the oldValue.
     *
     * Use {@link #addValue(XAddress, long, XValue)},
     * {@link #changeValue(XAddress, long, XValue)},
     * {@link #removeValue(XAddress, long)} to create
     *
     * Use {@link #setValue(XAddress, XValue)} to force a specific value no
     * matter what the fields current value is.
     *
     * @param fieldAddr Address of the {@link XField} which is to be changed.
     * @param revision Current revision number of the {@link XField}.
     * @param oldValue The old {@link XValue} that the {@link XField} contains
     *            before the change.
     * @param newValue The new {@link XValue} the {@link XField} will contain
     *            after the change.
     */
    public void changeValueSafe(final XAddress fieldAddr, final long revision, final XValue oldValue, final XValue newValue) {
        if(oldValue == null) {
            if(newValue != null) {
                addValue(fieldAddr, revision, newValue);
            }
        } else if(!oldValue.equals(newValue)) {
            if(newValue == null) {
                removeValue(fieldAddr, revision);
            } else {
                changeValue(fieldAddr, revision, newValue);
            }
        }
    }

    /**
     * Adds {@link XCommand XCommands} to the transaction which is being built
     * which will change an {@link XField} containing an {@link XValue} into one
     * containing another {@link XValue}.
     *
     * This will add a CHANGE, ADD or REMOVE command or no command at all
     * depending on how the newValue compares to the oldValue.
     *
     * Use {@link #addValue(XAddress, long, XValue)},
     * {@link #changeValue(XAddress, long, XValue)},
     * {@link #removeValue(XAddress, long)} to create
     *
     * @param fieldAddr Address of the {@link XField} which is to be changed.
     * @param revision Current revision number of the {@link XField}.
     * @param oldValue The old {@link XValue} that the {@link XField} contains
     *            before the change.
     * @param newValue The new {@link XValue} the {@link XField} will contain
     *            after the change.
     */
    public void changeValueForced(final XAddress fieldAddr, final long revision, final XValue oldValue,
            final XValue newValue) {
        if(oldValue == null) {
            if(newValue != null) {
                addValue(fieldAddr, XCommand.FORCED, newValue);
            }
        } else if(!oldValue.equals(newValue)) {
            if(newValue == null) {
                removeValue(fieldAddr, XCommand.FORCED);
            } else {
                changeValue(fieldAddr, XCommand.FORCED, newValue);
            }
        }
    }

    /**
     * Returns the {@link XCommand} at the given index of the
     * {@link XTransaction} which is being built by this transaction builder.
     *
     * @param index The {@link XCommand} at the given index.
     * @return The {@link XCommand} at the given index.
     */
    public XAtomicCommand getCommand(final int index) {
        return this.commands.get(index);
    }

    /**
     * @return true, if there currently are no {@link XCommand XCommands} in the
     *         {@link XTransaction} which is being built by this builder.
     */
    public boolean isEmpty() {
        return this.commands.isEmpty();
    }

    /**
     * @return an iterator over the {@link XCommand XCommands} which are
     *         currently part of the {@link XTransaction} which is being built
     *         by this transaction builder.
     */
    @Override
    public Iterator<XAtomicCommand> iterator() {
        return this.commands.iterator();
    }

    /**
     * Removes the {@link XCommand} at the given index from the
     * {@link XTransaction} which is built by this transaction builder.
     *
     * @param index The index of the {@link XCommand} which is to be removed
     * @return the element previously at the given index or null if the method
     *         fails
     * @throws IndexOutOfBoundsException if the given index is negative or
     *             greater than or equal to the size of this
     *             {@link XTransaction}
     */
    public XAtomicCommand removeCommand(final int index) throws IndexOutOfBoundsException {
        return this.commands.remove(index);
    }

    /**
     * Removes the first occurrence of the given {@link XCommand} from the
     * {@link XTransaction} which is being built by this transaction builder.
     *
     * If the given {@link XCommand} which is to removed is an
     * {@link XTransaction}, this method will try to remove all {@link XCommand}
     * that are contained in the given {@link XTransaction} from the
     * {@link XTransaction} which is being built by this transaction builder.
     *
     * @param command The {@link XCommand} which is to be deleted
     * @return true if the {@link XTransaction} previously contained the given
     *         {@link XCommand}
     */
    public boolean removeCommand(final XCommand command) {

        if(!(command instanceof XTransaction)) {
			return this.commands.remove(command);
		}

        final XTransaction transaction = (XTransaction)command;

        if(transaction.size() == 0) {
            return false;
        }

        int index = this.commands.indexOf(transaction.getCommand(0));

        if(index + transaction.size() > size()) {
            return false;
        }

        boolean removeable = true;
        for(int j = 0; j < transaction.size() && removeable; j++) {
            if(!transaction.getCommand(j).equals(this.commands.get(index + j))) {
                removeable = false;
            }
        }

        if(!removeable) {
			return false;
		}

        for(; index < transaction.size(); index++) {
            this.commands.remove(index);
        }

        return true;

    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will remove the {@link XField} specified by the given {@link XAddress}
     * and {@link XId}.
     *
     * @param objectAddr The {@link XAddress} of the {@link XObject} which is
     *            supposed to hold the {@link XField} which is be removed.
     * @param revision The old revision number of the {@link XField} or
     *            {@link XCommand#FORCED}
     * @param fieldId The {@link XId} of the {@link XField} being removed.
     *
     * @throws IllegalArgumentException if this builders target doesn't contain
     *             the given {@link XField}.
     */
    public void removeField(final XAddress objectAddr, final long revision, final XId fieldId) {
        addCommand(MemoryObjectCommand.createRemoveCommand(objectAddr, revision, fieldId));
    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will remove the given {@link XField}.
     *
     * The revision number of the created {@link XCommand} will be set so that
     * the transaction will apply to the old {@link XField} in the state it is
     * in while this method is being executed.
     *
     * @param oldField The old {@link XField} that is to be removed.
     *
     * @throws IllegalArgumentException if this builders target does not contain
     *             oldField.
     */
    public void removeFieldSafe(final XReadableField oldField) {
        removeField(oldField.getAddress().getParent(), oldField.getRevisionNumber(),
                oldField.getId());
    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will remove the given {@link XField}.
     *
     * The revision number of the created {@link XCommand} will be set so
     * {@link XCommand#FORCED}.
     *
     * @param oldField The old {@link XField} that is to be removed.
     *
     * @throws IllegalArgumentException if this builders target does not contain
     *             oldField.
     */
    public void removeFieldForced(final XReadableField oldField) {
        removeField(oldField.getAddress().getParent(), XCommand.FORCED, oldField.getId());
    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will remove the {@link XObject} specified by the given {@link XAddress}
     * and {@link XId}.
     *
     * @param modelAddr The {@link XAddress} of the {@link XModel} which is to
     *            supposed to hold the {@link XObject} which is to be removed
     * @param revision The old revision number of the {@link XObject} or
     *            {@link XCommand#FORCED}
     * @param objectId The {@link XId} of the {@link XObject} which will be
     *            removed.
     *
     * @throws IllegalArgumentException if this builders target doesn't contain
     *             the given {@link XObject}.
     */
    public void removeObject(final XAddress modelAddr, final long revision, final XId objectId) {
        addCommand(MemoryModelCommand.createRemoveCommand(modelAddr, revision, objectId));
    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will remove the {@link XModel} specified by the given {@link XAddress}
     * and {@link XId}.
     *
     * @param repoAddr The {@link XAddress} of the {@link XRepository} which is
     *            to supposed to hold the {@link XModel} which is to be removed
     * @param revision The old revision number of the {@link XModel} or
     *            {@link XCommand#FORCED}
     * @param modelId The {@link XId} of the {@link XModel} which will be
     *            removed.
     *
     * @throws IllegalArgumentException if this builders target doesn't contain
     *             the given {@link XModel}.
     */
    public void removeModel(final XAddress repoAddr, final long revision, final XId modelId) {
        addCommand(MemoryRepositoryCommand.createRemoveCommand(repoAddr, revision, modelId));
    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will remove the given object.
     *
     * The revision number of the created {@link XCommand} will be set so that
     * the transaction will apply to the old {@link XObject} in the state it is
     * in while this method is being executed.
     *
     * @param oldObject The old {@link XObject} that is to be removed.
     *
     * @throws IllegalArgumentException if this builders target is not the
     *             oldObject or its parent {@link XModel}.
     */
    public void removeObjectSafe(final XReadableObject oldObject) {
        removeObject(oldObject.getAddress().getParent(), oldObject.getRevisionNumber(),
                oldObject.getId());
    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will remove the given object.
     *
     * The revision number of the created {@link XCommand} will be set so
     * {@link XCommand#FORCED}.
     *
     * @param oldObject The old {@link XObject} that is to be removed.
     *
     * @throws IllegalArgumentException if this builders target is not the
     *             oldObject or its parent {@link XModel}.
     */
    public void removeObjectForced(final XReadableObject oldObject) {
        removeObject(oldObject.getAddress().getParent(), XCommand.FORCED, oldObject.getId());
    }

    /**
     * Adds an {@link XCommand} to the transaction which is being built which
     * will remove the {@link XValue} of the {@link XField} specified by the
     * given {@link XAddress}.
     *
     * @param fieldAddr The {@link XAddress} of the {@link XField} which
     *            {@link XValue} is to be removed.
     * @param revision The old revision number of the {@link XField} or
     *            {@link XCommand#FORCED}.
     *
     * @throws IllegalArgumentException if this builders target doesn't contain
     *             the {@link XField} specified by the given {@link XAddress}.
     */
    public void removeValue(final XAddress fieldAddr, final long revision) {
        addCommand(MemoryFieldCommand.createRemoveCommand(fieldAddr, revision));
    }

    /**
     * Sets the value of the field with the given address no matter what the
     * fields current value or revision is.
     *
     * This is equivalent to {@link #addValue(XAddress, long, XValue)} or
     * {@link #changeValue(XAddress, long, XValue)} with a revision of
     * {@link XCommand#FORCED}.
     *
     * @param fieldAddr
     * @param value
     */
    public void setValue(final XAddress fieldAddr, final XValue value) {
        changeValue(fieldAddr, XCommand.FORCED, value);
    }

    /**
     * Ensures that there is a field with the state of newField in the object
     * addressed by objectAddr. Overwrites any existing field with the same ID
     * and only fails if the given object doesn't exist.
     *
     * @param objectAddr
     * @param newField
     */
    public void setField(final XAddress objectAddr, final XReadableField newField) {
        removeField(objectAddr, XCommand.FORCED, newField.getId());
        addFieldSafe(objectAddr, newField);
    }

    /**
     * Adds {@link XCommand XCommands} to the transaction which is being built
     * which will create or change an {@link XField} to the state of another.
     *
     * The revision number of the created {@link XCommand XCommands} will be set
     * so that the transaction will apply to the old {@link XObject} in the
     * state it is in while this method is being executed. Revision numbers in
     * the new {@link XField} are ignored.
     *
     * The {@link XRepository}, {@link XModel} and {@link XObject} {@link XId
     * XIds} the created {@link XCommand XCommands} will refer to will be taken
     * from the old {@link XObject}.
     *
     * @param oldObject The old {@link XObject} that is to be changed.
     * @param newField The {@link XField} that is being added to or changed in
     *            oldObject.
     *
     * @throws IllegalArgumentException if this builder's target is not the
     *             oldObject or its parent {@link XModel}.
     */
    public void setField(final XReadableObject oldObject, final XReadableField newField)
            throws IllegalArgumentException {
        final XReadableField oldField = oldObject.getField(newField.getId());
        if(oldField == null) {
            addFieldSafe(oldObject.getAddress(), newField);
        } else {
            changeField(oldField, newField);
        }
    }

    /**
     * Ensures that there is an object with the state of newObject in the model
     * addressed by modelAddr. Overwrites any existing object with the same ID
     * and only fails if the given model doesn't exist.
     *
     * @param modelAddr
     * @param newObject
     */
    public void setObject(final XAddress modelAddr, final XReadableObject newObject) {
        removeObject(modelAddr, XCommand.FORCED, newObject.getId());
        addObjectSafe(modelAddr, newObject);
    }

    /**
     * Adds {@link XCommand XCommands} to the transaction which is being built,
     * which will create or change an {@link XObject} to the state of another.
     *
     * The revision number of the created {@link XCommand XCommands} will be set
     * so that the transaction will apply to the old {@link XModel} in the state
     * it is in while this method is being executed. Revision numbers in the new
     * {@link XObject} are ignored.
     *
     * The {@link XRepository} and {@link XModel} {@link XId XIds} the created
     * {@link XCommand XCommands} will refer to will be taken from the old
     * {@link XModel}.
     *
     * @param oldModel The old {@link XModel} that is to be changed.
     * @param newObject The {@link XObject} that is being added to or changed in
     *            oldModel.
     *
     * @throws IllegalArgumentException if this builders target is not the
     *             oldModel
     */
    public void setObject(final XReadableModel oldModel, final XReadableObject newObject)
            throws IllegalArgumentException {
        final XReadableObject oldObject = oldModel.getObject(newObject.getId());
        if(oldObject == null) {
            addObjectSafe(oldModel.getAddress(), newObject);
        } else {
            changeObject(oldObject, newObject);
        }
    }

    /**
     * @return the current number of {@link XCommand XCommands} in the
     *         {@link XTransaction} which is being built by this transaction
     *         builder.
     */
    public int size() {
        return this.commands.size();
    }

    @Override
    public String toString() {
        return "TransactionBuilder for " + this.target + ": " + this.commands.toString();
    }

    /**
     * TODO the methods {@link #setObject(XReadableModel, XReadableObject)},
     * {@link #setField(XReadableObject, XReadableField)},
     * {@link #changeModel(XReadableModel, XReadableModel)},
     * {@link #changeObject(XReadableObject, XReadableObject)} and
     * {@link #changeField(XReadableField, XReadableField)} have unclear
     * semantics when executing the resulting transaction on a model with a
     * state different from the first operand.
     */

}
