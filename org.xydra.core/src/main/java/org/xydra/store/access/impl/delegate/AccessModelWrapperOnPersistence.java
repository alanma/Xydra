package org.xydra.store.access.impl.delegate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.access.impl.memory.MemoryAccessDefinition;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.model.XWritableObject;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XValue;
import org.xydra.index.query.Pair;
import org.xydra.store.MAXTodo;
import org.xydra.store.NamingUtils;
import org.xydra.store.access.XAccessDatabase;
import org.xydra.store.access.XAccessDefinition;
import org.xydra.store.access.XAccessValue;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * Wraps a XydraStore model to store access right definitions.
 * 
 * Naive implementation on top of a {@link XydraPersistence}. All writes are
 * executed immediately.
 * 
 * Is be {@link Serializable} so that it can be stored in GAE MemCache.
 * 
 * Data modelling:
 * 
 * <pre>
 * objectId | fieldId                        | value
 * ---------+--------------------------------+----------
 * actorId  | "enc(address)+"_."+enc(rightId) | boolean
 * </pre>
 * 
 * CAUTION: There may ONLY be actorIds be used as objectIds in this model.
 * 
 * Rights can be READ, WRITE, ADMIN.
 * 
 * Non-existing field: right not defined.
 * 
 * @author voelkel
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
@MAXTodo
public class AccessModelWrapperOnPersistence implements XAccessDatabase, Serializable {
	
	private static final long serialVersionUID = 3858107275113200924L;
	
	private XydraPersistence persistence;
	private XID internalActor;
	private XID modelId;
	
	private transient XAddress address;
	private transient XWritableModel modelSnapshot;
	
	/**
	 * @param persistence
	 * @param internalActorId used to create commands
	 * @param modelId
	 */
	public AccessModelWrapperOnPersistence(XydraPersistence persistence, XID internalActorId,
	        XID modelId) {
		this.persistence = persistence;
		this.internalActor = internalActorId;
		this.modelId = modelId;
	}
	
	@Override
	public XAccessValue getAccessDefinition(XID actor, XAddress resource, XID access)
	        throws IllegalArgumentException {
		XWritableObject object = getModelSnapshot().getObject(actor);
		if(object == null) {
			return XAccessValue.UNDEFINED;
		}
		XID fieldId = toFieldId(resource, access);
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return XAccessValue.UNDEFINED;
		}
		XValue value = field.getValue();
		if(value == null) {
			return XAccessValue.UNDEFINED;
		}
		
		if(((XBooleanValue)value).contents()) {
			return XAccessValue.ALLOWED;
		} else {
			return XAccessValue.DENIED;
		}
	}
	
	private XWritableModel getModelSnapshot() {
		if(this.modelSnapshot == null) {
			this.modelSnapshot = this.persistence.getModelSnapshot(getModelAddress());
		}
		if(this.modelSnapshot == null) {
			throw new IllegalStateException("No model found with address '" + getModelAddress()
			        + "'");
		}
		return this.modelSnapshot;
	}
	
	private XAddress getModelAddress() {
		if(this.address == null) {
			this.address = X.getIDProvider().fromComponents(this.persistence.getRepositoryId(),
			        this.modelId, null, null);
		}
		return this.address;
	}
	
	@Override
	public Set<XAccessDefinition> getDefinitions() {
		Set<XAccessDefinition> defs = new HashSet<XAccessDefinition>();
		// each objectId is an actorId
		for(XID actorId : getModelSnapshot()) {
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
		XWritableObject actorObject = getModelSnapshot().getObject(actorId);
		for(XID fieldId : actorObject) {
			XWritableField field = actorObject.getField(fieldId);
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
		return BooleanValueUtils.hasValue(getModelSnapshot(), actor, toFieldId(resource, access));
	}
	
	@Override
	public void resetAccess(XID actor, XAddress resource, XID access) {
		XID fieldId = toFieldId(resource, access);
		// write to this.persistence
		BooleanValueUtils.removeValueInObject(this.persistence, this.internalActor, this.modelId,
		        actor, fieldId);
		// write to local snapshot, if any
		if(this.modelSnapshot != null) {
			BooleanValueUtils.removeValueInObject(this.modelSnapshot, actor, fieldId);
		}
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
		XID fieldId = XX.toId(NamingUtils.encode(resource) + NamingUtils.ENCODING_SEPARATOR
		        + NamingUtils.encode(access));
		// write to this.persistence
		BooleanValueUtils.setValueInObject(this.persistence, this.internalActor, this.modelId,
		        actor, fieldId, allowed);
		// write to local snapshot, if any
		if(this.modelSnapshot != null) {
			BooleanValueUtils.setValueInObject(getModelSnapshot(), actor, fieldId, allowed);
		}
	}
	
}
