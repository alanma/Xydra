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
 * @author Kaidel
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
    public XObjectCommand createAddFieldCommand(XId objectId, XId fieldId) {
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
    public XObjectCommand createAddFieldCommand(XId objectId, XId fieldId, boolean isForced) {
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
    public XObjectCommand createAddFieldCommand(XId modelId, XId objectId, XId fieldId) {
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
    public XObjectCommand createAddFieldCommand(XId modelId, XId objectId, XId fieldId,
            boolean isForced) {
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
    public XObjectCommand createAddFieldCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId) {
        return createAddFieldCommand(Base.resolveObject(repositoryId, modelId, objectId), fieldId,
                false);
    }
    
    @Override
    public XObjectCommand createAddFieldCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, boolean isForced) {
        long revNr = isForced ? XCommand.FORCED : XCommand.SAFE_STATE_BOUND;
        
        XAddress target = Base.toAddress(repositoryId, modelId, objectId, null);
        
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
    public XRepositoryCommand createAddModelCommand(XId repositoryId, XId modelId) {
        return createAddModelCommand(repositoryId, modelId, false);
    }
    
    @Override
    public XRepositoryCommand createAddModelCommand(XId repositoryId, XId modelId, boolean isForced) {
        long revNr = isForced ? XCommand.FORCED : XCommand.SAFE_STATE_BOUND;
        
        XAddress target = Base.toAddress(repositoryId, null, null, null);
        
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
    public XModelCommand createAddObjectCommand(XId modelId, XId objectId) {
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
    public XModelCommand createAddObjectCommand(XId modelId, XId objectId, boolean isForced) {
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
    public XModelCommand createAddObjectCommand(XId repositoryId, XId modelId, XId objectId) {
        return createAddObjectCommand(Base.resolveModel(repositoryId, modelId), objectId, false);
    }
    
    @Override
    public XModelCommand createAddObjectCommand(XId repositoryId, XId modelId, XId objectId,
            boolean isForced) {
        long revNr = isForced ? XCommand.FORCED : XCommand.SAFE_STATE_BOUND;
        
        XAddress target = Base.toAddress(repositoryId, modelId, null, null);
        
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
    public XFieldCommand createAddValueCommand(XId fieldId, long fieldRevision, XValue value) {
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
    public XFieldCommand createAddValueCommand(XId fieldId, long fieldRevision, XValue value,
            boolean isForced) {
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
    public XFieldCommand createAddValueCommand(XId objectId, XId fieldId, long fieldRevision,
            XValue value) {
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
    public XFieldCommand createAddValueCommand(XId objectId, XId fieldId, long fieldRevision,
            XValue value, boolean isForced) {
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
    public XFieldCommand createAddValueCommand(XId modelId, XId objectId, XId fieldId,
            long fieldRevision, XValue value) {
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
    public XFieldCommand createAddValueCommand(XId modelId, XId objectId, XId fieldId,
            long fieldRevision, XValue value, boolean isForced) {
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
    public XFieldCommand createAddValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision, XValue value) {
        return createAddValueCommand(Base.resolveField(repositoryId, modelId, objectId, fieldId),
                fieldRevision, value, false);
    }
    
    @Override
    public XFieldCommand createAddValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision, XValue value, boolean isForced) {
        if(fieldRevision == XCommand.FORCED && !isForced) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        long revNr = fieldRevision;
        
        if(isForced) {
            revNr = XCommand.FORCED;
        }
        
        XAddress target = Base.toAddress(repositoryId, modelId, objectId, fieldId);
        
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
    
    public XFieldCommand createChangeValueCommand(XId fieldId, long fieldRevision, XValue value) {
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
    public XFieldCommand createChangeValueCommand(XId fieldId, long fieldRevision, XValue value,
            boolean isForced) {
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
    public XFieldCommand createChangeValueCommand(XId objectId, XId fieldId, long fieldRevision,
            XValue value) {
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
    public XFieldCommand createChangeValueCommand(XId objectId, XId fieldId, long fieldRevision,
            XValue value, boolean isForced) {
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
    public XFieldCommand createChangeValueCommand(XId modelId, XId objectId, XId fieldId,
            long fieldRevision, XValue value) {
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
    public XFieldCommand createChangeValueCommand(XId modelId, XId objectId, XId fieldId,
            long fieldRevision, XValue value, boolean isForced) {
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
    public XFieldCommand createChangeValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision, XValue value) {
        return createChangeValueCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
                value, false);
    }
    
    @Override
    public XFieldCommand createChangeValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision, XValue value, boolean isForced) {
        if(fieldRevision == XCommand.FORCED && !isForced) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        long revNr = fieldRevision;
        
        if(isForced) {
            revNr = XCommand.FORCED;
        }
        
        XAddress target = Base.toAddress(repositoryId, modelId, objectId, fieldId);
        
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
    public XObjectCommand createRemoveFieldCommand(XId objectId, XId fieldId, long fieldRevision) {
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
    public XObjectCommand createRemoveFieldCommand(XId objectId, XId fieldId, long fieldRevision,
            boolean isForced) {
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
    public XObjectCommand createRemoveFieldCommand(XId modelId, XId objectId, XId fieldId,
            long fieldRevision) {
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
    public XObjectCommand createRemoveFieldCommand(XId modelId, XId objectId, XId fieldId,
            long fieldRevision, boolean isForced) {
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
    public XObjectCommand createRemoveFieldCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision) {
        return createRemoveFieldCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
                false);
    }
    
    @Override
    public XObjectCommand createRemoveFieldCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision, boolean isForced) {
        if(fieldRevision == XCommand.FORCED && !isForced) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        long revNr = fieldRevision;
        
        if(isForced) {
            revNr = XCommand.FORCED;
        }
        
        XAddress target = Base.toAddress(repositoryId, modelId, objectId, null);
        
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
    public XRepositoryCommand createRemoveModelCommand(XId repositoryId, XId modelId,
            long modelRevision) {
        return createRemoveModelCommand(repositoryId, modelId, modelRevision, false);
    }
    
    @Override
    public XRepositoryCommand createRemoveModelCommand(XId repositoryId, XId modelId,
            long modelRevision, boolean isForced) {
        if(modelRevision == XCommand.FORCED && !isForced) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        long revNr = modelRevision;
        
        if(isForced) {
            revNr = XCommand.FORCED;
        }
        
        XAddress target = Base.toAddress(repositoryId, null, null, null);
        
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
    public XModelCommand createRemoveObjectCommand(XId modelId, XId objectId, long objectRevision) {
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
    public XModelCommand createRemoveObjectCommand(XId modelId, XId objectId, long objectRevision,
            boolean isForced) {
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
    public XModelCommand createRemoveObjectCommand(XId repositoryId, XId modelId, XId objectId,
            long objectRevision) {
        return createRemoveObjectCommand(repositoryId, modelId, objectId, objectRevision, false);
    }
    
    @Override
    public XModelCommand createRemoveObjectCommand(XId repositoryId, XId modelId, XId objectId,
            long objectRevision, boolean isForced) {
        if(objectRevision == XCommand.FORCED && !isForced) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        long revNr = objectRevision;
        
        if(isForced) {
            revNr = XCommand.FORCED;
        }
        
        XAddress target = Base.toAddress(repositoryId, modelId, null, null);
        
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
    public XFieldCommand createRemoveValueCommand(XId fieldId, long fieldRevision) {
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
    public XFieldCommand createRemoveValueCommand(XId fieldId, long fieldRevision, boolean isForced) {
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
    public XFieldCommand createRemoveValueCommand(XId objectId, XId fieldId, long fieldRevision) {
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
    public XFieldCommand createRemoveValueCommand(XId objectId, XId fieldId, long fieldRevision,
            boolean isForced) {
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
    public XFieldCommand createRemoveValueCommand(XId modelId, XId objectId, XId fieldId,
            long fieldRevision) {
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
    public XFieldCommand createRemoveValueCommand(XId modelId, XId objectId, XId fieldId,
            long fieldRevision, boolean isForced) {
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
    public XFieldCommand createRemoveValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision) {
        return createRemoveValueCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
                false);
    }
    
    @Override
    public XFieldCommand createRemoveValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision, boolean isForced) {
        if(fieldRevision == XCommand.FORCED && !isForced) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        long revNr = fieldRevision;
        
        if(isForced) {
            revNr = XCommand.FORCED;
        }
        
        XAddress target = Base.toAddress(repositoryId, modelId, objectId, fieldId);
        
        return MemoryFieldCommand.createRemoveCommand(target, revNr);
    }
    
    @Override
    public XObjectCommand createAddFieldCommand(XAddress objectAddress, XId fieldId,
            boolean isForced) {
        return createAddFieldCommand(objectAddress.getRepository(), objectAddress.getModel(),
                objectAddress.getObject(), fieldId, isForced);
    }
    
    @Override
    public XModelCommand createAddObjectCommand(XAddress modelAddress, XId objectId,
            boolean isForced) {
        return createAddObjectCommand(modelAddress.getRepository(), modelAddress.getModel(),
                objectId, isForced);
    }
    
    @Override
    public XFieldCommand createAddValueCommand(XAddress fieldAddress, long fieldRevision,
            XValue value, boolean isForced) {
        return createAddValueCommand(fieldAddress.getRepository(), fieldAddress.getModel(),
                fieldAddress.getObject(), fieldAddress.getField(), fieldRevision, value, isForced);
    }
    
    @Override
    public XFieldCommand createChangeValueCommand(XAddress fieldAddress, long fieldRevision,
            XValue value, boolean isForced) {
        return createChangeValueCommand(fieldAddress.getRepository(), fieldAddress.getModel(),
                fieldAddress.getObject(), fieldAddress.getField(), fieldRevision, value, isForced);
    }
    
    @Override
    public XObjectCommand createRemoveFieldCommand(XAddress fieldAddress, long fieldRevision,
            boolean isForced) {
        return createRemoveFieldCommand(fieldAddress.getRepository(), fieldAddress.getModel(),
                fieldAddress.getObject(), fieldAddress.getField(), fieldRevision, isForced);
    }
    
    @Override
    public XRepositoryCommand createRemoveModelCommand(XAddress modelAddress, long modelRevision,
            boolean isForced) {
        return createRemoveModelCommand(modelAddress.getRepository(), modelAddress.getModel(),
                modelRevision, isForced);
    }
    
    @Override
    public XModelCommand createRemoveObjectCommand(XAddress objectAddress, long objectRevision,
            boolean isForced) {
        return createRemoveObjectCommand(objectAddress.getRepository(), objectAddress.getModel(),
                objectAddress.getObject(), objectRevision, isForced);
    }
    
    @Override
    public XFieldCommand createRemoveValueCommand(XAddress fieldAddress, long fieldRevision,
            boolean isForced) {
        return createRemoveValueCommand(fieldAddress.getRepository(), fieldAddress.getModel(),
                fieldAddress.getObject(), fieldAddress.getField(), fieldRevision, isForced);
    }
    
    @Override
    public XObjectCommand createSafeAddFieldCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId) {
        return createAddFieldCommand(Base.resolveObject(repositoryId, modelId, objectId), fieldId,
                false);
    }
    
    @Override
    public XObjectCommand createForcedAddFieldCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId) {
        return createAddFieldCommand(Base.resolveObject(repositoryId, modelId, objectId), fieldId,
                true);
    }
    
    @Override
    public XObjectCommand createSafeAddFieldCommand(XAddress objectAddress, XId fieldId) {
        return createAddFieldCommand(objectAddress, fieldId, false);
    }
    
    @Override
    public XObjectCommand createForcedAddFieldCommand(XAddress objectAddress, XId fieldId) {
        return createAddFieldCommand(objectAddress, fieldId, true);
    }
    
    @Override
    public XRepositoryCommand createSafeAddModelCommand(XId repositoryId, XId modelId) {
        return createAddModelCommand(repositoryId, modelId, false);
    }
    
    @Override
    public XRepositoryCommand createForcedAddModelCommand(XId repositoryId, XId modelId) {
        return createAddModelCommand(repositoryId, modelId, true);
    }
    
    @Override
    public XModelCommand createForcedAddObjectCommand(XId repositoryId, XId modelId, XId objectId) {
        return createAddObjectCommand(Base.resolveModel(repositoryId, modelId), objectId, true);
    }
    
    @Override
    public XModelCommand createSafeAddObjectCommand(XAddress modelAddress, XId objectId) {
        return createAddObjectCommand(modelAddress, objectId, false);
    }
    
    @Override
    public XModelCommand createForcedAddObjectCommand(XAddress modelAddress, XId objectId) {
        return createAddObjectCommand(modelAddress, objectId, true);
    }
    
    @Override
    public XFieldCommand createSafeAddValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision, XValue value) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        return createAddValueCommand(Base.resolveField(repositoryId, modelId, objectId, fieldId),
                fieldRevision, value, false);
    }
    
    @Override
    public XFieldCommand createForcedAddValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, XValue value) {
        return createAddValueCommand(Base.resolveField(repositoryId, modelId, objectId, fieldId),
                XCommand.FORCED, value, true);
    }
    
    @Override
    public XFieldCommand createSafeAddValueCommand(XAddress fieldAddress, long fieldRevision,
            XValue value) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        return createAddValueCommand(fieldAddress, fieldRevision, value, false);
    }
    
    @Override
    public XFieldCommand createForcedAddValueCommand(XAddress fieldAddress, XValue value) {
        return createAddValueCommand(fieldAddress, XCommand.FORCED, value, true);
    }
    
    @Override
    public XFieldCommand createSafeChangeValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision, XValue value) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        return createChangeValueCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
                value, false);
    }
    
    @Override
    public XFieldCommand createForcedChangeValueCommand(XId repositoryId, XId modelId,
            XId objectId, XId fieldId, XValue value) {
        return createChangeValueCommand(repositoryId, modelId, objectId, fieldId, XCommand.FORCED,
                value, true);
    }
    
    @Override
    public XFieldCommand createSafeChangeValueCommand(XAddress fieldAddress, long fieldRevision,
            XValue value) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        return createChangeValueCommand(fieldAddress, fieldRevision, value, false);
    }
    
    @Override
    public XFieldCommand createForcedChangeValueCommand(XAddress fieldAddress, XValue value) {
        return createChangeValueCommand(fieldAddress, XCommand.FORCED, value, true);
    }
    
    @Override
    public XObjectCommand createSafeRemoveFieldCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        return createRemoveFieldCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
                false);
    }
    
    @Override
    public XObjectCommand createForcedRemoveFieldCommand(XId repositoryId, XId modelId,
            XId objectId, XId fieldId) {
        return createRemoveFieldCommand(repositoryId, modelId, objectId, fieldId, XCommand.FORCED,
                true);
    }
    
    @Override
    public XObjectCommand createSafeRemoveFieldCommand(XAddress fieldAddress, long fieldRevision) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        return createRemoveFieldCommand(fieldAddress, fieldRevision, false);
    }
    
    @Override
    public XObjectCommand createForcedRemoveFieldCommand(XAddress fieldAddress) {
        return createRemoveFieldCommand(fieldAddress, XCommand.FORCED, true);
    }
    
    @Override
    public XRepositoryCommand createSafeRemoveModelCommand(XId repositoryId, XId modelId,
            long modelRevision) {
        if(modelRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        return createRemoveModelCommand(repositoryId, modelId, modelRevision, false);
    }
    
    @Override
    public XRepositoryCommand createForcedRemoveModelCommand(XId repositoryId, XId modelId) {
        return createRemoveModelCommand(repositoryId, modelId, XCommand.FORCED, true);
    }
    
    @Override
    public XRepositoryCommand createSafeRemoveModelCommand(XAddress modelAddress, long modelRevision) {
        if(modelRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        return createRemoveModelCommand(modelAddress, modelRevision, false);
    }
    
    @Override
    public XRepositoryCommand createForcedRemoveModelCommand(XAddress modelAddress) {
        return createRemoveModelCommand(modelAddress, XCommand.FORCED, true);
    }
    
    @Override
    public XModelCommand createSafeRemoveObjectCommand(XId repositoryId, XId modelId, XId objectId,
            long objectRevision) {
        if(objectRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        
        return createRemoveObjectCommand(repositoryId, modelId, objectId, objectRevision, false);
    }
    
    @Override
    public XModelCommand createForcedRemoveObjectCommand(XId repositoryId, XId modelId, XId objectId) {
        return createRemoveObjectCommand(repositoryId, modelId, objectId, XCommand.FORCED, true);
    }
    
    @Override
    public XModelCommand createSafeRemoveObjectCommand(XAddress objectAddress, long objectRevision) {
        if(objectRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        return createRemoveObjectCommand(objectAddress, objectRevision, false);
    }
    
    @Override
    public XModelCommand createForcedRemoveObjectCommand(XAddress objectAddress) {
        return createRemoveObjectCommand(objectAddress, XCommand.FORCED, true);
    }
    
    @Override
    public XFieldCommand createSafeRemoveValueCommand(XId repositoryId, XId modelId, XId objectId,
            XId fieldId, long fieldRevision) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        return createRemoveValueCommand(repositoryId, modelId, objectId, fieldId, fieldRevision,
                false);
    }
    
    @Override
    public XFieldCommand createForcedRemoveValueCommand(XId repositoryId, XId modelId,
            XId objectId, XId fieldId) {
        return createRemoveValueCommand(repositoryId, modelId, objectId, fieldId, XCommand.FORCED,
                true);
    }
    
    @Override
    public XFieldCommand createSafeRemoveValueCommand(XAddress fieldAddress, long fieldRevision) {
        if(fieldRevision == XCommand.FORCED) {
            throw new IllegalArgumentException(
                    "Safe commands cannot have XCommand.FORCED as their revision number.");
        }
        return createRemoveValueCommand(fieldAddress, fieldRevision, false);
    }
    
    @Override
    public XFieldCommand createForcedRemoveValueCommand(XAddress fieldAddress) {
        return createRemoveValueCommand(fieldAddress, XCommand.FORCED, true);
    }
    
    @Override
    public XModelCommand createSafeAddObjectCommand(XId repositoryId, XId modelId, XId objectId) {
        return createAddObjectCommand(Base.resolveModel(repositoryId, modelId), objectId, true);
    }
    
}
