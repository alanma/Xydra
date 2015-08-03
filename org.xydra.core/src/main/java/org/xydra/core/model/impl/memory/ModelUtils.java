package org.xydra.core.model.impl.memory;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;


public class ModelUtils {

    /**
     * @param model
     * @param fieldAddress
     * @return the field or @CanBeNull
     */
    @SuppressWarnings("unchecked")
	public static <M extends XReadableModel, O extends XReadableObject, F extends XReadableField, V extends XValue> V getValue(
            final M model, final XAddress fieldAddress) {
        final O object = (O)model.getObject(fieldAddress.getObject());
        if(object == null) {
			return null;
		}
        final F field = (F)object.getField(fieldAddress.getField());
        if(field == null) {
			return null;
		}
        return (V)field.getValue();
    }

    /**
     * @param model
     * @param fieldAddress
     * @return the field or @CanBeNull
     */
    @SuppressWarnings("unchecked")
    public static <M extends XReadableModel, O extends XReadableObject, F extends XReadableField> F getField(
            final M model, final XAddress fieldAddress) {
        final O object = (O)model.getObject(fieldAddress.getObject());
        if(object == null) {
			return null;
		}
        final F field = (F)object.getField(fieldAddress.getField());
        return field;
    }

    @SuppressWarnings("unchecked")
    public static <M extends XReadableModel, O extends XReadableObject> O getObject(final M model,
            final XAddress objectAddress) {
        final O object = (O)model.getObject(objectAddress.getObject());
        return object;
    }

    @SuppressWarnings("unused")
    public static void main(final String[] args) {
        final SimpleModel sm = new SimpleModel(Base.resolveModel(Base.toId("r1"), Base.toId("m1")));
        final XRevWritableField field = sm.createObject(Base.toId("o1")).createField(Base.toId("f1"));

        final SimpleField field2 = getField(sm, Base.toAddress("/r1/m1/o1/f1"));
        assert field == field2;
        System.out.println("Done");

        final SimpleField field3 = getField(sm, Base.toAddress("/r1/m1/o1/f2"));
        final SimpleField field4 = getField(sm, Base.toAddress("/r1/m1/o2/f1"));
        final SimpleField field5 = getField(sm, Base.toAddress("/r1/m2/o1/f1"));
    }

}
