package org.xydra.core;

import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;


/**
 * A helper class containing methods for copying different kinds of Xydra
 * entities.
 * 
 * @author Kaidel
 * @author dscharrer
 * 
 */
public class XCopyUtils {
	
	/**
	 * Copy all state information from sourceModel to targetModel. Possibly
	 * overwriting some data in targetModel. Existing data in targetModel is not
	 * deleted.
	 * 
	 * @param sourceModel The {@link XReadableModel} which is to be copied
	 * @param targetModel The {@link XWritableModel} in which the data of
	 *            sourceModel is to be pasted.
	 */
	public static void copyData(XReadableModel sourceModel, XWritableModel targetModel) {
		// copy model to _model
		for(XID objectId : sourceModel) {
			XReadableObject sourceObject = sourceModel.getObject(objectId);
			XWritableObject targetObject = targetModel.createObject(objectId);
			copyData(sourceObject, targetObject);
		}
	}
	
	/**
	 * Copy all state information from sourceObject to targetObject. Possibly
	 * overwriting some data in targetObject. Existing data in targetObject is
	 * not deleted.
	 * 
	 * @param sourceObject The {@link XReadableObject} which is to be copied
	 * @param targetObject The {@link XWritableObject} in which the data of
	 *            sourceObject is to be pasted.
	 */
	public static void copyData(XReadableObject sourceObject, XWritableObject targetObject) {
		for(XID fieldId : sourceObject) {
			XReadableField sourceField = sourceObject.getField(fieldId);
			XWritableField targetField = targetObject.createField(fieldId);
			copyData(sourceField, targetField);
		}
	}
	
	public static void copyData(XReadableField sourceField, XWritableField targetField) {
		XValue sourceValue = sourceField.getValue();
		targetField.setValue(sourceValue);
	}
	
	/**
	 * Copy all state information from sourceModel to targetModel. Possibly
	 * overwriting some data in targetModel. Existing data in targetModel is not
	 * deleted.
	 * 
	 * @param sourceRepository The {@link XReadableRepository} which is to be
	 *            copied
	 * @param targetRepository The {@link XWritableRepository} in which the data
	 *            of sourceRepository is to be pasted.
	 */
	public static void copyData(XReadableRepository sourceRepository,
	        XWritableRepository targetRepository) {
		// copy repository to _repository
		for(XID modelId : sourceRepository) {
			XReadableModel sourceModel = sourceRepository.getModel(modelId);
			XWritableModel targetModel = targetRepository.createModel(modelId);
			copyData(sourceModel, targetModel);
		}
	}
	
	/**
	 * Copy all state information from sourceModel to targetModel. Possibly
	 * overwriting some data in targetModel. Existing data in targetModel is not
	 * deleted.
	 * 
	 * @param sourceModel The {@link XReadableModel} which is to be copied
	 * @param targetModel The {@link XRevWritableModel} in which the data of
	 *            sourceModel is to be pasted.
	 */
	public static void copyDataAndRevisions(XReadableModel sourceModel,
	        XRevWritableModel targetModel) {
		targetModel.setRevisionNumber(sourceModel.getRevisionNumber());
		for(XID objectId : sourceModel) {
			XReadableObject object = sourceModel.getObject(objectId);
			XRevWritableObject localObject = targetModel.createObject(object.getID());
			copyDataAndRevisions(object, localObject);
		}
	}
	
	/**
	 * Copy all state information from sourceObject to targetObject. Possibly
	 * overwriting some data in targetObject. Existing data in targetObject is
	 * not deleted.
	 * 
	 * @param sourceObject The {@link XReadableObject} which is to be copied
	 * @param targetObject The {@link XRevWritableObject} in which the data of
	 *            sourceObject is to be pasted.
	 */
	public static void copyDataAndRevisions(XReadableObject sourceObject,
	        XRevWritableObject targetObject) {
		targetObject.setRevisionNumber(sourceObject.getRevisionNumber());
		for(XID fieldId : sourceObject) {
			XReadableField sourceField = sourceObject.getField(fieldId);
			XRevWritableField targetField = targetObject.createField(fieldId);
			copyDataAndRevisions(sourceField, targetField);
		}
	}
	
	/**
	 * Copy all state information from sourceField to targetField. Possibly
	 * targetField some data in targetObject. Existing data in targetObject is
	 * not deleted.
	 * 
	 * @param sourceField The {@link XReadableField} which is to be copied
	 * @param targetField The {@link XRevWritableField} in which the data of
	 *            sourceField is to be pasted.
	 */
	public static void copyDataAndRevisions(XReadableField sourceField,
	        XRevWritableField targetField) {
		targetField.setRevisionNumber(sourceField.getRevisionNumber());
		targetField.setValue(sourceField.getValue());
	}
	
	/**
	 * Create a {@link XModel} with the same initial state as the given model
	 * snapshot.
	 */
	public static XModel copyModel(XID actor, String password, XReadableModel modelSnapshot) {
		XRevWritableModel modelState = createSnapshot(modelSnapshot);
		return new MemoryModel(actor, password, modelState);
	}
	
	/**
	 * Create a XObject with the same initial state as the given model snapshot.
	 */
	public static XObject copyObject(XID actor, String password, XReadableObject objectSnapshot) {
		XRevWritableObject objectState = createSnapshot(objectSnapshot);
		return new MemoryObject(actor, password, objectState);
	}
	
	/**
	 * Copy all state information from sourceModel.
	 * 
	 * @param sourceModel The {@link XReadableModel} which is to be copied
	 * @return the snapshot or null
	 */
	public static XRevWritableModel createSnapshot(XReadableModel sourceModel) {
		if(sourceModel == null) {
			return null;
		}
		XRevWritableModel targetModel = new SimpleModel(sourceModel.getAddress());
		copyDataAndRevisions(sourceModel, targetModel);
		assert sourceModel.getRevisionNumber() == targetModel.getRevisionNumber();
		return targetModel;
	}
	
	/**
	 * Copy all state information from sourceObject.
	 * 
	 * @param sourceObject The {@link XReadableObject} which is to be copied
	 */
	public static XRevWritableObject createSnapshot(XReadableObject sourceObject) {
		if(sourceObject == null) {
			return null;
		}
		XRevWritableObject targetObject = new SimpleObject(sourceObject.getAddress());
		copyDataAndRevisions(sourceObject, targetObject);
		return targetObject;
	}
	
	/**
	 * Copy all state information from sourceField.
	 * 
	 * @param sourceField The {@link XReadableField} which is to be copied
	 */
	public static XRevWritableField createSnapshot(XReadableField sourceField) {
		if(sourceField == null) {
			return null;
		}
		XRevWritableField targetField = new SimpleField(sourceField.getAddress());
		copyDataAndRevisions(sourceField, targetField);
		return targetField;
	}
	
}
