package org.xydra.core;

import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XBaseRepository;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;
import org.xydra.core.value.XValue;
import org.xydra.index.XI;

/**
 * A helper class containing methods for comparing Xydra entities in different ways.
 * 
 * @author Kaidel
 *
 */

public class XCompareUtils {

	/**
	 * Check if two {@link XBaseRepository}s have the same {@link XID}, the same
	 * revision and the same {@link XBaseModel}s as defined by
	 * {@link XCompareUtils#equalState(XBaseModel, XBaseModel)}.
	 * 
	 * This is similar to {@link XCompareUtils#equalTree(XBaseRepository, XBaseRepository)}
	 * but also checks the revision number.
	 * 
	 * @return true if the two {@link XBaseRepository}s have the same state.
	 */
	public static boolean equalState(XBaseRepository repoA, XBaseRepository repoB) {
		
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
			
			XBaseModel modelA = repoA.getModel(modelId);
			XBaseModel modelB = repoB.getModel(modelId);
			
			if(modelB == null) {
				return false;
			}
			
			if(!XCompareUtils.equalState(modelA, modelB)) {
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
	 * Check if two {@link XBaseModel}s have the same {@link XID}, the same
	 * revision and the same {@link XBaseObject}s as defined by
	 * {@link XCompareUtils#equalState(XBaseObject, XBaseObject)}.
	 * 
	 * This is similar to {@link XCompareUtils#equalTree(XBaseModel, XBaseModel)} but also
	 * checks the revision number.
	 * 
	 * Parent-{@link XRepository}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XBaseModel}s have the same state.
	 */
	public static boolean equalState(XBaseModel modelA, XBaseModel modelB) {
		
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
		
		if(modelA.getRevisionNumber() != modelB.getRevisionNumber()) {
			return false;
		}
		
		for(XID objectId : modelA) {
			
			XBaseObject objectA = modelA.getObject(objectId);
			XBaseObject objectB = modelB.getObject(objectId);
			
			if(objectB == null) {
				return false;
			}
			
			if(!XCompareUtils.equalState(objectA, objectB)) {
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
	 * Check if two {@link XBaseObject}s have the same {@link XID}, the same
	 * revision and the same {@link XBaseField}s as defined by
	 * {@link XCompareUtils#equalState(XBaseField, XBaseField)}.
	 * 
	 * This is similar to {@link XCompareUtils#equalTree(XBaseObject, XBaseObject)} but also
	 * checks the revision number.
	 * 
	 * Parent-{@link XBaseModel}s, if they exist, are not compared
	 * 
	 * @return true if the two {@link XBaseObject}s have the same state.
	 */
	public static boolean equalState(XBaseObject objectA, XBaseObject objectB) {
		
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
		
		if(objectA.getRevisionNumber() != objectB.getRevisionNumber()) {
			return false;
		}
		
		for(XID fieldId : objectA) {
			
			XBaseField fieldA = objectA.getField(fieldId);
			XBaseField fieldB = objectB.getField(fieldId);
			
			if(fieldB == null) {
				return false;
			}
			
			if(!XCompareUtils.equalState(fieldA, fieldB)) {
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
	 * Check if two {@link XBaseField}s have the same ID, the same revision and
	 * the same {@link XValue}.
	 * 
	 * This is similar to {@link XCompareUtils#equalTree(XBaseField, XBaseField)} but also
	 * checks the revision number.
	 * 
	 * Parent-{@link XBaseObject}s, if they exist, are not compared
	 * 
	 * @return true if the two {@link XBaseField}s have the same state.
	 */
	// 2010-10-27: used here + in several different functionality tests
	public static boolean equalState(XBaseField fieldA, XBaseField fieldB) {
		
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
		
		if(fieldA.getRevisionNumber() != fieldB.getRevisionNumber()) {
			return false;
		}
		
		return true;
	}

	/**
	 * Check if two {@link XBaseRepository}s have the same {@link XID} and the
	 * same {@link XBaseModel}s as defined by
	 * {@link XCompareUtils#equalTree(XBaseModel, XBaseModel)}.
	 * 
	 * This is similar to {@link equalState}
	 * but ignores the revision number.
	 * 
	 * Parent-{@link XBaseModel}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XBaseRepository}s represent the same tree.
	 */
	public static boolean equalTree(XBaseRepository repoA, XBaseRepository repoB) {
		
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
			
			XBaseModel modelA = repoA.getModel(modelId);
			XBaseModel modelB = repoB.getModel(modelId);
			
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
	 * Check if two {@link XBaseModel}s have the same {@link XID} and the same
	 * {@link XBaseObject}s as defined by
	 * {@link XCompareUtils#equalTree(XBaseObject, XBaseObject)}.
	 * 
	 * This is similar to {@link equalState} but
	 * ignores the revision number.
	 * 
	 * Parent-{@link XBaseRepository}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XBaseModel}s represent the same tree.
	 */
	public static boolean equalTree(XBaseModel modelA, XBaseModel modelB) {
		
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
			
			XBaseObject objectA = modelA.getObject(objectId);
			XBaseObject objectB = modelB.getObject(objectId);
			
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
	 * Check if two {@link XBaseObject}s have the same {@link XID} and the same
	 * {@link XBaseField}s as defined by
	 * {@link XCompareUtils#equalTree(XBaseField, XBaseField)}.
	 * 
	 * This is similar to {@link equalState} but
	 * ignores the revision number.
	 * 
	 * Parent-{@link XBaseModel}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XBaseObject}s represent the same subtree.
	 */
	public static boolean equalTree(XBaseObject objectA, XBaseObject objectB) {
		
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
			
			XBaseField fieldA = objectA.getField(fieldId);
			XBaseField fieldB = objectB.getField(fieldId);
			
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
	 * Check if two {@link XBaseField}s have the same {@link XID} and the same
	 * {@link XValue}.
	 * 
	 * This is similar to {@link equalState} but
	 * ignores the revision number.
	 * 
	 * Parent-{@link XBaseObject}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XBaseField}s represent the same subtree.
	 */
	public static boolean equalTree(XBaseField fieldA, XBaseField fieldB) {
		
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

}
