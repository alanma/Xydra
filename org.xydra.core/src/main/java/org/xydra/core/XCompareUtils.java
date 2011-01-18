package org.xydra.core;

import org.xydra.base.XReadableField;
import org.xydra.base.XReadableModel;
import org.xydra.base.XReadableObject;
import org.xydra.base.XReadableRepository;
import org.xydra.base.XID;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XRepository;
import org.xydra.index.XI;

/**
 * A helper class containing methods for comparing Xydra entities in different ways.
 * 
 * @author Kaidel
 *
 */

public class XCompareUtils {

	/**
	 * Check if two {@link XReadableRepository}s have the same {@link XID}, the same
	 * revision and the same {@link XReadableModel}s as defined by
	 * {@link XCompareUtils#equalState(XReadableModel, XReadableModel)}.
	 * 
	 * This is similar to {@link XCompareUtils#equalTree(XReadableRepository, XReadableRepository)}
	 * but also checks the revision number.
	 * 
	 * @return true if the two {@link XReadableRepository}s have the same state.
	 */
	public static boolean equalState(XReadableRepository repoA, XReadableRepository repoB) {
		
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
	 * Check if two {@link XReadableModel}s have the same {@link XID}, the same
	 * revision and the same {@link XReadableObject}s as defined by
	 * {@link XCompareUtils#equalState(XReadableObject, XReadableObject)}.
	 * 
	 * This is similar to {@link XCompareUtils#equalTree(XReadableModel, XReadableModel)} but also
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
			
			XReadableObject objectA = modelA.getObject(objectId);
			XReadableObject objectB = modelB.getObject(objectId);
			
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
	 * Check if two {@link XReadableObject}s have the same {@link XID}, the same
	 * revision and the same {@link XReadableField}s as defined by
	 * {@link XCompareUtils#equalState(XReadableField, XReadableField)}.
	 * 
	 * This is similar to {@link XCompareUtils#equalTree(XReadableObject, XReadableObject)} but also
	 * checks the revision number.
	 * 
	 * Parent-{@link XReadableModel}s, if they exist, are not compared
	 * 
	 * @return true if the two {@link XReadableObject}s have the same state.
	 */
	public static boolean equalState(XReadableObject objectA, XReadableObject objectB) {
		
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
			
			XReadableField fieldA = objectA.getField(fieldId);
			XReadableField fieldB = objectB.getField(fieldId);
			
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
	 * Check if two {@link XReadableField}s have the same ID, the same revision and
	 * the same {@link XValue}.
	 * 
	 * This is similar to {@link XCompareUtils#equalTree(XReadableField, XReadableField)} but also
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
	 * Check if two {@link XReadableRepository}s have the same {@link XID} and the
	 * same {@link XReadableModel}s as defined by
	 * {@link XCompareUtils#equalTree(XReadableModel, XReadableModel)}.
	 * 
	 * This is similar to {@link equalState}
	 * but ignores the revision number.
	 * 
	 * Parent-{@link XReadableModel}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XReadableRepository}s represent the same tree.
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
	 * Check if two {@link XReadableModel}s have the same {@link XID} and the same
	 * {@link XReadableObject}s as defined by
	 * {@link XCompareUtils#equalTree(XReadableObject, XReadableObject)}.
	 * 
	 * This is similar to {@link equalState} but
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
	 * Check if two {@link XReadableObject}s have the same {@link XID} and the same
	 * {@link XReadableField}s as defined by
	 * {@link XCompareUtils#equalTree(XReadableField, XReadableField)}.
	 * 
	 * This is similar to {@link equalState} but
	 * ignores the revision number.
	 * 
	 * Parent-{@link XReadableModel}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XReadableObject}s represent the same subtree.
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
	 * Check if two {@link XReadableField}s have the same {@link XID} and the same
	 * {@link XValue}.
	 * 
	 * This is similar to {@link equalState} but
	 * ignores the revision number.
	 * 
	 * Parent-{@link XReadableObject}s, if they exist, are not compared.
	 * 
	 * @return true if the two {@link XReadableField}s represent the same subtree.
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

}
