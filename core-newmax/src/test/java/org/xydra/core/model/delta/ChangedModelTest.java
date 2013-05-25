package org.xydra.core.model.delta;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.core.XX;


public class ChangedModelTest {
    
    @Test
    public void testChangedModel() {
        XAddress modelAddress = XX.toAddress("/repo/model1");
        XWritableModel base = new SimpleModel(modelAddress);
        System.out.println(base.getRevisionNumber());
        ChangedModel changedModel = new ChangedModel(base);
        XWritableObject object1 = changedModel.createObject(XX.toId("object1"));
        System.out.println(object1.getRevisionNumber());
        XWritableObject object2 = changedModel.createObject(XX.toId("object2"));
        System.out.println(object2.getRevisionNumber());
        System.out.println(changedModel.getRevisionNumber());
        ChangedModel.commitTo(changedModel, base);
        System.out.println(base.getRevisionNumber());
    }
    
}
