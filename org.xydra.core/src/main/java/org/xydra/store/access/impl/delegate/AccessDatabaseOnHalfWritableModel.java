package org.xydra.store.access.impl.delegate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.base.HalfWritableUtils;
import org.xydra.base.XAddress;
import org.xydra.base.XHalfWritableField;
import org.xydra.base.XHalfWritableModel;
import org.xydra.base.XHalfWritableObject;
import org.xydra.base.XID;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.access.impl.memory.MemoryAccessDefinition;
import org.xydra.index.query.Pair;
import org.xydra.store.MAXTodo;
import org.xydra.store.NamingUtils;
import org.xydra.store.access.XAccessDatabase;
import org.xydra.store.access.XAccessDefinition;
import org.xydra.store.access.XAccessValue;


/**
 * Wraps a XydraStore model to store access right definitions.
 * 
 * Naive implementation on top of a {@link XHalfWritableModel}. All writes are
 * executed immediately.
 * 
 * Is {@link Serializable} so that it can, e.g., be stored in GAE MemCache.
 * 
 * See {@link Documentation_AccessModel} for the mapping from internal data
 * structures to Xydra layout (repo/model/object/field).
 * 
 * TODO refresh from persistence if revNr is no longer fresh -- see
 * {@link AccountModelWrapperOnPersistence}
 * 
 * @author voelkel
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
@MAXTodo
public class AccessDatabaseOnHalfWritableModel implements XAccessDatabase, Serializable {
	
	private static final long serialVersionUID = 3858107275113200924L;
	
	private XHalfWritableModel rightsModel;
	
	/**
	 * @param persistence
	 * @param internalActorId used to create commands
	 * @param modelId
	 */
	public AccessDatabaseOnHalfWritableModel(XHalfWritableModel rightsModel) {
		this.rightsModel = rightsModel;
	}
	
	@Override
	public XAccessValue getAccessDefinition(XID actor, XAddress resource, XID access)
	        throws IllegalArgumentException {
		return valueToAccessValue(HalfWritableUtils.getValue(this.rightsModel, actor,
		        toFieldId(resource, access)));
	}
	
	private XAccessValue valueToAccessValue(XValue value) {
		if(value == null) {
			return XAccessValue.UNDEFINED;
		}
		if(((XBooleanValue)value).contents()) {
			return XAccessValue.ALLOWED;
		} else {
			return XAccessValue.DENIED;
		}
	}
	
	private XValue booleanToValue(boolean access) {
		return X.getValueFactory().createBooleanValue(access);
	}
	
	@Override
	public Set<XAccessDefinition> getDefinitions() {
		Set<XAccessDefinition> defs = new HashSet<XAccessDefinition>();
		// each objectId is an actorId
		for(XID actorId : this.rightsModel) {
			defs.addAll(getDefinitionsFor(actorId));
		}
		return defs;
	}
	
	/**
	 * @param actorId
	 * @return all {@link XAccessDefinition} defined for actorId
	 */
	public Set<XAccessDefinition> getDefinitionsFor(XID actorId) {
		Set<XAccessDefinition> defs = new HashSet<XAccessDefinition>();
		XHalfWritableObject actorObject = this.rightsModel.getObject(actorId);
		for(XID fieldId : actorObject) {
			XHalfWritableField field = actorObject.getField(fieldId);
			XValue value = field.getValue();
			if(value != null) {
				// parse fieldId
				Pair<XAddress,XID> pair = fromFieldId(fieldId);
				XAddress resource = pair.getFirst();
				XID access = pair.getSecond();
				boolean allowed = ((XBooleanValue)value).contents();
				MemoryAccessDefinition mad = new MemoryAccessDefinition(access, resource, actorId,
				        allowed);
				defs.add(mad);
			}
		}
		return defs;
	}
	
	@Override
	public boolean isAccessDefined(XID actor, XAddress resource, XID access) {
		return HalfWritableUtils.getValue(this.rightsModel, actor, toFieldId(resource, access)) != null;
	}
	
	@Override
	public void resetAccess(XID actor, XAddress resource, XID access) {
		HalfWritableUtils.removeValue(this.rightsModel, actor, toFieldId(resource, access));
	}
	
	private static final XID toFieldId(XAddress resource, XID access) {
		return XX.toId(NamingUtils.encode(resource) + NamingUtils.ENCODING_SEPARATOR
		        + NamingUtils.encode(access));
	}
	
	/**
	 * Parse fieldId to {@link XAddress} resource, {@link XID} access.
	 * 
	 * @return XAddress resource, XID access
	 */
	private static final Pair<XAddress,XID> fromFieldId(XID fieldId) {
		String[] parts = fieldId.toString().split("_\\.");
		return new Pair<XAddress,XID>(NamingUtils.decodeXAddress(parts[0]),
		        NamingUtils.decodeXid(parts[1]));
	}
	
	@Override
	public void setAccess(XID actor, XAddress resource, XID access, boolean allowed) {
		HalfWritableUtils.setValue(this.rightsModel, actor, toFieldId(resource, access),
		        booleanToValue(allowed));
	}
	
}
