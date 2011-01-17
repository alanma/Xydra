package org.xydra.core.access.impl.memory;

import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.model.XWritableObject;
import org.xydra.core.model.XWritableRepository;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XValue;
import org.xydra.index.query.Pair;
import org.xydra.store.MAXTodo;
import org.xydra.store.NamingUtils;
import org.xydra.store.XydraStore;
import org.xydra.store.access.XAccessDatabase;
import org.xydra.store.access.XAccessDefinition;
import org.xydra.store.access.XAccessValue;
import org.xydra.store.access.impl.delegate.AccessModelWrapperOnPersistence;
import org.xydra.store.access.impl.delegate.BooleanValueUtils;
import org.xydra.store.base.Credentials;
import org.xydra.store.base.WritableRepository;


/**
 * Wraps a {@link XydraStore} model to store access right definitions. For
 * better performance use a {@link AccessModelWrapperOnPersistence} if you can.
 * 
 * Data modelling:
 * 
 * <pre>
 * objectId | fieldId                        | value
 * ---------+--------------------------------+----------
 * actorId  | "enc(address)+"_."+enc(rightId) | boolean
 * </pre>
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
public class AccessModelWrapper implements XAccessDatabase {
	
	private static final long serialVersionUID = -7345262691858094628L;
	
	private XWritableModel wrappedModel;
	
	public AccessModelWrapper(XydraStore store, Credentials credentials, XID modelId) {
		XWritableRepository repo = new WritableRepository(credentials, store);
		this.wrappedModel = repo.createModel(modelId);
	}
	
	/**
	 * To test
	 * 
	 * @param repository
	 * @param modelId
	 */
	protected AccessModelWrapper(XRepository repository, XID modelId) {
		this.wrappedModel = repository.createModel(modelId);
	}
	
	@Override
	public XAccessValue getAccessDefinition(XID actor, XAddress resource, XID access)
	        throws IllegalArgumentException {
		XWritableObject object = this.wrappedModel.getObject(actor);
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
	
	@Override
	public Set<XAccessDefinition> getDefinitions() {
		Set<XAccessDefinition> defs = new HashSet<XAccessDefinition>();
		for(XID actorId : this.wrappedModel) {
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
		XWritableObject actorObject = this.wrappedModel.getObject(actorId);
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
	
	// @Override
	// public Pair<Set<XID>,Set<XID>> getPermissions(XID actor, XAddress
	// resource) {
	// Set<XAccessDefinition> defs = getDefinitionsFor(actor);
	//
	// Set<XID> allowed = new HashSet<XID>();
	// Set<XID> denied = new HashSet<XID>();
	//
	// for(XAccessDefinition def : defs) {
	// if(def.getResource().equals(resource)) {
	// if(def.isAllowed()) {
	// allowed.add(def.getAccess());
	// } else {
	// denied.add(def.getAccess());
	// }
	// }
	// }
	// return new Pair<Set<XID>,Set<XID>>(allowed, denied);
	// }
	
	@Override
	public boolean isAccessDefined(XID actor, XAddress resource, XID access) {
		return BooleanValueUtils.hasValue(this.wrappedModel, actor, toFieldId(resource, access));
	}
	
	@Override
	public void resetAccess(XID actor, XAddress resource, XID access) {
		XID fieldId = toFieldId(resource, access);
		BooleanValueUtils.removeValueInObject(this.wrappedModel, actor, fieldId);
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
		BooleanValueUtils.setValueInObject(this.wrappedModel, actor, fieldId, allowed);
	}
	
}
