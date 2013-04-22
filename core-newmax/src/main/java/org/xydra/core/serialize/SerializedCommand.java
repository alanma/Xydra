package org.xydra.core.serialize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
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
import org.xydra.base.value.XValue;
import org.xydra.sharedutils.XyAssert;


/**
 * Collection of methods to (de-)serialize variants of {@link XCommand} to and
 * from their XML/JSON representation.
 * 
 * @author dscharrer
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class SerializedCommand {
    
    private static final String NAME_COMMANDS = "commands";
    private static final String NAME_VALUE = "value";
    private static final String FORCED_ATTRIBUTE = "forced";
    private static final String REVISION_ATTRIBUTE = "revision";
    private static final String XFIELDCOMMAND_ELEMENT = "xfieldCommand";
    private static final String XMODELCOMMAND_ELEMENT = "xmodelCommand";
    private static final String XOBJECTCOMMAND_ELEMENT = "xobjectCommand";
    
    private static final String XREPOSITORYCOMMAND_ELEMENT = "xrepositoryCommand";
    private static final String XTRANSACTION_ELEMENT = "xtransaction";
    private static final String REVISION_RELATIVE_ATTRIBUTE = "relative";
    private static final String XCOMMANDLIST_ELEMENT = "xcommands";
    
    private static long getRevision(XydraElement element, boolean revisioned) {
        
        Object forcedString = element.getAttribute(FORCED_ATTRIBUTE);
        Object revisionString = element.getAttribute(REVISION_ATTRIBUTE);
        
        boolean forced;
        if(forcedString == null) {
            forced = false;
        } else {
            forced = SerializingUtils.toBoolean(forcedString);
        }
        
        if(forced) {
            if(revisionString != null)
                throw new ParsingError(element, "Attribute " + REVISION_ATTRIBUTE
                        + " not allowed for forced changes");
            return XCommand.FORCED;
        }
        
        if(!revisioned) {
            if(revisionString != null)
                throw new ParsingError(element, "Attribute " + REVISION_ATTRIBUTE
                        + " not allowed for non-field-changes of type ADD");
            return XCommand.SAFE;
        }
        
        if(revisionString == null) {
            throw new ParsingError(element, "Missing attribute" + REVISION_ATTRIBUTE
                    + " from non-forced change");
        }
        
        long rev = SerializingUtils.toLong(revisionString);
        
        Object relativeString = element.getAttribute(REVISION_RELATIVE_ATTRIBUTE);
        if(relativeString != null) {
            XyAssert.xyAssert(rev < XCommand.RELATIVE_REV);
            if(SerializingUtils.toBoolean(relativeString)) {
                rev += XCommand.RELATIVE_REV;
            }
        }
        
        return rev;
    }
    
    /**
     * Encode the given {@link XAtomicCommand} as an XML element.
     * 
     * @param event The command to encode.
     * @param out The XML encoder to write to.
     * @param context The part of this command's target address that doesn't
     *            need to be encoded in the element.
     */
    private static void setAtomicCommandAttributes(XAtomicCommand command, XydraOut out,
            XAddress context, boolean saveRevision) {
        
        out.attribute(SerializingUtils.TYPE_ATTRIBUTE, command.getChangeType());
        
        SerializingUtils.setAddress(command.getChangedEntity(), out, context);
        
        if(command.isForced()) {
            out.attribute(FORCED_ATTRIBUTE, true);
        } else if(saveRevision) {
            
            long rev = command.getRevisionNumber();
            
            if(rev >= XCommand.RELATIVE_REV) {
                out.attribute(REVISION_RELATIVE_ATTRIBUTE, true);
                rev -= XCommand.RELATIVE_REV;
            }
            
            out.attribute(REVISION_ATTRIBUTE, rev);
        }
        
    }
    
    private static XAtomicCommand toAtomicCommand(XydraElement element, XAddress context)
            throws ParsingError {
        String name = element.getType();
        if(name.equals(XFIELDCOMMAND_ELEMENT)) {
            return toFieldCommand(element, context);
        } else if(name.equals(XOBJECTCOMMAND_ELEMENT)) {
            return toObjectCommand(element, context);
        } else if(name.equals(XMODELCOMMAND_ELEMENT)) {
            return toModelCommand(element, context);
        } else if(name.equals(XREPOSITORYCOMMAND_ELEMENT)) {
            return toRepositoryCommand(element, context);
        } else {
            throw new ParsingError(element, "Unexpected command element: <" + name + ">.");
        }
    }
    
    /**
     * Get the {@link XCommand} represented by the given XML/JSON element.
     * 
     * @param element
     * 
     * @param context The {@link XId XIds} of the repository, model, object and
     *            field to fill in if not specified in the XML/JSON. If the
     *            given element represents a transaction, the context for the
     *            contained commands will be given by the transaction.
     * @return The {@link XCommand} represented by the given XML/JSON element.
     * @throws IllegalArgumentException if the XML element does not represent a
     *             valid command.
     * 
     */
    public static XCommand toCommand(XydraElement element, XAddress context)
            throws IllegalArgumentException {
        String name = element.getType();
        if(name.equals(XTRANSACTION_ELEMENT)) {
            return toTransaction(element, context);
        } else {
            return toAtomicCommand(element, context);
        }
    }
    
    private static XFieldCommand toFieldCommand(XydraElement element, XAddress context) {
        
        SerializingUtils.checkElementType(element, XFIELDCOMMAND_ELEMENT);
        
        XAddress target = SerializingUtils.getAddress(element, context);
        
        ChangeType type = SerializingUtils.getChangeType(element);
        long rev = getRevision(element, true);
        
        XValue value = null;
        if(type != ChangeType.REMOVE) {
            value = SerializedValue.toValue(element.getElement(NAME_VALUE));
            if(value == null) {
                throw new ParsingError(element, "Missing xvalue.");
            }
        }
        
        if(type == ChangeType.ADD) {
            return MemoryFieldCommand.createAddCommand(target, rev, value);
        } else if(type == ChangeType.CHANGE) {
            return MemoryFieldCommand.createChangeCommand(target, rev, value);
        } else if(type == ChangeType.REMOVE) {
            return MemoryFieldCommand.createRemoveCommand(target, rev);
        } else {
            throw new ParsingError(element, "Attribute " + SerializingUtils.TYPE_ATTRIBUTE
                    + " does not contain a valid type for field commands, but '" + type + "'");
        }
    }
    
    private static XModelCommand toModelCommand(XydraElement element, XAddress context) {
        
        SerializingUtils.checkElementType(element, XMODELCOMMAND_ELEMENT);
        
        if(context != null && (context.getObject() != null || context.getField() != null)) {
            throw new IllegalArgumentException("invalid context for model commands: " + context);
        }
        
        XAddress address = SerializingUtils.getAddress(element, context);
        
        if(address.getModel() == null) {
            throw new ParsingError(element, "Missing attribute "
                    + SerializingUtils.MODELID_ATTRIBUTE);
        }
        
        if(address.getObject() == null)
            throw new ParsingError(element, "Missing attribute "
                    + SerializingUtils.OBJECTID_ATTRIBUTE);
        
        ChangeType type = SerializingUtils.getChangeType(element);
        long rev = getRevision(element, type != ChangeType.ADD);
        
        XAddress target = address.getParent();
        XId objectId = address.getObject();
        
        if(type == ChangeType.ADD) {
            return MemoryModelCommand.createAddCommand(target, rev, objectId);
        } else if(type == ChangeType.REMOVE) {
            return MemoryModelCommand.createRemoveCommand(target, rev, objectId);
        } else {
            throw new ParsingError(element, "Attribute " + SerializingUtils.TYPE_ATTRIBUTE
                    + " does not contain a valid type for model commands, but '" + type + "'");
        }
    }
    
    private static XObjectCommand toObjectCommand(XydraElement element, XAddress context) {
        
        SerializingUtils.checkElementType(element, XOBJECTCOMMAND_ELEMENT);
        
        if(context != null && context.getField() != null) {
            throw new IllegalArgumentException("invalid context for object commands: " + context);
        }
        
        XAddress address = SerializingUtils.getAddress(element, context);
        
        if(address.getObject() == null) {
            throw new ParsingError(element, "Missing attribute "
                    + SerializingUtils.OBJECTID_ATTRIBUTE);
        }
        
        if(address.getField() == null) {
            throw new ParsingError(element, "Missing attribute "
                    + SerializingUtils.FIELDID_ATTRIBUTE);
        }
        
        ChangeType type = SerializingUtils.getChangeType(element);
        long rev = getRevision(element, type != ChangeType.ADD);
        
        XAddress target = address.getParent();
        XId fieldId = address.getField();
        
        if(type == ChangeType.ADD) {
            return MemoryObjectCommand.createAddCommand(target, rev, fieldId);
        } else if(type == ChangeType.REMOVE) {
            return MemoryObjectCommand.createRemoveCommand(target, rev, fieldId);
        } else {
            throw new ParsingError(element, "Attribute " + SerializingUtils.TYPE_ATTRIBUTE
                    + " does not contain a valid type for object commands, but '" + type + "'");
        }
    }
    
    private static XRepositoryCommand toRepositoryCommand(XydraElement element, XAddress context) {
        
        SerializingUtils.checkElementType(element, XREPOSITORYCOMMAND_ELEMENT);
        
        // FIXME BIG MONKEY
        if(context != null
                && (context.getModel() != null || context.getObject() != null || context.getField() != null)) {
            throw new IllegalArgumentException("invalid context for repository commands: "
                    + context);
        }
        
        XAddress address = SerializingUtils.getAddress(element, context);
        
        if(address.getRepository() == null) {
            throw new ParsingError(element, "Missing attribute "
                    + SerializingUtils.REPOSITORYID_ATTRIBUTE);
        }
        
        if(address.getModel() == null) {
            throw new ParsingError(element, "Missing attribute "
                    + SerializingUtils.MODELID_ATTRIBUTE);
        }
        
        ChangeType type = SerializingUtils.getChangeType(element);
        long rev = getRevision(element, type != ChangeType.ADD);
        
        XAddress target = address.getParent();
        XId modelId = address.getModel();
        
        if(type == ChangeType.ADD) {
            return MemoryRepositoryCommand.createAddCommand(target, rev, modelId);
        } else if(type == ChangeType.REMOVE) {
            return MemoryRepositoryCommand.createRemoveCommand(target, rev, modelId);
        } else {
            throw new ParsingError(element, "Attribute " + SerializingUtils.TYPE_ATTRIBUTE
                    + " does not contain a valid type for repository commands, but '" + type + "'");
        }
    }
    
    private static XTransaction toTransaction(XydraElement element, XAddress context) {
        
        SerializingUtils.checkElementType(element, XTRANSACTION_ELEMENT);
        
        XAddress target = SerializingUtils.getAddress(element, context);
        
        if(target.getField() != null || (target.getModel() == null && target.getObject() == null)) {
            throw new IllegalArgumentException("Transaction element " + element
                    + " does not specify a model or object target.");
        }
        
        List<XAtomicCommand> commands = new ArrayList<XAtomicCommand>();
        Iterator<XydraElement> it = element.getChildrenByName(NAME_COMMANDS);
        while(it.hasNext()) {
            XydraElement command = it.next();
            commands.add(toAtomicCommand(command, target));
        }
        
        return MemoryTransaction.createTransaction(target, commands);
    }
    
    /**
     * Encode the given {@link XCommand} as an XML/JSON element.
     * 
     * @param command The command to encode.
     * @param out The XML/JSON encoder to write to.
     * @param context The part of this command's target address that doesn't
     *            need to be encoded in the element.
     * @throws IllegalArgumentException
     */
    public static void serialize(XCommand command, XydraOut out, XAddress context)
            throws IllegalArgumentException {
        if(command instanceof XTransaction) {
            serialize((XTransaction)command, out, context);
        } else if(command instanceof XFieldCommand) {
            serialize((XFieldCommand)command, out, context);
        } else if(command instanceof XObjectCommand) {
            serialize((XObjectCommand)command, out, context);
        } else if(command instanceof XModelCommand) {
            serialize((XModelCommand)command, out, context);
        } else if(command instanceof XRepositoryCommand) {
            serialize((XRepositoryCommand)command, out, context);
        } else {
            throw new RuntimeException("command " + command + " is of unexpected type: "
                    + command.getClass());
        }
    }
    
    private static void serialize(XFieldCommand command, XydraOut out, XAddress context)
            throws IllegalArgumentException {
        
        out.open(XFIELDCOMMAND_ELEMENT);
        
        setAtomicCommandAttributes(command, out, context, true);
        
        if(command.getValue() != null) {
            out.child(NAME_VALUE);
            SerializedValue.serialize(command.getValue(), out);
        }
        
        out.close(XFIELDCOMMAND_ELEMENT);
        
    }
    
    private static void serialize(XModelCommand command, XydraOut out, XAddress context)
            throws IllegalArgumentException {
        
        if(context != null && (context.getObject() != null || context.getField() != null)) {
            throw new IllegalArgumentException("invalid context for model commands: " + context);
        }
        
        out.open(XMODELCOMMAND_ELEMENT);
        setAtomicCommandAttributes(command, out, context, command.getChangeType() != ChangeType.ADD);
        out.close(XMODELCOMMAND_ELEMENT);
        
    }
    
    private static void serialize(XObjectCommand command, XydraOut out, XAddress context)
            throws IllegalArgumentException {
        
        if(context != null && context.getField() != null) {
            throw new IllegalArgumentException("invalid context for object commands: " + context);
        }
        
        out.open(XOBJECTCOMMAND_ELEMENT);
        setAtomicCommandAttributes(command, out, context, command.getChangeType() != ChangeType.ADD);
        out.close(XOBJECTCOMMAND_ELEMENT);
        
    }
    
    private static void serialize(XRepositoryCommand command, XydraOut out, XAddress context)
            throws IllegalArgumentException {
        
        // FIXME BIG MONKEY
        // if(context != null
        // && (context.getModel() != null || context.getObject() != null ||
        // context.getField() != null)) {
        // throw new IllegalArgumentException("invalid context '" + context
        // + "' for repository command: " + command);
        // }
        
        out.open(XREPOSITORYCOMMAND_ELEMENT);
        setAtomicCommandAttributes(command, out, context, command.getChangeType() != ChangeType.ADD);
        out.close(XREPOSITORYCOMMAND_ELEMENT);
        
    }
    
    private static void serialize(XTransaction trans, XydraOut out, XAddress context)
            throws IllegalArgumentException {
        
        out.open(XTRANSACTION_ELEMENT);
        
        SerializingUtils.setAddress(trans.getTarget(), out, context);
        
        XAddress newContext = trans.getTarget();
        
        out.child(NAME_COMMANDS);
        out.beginArray();
        for(XAtomicCommand command : trans) {
            serialize(command, out, newContext);
        }
        out.endArray();
        
        out.close(XTRANSACTION_ELEMENT);
        
    }
    
    /**
     * Encode the given {@link XCommand} list as an XML/JSON element.
     * 
     * @param commands The commands to encode.
     * @param out The XML/JSON encoder to write to.
     * @param context The part of this event's target address that doesn't need
     *            to be encoded in the element.
     * @throws IllegalArgumentException
     */
    public static void serialize(Iterator<? extends XCommand> commands, XydraOut out,
            XAddress context) throws IllegalArgumentException {
        
        out.open(XCOMMANDLIST_ELEMENT);
        
        out.child(NAME_COMMANDS);
        out.beginArray();
        while(commands.hasNext()) {
            serialize(commands.next(), out, context);
        }
        out.endArray();
        
        out.close(XCOMMANDLIST_ELEMENT);
    }
    
    /**
     * Get the {@link XCommand} list represented by the given XML/JSON element.
     * 
     * @param element
     * 
     * @param context The {@link XId XIds} of the repository, model, object and
     *            field to fill in if not specified in the XML/JSON. The context
     *            for the commands contained in a transaction will be given by
     *            the transaction.
     * @return The {@link List} of {@link XCommand}s represented by the given
     *         XML/JSON element.
     * @throws IllegalArgumentException if the XML/JSON element does not
     *             represent a valid command list.
     * 
     */
    public static List<XCommand> toCommandList(XydraElement element, XAddress context) {
        
        SerializingUtils.checkElementType(element, XCOMMANDLIST_ELEMENT);
        
        List<XCommand> events = new ArrayList<XCommand>();
        Iterator<XydraElement> it = element.getChildrenByName(NAME_COMMANDS);
        while(it.hasNext()) {
            XydraElement command = it.next();
            events.add(toCommand(command, context));
        }
        
        return events;
    }
    
}
