package org.xydra.base;

import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XStateReadableField;
import org.xydra.base.rmof.XStateReadableModel;
import org.xydra.base.rmof.XStateReadableObject;
import org.xydra.base.rmof.XStateReadableRepository;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.impl.memory.sync.ISyncLog;
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
	 * @param fieldA the bigger field
	 * @param fieldB the smaller field
	 * @return true if all data from fieldB is also contained in fieldA
	 */
	public static boolean containsTree(XWritableField fieldA, XWritableField fieldB) {
		return fieldB.isEmpty() || !fieldA.isEmpty();
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
		for(XId objectId : modelB) {
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
		for(XId fieldId : objectB) {
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
	 * Check if two {@link XReadableField}s have the same ID, the same revision
	 * and the same {@link XValue}.
	 * 
	 * This is similar to
	 * {@link XCompareUtils#equalTree(XStateReadableField, XStateReadableField)}
	 * but also checks the revision number.
	 * 
	 * Parent-{@link XReadableObject}s, if they exist, are not compared
	 * 
	 * @param fieldA
	 * @param fieldB
	 * 
	 * @return true if the two {@link XReadableField}s have the same state.
	 */
	// 2010-10-27: used here + in several different functionality tests
	public static boolean equalState(XReadableField fieldA, XReadableField fieldB) {
		if(equalTree(fieldA, fieldB)) {
			return true;
		}
		
		if(fieldA.getRevisionNumber() != fieldB.getRevisionNumber()) {
			log.debug("revNr differs");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableModel}s have the same {@link XId}, the same
	 * revision and the same {@link XReadableObject}s as defined by
	 * {@link XCompareUtils#equalState(XReadableObject, XReadableObject)}.
	 * 
	 * This is similar to
	 * {@link XCompareUtils#equalTree(XStateReadableModel, XStateReadableModel)}
	 * but also checks the revision number.
	 * 
	 * Parent-repositories, if they exist, are not compared.
	 * 
	 * @param modelA
	 * @param modelB
	 * 
	 * @return true if the two {@link XReadableModel}s have the same state.
	 */
	public static boolean equalState(XReadableModel modelA, XReadableModel modelB) {
		if(!equalId(modelA, modelB)) {
			log.debug("model id differs");
			return false;
		}
		
		if(modelA.getRevisionNumber() != modelB.getRevisionNumber()) {
			log.debug("model revNr differs A=" + modelA.getRevisionNumber() + " vs. B="
			        + modelB.getRevisionNumber());
			return false;
		}
		
		for(XId objectId : modelA) {
			
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
		
		for(XId objectId : modelB) {
			
			if(modelA.getObject(objectId) == null) {
				log.debug("A has no object " + objectId);
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableObject}s have the same {@link XId}, the same
	 * revision and the same {@link XReadableField}s as defined by
	 * {@link XCompareUtils#equalState(XReadableField, XReadableField)}.
	 * 
	 * This is similar to
	 * {@link XCompareUtils#equalTree(XStateReadableObject, XStateReadableObject)}
	 * but also checks the revision number.
	 * 
	 * Parent-{@link XReadableModel}s, if they exist, are not compared
	 * 
	 * @param objectA
	 * @param objectB
	 * 
	 * @return true if the two {@link XReadableObject}s have the same state.
	 */
	public static boolean equalState(XReadableObject objectA, XReadableObject objectB) {
		if(!equalId(objectA, objectB)) {
			log.debug("object id differs");
			return false;
		}
		
		if(objectA.getRevisionNumber() != objectB.getRevisionNumber()) {
			log.debug("revNr differs; a=" + objectA.getRevisionNumber() + " b="
			        + objectB.getRevisionNumber() + " equalTree?" + equalTree(objectA, objectB));
			return false;
		}
		
		for(XId fieldId : objectA) {
			
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
		
		for(XId fieldId : objectB) {
			if(objectA.getField(fieldId) == null) {
				log.debug("A has no field " + fieldId);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableRepository}s have the same {@link XId}, the
	 * same revision and the same {@link XReadableModel}s as defined by
	 * {@link XCompareUtils#equalState(XReadableModel, XReadableModel)}.
	 * 
	 * This is similar to
	 * {@link XCompareUtils#equalTree(XStateReadableRepository, XStateReadableRepository)}
	 * but also checks the revision number.
	 * 
	 * @param repoA
	 * @param repoB
	 * 
	 * @return true if the two {@link XReadableRepository}s have the same state.
	 */
	public static boolean equalState(XReadableRepository repoA, XReadableRepository repoB) {
		if(!equalId(repoA, repoB)) {
			log.debug("repo id differs");
			return false;
		}
		
		for(XId modelId : repoA) {
			
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
		
		for(XId modelId : repoB) {
			
			if(repoA.getModel(modelId) == null) {
				log.debug("A has no model " + modelId);
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XStateReadableField}s have the same {@link XId} and
	 * the same {@link XValue}.
	 * 
	 * This is similar to {@link #equalState(XReadableField, XReadableField)}
	 * but ignores the revision number.
	 * 
	 * Parent-{@link XReadableObject}s, if they exist, are not compared.
	 * 
	 * @param fieldA
	 * @param fieldB
	 * 
	 * @return true if the two {@link XStateReadableField}s represent the same
	 *         subtree.
	 */
	public static boolean equalTree(XStateReadableField fieldA, XStateReadableField fieldB) {
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
		
		if(!fieldA.getId().equals(fieldB.getId())) {
			log.debug("field Ids differ");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableModel}s have the same {@link XId} and the
	 * same {@link XReadableObject}s as defined by
	 * {@link XCompareUtils#equalTree(XStateReadableObject, XStateReadableObject)}
	 * .
	 * 
	 * This is similar to {@link #equalState(XReadableModel, XReadableModel)}
	 * ignores the revision number.
	 * 
	 * Parent-{@link XReadableRepository}s, if they exist, are not compared.
	 * 
	 * @param modelA
	 * @param modelB
	 * 
	 * @return true if the two {@link XReadableModel}s represent the same tree.
	 */
	public static boolean equalTree(XStateReadableModel modelA, XStateReadableModel modelB) {
		if(!equalId(modelA, modelB)) {
			log.debug("model ids differ");
			return false;
		}
		
		for(XId objectId : modelA) {
			
			XStateReadableObject objectA = modelA.getObject(objectId);
			XStateReadableObject objectB = modelB.getObject(objectId);
			
			if(objectB == null) {
				log.debug("B has no object " + objectId);
				return false;
			}
			
			if(!XCompareUtils.equalTree(objectA, objectB)) {
				log.debug("object " + objectId + " differs");
				return false;
			}
			
		}
		
		for(XId objectId : modelB) {
			
			if(modelA.getObject(objectId) == null) {
				log.debug("A has no object " + objectId);
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableObject}s have the same {@link XId} and the
	 * same {@link XReadableField}s as defined by
	 * {@link XCompareUtils#equalTree(XStateReadableField, XStateReadableField)}
	 * .
	 * 
	 * This is similar to {@link #equalState(XReadableObject, XReadableObject)}
	 * but ignores the revision number.
	 * 
	 * Parent-{@link XReadableModel}s, if they exist, are not compared.
	 * 
	 * @param objectA
	 * @param objectB
	 * 
	 * @return true if the two {@link XReadableObject}s represent the same
	 *         subtree.
	 */
	public static boolean equalTree(XStateReadableObject objectA, XStateReadableObject objectB) {
		if(!equalId(objectA, objectB)) {
			log.debug("object ids differ");
			return false;
		}
		
		for(XId fieldId : objectA) {
			
			XStateReadableField fieldA = objectA.getField(fieldId);
			XStateReadableField fieldB = objectB.getField(fieldId);
			
			if(fieldB == null) {
				log.debug("B has no field " + fieldId);
				return false;
			}
			
			if(!XCompareUtils.equalTree(fieldA, fieldB)) {
				log.debug("field " + fieldId + " differs");
				return false;
			}
			
		}
		
		for(XId fieldId : objectB) {
			if(objectA.getField(fieldId) == null) {
				log.debug("A has no field " + fieldId);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Check if two {@link XReadableRepository}s have the same {@link XId} and
	 * the same {@link XReadableModel}s as defined by
	 * {@link XCompareUtils#equalTree(XStateReadableModel, XStateReadableModel)}
	 * .
	 * 
	 * This is similar to
	 * {@link #equalState(XReadableRepository, XReadableRepository)} but ignores
	 * the revision number.
	 * 
	 * Parent-{@link XReadableModel}s, if they exist, are not compared.
	 * 
	 * @param repoA
	 * @param repoB
	 * 
	 * @return true if the two {@link XReadableRepository}s represent the same
	 *         tree.
	 */
	public static boolean equalTree(XStateReadableRepository repoA, XStateReadableRepository repoB) {
		if(!equalId(repoA, repoB)) {
			log.debug("repo id differs");
			return false;
		}
		
		for(XId modelId : repoA) {
			XStateReadableModel modelA = repoA.getModel(modelId);
			XStateReadableModel modelB = repoB.getModel(modelId);
			
			if(modelB == null) {
				log.debug("B has no model " + modelId);
				return false;
			}
			
			if(!XCompareUtils.equalTree(modelA, modelB)) {
				log.debug("Model " + modelId + " differs");
				return false;
			}
			
		}
		
		for(XId modelId : repoB) {
			
			if(repoA.getModel(modelId) == null) {
				log.debug("A has no model " + modelId);
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * @param a
	 * @param b
	 * @return true if both objects are null or both are not-null and have the
	 *         same id.
	 */
	public static boolean equalId(IHasXId a, IHasXId b) {
		if(a == null && b == null) {
			return true;
		}
		
		if(a == null || b == null) {
			log.debug("one of them is null, the other isn't");
			return false;
		}
		
		return a.getId().equals(b.getId());
	}
	
	/**
	 * @param modelA
	 * @param modelB
	 * @return true if both models have the same sync log
	 */
	public static boolean equalHistory(XReadableModel modelA, XReadableModel modelB) {
		boolean equalState = equalState(modelA, modelB);
		if(equalState) {
			ISyncLog syncLogA = (ISyncLog)((XSynchronizesChanges)modelA).getChangeLog();
			ISyncLog syncLogB = (ISyncLog)((XSynchronizesChanges)modelB).getChangeLog();
			
			return syncLogA.equals(syncLogB);
		} else
			return false;
	}
	
	/**
	 * @param objectA
	 * @param objectB
	 * @return true if both objects have the same sync log
	 */
	public static boolean equalHistory(XReadableObject objectA, XObject objectB) {
		boolean equalState = equalState(objectA, objectB);
		if(equalState) {
			ISyncLog syncLogA = (ISyncLog)((XSynchronizesChanges)objectA).getChangeLog();
			ISyncLog syncLogB = (ISyncLog)((XSynchronizesChanges)objectB).getChangeLog();
			
			return syncLogA.equals(syncLogB);
		} else
			return false;
	}
	
	/**
	 * @param repoA
	 * @param repoB
	 * @return true if both repos models have the same sync log
	 */
	public static boolean equalHistory(XReadableRepository repoA, XRepository repoB) {
		boolean equal = true;
		equal = equalState(repoA, repoB);
		if(!equal) {
			return false;
		}
		for(XId modelId : repoA) {
			
			XReadableModel modelA = repoA.getModel(modelId);
			XReadableModel modelB = repoB.getModel(modelId);
			
			if(!XCompareUtils.equalHistory(modelA, modelB)) {
				log.debug("Model " + modelId + " has different sync log");
				return false;
			}
			
		}
		
		return true;
	}
	
}
