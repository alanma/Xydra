package org.xydra.core.model.delta;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;


public class ChangedModelTest {
    
    @Test
    public void testChangedModel() {
        XAddress modelAddress = XX.toAddress("/repo/model1");
        XWritableModel base = new SimpleModel(modelAddress);
        assertEquals(0, base.getRevisionNumber());
        ChangedModel changedModel = new ChangedModel(base);
        XWritableObject object1 = changedModel.createObject(XX.toId("object1"));
        assertEquals(0, object1.getRevisionNumber());
        XWritableObject object2 = changedModel.createObject(XX.toId("object2"));
        assertEquals(0, object2.getRevisionNumber());
        assertEquals(0, changedModel.getRevisionNumber());
        assertEquals(0, base.getRevisionNumber());
        
        XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addModel(modelAddress.getParent(), XCommand.FORCED, XX.toId("model1"));
        txnBuilder.addObject(modelAddress, XCommand.FORCED, XX.toId("o1"));
        txnBuilder.addObject(modelAddress, XCommand.FORCED, XX.toId("o2"));
        XTransaction txn = txnBuilder.build();
        
        ChangedModel cm = DeltaUtils.executeCommand(base, txn);
        System.out.println(cm);
    }
}
