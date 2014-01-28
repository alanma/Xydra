package org.xydra.core.model.delta;

import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XAtomicCommand.Intent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


public class ChangeExecutor {
    
    private static final Logger log = LoggerFactory.getLogger(ChangeExecutor.class);
    
    public static boolean executeObjectCommand(XObjectCommand objectCommand,
            ChangedModel changedModel) {
        if(!objectCommand.getTarget().getParent().equals(changedModel.getAddress())) {
            log.warn("XObjectCommand " + objectCommand.getTarget()
                    + " does not target this model: " + changedModel.getAddress());
            return false;
        }
        
        XWritableObject object = changedModel.getObject(objectCommand.getObjectId());
        if(object == null) {
            log.warn("XObjectCommand is invalid: " + objectCommand);
            return false;
        }
        
        if(objectCommand.getChangeType() == ChangeType.ADD
                && objectCommand.getIntent() == Intent.SafeRevBound) {
            if(objectCommand.getRevisionNumber() != changedModel.getRevisionNumber()) {
                log.warn("XObjectCommand " + objectCommand + " failed. Expected rev="
                        + objectCommand.getRevisionNumber() + " modelRev="
                        + changedModel.getRevisionNumber());
                return false;
            }
        }
        return executeObjectCommand(objectCommand, object);
    }
    
    public static boolean executeTransaction(XTransaction transaction, ChangedModel changedModel) {
        for(int i = 0; i < transaction.size(); i++) {
            XAtomicCommand command = transaction.getCommand(i);
            
            if(command instanceof XRepositoryCommand) {
                if(!executeRepositoryCommand((XRepositoryCommand)command, changedModel)) {
                    return false;
                }
            } else if(command instanceof XModelCommand) {
                if(!executeModelCommand((XModelCommand)command, changedModel)) {
                    return false;
                }
            } else if(command instanceof XObjectCommand) {
                assert changedModel.exists();
                if(!executeObjectCommand((XObjectCommand)command, changedModel)) {
                    return false;
                }
            } else if(command instanceof XFieldCommand) {
                assert changedModel.exists();
                if(!executeFieldCommand((XFieldCommand)command, changedModel)) {
                    return false;
                }
            } else {
                assert false;
            }
        }
        return true;
    }
    
    public static boolean executeAnyCommand(XCommand command, ChangedModel changedModel) {
        if(command instanceof XTransaction) {
            return executeTransaction((XTransaction)command, changedModel);
        } else if(command instanceof XRepositoryCommand) {
            return executeRepositoryCommand((XRepositoryCommand)command, changedModel);
        } else if(command instanceof XModelCommand) {
            return executeModelCommand((XModelCommand)command, changedModel);
        } else if(command instanceof XObjectCommand) {
            assert changedModel.exists();
            return executeObjectCommand((XObjectCommand)command, changedModel);
        } else if(command instanceof XFieldCommand) {
            assert changedModel.exists();
            return executeFieldCommand((XFieldCommand)command, changedModel);
        } else {
            throw new IllegalArgumentException("unexpected command type: " + command);
        }
    }
    
    public static boolean executeFieldCommand(XFieldCommand fieldCommand, ChangedModel changedModel) {
        assert changedModel.exists();
        if(!fieldCommand.getTarget().getParent().getParent().equals(changedModel.getAddress())) {
            log.warn("XFieldCommand " + fieldCommand + " does not target this model: "
                    + changedModel.getAddress());
            return false;
        }
        
        XWritableObject object = changedModel.getObject(fieldCommand.getObjectId());
        if(object == null) {
            log.warn("{" + fieldCommand + "} is invalid - object is null");
            return false;
        }
        return executeFieldCommand(fieldCommand, object);
    }
    
