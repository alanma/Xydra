package org.xydra.core;

import org.xydra.base.XId;
import org.xydra.base.change.impl.memory.RevisionConstants;
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
import org.xydra.base.rmof.impl.XExistsRevWritableField;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableObject;
import org.xydra.base.rmof.impl.XExistsRevWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.base.rmof.impl.memory.SimpleRepository;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.sharedutils.XyAssert;


/**
 * A helper class containing methods for copying different kinds of Xydra
 * entities.
 *
 * @author kaidel
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
    public static void copyData(final XReadableModel sourceModel, final XWritableModel targetModel) {
        // copy model to _model
        for(final XId objectId : sourceModel) {
            final XReadableObject sourceObject = sourceModel.getObject(objectId);
            final XWritableObject targetObject = targetModel.createObject(objectId);
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
    public static void copyData(final XReadableObject sourceObject, final XWritableObject targetObject) {
        for(final XId fieldId : sourceObject) {
            final XReadableField sourceField = sourceObject.getField(fieldId);
            final XWritableField targetField = targetObject.createField(fieldId);
            copyData(sourceField, targetField);
        }
    }

    public static void copyData(final XReadableField sourceField, final XWritableField targetField) {
        final XValue sourceValue = sourceField.getValue();
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
    public static void copyData(final XReadableRepository sourceRepository,
            final XWritableRepository targetRepository) {
        // copy repository to _repository
        for(final XId modelId : sourceRepository) {
            final XReadableModel sourceModel = sourceRepository.getModel(modelId);
            final XWritableModel targetModel = targetRepository.createModel(modelId);
            copyData(sourceModel, targetModel);
        }
    }

    public static void copyDataAndRevisions(final XReadableRepository sourceRepository,
            final SimpleRepository targetRepository) {
        assert sourceRepository != null;
        targetRepository.setExists(true);
        for(final XId modelId : sourceRepository) {
            final XReadableModel model = sourceRepository.getModel(modelId);
            final XExistsRevWritableModel localModel = targetRepository.createModel(model.getId());
            copyDataAndRevisions(model, localModel);
        }
    }

    /**
     * Copy all state information from sourceModel to targetModel. Possibly
     * overwriting some data in targetModel. Existing data in targetModel is not
     * deleted.
     *
     * @param sourceModel The {@link XReadableModel} which is to be copied @NeverNull
     * @param targetModel The {@link XRevWritableModel} in which the data of
     *            sourceModel is to be pasted.
     */
    public static void copyDataAndRevisions(final XReadableModel sourceModel,
            final XExistsRevWritableModel targetModel) {
        assert sourceModel != null;
        targetModel.setRevisionNumber(sourceModel.getRevisionNumber());
        targetModel.setExists(true);
        for(final XId objectId : sourceModel) {
            final XReadableObject object = sourceModel.getObject(objectId);
            final XRevWritableObject localObject = targetModel.createObject(object.getId());
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
    public static void copyDataAndRevisions(final XReadableObject sourceObject,
            final XRevWritableObject targetObject) {
        targetObject.setRevisionNumber(sourceObject.getRevisionNumber());
        for(final XId fieldId : sourceObject) {
            final XReadableField sourceField = sourceObject.getField(fieldId);
            final XRevWritableField targetField = targetObject.createField(fieldId);
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
    public static void copyDataAndRevisions(final XReadableField sourceField,
            final XRevWritableField targetField) {
        targetField.setRevisionNumber(sourceField.getRevisionNumber());
        targetField.setValue(sourceField.getValue());
    }

    /**
     * Create a {@link XModel} with the same initial state as the given model
     * snapshot.
     *
     * @param actor
     * @param password
     * @param modelSnapshot
     * @return a copy based on a {@link MemoryModel} instance
     */
    public static XModel copyModel(final XId actor, final String password, final XReadableModel modelSnapshot) {
        final XExistsRevWritableModel modelState = createSnapshot(modelSnapshot);
        return new MemoryModel(actor, password, modelState);
    }

    /**
     * Create a XObject with the same initial state as the given model snapshot.
     *
     * @param actor
     * @param password
     * @param objectSnapshot
     * @return a copy based on a {@link MemoryObject}
     */
    public static XObject copyObject(final XId actor, final String password, final XReadableObject objectSnapshot) {
        final XRevWritableObject objectState = createSnapshot(objectSnapshot);
        return new MemoryObject(actor, password, objectState, null);
    }

    /**
     * Copy all state information from sourceModel.
     *
     * @param sourceModel The {@link XReadableModel} which is to be copied
     * @return the snapshot or null
     */
    public static XExistsRevWritableModel createSnapshot(final XReadableModel sourceModel) {
        if(sourceModel == null || sourceModel.getRevisionNumber() == RevisionConstants.NOT_EXISTING) {
            return null;
        }
        final XExistsRevWritableModel targetModel = new SimpleModel(sourceModel.getAddress());
        copyDataAndRevisions(sourceModel, targetModel);
        XyAssert.xyAssert(sourceModel.getRevisionNumber() == targetModel.getRevisionNumber());
        return targetModel;
    }

    /**
     * Copy all state information from sourceObject.
     *
     * @param sourceObject The {@link XReadableObject} which is to be copied
     * @return a copy based on a new {@link SimpleObject} instance
     */
    public static XExistsRevWritableObject createSnapshot(final XReadableObject sourceObject) {
        if(sourceObject == null) {
            return null;
        }
        final XExistsRevWritableObject targetObject = new SimpleObject(sourceObject.getAddress());
        copyDataAndRevisions(sourceObject, targetObject);
        return targetObject;
    }

    /**
     * Copy all state information from sourceField.
     *
     * @param sourceField The {@link XReadableField} which is to be copied
     * @return a copy based on a {@link SimpleField} instance
     */
    public static XExistsRevWritableField createSnapshot(final XReadableField sourceField) {
        if(sourceField == null) {
            return null;
        }
        final XExistsRevWritableField targetField = new SimpleField(sourceField.getAddress());
        copyDataAndRevisions(sourceField, targetField);
        return targetField;
    }

    /**
     * @param repository @NeverNull
     * @return @NeverNull
     */
    public static XExistsRevWritableRepository cloneRepository(final XReadableRepository repository) {
        final SimpleRepository targetRepository = new SimpleRepository(repository.getAddress());
        copyDataAndRevisions(repository, targetRepository);
        return targetRepository;
    }

}
