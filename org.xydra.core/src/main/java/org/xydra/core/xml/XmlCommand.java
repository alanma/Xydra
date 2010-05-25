package org.xydra.core.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.impl.memory.MemoryFieldCommand;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.change.impl.memory.MemoryTransaction;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.value.XValue;



/**
 * Collection of methods to (de-)serialize variants of {@link XCommand} to and
 * from their XML representation.
 * 
 * @author dscharrer
 */
@RunsInGWT
@RunsInAppEngine
@RunsInJava
public class XmlCommand {
	
	private static final String XREPOSITORYCOMMAND_ELEMENT = "xrepositoryCommand";
	private static final String XMODELCOMMAND_ELEMENT = "xmodelCommand";
	private static final String XOBJECTCOMMAND_ELEMENT = "xobjectCommand";
	private static final String XFIELDCOMMAND_ELEMENT = "xfieldCommand";
	private static final String XTRANSACTION_ELEMENT = "xtransaction";
	
	private static final String REPOSITORYID_ATTRIBUTE = "repositoryId";
	private static final String MODELID_ATTRIBUTE = "modelId";
	private static final String OBJECTID_ATTRIBUTE = "objectId";
	private static final String FIELDID_ATTRIBUTE = "fieldId";
	private static final String REVISION_ATTRIBUTE = "revision";
	private static final String FORCED_ATTRIBUTE = "forced";
	private static final String TYPE_ATTRIBUTE = "type";
	
	private static final String ADD_VALUE = "ADD";
	private static final String REMOVE_VALUE = "REMOVE";
	private static final String CHANGE_VALUE = "CHANGE";
	
	private static long getRevision(MiniElement xml, String elementName, boolean revisioned) {
		
		String forcedString = xml.getAttribute(FORCED_ATTRIBUTE);
		String revisionString = xml.getAttribute(REVISION_ATTRIBUTE);
		
		boolean forced;
		if(forcedString == null)
			forced = false;
		else
			forced = Boolean.parseBoolean(forcedString);
		
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
		
		try {
			return Long.parseLong(revisionString);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("<" + elementName + ">@" + REVISION_ATTRIBUTE
			        + " does not contain a long, but '" + revisionString + "'");
		}
	}
	
	private static ChangeType getTypeAttribute(MiniElement xml, String elementName) {
		String typeString = xml.getAttribute(TYPE_ATTRIBUTE);
		if(typeString == null)
			throw new IllegalArgumentException("<" + elementName + ">@" + TYPE_ATTRIBUTE
			        + " is missing");
		if(typeString.equals(ADD_VALUE))
			return ChangeType.ADD;
		else if(typeString.equals(REMOVE_VALUE))
			return ChangeType.REMOVE;
		else if(typeString.equals(CHANGE_VALUE))
			return ChangeType.CHANGE;
		else
			throw new IllegalArgumentException("<" + elementName + ">@" + TYPE_ATTRIBUTE
			        + " does not contain a valid type, but '" + typeString + "'");
	}
	
	private static XID getXidAttribute(MiniElement xml, String attributeName, XID def) {
		String xidString = xml.getAttribute(attributeName);
		if(xidString == null)
			return def;
		return X.getIDProvider().fromString(xidString);
	}
	
	private static void checkElementName(MiniElement xml, String expectedName) {
		if(!xml.getName().equals(expectedName)) {
			throw new IllegalArgumentException("Given element " + xml + " is not an <"
			        + expectedName + "> element.");
		}
	}
	
	private static XAddress getTarget(MiniElement xml, XAddress context) {
		
		XID repoId = getXidAttribute(xml, REPOSITORYID_ATTRIBUTE, context == null ? null : context
		        .getRepository());
		XID modelId = getXidAttribute(xml, MODELID_ATTRIBUTE, context == null ? null : context
		        .getModel());
		XID objectId = getXidAttribute(xml, OBJECTID_ATTRIBUTE, context == null ? null : (XX
		        .equals(modelId, context.getModel()) ? context.getObject() : null));
		XID fieldId = getXidAttribute(xml, FIELDID_ATTRIBUTE, context == null ? null : (XX.equals(
		        objectId, context.getObject()) ? context.getField() : null));
		
		return X.getIDProvider().fromComponents(repoId, modelId, objectId, fieldId);
	}
	