    /**
     * @param modelCommand
     * @param changedModel
     * @return true if command succeeds
     */
    public static boolean executeModelCommand(XModelCommand modelCommand, ChangedModel changedModel) {
        if(!modelCommand.getTarget().equals(changedModel.getAddress())) {
            log.warn("XModelCommand " + modelCommand + " does not target this model: "
                    + changedModel.getAddress());
            return false;
        }
        
        XId objectId = modelCommand.getObjectId();
        
        switch(modelCommand.getChangeType()) {
        
        case ADD: {
            if(changedModel.hasObject(objectId)) {
                // command is invalid or doesn't change anything
                log.warn("XModelCommand " + modelCommand + " ADDs object '" + objectId
                        + "' which is already there");
                return modelCommand.isForced();
            } else {
                if(modelCommand.getIntent() == Intent.SafeRevBound) {
                    if(modelCommand.getRevisionNumber() != changedModel.getRevisionNumber()) {
                        log.warn("XModelCommand " + modelCommand + " failed. Expected rev="
                                + modelCommand.getRevisionNumber() + " modelRev="
                                + changedModel.getRevisionNumber());
                        return false;
                    }
                }
                // command is OK and adds a new object
                XWritableObject object = changedModel.createObject(objectId);
                if(object instanceof XRevWritableObject) {
                    ((XRevWritableObject)object)
                            .setRevisionNumber(changedModel.getRevisionNumber());
                }
                return true;
            }
        }
        case REMOVE: {
            XReadableObject object = changedModel.getObject(objectId);
            
            if(object == null) {
                if(modelCommand.getIntent() != Intent.Forced) {
                    log.warn("XModelCommand REMOVE " + modelCommand
                            + " cannot remove non-existing object");
                    return false;
                }
                return true;
            } else {
                // object exists
                if(modelCommand.getIntent() == Intent.SafeRevBound) {
                    if(modelCommand.getRevisionNumber() != object.getRevisionNumber()) {
                        // command is invalid
                        log.warn("Safe XModelCommand " + modelCommand
                                + " is invalid (revNr mismatch)");
                        return false;
                    }
                }
                // command is OK and removes an existing object
                changedModel.removeObject(objectId);
                return true;
            }
        }
        default:
            throw new AssertionError("impossible type for model command " + modelCommand);
        }
    }
    
    public static boolean executeRepositoryCommand(XRepositoryCommand command,
            ChangedModel changedModel) {
        if(!command.getRepositoryId().equals(changedModel.getAddress().getRepository())) {
            log.warn("XRepositoryCommand " + command + " does not target this models repository: "
                    + changedModel.getAddress());
            return false;
        }
        XId modelId = command.getModelId();
        if(!modelId.equals(changedModel.getId())) {
            log.warn("XRepositoryCommand " + command + " does not target this model: "
                    + changedModel.getAddress());
            return false;
        }
        
        switch(command.getChangeType()) {
        
        case ADD:
            if(changedModel.exists()) {
                // command is invalid or doesn't change anything
                log.warn("XRepositoryCommand " + command + " ADDs model '" + modelId
                        + "' which is already there");
                return command.isForced();
            } else {
                if(command.getIntent() == Intent.SafeRevBound) {
                    if(command.getRevisionNumber() != changedModel.getRevisionNumber()) {
                        // command is invalid
                        log.warn("Safe XRepositoryCommand " + command
                                + " is invalid. Expected-rev=" + command.getRevisionNumber()
                                + " found modelRev=" + changedModel.getRevisionNumber());
                        return false;
                    }
                }
                // command is OK and adds a new model
                changedModel.setExists(true);
                return true;
            }
            
        case REMOVE:
            if(!changedModel.exists()) {
                // command is invalid or doesn't change anything
                if(command.getIntent() != Intent.Forced) {
                    log.warn("XRepositoryCommand SAFE REMOVE " + command
                            + " is invalid - model not present");
                    return false;
                } else {
                    log.info("XRepositoryCommand REMOVE " + command + " doesn't change anything");
                    return true;
                }
            } else {
                if(command.getIntent() == Intent.SafeRevBound) {
                    if(command.getRevisionNumber() != changedModel.getRevisionNumber()) {
                        // command is invalid
                        log.warn("Safe XRepositoryCommand " + command
                                + " is invalid. Expected-rev=" + command.getRevisionNumber()
                                + " found modelRev=" + changedModel.getRevisionNumber());
                        return false;
                    }
                }
                // command is OK and removes an existing object
                changedModel.setExists(false);
                return true;
            }
            
        default:
            throw new AssertionError("impossible type for model command " + command);
        }
    }
    
