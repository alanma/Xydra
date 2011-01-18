package org.xydra.meta;

import org.xydra.base.XID;
import org.xydra.base.value.XStringValue;
import org.xydra.core.X;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


/**
 * Typing should be done by using a field with a given XID (we suggest to use
 * rdf:type).
 * 
 * TODO Type names should be unique URIs, using namespace prefixes.
 * 
 * TODO Describe namespace mechanism, i.e. a way to bind 'rdf:type' to
 * 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type'
 * 
 * TODO Cardinalities 0..1, 1, 0..n, 1..n
 * 
 * @author voelkel
 * 
 */
public class MetaModel {
	
	/**
	 * For a given namespace prefix 'abc', store a field with this ID to model
	 * the namespace expansion.
	 */
	public static final XID NAMESPACE_EXPANSION = X.getIDProvider().fromString(
	        "xydra--ns--expansion");
	
	public static final XID TYPE = X.getIDProvider().fromString("rdf--type");
	
	public static final XID MIN_CARDINALITY = X.getIDProvider().fromString("owl--minCardinality");
	
	public static final XID MAX_CARDINALITY = X.getIDProvider().fromString("owl--maxCardinality");
	
	private static final XID ACTOR = X.getIDProvider().fromString(
	        MetaModel.class.getCanonicalName());
	
	private XModel metaModel, baseModel;
	
	/**
	 * @param prefix must be a valid XID string with the additional restriction
	 *            to not contain any dash ('-') characters.
	 * @return the registered
	 */
	public String getNamespaceExpansion(String prefix) {
		XObject prefixObject = this.getOrCreateAnnotationForObject(X.getIDProvider().fromString(
		        prefix), false);
		if(prefixObject == null) {
			return null;
		} else {
			XField expField = prefixObject.getField(NAMESPACE_EXPANSION);
			if(expField == null) {
				return null;
			} else {
				return ((XStringValue)expField.getValue()).contents();
			}
		}
	}
	
	/**
	 * Registers a namespace prefix/expansion pair.
	 * 
	 * @param prefix
	 * @param expansion if this is 'null' any registered mapping for the given
	 *            prefix will simply be deleted.
	 */
	public void setNamespaceExpansion(String prefix, String expansion) {
		XID id = X.getIDProvider().fromString(prefix);
		if(expansion == null) {
			// delete value
			this.metaModel.removeObject(id);
		}
		
		XObject prefixObject = this.getOrCreateAnnotationForObject(id, true);
		assert prefixObject != null;
		
		XField expField = prefixObject.createField(NAMESPACE_EXPANSION);
		expField.setValue(X.getValueFactory().createStringValue(expansion));
	}
	
	/**
	 * Convention: For every model named abc, store the meta-data in a model
	 * named abc-meta in the same repository.
	 * 
	 * @param repository
	 * @param xmodel which must be inside the given repository
	 */
	public MetaModel(XRepository repository, XModel baseModel) {
		this.baseModel = baseModel;
		XID metaModelID = X.getIDProvider().fromString(baseModel.getID().toString() + "-meta");
		this.metaModel = repository.createModel(metaModelID);
		// add built-ins
		setNamespaceExpansion("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	}
	
	/**
	 * XIDs of the format "abc--efgh" are considered to have the prefix 'abc'
	 * and the name 'efgh'
	 * 
	 * @param xidString
	 * @return the prefix part of an XID or null if there is none
	 */
	public static String getPrefix(String xidString) {
		String[] parts = xidString.split("--");
		if(parts.length == 2) {
			return parts[0];
		}
		return null;
	}
	
	/**
	 * XIDs of the format "abc--efgh" are considered to have the prefix 'abc'
	 * and the local name 'efgh'
	 * 
	 * @param xidString
	 * @return the prefix part of an XID or null if there is none
	 */
	public static String getLocalName(String xidString) {
		String[] parts = xidString.split("--");
		if(parts.length == 2) {
			return parts[1];
		}
		return null;
	}
	
	/**
	 * Return a full RDF URI from a prefix-name which has the prefix registered
	 * in the metamodel.
	 * 
	 * XIDs of the format "abc--efgh" are considered to have the prefix 'abc'
	 * and the local name 'efgh'.
	 * 
	 * @param xidString
	 * @return a complete URI if this metamodel contains a mapping for the given
	 *         prefix.
	 */
	public String toURI(String xidString) {
		String prefix = getPrefix(xidString);
		if(prefix == null) {
			return null;
		}
		String localName = getLocalName(xidString);
		if(localName == null) {
			return null;
		}
		String expansion = getNamespaceExpansion(prefix);
		String uri = expansion + localName;
		return uri;
	}
	
	/**
	 * Given a model 'phonebook' with an object 'john' and field 'email' with
	 * value 'john@example.com', we use 'phonebook' to annotate the complete
	 * model
	 */
	public XObject getOrCreateAnnotationForModel(boolean create) {
		String modelID = this.baseModel.getID().toString();
		String metaID = escapeDots(modelID);
		XID xid = X.getIDProvider().fromString(metaID);
		if(create) {
			return this.metaModel.createObject(xid);
		} else {
			return this.metaModel.getObject(xid);
		}
	}
	
	/**
	 * Given a model 'phonebook' with an object 'john' and field 'email' with
	 * value 'john@example.com', we use 'phonebook.john' to annotate the object
	 * 'john' in the 'phonebook' model
	 */
	public XObject getOrCreateAnnotationForObject(XID objectIdInBaseModel, boolean create) {
		String modelID = this.baseModel.getID().toString();
		String objectID = objectIdInBaseModel.toString();
		String metaID = escapeDots(modelID) + "." + escapeDots(objectID);
		XID xid = X.getIDProvider().fromString(metaID);
		if(create) {
			return this.metaModel.createObject(xid);
		} else {
			return this.metaModel.getObject(xid);
		}
	}
	
	/**
	 * Given a model 'phonebook' with an object 'john' and field 'email' with
	 * value 'john@example.com', we use 'phonebook.john.email' to annotate the
	 * field 'email' in the object phonebook.john.
	 * 
	 * @param objectIdInBaseModel
	 * @param fieldIdInBaseModel
	 * @param create if true, new annotations objects are created
	 * @return an XObject representing the metadata or null if created == false
	 *         and there was no such object.
	 */
	public XObject getOrCreateAnnotationForField(XID objectIdInBaseModel, XID fieldIdInBaseModel,
	        boolean create) {
		String modelID = this.baseModel.getID().toString();
		String objectID = objectIdInBaseModel.toString();
		String fieldId = fieldIdInBaseModel.toString();
		String metaID = escapeDots(modelID) + "." + escapeDots(objectID) + "."
		        + escapeDots(fieldId);
		XID xid = X.getIDProvider().fromString(metaID);
		if(create) {
			return this.metaModel.createObject(xid);
		} else {
			return this.metaModel.getObject(xid);
		}
	}
	
	/**
	 * If the XID of a model, object or field contains a dot ('.') it should be
	 * escaped as two dots ('..').
	 * 
	 * @param raw
	 * @return
	 */
	public static String escapeDots(String raw) {
		return raw.replace(".", "..");
	}
	
	public static String unescapeDots(String escaped) {
		return escaped.replace("..", ".");
	}
	
}
