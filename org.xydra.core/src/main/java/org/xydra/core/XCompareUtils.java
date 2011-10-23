package org.xydra.core;

import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XRepository;
import org.xydra.index.XI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * A helper class containing methods for comparing Xydra entities in different
 * ways.
 * 
 * @author Kaidel
 * 
 */
public class XCompareUtils {
	
	private static final Logger log = LoggerFactory.getLogger(XCompareUtils.class);
	
	/**
	 * Check if two {@link XReadableField}s have the same ID, the same revision
	 * and the same {@link XValue}.
	 * 
	 * This is similar to
	 * {@link XCompareUtils#equalTree(XReadableField, XReadableField)} but also
	 * checks the revision number.
	 * 
	 * Parent-{@link XReadableObject}s, if they exist, are not compared
	 * 
	 * @return true if the two {@link XReadableField}s have the same state.
	 */
	// 2010-10-27: used here + in several different functionality tests
	public static boolean equalState(XReadableField fieldA, XReadableField fieldB) {
		
		if(fieldA == null && fieldB == null) {
			return true;
		}
		
		if(fieldA == null || fieldB == null) {
			log.debug("one of them is null, the other isn't");
			return false;
		}
		
		if(!XI.equals(fieldA.getValue(), fieldB.getValue())) {
			log.debug("values differ");
			return false;
		}
		
		if(!fieldA.getID().equals(fieldB.getID())) {
			log.debug("field ids differ");
			return false;
		}
		
		if(fieldA.getRevisionNumber() != fieldB.getRevisionNumber()) {
			log.debug("rebNr differs");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableModel}s have the same {@link XID}, the same
	 * revision and the same {@link XReadableObject}s as defined by
	 * {@link XCompareUtils#equalState(XReadableObject, XReadableObject)}.
	 * 
	 * This is similar to
	 * {@link XCompareUtils#equalTree(XReadableModel, XReadableModel)} but also
	 * checks the revision number.
	 * 
	 * Parent-{@link XRepository}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XReadableModel}s have the same state.
	 */
	public static boolean equalState(XReadableModel modelA, XReadableModel modelB) {
		
		if(modelA == null && modelB == null) {
			return true;
		}
		
		if(modelA == null || modelB == null) {
			log.debug("one of them is null, the other isn't");
			return false;
		}
		
		if(!modelA.getID().equals(modelB.getID())) {
			log.debug("model id differs");
			return false;
		}
		
		if(modelA.getRevisionNumber() != modelB.getRevisionNumber()) {
			log.debug("model revNr differs A=" + modelA.getRevisionNumber() + " vs. B="
			        + modelB.getRevisionNumber());
			return false;
		}
		
		for(XID objectId : modelA) {
			
			XReadableObject objectA = modelA.getObject(objectId);
			XReadableObject objectB = modelB.getObject(objectId);
			
			if(objectB == null) {
				log.debug("B has no object " + objectId);
				return false;
			}
			
			if(!XCompareUtils.equalState(objectA, objectB)) {
				log.debug("object " + objectId + " differs");
				return false;
			}
			
		}
		
		for(XID objectId : modelB) {
			
			if(modelA.getObject(objectId) == null) {
				log.debug("A has no object " + objectId);
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableObject}s have the same {@link XID}, the same
	 * revision and the same {@link XReadableField}s as defined by
	 * {@link XCompareUtils#equalState(XReadableField, XReadableField)}.
	 * 
	 * This is similar to
	 * {@link XCompareUtils#equalTree(XReadableObject, XReadableObject)} but
	 * also checks the revision number.
	 * 
	 * Parent-{@link XReadableModel}s, if they exist, are not compared
	 * 
	 * @return true if the two {@link XReadableObject}s have the same state.
	 */
	public static boolean equalState(XReadableObject objectA, XReadableObject objectB) {
		
		if(objectA == null && objectB == null) {
			return true;
		}
		
		if(objectA == null || objectB == null) {
			log.debug("one of them is null, the other isn't");
			return false;
		}
		
		if(!objectA.getID().equals(objectB.getID())) {
			log.debug("object id differs");
			return false;
		}
		
		if(objectA.getRevisionNumber() != objectB.getRevisionNumber()) {
			log.debug("revNr differs");
			return false;
		}
		
		for(XID fieldId : objectA) {
			
			XReadableField fieldA = objectA.getField(fieldId);
			XReadableField fieldB = objectB.getField(fieldId);
			
			if(fieldB == null) {
				log.debug("B has no field " + fieldId);
				return false;
			}
			
			if(!XCompareUtils.equalState(fieldA, fieldB)) {
				log.debug("field " + fieldId + " differs");
				return false;
			}
			
		}
		
		for(XID fieldId : objectB) {
			if(objectA.getField(fieldId) == null) {
				log.debug("A has no field " + fieldId);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableRepository}s have the same {@link XID}, the
	 * same revision and the same {@link XReadableModel}s as defined by
	 * {@link XCompareUtils#equalState(XReadableModel, XReadableModel)}.
	 * 
	 * This is similar to
	 * {@link XCompareUtils#equalTree(XReadableRepository, XReadableRepository)}
	 * but also checks the revision number.
	 * 
	 * @return true if the two {@link XReadableRepository}s have the same state.
	 */
	public static boolean equalState(XReadableRepository repoA, XReadableRepository repoB) {
		
		if(repoA == null && repoB == null) {
			return true;
		}
		
		if(repoA == null || repoB == null) {
			log.debug("one of them is null, the other isn't");
			return false;
		}
		
		if(!repoA.getID().equals(repoB.getID())) {
			log.debug("repo id differs");
			return false;
		}
		
		for(XID modelId : repoA) {
			
			XReadableModel modelA = repoA.getModel(modelId);
			XReadableModel modelB = repoB.getModel(modelId);
			
			if(modelB == null) {
				log.debug("B has no model " + modelId);
				return false;
			}
			
			if(!XCompareUtils.equalState(modelA, modelB)) {
				log.debug("Model " + modelId + " differs");
				return false;
			}
			
		}
		
		for(XID modelId : repoB) {
			
			if(repoA.getModel(modelId) == null) {
				log.debug("A has no model " + modelId);
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableField}s have the same {@link XID} and the
	 * same {@link XValue}.
	 * 
	 * This is similar to {@link #equalState(XReadableField, XReadableField)}
	 * but ignores the revision number.
	 * 
	 * Parent-{@link XReadableObject}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XReadableField}s represent the same
	 *         subtree.
	 */
	public static boolean equalTree(XReadableField fieldA, XReadableField fieldB) {
		
		if(fieldA == null && fieldB == null) {
			return true;
		}
		
		// one of them is null, the other isn't
		if(fieldA == null || fieldB == null) {
			return false;
		}
		
		if(!XI.equals(fieldA.getValue(), fieldB.getValue())) {
			return false;
		}
		
		if(!fieldA.getID().equals(fieldB.getID())) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableModel}s have the same {@link XID} and the
	 * same {@link XReadableObject}s as defined by
	 * {@link XCompareUtils#equalTree(XReadableObject, XReadableObject)}.
	 * 
	 * This is similar to {@link #equalState(XReadableModel, XReadableModel)}
	 * ignores the revision number.
	 * 
	 * Parent-{@link XReadableRepository}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XReadableModel}s represent the same tree.
	 */
	public static boolean equalTree(XReadableModel modelA, XReadableModel modelB) {
		
		if(modelA == null && modelB == null) {
			return true;
		}
		
		// one of them is null, the other isn't
		if(modelA == null || modelB == null) {
			return false;
		}
		
		if(!modelA.getID().equals(modelB.getID())) {
			return false;
		}
		
		for(XID objectId : modelA) {
			
			XReadableObject objectA = modelA.getObject(objectId);
			XReadableObject objectB = modelB.getObject(objectId);
			
			if(objectB == null) {
				return false;
			}
			
			if(!XCompareUtils.equalTree(objectA, objectB)) {
				return false;
			}
			
		}
		
		for(XID objectId : modelB) {
			
			if(modelA.getObject(objectId) == null) {
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableObject}s have the same {@link XID} and the
	 * same {@link XReadableField}s as defined by
	 * {@link XCompareUtils#equalTree(XReadableField, XReadableField)}.
	 * 
	 * This is similar to {@link #equalState(XReadableObject, XReadableObject)}
	 * but ignores the revision number.
	 * 
	 * Parent-{@link XReadableModel}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XReadableObject}s represent the same
	 *         subtree.
	 */
	public static boolean equalTree(XReadableObject objectA, XReadableObject objectB) {
		
		if(objectA == null && objectB == null) {
			return true;
		}
		
		// one of them is null, the other isn't
		if(objectA == null || objectB == null) {
			return false;
		}
		
		if(!objectA.getID().equals(objectB.getID())) {
			return false;
		}
		
		for(XID fieldId : objectA) {
			
			XReadableField fieldA = objectA.getField(fieldId);
			XReadableField fieldB = objectB.getField(fieldId);
			
			if(fieldB == null) {
				return false;
			}
			
			if(!XCompareUtils.equalTree(fieldA, fieldB)) {
				return false;
			}
			
		}
		
		for(XID fieldId : objectB) {
			
			if(objectA.getField(fieldId) == null) {
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableRepository}s have the same {@link XID} and
	 * the same {@link XReadableModel}s as defined by
	 * {@link XCompareUtils#equalTree(XReadableModel, XReadableModel)}.
	 * 
	 * This is similar to
	 * {@link #equalState(XReadableRepository, XReadableRepository)} but ignores
	 * the revision number.
	 * 
	 * Parent-{@link XReadableModel}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XReadableRepository}s represent the same
	 *         tree.
	 */
	public static boolean equalTree(XReadableRepository repoA, XReadableRepository repoB) {
		
		if(repoA == null && repoB == null) {
			return true;
		}
		
		// one of them is null, the other isn't
		if(repoA == null || repoB == null) {
			return false;
		}
		
		if(!repoA.getID().equals(repoB.getID())) {
			return false;
		}
		
		for(XID modelId : repoA) {
			
			XReadableModel modelA = repoA.getModel(modelId);
			XReadableModel modelB = repoB.getModel(modelId);
			
			if(modelB == null) {
				return false;
			}
			
			if(!XCompareUtils.equalTree(modelA, modelB)) {
				return false;
			}
			
		}
		
		for(XID modelId : repoB) {
			
			if(repoA.getModel(modelId) == null) {
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * @param modelA the bigger model
	 * @param modelB the smaller model
	 * @return true if all tree-state information of modelB is also contained in
	 *         modelA. In other words: modelA must be a super-set of modelB.
	 */
	public static boolean containsTree(XWritableModel modelA, XWritableModel modelB) {
		if(modelA == null) {
			log.warn("ModelA is null");
			return modelB == null;
		}
		if(modelB == null)
			return true;
		// for every object: assert it is in A
		for(XID objectId : modelB) {
			if(!modelA.hasObject(objectId)) {
				return false;
			}
			XWritableObject objectA = modelA.getObject(objectId);
			if(!containsTree(objectA, modelB.getObject(objectId))) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean containsTree(XWritableObject objectA, XWritableObject objectB) {
		// for every field: assert it is in A
		for(XID fieldId : objectB) {
			if(!objectA.hasField(fieldId)) {
				return false;
			}
			XWritableField fieldA = objectA.getField(fieldId);
			if(!containsTree(fieldA, objectB.getField(fieldId))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param fieldA the bigger field
	 * @param fieldB the smaller field
	 * @return true if all data from fieldB is also contained in fieldA
	 */
	public static boolean containsTree(XWritableField fieldA, XWritableField fieldB) {
		return fieldB.isEmpty() || !fieldA.isEmpty();
	}
	
}
