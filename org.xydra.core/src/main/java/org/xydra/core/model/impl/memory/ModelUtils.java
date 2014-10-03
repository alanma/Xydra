package org.xydra.core.model.impl.memory;

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
            M model, XAddress fieldAddress) {
        O object = (O)model.getObject(fieldAddress.getObject());
        if(object == null)
            return null;
        F field = (F)object.getField(fieldAddress.getField());
        if(field == null)
            return null;
        return (V)field.getValue();
    }
    
    /**
     * @param model
     * @param fieldAddress
     * @return the field or @CanBeNull
     */
    @SuppressWarnings("unchecked")
    public static <M extends XReadableModel, O extends XReadableObject, F extends XReadableField> F getField(
            M model, XAddress fieldAddress) {
        O object = (O)model.getObject(fieldAddress.getObject());
        if(object == null)
            return null;
        F field = (F)object.getField(fieldAddress.getField());
        return field;
    }
    
    @SuppressWarnings("unchecked")
    public static <M extends XReadableModel, O extends XReadableObject> O getObject(M model,
            XAddress objectAddress) {
        O object = (O)model.getObject(objectAddress.getObject());
        return object;
    }
    
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        SimpleModel sm = new SimpleModel(XX.resolveModel(XX.toId("r1"), XX.toId("m1")));
        XRevWritableField field = sm.createObject(XX.toId("o1")).createField(XX.toId("f1"));
        
        SimpleField field2 = getField(sm, XX.toAddress("/r1/m1/o1/f1"));
        assert field == field2;
        System.out.println("Done");
        
        SimpleField field3 = getField(sm, XX.toAddress("/r1/m1/o1/f2"));
        SimpleField field4 = getField(sm, XX.toAddress("/r1/m1/o2/f1"));
        SimpleField field5 = getField(sm, XX.toAddress("/r1/m2/o1/f1"));
    }
    
}
