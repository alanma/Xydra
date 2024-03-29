package org.xydra.store.serialize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.core.serialize.SerializingUtils;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
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
	 * @param xml
	 * @return The access definition represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid access definition.
	 *
	 */
	public static XAccessRightDefinition toAccessDefinition(final XydraElement xml)
	        throws IllegalArgumentException {

		SerializingUtils.checkElementType(xml, XACCESSDEFINITION_ELEMENT);

		final XId actor = SerializingUtils.toId(SerializingUtils.getRequiredAttribute(xml,
		        ACTOR_ATTRIBUTE));
		final XAddress resource = SerializingUtils.toAddress(SerializingUtils.getRequiredAttribute(xml,
		        RESOURCE_ATTRIBUTE));
		final XId access = SerializingUtils.toId(SerializingUtils.getRequiredAttribute(xml,
		        ACCESS_ATTRIBUTE));
		final boolean allowed = SerializingUtils.toBoolean(SerializingUtils.getRequiredAttribute(xml,
		        ALLOWED_ATTRIBUTE));

		return new MemoryAccessDefinition(access, resource, actor, allowed);
	}

	/**
	 *
	 * @param xml
	 * @return The list of access definition represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid access definition list.
	 *
	 */
	public static List<XAccessRightDefinition> toAccessDefinitionList(final XydraElement xml)
	        throws IllegalArgumentException {

		SerializingUtils.checkElementType(xml, XACCESSDEFS_ELEMENT);

		final List<XAccessRightDefinition> result = new ArrayList<XAccessRightDefinition>();

		final Iterator<XydraElement> it = xml.getChildrenByType(NAME_DEFINITIONS,
		        XACCESSDEFINITION_ELEMENT);
		while(it.hasNext()) {
			result.add(toAccessDefinition(it.next()));
		}

		return result;
	}

	/**
	 *
	 * @param xml
	 * @param groups
	 * @return The access manager represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid access manager.
	 *
	 */
	public static XAuthorisationManager toAccessManager(final XydraElement xml,
	        final XGroupDatabaseWithListeners groups) throws IllegalArgumentException {

		SerializingUtils.checkElementType(xml, XACCESSDEFS_ELEMENT);

		final XAuthorisationManager arm = new MemoryAuthorisationManager(groups);

		final Iterator<XydraElement> it = xml.getChildrenByType(NAME_DEFINITIONS,
		        XACCESSDEFINITION_ELEMENT);
		while(it.hasNext()) {
			final XAccessRightDefinition def = toAccessDefinition(it.next());
			arm.getAuthorisationDatabase().setAccess(def.getActor(), def.getResource(),
			        def.getAccess(), def.isAllowed());
		}

		return arm;
	}

	/**
	 * Encode the given {@link XAccessRightDefinition} list as an XML element.
	 *
	 * @param defs
	 *
	 * @param out The XML encoder to write to.
	 * @throws IllegalArgumentException
	 */
	public static void toXml(final Set<XAccessRightDefinition> defs, final XydraOut out)
	        throws IllegalArgumentException {

		out.open(XACCESSDEFS_ELEMENT);

		out.child(NAME_DEFINITIONS);
		out.beginArray(XACCESSDEFINITION_ELEMENT);
		for(final XAccessRightDefinition def : defs) {
			toXml(def, out);
		}
		out.endArray();

		out.close(XACCESSDEFS_ELEMENT);

	}

	/**
	 * Encode the given {@link XAccessRightDefinition} as an XML element.
	 *
	 * @param def
	 *
	 * @param out The XML encoder to write to.
	 * @throws IllegalArgumentException
	 */
	public static void toXml(final XAccessRightDefinition def, final XydraOut out)
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
	 * @param arm
	 *
	 * @param out The XML encoder to write to.
	 * @throws IllegalArgumentException
	 */
	public static void toXml(final XAuthorisationManager arm, final XydraOut out)
	        throws IllegalArgumentException {
		if(arm.getAuthorisationDatabase() != null) {
			toXml(arm.getAuthorisationDatabase().getDefinitions(), out);
		}
	}

}