    public static boolean executeAnyCommand(XCommand command, ChangedObject changedObject) {
        if(command instanceof XTransaction) {
            return executeTransaction((XTransaction)command, changedObject);
        } else if(command instanceof XRepositoryCommand) {
            throw new IllegalArgumentException("objects cannot handle repository commands: "
                    + command);
        } else if(command instanceof XModelCommand) {
            return executeModelCommand((XModelCommand)command, changedObject);
        } else if(command instanceof XObjectCommand) {
            assert changedObject.exists();
            return executeObjectCommand((XObjectCommand)command, changedObject);
        } else if(command instanceof XFieldCommand) {
            assert changedObject.exists();
            return executeFieldCommand((XFieldCommand)command, changedObject);
        } else {
            throw new IllegalArgumentException("unexpected command type: " + command);
        }
    }
    
    public static boolean executeTransaction(XTransaction transaction, ChangedObject changedObject) {
        for(int i = 0; i < transaction.size(); i++) {
            XAtomicCommand command = transaction.getCommand(i);
            
            if(command instanceof XRepositoryCommand) {
                if(!executeAnyCommand((XRepositoryCommand)command, changedObject)) {
                    return false;
                }
            } else if(command instanceof XModelCommand) {
                if(!executeModelCommand((XModelCommand)command, changedObject)) {
                    return false;
                }
            } else if(command instanceof XObjectCommand) {
                assert changedObject.exists();
                if(!executeAnyCommand((XObjectCommand)command, changedObject)) {
                    return false;
                }
            } else if(command instanceof XFieldCommand) {
                assert changedObject.exists();
                if(!executeAnyCommand((XFieldCommand)command, changedObject)) {
                    return false;
                }
            } else {
                assert false;
            }
        }
        return true;
    }
    
    /**
     * ADD SafeRevBound must be checked outside.
     * 
     * @param modelCommand
     * @param changedObject
     * @return true if command succeeds
     */
    public static boolean executeModelCommand(XModelCommand modelCommand,
            ChangedObject changedObject) {
        if(!modelCommand.getChangedEntity().equals(changedObject.getAddress())) {
            log.warn("XModelCommand " + modelCommand + " does not target this object: "
                    + changedObject.getAddress());
            return false;
        }
        
        XId objectId = modelCommand.getObjectId();
        
        switch(modelCommand.getChangeType()) {
        
        case ADD:
            if(changedObject.exists()) {
                // command is invalid or doesn't change anything
                log.warn("XModelCommand " + modelCommand + " ADDs object '" + objectId
                        + "' which is already there");
                return modelCommand.isForced();
            } else {
                // command is OK and adds a new object
                changedObject.setExists(true);
                return true;
            }
            
        case REMOVE:
            if(!changedObject.exists()) {
                // command is invalid or doesn't change anything
                log.warn("XModelCommand REMOVE " + modelCommand
                        + " is invalid or doesn't change anything");
                return modelCommand.isForced();
            } else {
                if(modelCommand.getIntent() == Intent.SafeRevBound) {
                    if(modelCommand.getRevisionNumber() != changedObject.getRevisionNumber()) {
                        // command is invalid
                        log.warn("Safe XModelCommand " + modelCommand
                                + " is invalid (revNr mismatch)");
                        return false;
                    }
                }
                // command is OK and removes an existing object
                changedObject.setExists(false);
                return true;
            }
        default:
            throw new AssertionError("impossible type for model command " + modelCommand);
        }
    }
    