	/**
	 * 
	 * @param context The XIDs of the model, object and field to fill in if not
	 *            specified in the XML. If the given element represents a
	 *            transaction, the context for the contained commands will be
	 *            given by the transaction.
	 * @param defaultRepository If the XML element does not specify a
	 *            repository, this will be filled in.
	 * @return The {@link XCommand} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XCommand toCommand(MiniElement xml, XAddress context)
	        throws IllegalArgumentException {
		String name = xml.getName();
		if(name.equals(XTRANSACTION_ELEMENT))
			return toTransaction(xml, context);
		else
			return toAtomicCommand(xml, context);
	}
	
	/**
	 * 
	 * @param context The XIDs of the model, object and field to fill in if not
	 *            specified in the XML. If the given element represents a
	 *            transaction, the context for the contained commands will be
	 *            given by the transaction.
	 * @param defaultRepository If the XML element does not specify a
	 *            repository, this will be filled in.
	 * @return The {@link XAtomicCommand} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XAtomicCommand toAtomicCommand(MiniElement xml, XAddress context)
	        throws IllegalArgumentException {
		String name = xml.getName();
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
	 * 
	 * @param context The XIDs of the model, object and field to fill in if not
	 *            specified in the XML. The context for the commands contained
	 *            in the transaction will be given by the transaction.
	 * @param defaultRepository If the XML element does not specify a
	 *            repository, this will be filled in.
	 * @return The {@link XTransaction} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XTransaction toTransaction(MiniElement xml, XAddress context) {
		
		checkElementName(xml, XTRANSACTION_ELEMENT);
		
		XAddress target = getTarget(xml, context);
		
		if(target.getField() != null || (target.getModel() == null && target.getObject() == null))
			throw new IllegalArgumentException("Transaction element " + xml
			        + " does not specify a model or object target.");
		
		List<XAtomicCommand> commands = new ArrayList<XAtomicCommand>();
		Iterator<MiniElement> it = xml.getElements();
		while(it.hasNext()) {
			MiniElement command = it.next();
			commands.add(toAtomicCommand(command, target));
		}
		
		return MemoryTransaction.createTransaction(target, commands);
	}
	
	/**
	 * 
	 * @param context The XIDs of the model, object and field to fill in if not
	 *            specified in the XML.
	 * @param defaultRepository If the XML element does not specify a
	 *            repository, this will be filled in.
	 * @return The {@link XFieldCommand} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XFieldCommand toFieldCommand(MiniElement xml, XAddress context) {
		
		checkElementName(xml, XFIELDCOMMAND_ELEMENT);
		
		XAddress target = getTarget(xml, context);
		
		ChangeType type = getTypeAttribute(xml, XFIELDCOMMAND_ELEMENT);
		long rev = getRevision(xml, XFIELDCOMMAND_ELEMENT, true);
		
		XValue value = null;
		Iterator<MiniElement> it = xml.getElements();
		if(type != ChangeType.REMOVE) {
			if(!it.hasNext())
				throw new IllegalArgumentException("<" + XFIELDCOMMAND_ELEMENT
				        + "> is missing it's xvalue child element");
			MiniElement valueElement = it.next();
			value = XmlValue.toValue(valueElement);
		}
		if(it.hasNext())
			throw new IllegalArgumentException("Invalid child of <" + XFIELDCOMMAND_ELEMENT
			        + ">: <" + it.next().getName() + ">");
		
		if(type == ChangeType.ADD)
			return MemoryFieldCommand.createAddCommand(target, rev, value);
		else if(type == ChangeType.CHANGE)
			return MemoryFieldCommand.createChangeCommand(target, rev, value);
		else if(type == ChangeType.REMOVE)
			return MemoryFieldCommand.createRemoveCommand(target, rev);
		else
			throw new IllegalArgumentException("<" + XFIELDCOMMAND_ELEMENT + ">@" + TYPE_ATTRIBUTE
			        + " does not contain a valid type for field commands, but '" + type + "'");
	}
	
	/**
	 * 
	 * @param context The XIDs of the model and object to fill in if not
	 *            specified in the XML.
	 * @param defaultRepository If the XML element does not specify a
	 *            repository, this will be filled in.
	 * @return The {@link XObjectCommand} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XObjectCommand toObjectCommand(MiniElement xml, XAddress context) {
		
		checkElementName(xml, XOBJECTCOMMAND_ELEMENT);
		
		if(context.getField() != null)
			throw new IllegalArgumentException("invalid context for object commands: " + context);
		
		XAddress address = getTarget(xml, context);
		
		if(address.getObject() == null)
			throw new IllegalArgumentException("<" + XOBJECTCOMMAND_ELEMENT + ">@"
			        + OBJECTID_ATTRIBUTE + " is missing");
		
		if(address.getField() == null)
			throw new IllegalArgumentException("<" + XOBJECTCOMMAND_ELEMENT + ">@"
			        + FIELDID_ATTRIBUTE + " is missing");
		
		ChangeType type = getTypeAttribute(xml, XOBJECTCOMMAND_ELEMENT);
		long rev = getRevision(xml, XOBJECTCOMMAND_ELEMENT, type != ChangeType.ADD);
		
		Iterator<MiniElement> it = xml.getElements();
		if(it.hasNext())
			throw new IllegalArgumentException("Invalid child of <" + XOBJECTCOMMAND_ELEMENT
			        + ">: <" + it.next().getName() + ">");
		
		XAddress target = address.getParent();
		XID fieldId = address.getField();
		
		if(type == ChangeType.ADD)
			return MemoryObjectCommand.createAddCommand(target, rev, fieldId);
		else if(type == ChangeType.REMOVE)
			return MemoryObjectCommand.createRemoveCommand(target, rev, fieldId);
		else
			throw new IllegalArgumentException("<" + XOBJECTCOMMAND_ELEMENT + ">@" + TYPE_ATTRIBUTE
			        + " does not contain a valid type for object commands, but '" + type + "'");
	}
	
	/**
	 * 
	 * @param context The XID of the model to fill in if not specified in the
	 *            XML.
	 * @param defaultRepository If the XML element does not specify a
	 *            repository, this will be filled in.
	 * @return The {@link XModelCommand} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XModelCommand toModelCommand(MiniElement xml, XAddress context) {
		
		checkElementName(xml, XMODELCOMMAND_ELEMENT);
		
		if(context.getObject() != null || context.getField() != null)
			throw new IllegalArgumentException("invalid context for model commands: " + context);
		
		XAddress address = getTarget(xml, context);
		
		if(address.getModel() == null)
			throw new IllegalArgumentException("<" + XMODELCOMMAND_ELEMENT + ">@"
			        + MODELID_ATTRIBUTE + " is missing");
		
		if(address.getObject() == null)
			throw new IllegalArgumentException("<" + XMODELCOMMAND_ELEMENT + ">@"
			        + OBJECTID_ATTRIBUTE + " is missing");
		
		ChangeType type = getTypeAttribute(xml, XMODELCOMMAND_ELEMENT);
		long rev = getRevision(xml, XMODELCOMMAND_ELEMENT, type != ChangeType.ADD);
		
		Iterator<MiniElement> it = xml.getElements();
		if(it.hasNext())
			throw new IllegalArgumentException("Invalid child of <" + XMODELCOMMAND_ELEMENT
			        + ">: <" + it.next().getName() + ">");
		
		XAddress target = address.getParent();
		XID objectId = address.getObject();
		
		if(type == ChangeType.ADD)
			return MemoryModelCommand.createAddCommand(target, rev, objectId);
		else if(type == ChangeType.REMOVE)
			return MemoryModelCommand.createRemoveCommand(target, rev, objectId);
		else
			throw new IllegalArgumentException("<" + XMODELCOMMAND_ELEMENT + ">@" + TYPE_ATTRIBUTE
			        + " does not contain a valid type for model commands, but '" + type + "'");
	}
	
	/**
	 * 
	 * @param defaultRepository If the XML element does not specify a
	 *            repository, this will be filled in.
	 * @return The {@link XRepositoryCommand} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid command.
	 * 
	 */
	public static XRepositoryCommand toRepositoryCommand(MiniElement xml, XAddress context) {
		
		checkElementName(xml, XREPOSITORYCOMMAND_ELEMENT);
		
		if(context.getModel() != null || context.getObject() != null || context.getField() != null)
			throw new IllegalArgumentException("invalid context for repository commands: "
			        + context);
		
		XAddress address = getTarget(xml, context);
		
		if(address.getRepository() == null)
			throw new IllegalArgumentException("<" + XREPOSITORYCOMMAND_ELEMENT + ">@"
			        + REPOSITORYID_ATTRIBUTE + " is missing");
		
		if(address.getModel() == null)
			throw new IllegalArgumentException("<" + XREPOSITORYCOMMAND_ELEMENT + ">@"
			        + MODELID_ATTRIBUTE + " is missing");
		
		ChangeType type = getTypeAttribute(xml, XREPOSITORYCOMMAND_ELEMENT);
		long rev = getRevision(xml, XREPOSITORYCOMMAND_ELEMENT, type != ChangeType.ADD);
		
		Iterator<MiniElement> it = xml.getElements();
		if(it.hasNext())
			throw new IllegalArgumentException("Invalid child of <" + XREPOSITORYCOMMAND_ELEMENT
			        + ">: <" + it.next().getName() + ">");
		
		XAddress target = address.getParent();
		XID modelId = address.getModel();
		
		if(type == ChangeType.ADD)
			return MemoryRepositoryCommand.createAddCommand(target, rev, modelId);
		else if(type == ChangeType.REMOVE)
			return MemoryRepositoryCommand.createRemoveCommand(target, rev, modelId);
		else
			throw new IllegalArgumentException("<" + XREPOSITORYCOMMAND_ELEMENT + ">@"
			        + TYPE_ATTRIBUTE
			        + " does not contain a valid type for repository commands, but '" + type + "'");
	}
	
