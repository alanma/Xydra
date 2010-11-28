package org.xydra.core.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.XX;
import org.xydra.core.access.XAccessManagerWithListeners;
import org.xydra.core.access.XGroupDatabaseWithListeners;
import org.xydra.core.access.impl.memory.MemoryAccessDefinition;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.store.access.XAccessDefinition;


/**
 * Collection of methods to (de-)serialize ARM related objects to and from their
 * XML representation.
 * 
 * @author dscharrer
 */
@RunsInGWT
@RunsInAppEngine
@RunsInJava
public class XmlAccess {
	
	private static final String XACCESSDEFINITION_ELEMENT = "define";
	private static final String XACCESSDEFS_ELEMENT = "arm";
	
	private static final String ACTOR_ATTRIBUTE = "actor";
	private static final String RESOURCE_ATTRIBUTE = "resource";
	private static final String ACCESS_ATTRIBUTE = "access";
	private static final String ALLOWED_ATTRIBUTE = "allowed";
	
	/**
	 * 
	 * @return The access definition represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid access definition.
	 * 
	 */
	public static XAccessDefinition toAccessDefinition(MiniElement xml)
	        throws IllegalArgumentException {
		
		XmlUtils.checkElementName(xml, XACCESSDEFINITION_ELEMENT);
		
		String actorStr = XmlUtils.getRequiredAttribbute(xml, ACTOR_ATTRIBUTE,
		        XACCESSDEFINITION_ELEMENT);
		XID actor = actorStr == null ? null : XX.toId(actorStr);
		
		String resourceStr = XmlUtils.getRequiredAttribbute(xml, RESOURCE_ATTRIBUTE,
		        XACCESSDEFINITION_ELEMENT);
		XAddress resource = XX.toAddress(resourceStr);
		
		String accessStr = XmlUtils.getRequiredAttribbute(xml, ACCESS_ATTRIBUTE,
		        XACCESSDEFINITION_ELEMENT);
		XID access = XX.toId(accessStr);
		
		String allowedStr = XmlUtils.getRequiredAttribbute(xml, ALLOWED_ATTRIBUTE,
		        XACCESSDEFINITION_ELEMENT);
		boolean allowed = Boolean.parseBoolean(allowedStr);
		
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
	public static List<XAccessDefinition> toAccessDefinitionList(MiniElement xml)
	        throws IllegalArgumentException {
		
		XmlUtils.checkElementName(xml, XACCESSDEFS_ELEMENT);
		
		List<XAccessDefinition> result = new ArrayList<XAccessDefinition>();
		
		Iterator<MiniElement> it = xml.getElements();
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
	public static XAccessManagerWithListeners toAccessManager(MiniElement xml,
	        XGroupDatabaseWithListeners groups) throws IllegalArgumentException {
		
		XmlUtils.checkElementName(xml, XACCESSDEFS_ELEMENT);
		
		XAccessManagerWithListeners arm = new MemoryAccessManager(groups);
		
		Iterator<MiniElement> it = xml.getElements();
		while(it.hasNext()) {
			XAccessDefinition def = toAccessDefinition(it.next());
			arm.setAccess(def.getActor(), def.getResource(), def.getAccess(), def.isAllowed());
		}
		
		return arm;
	}
	
	/**
	 * Encode the given {@link XAccessDefinition} as an XML element.
	 * 
	 * @param out The XML encoder to write to.
	 */
	public static void toXml(XAccessDefinition def, XmlOut out) throws IllegalArgumentException {
		
		out.open(XACCESSDEFINITION_ELEMENT);
		
		out.attribute(ACCESS_ATTRIBUTE, def.getAccess().toString());
		out.attribute(RESOURCE_ATTRIBUTE, def.getResource().toString());
		if(def.getActor() != null)
			out.attribute(ACTOR_ATTRIBUTE, def.getActor().toString());
		out.attribute(ALLOWED_ATTRIBUTE, Boolean.toString(def.isAllowed()));
		
		out.close(XACCESSDEFINITION_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XAccessDefinition} list as an XML element.
	 * 
	 * @param out The XML encoder to write to.
	 */
	public static void toXml(Set<XAccessDefinition> defs, XmlOut out)
	        throws IllegalArgumentException {
		
		out.open(XACCESSDEFS_ELEMENT);
		
		for(XAccessDefinition def : defs) {
			toXml(def, out);
		}
		
		out.close(XACCESSDEFS_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XAccessManagerWithListeners}'s
	 * {@link XAccessDefinition XAccessDefinitions} as an XML element.
	 * 
	 * @param out The XML encoder to write to.
	 */
	public static void toXml(XAccessManagerWithListeners arm, XmlOut out)
	        throws IllegalArgumentException {
		
		toXml(arm.getDefinitions(), out);
		
	}
	
}
