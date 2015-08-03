package org.xydra.base;

import java.util.Collection;
import java.util.LinkedList;

import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.value.XValue;
import org.xydra.sharedutils.XyAssert;


public class WritableUtils {

    public static void deleteAllModels(final XWritableRepository repository) {
        // if the model.iterator() would support remove(), document it and
        // change this code
        final Collection<XId> toBeDeleted = new LinkedList<XId>();
        for(final XId xid : repository) {
            toBeDeleted.add(xid);
        }
        for(final XId xid : toBeDeleted) {
            repository.removeModel(xid);
        }
    }

    public static void deleteAllObjects(final XWritableModel model) {
        // if the model.iterator() would support remove(), document it and
        // change this code
        final Collection<XId> toBeDeleted = new LinkedList<XId>();
        for(final XId xid : model) {
            toBeDeleted.add(xid);
        }
        for(final XId xid : toBeDeleted) {
            model.removeObject(xid);
        }
    }

    public static XValue getValue(final XWritableModel model, final XId objectId, final XId fieldId) {
        final XWritableObject object = model.getObject(objectId);
        if(object == null) {
            return null;
        }
        return getValue(object, fieldId);
    }

    public static XValue getValue(final XWritableObject object, final XId fieldId) {
        final XWritableField field = object.getField(fieldId);
        if(field == null) {
            return null;
        }
        return field.getValue();
    }

    public static XValue getValue(final XWritableRepository repository, final XId modelId, final XId objectId,
            final XId fieldId) {
        final XWritableModel model = repository.getModel(modelId);
        if(model == null) {
            return null;
        }
        return getValue(model, objectId, fieldId);
    }

    public static void removeValue(final XWritableModel model, final XId objectId, final XId fieldId) {
        final XWritableObject object = model.getObject(objectId);
        if(object == null) {
            return;
        }
        removeValue(object, fieldId);
    }

    public static void removeValue(final XWritableObject object, final XId fieldId) {
        final XWritableField field = object.getField(fieldId);
        if(field == null) {
            return;
        }
        field.setValue(null);
    }

    public static void removeValue(final XWritableRepository repository, final XId modelId, final XId objectId,
            final XId fieldId) {
        final XWritableModel model = repository.getModel(modelId);
        if(model == null) {
            return;
        }
        removeValue(model, objectId, fieldId);
    }

    public static boolean setValue(final XWritableModel model, final XId objectId, final XId fieldId, final XValue value) {
        XWritableObject object = model.getObject(objectId);
        boolean changed = false;
        if(object == null) {
            object = model.createObject(objectId);
            changed = true;
        }
        assert model.hasObject(objectId) : model.getAddress() + " should have " + objectId;
        return changed | setValue(object, fieldId, value);
    }

    public static boolean setValue(final XWritableObject object, final XId fieldId, final XValue value) {
        XWritableField field = object.getField(fieldId);
        boolean changed = false;
        if(field == null) {
            field = object.createField(fieldId);
            changed = true;
        }
        XyAssert.xyAssert(object.hasField(fieldId));
        return changed | field.setValue(value);
    }

    /**
     * @param repository where to set the value
     * @param modelId not null
     * @param objectId not null
     * @param fieldId not null
     * @param value can be null
     * @return true if the operation changed something
     */
    public static boolean setValue(final XWritableRepository repository, final XId modelId, final XId objectId,
            final XId fieldId, final XValue value) {
        XWritableModel model = repository.getModel(modelId);
        boolean changed = false;
        if(model == null) {
            changed = true;
            model = repository.createModel(modelId);
        }
        XyAssert.xyAssert(repository.hasModel(modelId));
        return changed | setValue(model, objectId, fieldId, value);
    }
}
