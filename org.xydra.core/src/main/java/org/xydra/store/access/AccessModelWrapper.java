package org.xydra.store.access;

import java.util.HashSet;
import java.util.Set;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.access.impl.memory.MemoryAccessDefinition;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.model.XWritableObject;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XValue;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraStore;
import org.xydra.store.base.Credentials;
import org.xydra.store.base.WritableModel;


/**
 * Wraps a XydraStore model to model groups and their members.
 * 
 * Data modelling:
 * 
 * <pre>
 * objectId | fieldId                        | value
 * ---------+--------------------------------+----------
 * actorId  | "enc(address)+"_"+enc(rightId) | boolean
 * </pre>
 * 
 * Rights can be READ, WRITE, ADMIN.
 * 
 * Non-eisting field: right not defined.
 * 
 * @author voelkel
 */
public class AccessModelWrapper implements XAccessDatabase {
	
	private static final Logger log = LoggerFactory.getLogger(AccessModelWrapper.class);
	
	public static final XID hasMember = XX.toId("hasMember");
	public static final XID isMemberOf = XX.toId("isMemberOf");
	
	private static final long serialVersionUID = 3858107275113200924L;
	
	private static final String NULL_ENCODED = "_N";
	
	private static final String SEPARATOR = "_.";
	
	/**
	 * @param address
	 * @return
	 */
	public static String encode(XAddress address) {
		if(address == null) {
			return "_N";
		} else {
			return encode(address.getRepository()) + SEPARATOR + encode(address.getModel())
			        + SEPARATOR + encode(address.getObject()) + SEPARATOR
			        + encode(address.getField());
		}
	}
	
	public static String encode(XID xid) {
		if(xid == null) {
			return NULL_ENCODED;
		} else {
			String enc = xid.toURI();
			enc.replace("_", "__");
			// now we can safely use "_." as a separator
			return enc;
		}
	}
	
	public static XID decodeXid(String encodedXid) {
		if(encodedXid.equals(NULL_ENCODED)) {
			return null;
		} else {
			String dec = encodedXid.replace("__", "_");
			return XX.toId(dec);
		}
	}
	
	public static XAddress decodeXAddress(String encodedXAddress) {
		if(encodedXAddress.equals(NULL_ENCODED)) {
			return null;
		} else {
			String[] encParts = encodedXAddress.split("_\\.");
			if(encParts.length != 4) {
				throw new IllegalArgumentException("Encoded address consits not of four parts: "
				        + encodedXAddress);
			}
			return X.getIDProvider().fromComponents(decodeXid(encParts[0]), decodeXid(encParts[1]),
			        decodeXid(encParts[2]), decodeXid(encParts[3]));
		}
	}
	