    /**
     * ADD-SafeRevBound commands must have checked before if modelRev matches or
     * not.
     * 
     * @param objectCommand
     * @param object
     * @return true if command succeeds
     */
    public static boolean executeObjectCommand(XObjectCommand objectCommand, XWritableObject object) {
        XId fieldId = objectCommand.getFieldId();
        
        switch(objectCommand.getChangeType()) {
        
        case ADD: {
            if(object.hasField(fieldId)) {
                if(!objectCommand.isForced()) {
                    log.warn(objectCommand + " object has already field '" + fieldId
                            + "' and foced=" + objectCommand.isForced());
                }
                return objectCommand.isForced();
            }
            // command is OK and adds a new field
            XWritableField field = object.createField(fieldId);
            if(field instanceof XRevWritableField) {
                ((XRevWritableField)field).setRevisionNumber(object.getRevisionNumber());
            }
            return true;
        }
        case REMOVE: {
            XReadableField field = object.getField(fieldId);
            
            if(field == null) {
                // command is invalid or doesn't change anything
                log.warn("XObjectCommand REMOVE '" + objectCommand
                        + "'is invalid or doesn't change anything, forced="
                        + objectCommand.isForced());
                return objectCommand.isForced();
            } else {
                if(objectCommand.getIntent() == Intent.SafeRevBound) {
                    if(objectCommand.getRevisionNumber() != field.getRevisionNumber()) {
                        // command is invalid
                        log.warn("Safe XObjectCommand REMOVE " + objectCommand + " revNr mismatch");
                        return false;
                    }
                }
                // command is OK and removes an existing field
                object.removeField(fieldId);
                return true;
            }
        }
        default:
            throw new AssertionError("impossible type for object command " + objectCommand);
        }
    }
    
    public static boolean executeFieldCommand(XFieldCommand fieldCommand, XWritableObject object) {
        XWritableField field = object.getField(fieldCommand.getFieldId());
        if(field == null) {
            log.warn("Command { " + fieldCommand + "} is invalid. Field '"
                    + fieldCommand.getFieldId() + "' not found in object '"
                    + fieldCommand.getObjectId() + "'");
            return false;
        }
        return executeFieldCommand(fieldCommand, field);
    }
    
    public static boolean executeFieldCommand(XFieldCommand fieldCommand, XWritableField field) {
        
        XValue currentValue = field.getValue();
        switch(fieldCommand.getChangeType()) {
        case ADD: {
            if(currentValue != null) {
                if(fieldCommand.getIntent() == Intent.Forced) {
                    field.setValue(fieldCommand.getValue());
                    return true;
                } else {
                    log.warn("Could not safely ADD value to a field that had already a value");
                    return false;
                }
            } else {
                if(fieldCommand.getIntent() == Intent.SafeRevBound) {
                    if(field.getRevisionNumber() != fieldCommand.getRevisionNumber()) {
                        log.warn("SafeRevBound FieldCommand {" + fieldCommand
                                + "} failed. expected=" + fieldCommand.getRevisionNumber()
                                + " fieldRev=" + field.getRevisionNumber());
                        return false;
                    }
                }
                field.setValue(fieldCommand.getValue());
                return true;
            }
        }
        case REMOVE: {
            if(currentValue == null) {
                if(fieldCommand.getIntent() == Intent.Forced) {
                    return true;
                } else {
                    log.warn("Could not safely REMOVE value from a field that had no value");
                    return false;
                }
            } else {
                if(fieldCommand.getIntent() == Intent.SafeRevBound) {
                    if(field.getRevisionNumber() != fieldCommand.getRevisionNumber()) {
                        log.warn("SafeRevBound FieldCommand {" + fieldCommand
                                + "} failed. expected=" + fieldCommand.getRevisionNumber()
                                + " fieldRev=" + field.getRevisionNumber());
                        return false;
                    }
                }
                field.setValue(fieldCommand.getValue());
                return true;
            }
        }
        case CHANGE: {
            if(currentValue == null) {
                if(fieldCommand.getIntent() == Intent.Forced) {
                    field.setValue(fieldCommand.getValue());
                    return true;
                } else {
                    log.warn("Could not safely CHANGE value of a field that had no value");
                    return false;
                }
            } else {
                switch(fieldCommand.getIntent()) {
                case Forced: {
                    // no checks
                }
                    break;
                case SafeStateBound: {
                    assert currentValue != null;
                    // passed, current value exists
                }
                    break;
                case SafeRevBound: {
                    if(field.getRevisionNumber() != fieldCommand.getRevisionNumber()) {
                        log.warn("SafeRevBound FieldCommand {" + fieldCommand
                                + "} failed. expected=" + fieldCommand.getRevisionNumber()
                                + " fieldRev==" + field.getRevisionNumber());
                        return false;
                    }
                }
                    break;
                }
                // all checks passed
                field.setValue(fieldCommand.getValue());
                return true;
            }
        }
        default:
            throw new AssertionError();
        }
    }
    
}
