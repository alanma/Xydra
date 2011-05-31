package org.xydra.core.serialize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.store.access.XAccessRightDefinition;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.memory.MemoryAccessDefinition;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;


/**
 * Collection of methods to (de-)serialize ARM related objects to and from their
 * XML representation.
 * 
 * @author dscharrer
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class SerializedAccess {
	
	private static final String NAME_DEFINITIONS = "rights";
	private static final String ACCESS_ATTRIBUTE = "access";
	private static final String ACTOR_ATTRIBUTE = "actor";
	
	private static final String ALLOWED_ATTRIBUTE = "allowed";
	private static final String RESOURCE_ATTRIBUTE = "resource";
	private static final String XACCESSDEFINITION_ELEMENT = "define";
	private static final String XACCESSDEFS_ELEMENT = "arm";
	
	/**
	 * 
	 * @return The access definition represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid access definition.
	 * 
	 */
	public static XAccessRightDefinition toAccessDefinition(XydraElement xml)
	        throws IllegalArgumentException {
		
		SerializingUtils.checkElementType(xml, XACCESSDEFINITION_ELEMENT);
		
		Object actorStr = SerializingUtils.getRequiredAttribute(xml, ACTOR_ATTRIBUTE);
		XID actor = actorStr == null ? null : XX.toId(actorStr.toString());
		
		Object resourceStr = SerializingUtils.getRequiredAttribute(xml, RESOURCE_ATTRIBUTE);
		XAddress resource = XX.toAddress(resourceStr.toString());
		
		Object accessStr = SerializingUtils.getRequiredAttribute(xml, ACCESS_ATTRIBUTE);
		XID access = XX.toId(accessStr.toString());
		
		Object allowedStr = SerializingUtils.getRequiredAttribute(xml, ALLOWED_ATTRIBUTE);
		boolean allowed = SerializingUtils.toBoolean(allowedStr);
		
		return new MemoryAccessDefinition(access, resource, actor, allowed);
	}
	
	/**
	 * 
	 * @return The list of access definition represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid access definition list.
	 * 
	 */
	public static List<XAccessRightDefinition> toAccessDefinitionList(XydraElement xml)
	        throws IllegalArgumentException {
		
		SerializingUtils.checkElementType(xml, XACCESSDEFS_ELEMENT);
		
		List<XAccessRightDefinition> result = new ArrayList<XAccessRightDefinition>();
		
		Iterator<XydraElement> it = xml.getChildren(NAME_DEFINITIONS, XACCESSDEFINITION_ELEMENT);
		while(it.hasNext()) {
			result.add(toAccessDefinition(it.next()));
		}
		
		return result;
	}
	
	/**
	 * 
	 * @return The access manager represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid access manager.
	 * 
	 */
	public static XAuthorisationManager toAccessManager(XydraElement xml,
	        XGroupDatabaseWithListeners groups) throws IllegalArgumentException {
		
		SerializingUtils.checkElementType(xml, XACCESSDEFS_ELEMENT);
		
		XAuthorisationManager arm = new MemoryAuthorisationManager(groups);
		
		Iterator<XydraElement> it = xml.getChildren(NAME_DEFINITIONS, XACCESSDEFINITION_ELEMENT);
		while(it.hasNext()) {
			XAccessRightDefinition def = toAccessDefinition(it.next());
			arm.getAuthorisationDatabase().setAccess(def.getActor(), def.getResource(),
			        def.getAccess(), def.isAllowed());
		}
		
		return arm;
	}
	
	/**
	 * Encode the given {@link XAccessRightDefinition} list as an XML element.
	 * 
	 * @param out The XML encoder to write to.
	 */
	public static void toXml(Set<XAccessRightDefinition> defs, XydraOut out)
	        throws IllegalArgumentException {
		
		out.open(XACCESSDEFS_ELEMENT);
		
		out.child(NAME_DEFINITIONS);
		out.beginArray(XACCESSDEFINITION_ELEMENT);
		for(XAccessRightDefinition def : defs) {
			toXml(def, out);
		}
		out.endArray();
		
		out.close(XACCESSDEFS_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XAccessRightDefinition} as an XML element.
	 * 
	 * @param out The XML encoder to write to.
	 */
	public static void toXml(XAccessRightDefinition def, XydraOut out)
	        throws IllegalArgumentException {
		
		out.open(XACCESSDEFINITION_ELEMENT);
		
		out.attribute(ACCESS_ATTRIBUTE, def.getAccess());
		out.attribute(RESOURCE_ATTRIBUTE, def.getResource());
		if(def.getActor() != null) {
			out.attribute(ACTOR_ATTRIBUTE, def.getActor());
		}
		out.attribute(ALLOWED_ATTRIBUTE, def.isAllowed());
		
		out.close(XACCESSDEFINITION_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XAuthorisationManager}'s
	 * {@link XAccessRightDefinition XAccessDefinitions} as an XML element.
	 * 
	 * @param out The XML encoder to write to.
	 */
	public static void toXml(XAuthorisationManager arm, XydraOut out)
	        throws IllegalArgumentException {
		if(arm.getAuthorisationDatabase() != null) {
			toXml(arm.getAuthorisationDatabase().getDefinitions(), out);
		}
	}
	
}
