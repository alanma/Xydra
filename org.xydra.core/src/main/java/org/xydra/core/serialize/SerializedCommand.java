package org.xydra.core.serialize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
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


/**
 * Collection of methods to (de-)serialize variants of {@link XCommand} to and
 * from their XML representation.
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
	
	private static long getRevision(MiniElement xml, String elementName, boolean revisioned) {
		
		Object forcedString = xml.getAttribute(FORCED_ATTRIBUTE);
		Object revisionString = xml.getAttribute(REVISION_ATTRIBUTE);
		
		boolean forced;
		if(forcedString == null)
			forced = false;
		else
			forced = SerializedValue.toBoolean(forcedString);
		
		if(forced) {
			if(revisionString != null)
				throw new IllegalArgumentException("<" + elementName + ">@" + REVISION_ATTRIBUTE
				        + " is not allowed for forced changes");
			return XCommand.FORCED;
		}
		
		if(!revisioned) {
			if(revisionString != null)
				throw new IllegalArgumentException("<" + elementName + ">@" + REVISION_ATTRIBUTE
				        + " is not allowed for non-field-changes of type ADD");
			return XCommand.SAFE;
		}
		
		if(revisionString == null)
			throw new IllegalArgumentException("<" + elementName + ">@" + REVISION_ATTRIBUTE
			        + " is missing from non-forced change");
		
		long rev = SerializedValue.toLong(revisionString);
		
		Object relativeString = xml.getAttribute(REVISION_RELATIVE_ATTRIBUTE);
		if(relativeString != null) {
			assert rev < XCommand.RELATIVE_REV;
			if(SerializedValue.toBoolean(relativeString)) {
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
		
		SerializingUtils.setTarget(command.getTarget(), out, context);
		
		if(command.isForced())
			out.attribute(FORCED_ATTRIBUTE, true);
		else if(saveRevision) {
			
			long rev = command.getRevisionNumber();
			
			if(rev >= XCommand.RELATIVE_REV) {
				out.attribute(REVISION_RELATIVE_ATTRIBUTE, true);
				rev -= XCommand.RELATIVE_REV;
			}
			
			out.attribute(REVISION_ATTRIBUTE, rev);
		}
		
	}
	
	/**
	 * Get the {@link XAtomicCommand} represented by the given XML element.
	 * 
	 * @param context The {@link XID XIDs} of the repository, model, object and
	 *            field to fill in if not specified in the XML. If the given
	 *            element represents a transaction, the context for the
	 *            contained commands will be given by the transaction.
	 * @return The {@link XAtomicCommand} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XAtomicCommand toAtomicCommand(MiniElement xml, XAddress context)
	        throws IllegalArgumentException {
		String name = xml.getType();
		if(name.equals(XFIELDCOMMAND_ELEMENT))
			return toFieldCommand(xml, context);
		else if(name.equals(XOBJECTCOMMAND_ELEMENT))
			return toObjectCommand(xml, context);
		else if(name.equals(XMODELCOMMAND_ELEMENT))
			return toModelCommand(xml, context);
		else if(name.equals(XREPOSITORYCOMMAND_ELEMENT))
			return toRepositoryCommand(xml, context);
		else
			throw new IllegalArgumentException("Unexpected command element: <" + name + ">.");
	}
	
	/**
	 * Get the {@link XCommand} represented by the given XML element.
	 * 
	 * @param context The {@link XID XIDs} of the repository, model, object and
	 *            field to fill in if not specified in the XML. If the given
	 *            element represents a transaction, the context for the
	 *            contained commands will be given by the transaction.
	 * @return The {@link XCommand} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XCommand toCommand(MiniElement xml, XAddress context)
	        throws IllegalArgumentException {
		String name = xml.getType();
		if(name.equals(XTRANSACTION_ELEMENT))
			return toTransaction(xml, context);
		else
			return toAtomicCommand(xml, context);
	}
	
	/**
	 * Get the {@link XFieldCommand} represented by the given XML element.
	 * 
	 * @param context The {@link XID XIDs} of the repository, model, object and
	 *            field to fill in if not specified in the XML.
	 * @return The {@link XFieldCommand} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XFieldCommand toFieldCommand(MiniElement xml, XAddress context) {
		
		SerializingUtils.checkElementName(xml, XFIELDCOMMAND_ELEMENT);
		
		XAddress target = SerializingUtils.getTarget(xml, context);
		
		ChangeType type = SerializingUtils.getChangeType(xml, XFIELDCOMMAND_ELEMENT);
		long rev = getRevision(xml, XFIELDCOMMAND_ELEMENT, true);
		
		XValue value = null;
		Iterator<MiniElement> it = xml.getChildren(NAME_VALUE);
		if(type != ChangeType.REMOVE) {
			if(!it.hasNext())
				throw new IllegalArgumentException("<" + XFIELDCOMMAND_ELEMENT
				        + "> is missing it's xvalue child element");
			MiniElement valueElement = it.next();
			value = SerializedValue.toValue(valueElement);
		}
		if(it.hasNext())
			throw new IllegalArgumentException("Invalid child of <" + XFIELDCOMMAND_ELEMENT
			        + ">: <" + it.next().getType() + ">");
		
		if(type == ChangeType.ADD)
			return MemoryFieldCommand.createAddCommand(target, rev, value);
		else if(type == ChangeType.CHANGE)
			return MemoryFieldCommand.createChangeCommand(target, rev, value);
		else if(type == ChangeType.REMOVE)
			return MemoryFieldCommand.createRemoveCommand(target, rev);
		else
			throw new IllegalArgumentException("<" + XFIELDCOMMAND_ELEMENT + ">@"
			        + SerializingUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for field commands, but '" + type + "'");
	}
	
	/**
	 * Get the {@link XModelCommand} represented by the given XML element.
	 * 
	 * @param context The {@link XID XIDs} of the repository and model to fill
	 *            in if not specified in the XML.
	 * @return The {@link XModelCommand} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XModelCommand toModelCommand(MiniElement xml, XAddress context) {
		
		SerializingUtils.checkElementName(xml, XMODELCOMMAND_ELEMENT);
		
		if(context != null && (context.getObject() != null || context.getField() != null))
			throw new IllegalArgumentException("invalid context for model commands: " + context);
		
		XAddress address = SerializingUtils.getTarget(xml, context);
		
		if(address.getModel() == null)
			throw new IllegalArgumentException("<" + XMODELCOMMAND_ELEMENT + ">@"
			        + SerializingUtils.MODELID_ATTRIBUTE + " is missing");
		
		if(address.getObject() == null)
			throw new IllegalArgumentException("<" + XMODELCOMMAND_ELEMENT + ">@"
			        + SerializingUtils.OBJECTID_ATTRIBUTE + " is missing");
		
		ChangeType type = SerializingUtils.getChangeType(xml, XMODELCOMMAND_ELEMENT);
		long rev = getRevision(xml, XMODELCOMMAND_ELEMENT, type != ChangeType.ADD);
		
		XAddress target = address.getParent();
		XID objectId = address.getObject();
		
		if(type == ChangeType.ADD)
			return MemoryModelCommand.createAddCommand(target, rev, objectId);
		else if(type == ChangeType.REMOVE)
			return MemoryModelCommand.createRemoveCommand(target, rev, objectId);
		else
			throw new IllegalArgumentException("<" + XMODELCOMMAND_ELEMENT + ">@"
			        + SerializingUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for model commands, but '" + type + "'");
	}
	
	/**
	 * Get the {@link XObjectCommand} represented by the given XML element.
	 * 
	 * @param context The {@link XID XIDs} of the repository, model and object
	 *            to fill in if not specified in the XML.
	 * @return The {@link XObjectCommand} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XObjectCommand toObjectCommand(MiniElement xml, XAddress context) {
		
		SerializingUtils.checkElementName(xml, XOBJECTCOMMAND_ELEMENT);
		
		if(context != null && context.getField() != null)
			throw new IllegalArgumentException("invalid context for object commands: " + context);
		
		XAddress address = SerializingUtils.getTarget(xml, context);
		
		if(address.getObject() == null)
			throw new IllegalArgumentException("<" + XOBJECTCOMMAND_ELEMENT + ">@"
			        + SerializingUtils.OBJECTID_ATTRIBUTE + " is missing");
		
		if(address.getField() == null)
			throw new IllegalArgumentException("<" + XOBJECTCOMMAND_ELEMENT + ">@"
			        + SerializingUtils.FIELDID_ATTRIBUTE + " is missing");
		
		ChangeType type = SerializingUtils.getChangeType(xml, XOBJECTCOMMAND_ELEMENT);
		long rev = getRevision(xml, XOBJECTCOMMAND_ELEMENT, type != ChangeType.ADD);
		
		XAddress target = address.getParent();
		XID fieldId = address.getField();
		
		if(type == ChangeType.ADD)
			return MemoryObjectCommand.createAddCommand(target, rev, fieldId);
		else if(type == ChangeType.REMOVE)
			return MemoryObjectCommand.createRemoveCommand(target, rev, fieldId);
		else
			throw new IllegalArgumentException("<" + XOBJECTCOMMAND_ELEMENT + ">@"
			        + SerializingUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for object commands, but '" + type + "'");
	}
	
	/**
	 * Get the {@link XRepositoryCommand} represented by the given XML element.
	 * 
	 * @param context The {@link XID} of the repository to fill in if not
	 *            specified in the XML.
	 * @return The {@link XRepositoryCommand} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XRepositoryCommand toRepositoryCommand(MiniElement xml, XAddress context) {
		
		SerializingUtils.checkElementName(xml, XREPOSITORYCOMMAND_ELEMENT);
		
		if(context != null
		        && (context.getModel() != null || context.getObject() != null || context.getField() != null))
			throw new IllegalArgumentException("invalid context for repository commands: "
			        + context);
		
		XAddress address = SerializingUtils.getTarget(xml, context);
		
		if(address.getRepository() == null)
			throw new IllegalArgumentException("<" + XREPOSITORYCOMMAND_ELEMENT + ">@"
			        + SerializingUtils.REPOSITORYID_ATTRIBUTE + " is missing");
		
		if(address.getModel() == null)
			throw new IllegalArgumentException("<" + XREPOSITORYCOMMAND_ELEMENT + ">@"
			        + SerializingUtils.MODELID_ATTRIBUTE + " is missing");
		
		ChangeType type = SerializingUtils.getChangeType(xml, XREPOSITORYCOMMAND_ELEMENT);
		long rev = getRevision(xml, XREPOSITORYCOMMAND_ELEMENT, type != ChangeType.ADD);
		
		XAddress target = address.getParent();
		XID modelId = address.getModel();
		
		if(type == ChangeType.ADD)
			return MemoryRepositoryCommand.createAddCommand(target, rev, modelId);
		else if(type == ChangeType.REMOVE)
			return MemoryRepositoryCommand.createRemoveCommand(target, rev, modelId);
		else
			throw new IllegalArgumentException("<" + XREPOSITORYCOMMAND_ELEMENT + ">@"
			        + SerializingUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for repository commands, but '" + type + "'");
	}
	
	/**
	 * Get the {@link XTransaction} represented by the given XML element.
	 * 
	 * @param context The {@link XID XIDs} of the repository, model, object and
	 *            field to fill in if not specified in the XML. The context for
	 *            the commands contained in the transaction will be given by the
	 *            transaction.
	 * @return The {@link XTransaction} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XTransaction toTransaction(MiniElement xml, XAddress context) {
		
		SerializingUtils.checkElementName(xml, XTRANSACTION_ELEMENT);
		
		XAddress target = SerializingUtils.getTarget(xml, context);
		
		if(target.getField() != null || (target.getModel() == null && target.getObject() == null))
			throw new IllegalArgumentException("Transaction element " + xml
			        + " does not specify a model or object target.");
		
		List<XAtomicCommand> commands = new ArrayList<XAtomicCommand>();
		Iterator<MiniElement> it = xml.getChildren(NAME_COMMANDS);
		while(it.hasNext()) {
			MiniElement command = it.next();
			commands.add(toAtomicCommand(command, target));
		}
		
		return MemoryTransaction.createTransaction(target, commands);
	}
	
	/**
	 * Encode the given {@link XCommand} as an XML element.
	 * 
	 * @param command The command to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this command's target address that doesn't
	 *            need to be encoded in the element.
	 */
	public static void toXml(XCommand command, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		if(command instanceof XTransaction) {
			toXml((XTransaction)command, out, context);
		} else if(command instanceof XFieldCommand) {
			toXml((XFieldCommand)command, out, context);
		} else if(command instanceof XObjectCommand) {
			toXml((XObjectCommand)command, out, context);
		} else if(command instanceof XModelCommand) {
			toXml((XModelCommand)command, out, context);
		} else if(command instanceof XRepositoryCommand) {
			toXml((XRepositoryCommand)command, out, context);
		} else {
			throw new RuntimeException("command " + command + " is of unexpected type: "
			        + command.getClass());
		}
	}
	
	/**
	 * Encode the given {@link XFieldCommand} as an XML element.
	 * 
	 * @param command The command to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this command's target address that doesn't
	 *            need to be encoded in the element.
	 */
	public static void toXml(XFieldCommand command, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XFIELDCOMMAND_ELEMENT);
		
		setAtomicCommandAttributes(command, out, context, true);
		
		out.children(NAME_VALUE, false);
		if(command.getValue() != null)
			SerializedValue.toXml(command.getValue(), out);
		
		out.close(XFIELDCOMMAND_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XModelCommand} as an XML element.
	 * 
	 * @param command The command to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this command's target address that doesn't
	 *            need to be encoded in the element.
	 */
	public static void toXml(XModelCommand command, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		
		if(context != null && (context.getObject() != null || context.getField() != null))
			throw new IllegalArgumentException("invalid context for model commands: " + context);
		
		out.open(XMODELCOMMAND_ELEMENT);
		
		setAtomicCommandAttributes(command, out, context, command.getChangeType() != ChangeType.ADD);
		
		out.attribute(SerializingUtils.OBJECTID_ATTRIBUTE, command.getObjectId());
		
		out.close(XMODELCOMMAND_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XObjectCommand} as an XML element.
	 * 
	 * @param command The command to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this command's target address that doesn't
	 *            need to be encoded in the element.
	 */
	public static void toXml(XObjectCommand command, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		
		if(context != null && context.getField() != null)
			throw new IllegalArgumentException("invalid context for object commands: " + context);
		
		out.open(XOBJECTCOMMAND_ELEMENT);
		
		setAtomicCommandAttributes(command, out, context, command.getChangeType() != ChangeType.ADD);
		
		out.attribute(SerializingUtils.FIELDID_ATTRIBUTE, command.getFieldId());
		
		out.close(XOBJECTCOMMAND_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XRepositoryCommand} as an XML element.
	 * 
	 * @param command The command to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this command's target address that doesn't
	 *            need to be encoded in the element.
	 */
	public static void toXml(XRepositoryCommand command, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		
		if(context != null
		        && (context.getModel() != null || context.getObject() != null || context.getField() != null))
			throw new IllegalArgumentException("invalid context for repository commands: "
			        + context);
		
		out.open(XREPOSITORYCOMMAND_ELEMENT);
		
		setAtomicCommandAttributes(command, out, context, command.getChangeType() != ChangeType.ADD);
		
		out.attribute(SerializingUtils.MODELID_ATTRIBUTE, command.getModelId());
		
		out.close(XREPOSITORYCOMMAND_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XTransaction} as an XML element.
	 * 
	 * @param trans The transaction to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this command's target address that doesn't
	 *            need to be encoded in the element.
	 */
	public static void toXml(XTransaction trans, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XTRANSACTION_ELEMENT);
		
		SerializingUtils.setTarget(trans.getTarget(), out, context);
		
		XAddress newContext = trans.getTarget();
		
		out.children(NAME_COMMANDS, true);
		for(XAtomicCommand command : trans) {
			toXml(command, out, newContext);
		}
		
		out.close(XTRANSACTION_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XCommand} list as an XML element.
	 * 
	 * @param commands The commands to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element.
	 */
	public static void toXml(Iterator<XCommand> commands, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XCOMMANDLIST_ELEMENT);
		
		out.children(NAME_COMMANDS, true);
		while(commands.hasNext()) {
			toXml(commands.next(), out, context);
		}
		
		out.close(XCOMMANDLIST_ELEMENT);
		
	}
	
	/**
	 * Get the {@link XCommand} list represented by the given XML element.
	 * 
	 * @param context The {@link XID XIDs} of the repository, model, object and
	 *            field to fill in if not specified in the XML. The context for
	 *            the commands contained in a transaction will be given by the
	 *            transaction.
	 * @return The {@link List} of {@link XCommand}s represented by the given
	 *         XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command list.
	 * 
	 */
	public static List<XCommand> toCommandList(MiniElement xml, XAddress context) {
		
		SerializingUtils.checkElementName(xml, XCOMMANDLIST_ELEMENT);
		
		List<XCommand> events = new ArrayList<XCommand>();
		Iterator<MiniElement> it = xml.getChildren(NAME_COMMANDS);
		while(it.hasNext()) {
			MiniElement command = it.next();
			events.add(toCommand(command, context));
		}
		
		return events;
	}
	
}