	public static void toXml(XCommand command, XmlOut out, XAddress context)
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
	
	public static void toXml(XTransaction trans, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XTRANSACTION_ELEMENT);
		
		setBasicAttributes(trans, out, context);
		
		XAddress newContext = trans.getTarget();
		
		for(XAtomicCommand command : trans) {
			toXml(command, out, newContext);
		}
		
		out.close(XTRANSACTION_ELEMENT);
		
	}
	
	public static void toXml(XFieldCommand command, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XFIELDCOMMAND_ELEMENT);
		
		setCommandAttributes(command, out, context, true);
		
		if(command.getValue() != null)
			XmlValue.toXml(command.getValue(), out);
		
		out.close(XFIELDCOMMAND_ELEMENT);
		
	}
	
	public static void toXml(XObjectCommand command, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		if(context.getField() != null)
			throw new IllegalArgumentException("invalid context for object commands: " + context);
		
		out.open(XOBJECTCOMMAND_ELEMENT);
		
		setCommandAttributes(command, out, context, command.getChangeType() != ChangeType.ADD);
		
		out.attribute(FIELDID_ATTRIBUTE, command.getFieldID().toString());
		
		out.close(XOBJECTCOMMAND_ELEMENT);
		
	}
	
	public static void toXml(XModelCommand command, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		if(context.getObject() != null || context.getField() != null)
			throw new IllegalArgumentException("invalid context for model commands: " + context);
		
		out.open(XMODELCOMMAND_ELEMENT);
		
		setCommandAttributes(command, out, context, command.getChangeType() != ChangeType.ADD);
		
		out.attribute(OBJECTID_ATTRIBUTE, command.getObjectID().toString());
		
		out.close(XMODELCOMMAND_ELEMENT);
		
	}
	
	public static void toXml(XRepositoryCommand command, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		if(context.getModel() != null || context.getObject() != null || context.getField() != null)
			throw new IllegalArgumentException("invalid context for repository commands: "
			        + context);
		
		out.open(XREPOSITORYCOMMAND_ELEMENT);
		
		setCommandAttributes(command, out, context, command.getChangeType() != ChangeType.ADD);
		
		out.attribute(MODELID_ATTRIBUTE, command.getModelID().toString());
		
		out.close(XREPOSITORYCOMMAND_ELEMENT);
		
	}
	
	private static void setBasicAttributes(XCommand command, XmlOut out, XAddress context) {
		
		XID repoId = command.getTarget().getRepository();
		if(repoId != null && (context == null || !XX.equals(context.getRepository(), repoId)))
			out.attribute(REPOSITORYID_ATTRIBUTE, repoId.toString());
		
		XID modelId = command.getTarget().getModel();
		if(modelId != null && (context == null || !XX.equals(context.getModel(), modelId)))
			out.attribute(MODELID_ATTRIBUTE, modelId.toString());
		
		XID objectId = command.getTarget().getObject();
		if(objectId != null && (context == null || !XX.equals(context.getObject(), objectId)))
			out.attribute(OBJECTID_ATTRIBUTE, objectId.toString());
		
		XID fieldId = command.getTarget().getField();
		if(fieldId != null && (context == null || !XX.equals(context.getField(), fieldId)))
			out.attribute(FIELDID_ATTRIBUTE, fieldId.toString());
		
	}
	
	private static void setCommandAttributes(XAtomicCommand command, XmlOut out, XAddress context,
	        boolean saveRevision) {
		
		out.attribute(TYPE_ATTRIBUTE, command.getChangeType().toString());
		
		setBasicAttributes(command, out, context);
		
		if(command.isForced())
			out.attribute(FORCED_ATTRIBUTE, Boolean.toString(true));
		else if(saveRevision)
			out.attribute(REVISION_ATTRIBUTE, Long.toString(command.getRevisionNumber()));
		
	}
	
}