	private static void setValueInObject(XWritableModel model, XID objectId, XID fieldId,
	        boolean value) {
		log.trace(objectId + " " + fieldId + " " + value + " .");
		
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			object = model.createObject(objectId);
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			field = object.createField(fieldId);
		}
		field.setValue(X.getValueFactory().createBooleanValue(value));
	}
	
	private static void removeValueInObject(XWritableModel model, XID objectId, XID fieldId) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			return;
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return;
		}
		field.setValue(null);
	}
	
	private static boolean getValue(XWritableModel model, XID objectId, XID fieldId) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			return false;
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return false;
		}
		XValue value = field.getValue();
		if(value == null) {
			return false;
		}
		return ((XBooleanValue)value).contents();
	}
	
	private static boolean hasValue(XWritableModel model, XID objectId, XID fieldId) {
		XWritableObject object = model.getObject(objectId);
		if(object == null) {
			return false;
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return false;
		}
		return field.getValue() != null;
	}
	
	// FIXME get credentials from config settings
	private Credentials credentials = new Credentials(XX.toId("__accessManager"), "TODO");
	
	private XWritableModel dataModel, indexModel;
	
	public AccessModelWrapper(XydraStore store, XID repositoryId, XID modelId) {
		this.dataModel = new WritableModel(this.credentials, store, X.getIDProvider()
		        .fromComponents(repositoryId, modelId, null, null));
		this.indexModel = new WritableModel(this.credentials, store, X.getIDProvider()
		        .fromComponents(repositoryId, XX.toId(modelId + "-index-by-resource"), null, null));
	}
	
	/**
	 * To test
	 * 
	 * @param repository
	 * @param modelId
	 */
	protected AccessModelWrapper(XRepository repository, XID modelId) {
		this.dataModel = repository.createModel(modelId);
		this.indexModel = repository.createModel(XX.toId(modelId + "-index-by-actor"));
	}
	
	@Override
	public XAccessValue getAccessDefinition(XID actor, XAddress resource, XID access)
	        throws IllegalArgumentException {
		XWritableObject object = this.dataModel.getObject(actor);
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
	public Pair<Set<XID>,Set<XID>> getActorsWithPermission(XAddress resource, XID access) {
		// TODO blocked on missing docu of this interface & needs index
		return null;
	}
	
	@Override
	public Set<XAccessDefinition> getDefinitions() {
		Set<XAccessDefinition> defs = new HashSet<XAccessDefinition>();
		for(XID actorId : this.dataModel) {
			defs.addAll(getDefinitionsFor(actorId));
		}
		return defs;
	}
	
	public Set<XAccessDefinition> getDefinitionsFor(XID actorId) {
		Set<XAccessDefinition> defs = new HashSet<XAccessDefinition>();
		XWritableObject actor = this.dataModel.getObject(actorId);
		for(XID fieldId : actor) {
			XWritableField field = actor.getField(fieldId);
			XValue value = field.getValue();
			if(value != null) {
				// parse fieldId
				Object[] o = fromFieldId(fieldId);
				XAddress resource = (XAddress)o[0];
				XID access = (XID)o[1];
				boolean allowed = ((XBooleanValue)value).contents();
				MemoryAccessDefinition mad = new MemoryAccessDefinition(access, resource, actorId,
				        allowed);
				defs.add(mad);
			}
		}
		return defs;
	}
	
	@Override
	public Pair<Set<XID>,Set<XID>> getPermissions(XID actor, XAddress resource) {
		Set<XAccessDefinition> defs = getDefinitionsFor(actor);
		
		Set<XID> allowed = new HashSet<XID>();
		Set<XID> denied = new HashSet<XID>();
		
		for(XAccessDefinition def : defs) {
			if(def.getResource().equals(resource)) {
				if(def.isAllowed()) {
					allowed.add(def.getAccess());
				} else {
					denied.add(def.getAccess());
				}
			}
		}
		return new Pair<Set<XID>,Set<XID>>(allowed, denied);
	}
	
	@Override
	public boolean isAccessDefined(XID actor, XAddress resource, XID access) {
		return hasValue(this.dataModel, actor, toFieldId(resource, access));
	}
	
	@Override
	public void resetAccess(XID actor, XAddress resource, XID access) {
		XID fieldId = toFieldId(resource, access);
		removeValueInObject(this.dataModel, actor, fieldId);
	}
	
	private static final XID toFieldId(XAddress resource, XID access) {
		return XX.toId(encode(resource) + SEPARATOR + encode(access));
	}
	
	/**
	 * @return XAddress resource, XID access
	 */
	private static final Object[] fromFieldId(XID fieldId) {
		String[] parts = fieldId.toURI().split("_\\.");
		return new Object[] { decodeXAddress(parts[0]), decodeXid(parts[1]) };
	}
	
	@Override
	public void setAccess(XID actor, XAddress resource, XID access, boolean allowed) {
		XID fieldId = XX.toId(encode(resource) + SEPARATOR + encode(access));
		setValueInObject(this.dataModel, actor, fieldId, allowed);
	}
	
}
