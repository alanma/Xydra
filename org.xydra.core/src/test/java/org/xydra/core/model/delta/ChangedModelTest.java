package org.xydra.core.model.delta;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.XExistsWritableModel;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;


public class ChangedModelTest {

    @Test
    public void testChangedModel() {
        final XAddress modelAddress = Base.toAddress("/repo/model1");
        final XExistsWritableModel base = new SimpleModel(modelAddress);
        assertEquals(0, base.getRevisionNumber());
        final ChangedModel changedModel = new ChangedModel(base);
        final XWritableObject object1 = changedModel.createObject(Base.toId("object1"));
        assertEquals(0, object1.getRevisionNumber());
        final XWritableObject object2 = changedModel.createObject(Base.toId("object2"));
        assertEquals(0, object2.getRevisionNumber());
        assertEquals(0, changedModel.getRevisionNumber());
        assertEquals(0, base.getRevisionNumber());

        final XTransactionBuilder txnBuilder = new XTransactionBuilder(modelAddress);
        txnBuilder.addModel(modelAddress.getParent(), XCommand.FORCED, Base.toId("model1"));
        txnBuilder.addObject(modelAddress, XCommand.FORCED, Base.toId("o1"));
        txnBuilder.addObject(modelAddress, XCommand.FORCED, Base.toId("o2"));
        final XTransaction txn = txnBuilder.build();

        final ChangedModel cm = DeltaUtils.executeCommand(base, txn);
        System.out.println(cm);
    }
}
