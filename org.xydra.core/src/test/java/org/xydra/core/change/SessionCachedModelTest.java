package org.xydra.core.change;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.XX;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


public class SessionCachedModelTest {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(SessionCachedModelTest.class);
    private final XAddress MODELADDRESS = XX.toAddress(XX.toId("repo1"), XX.toId("testModel"),
            null, null);

    public static final XId ACTOR = XX.toId("_SessionCachedModelTest");

    @Test
    public void testDiscardChanges() {

        final XRepository xr = new MemoryRepository(ACTOR, "pass", Base.toId("repo1"));
        DemoModelUtil.addPhonebookModel(xr);
        final XModel demoModel = xr.getModel(DemoModelUtil.PHONEBOOK_ID);

        final SessionCachedModel model = new SessionCachedModel(this.MODELADDRESS);
        model.indexModel(demoModel);

        /* phase 1: check for object changes */
        final XId newObjectId = Base.toId("addedObject");
        model.createObject(newObjectId);

        model.removeObject(DemoModelUtil.CLAUDIA_ID);
        model.removeObject(DemoModelUtil.PETER_ID);

        int count = countItems(model);

        Assert.assertEquals(2, count);

        model.discardAllChanges();

        count = countItems(model);

        Assert.assertEquals(3, count);
        Assert.assertTrue(model.hasObject(DemoModelUtil.CLAUDIA_ID));
        Assert.assertTrue(model.hasObject(DemoModelUtil.JOHN_ID));
        Assert.assertTrue(model.hasObject(DemoModelUtil.PETER_ID));

        /* phase 2: check for field changes */
        final XId newFieldID = Base.toId("newField");
        final XWritableObject existingObject = model.getObject(DemoModelUtil.JOHN_ID);

        int initialFieldCount = 0;
        final HashSet<XId> fields = new HashSet<XId>();
        for(final XId xid : existingObject) {
            fields.add(xid);
            initialFieldCount++;
        }

        for(final XId fieldID : fields) {
            existingObject.removeField(fieldID);
        }

        existingObject.createField(newFieldID);
        count = countItems(existingObject);
        Assert.assertEquals(1, count);

        model.discardAllChanges();
        count = countItems(existingObject);
        Assert.assertEquals(initialFieldCount, count);

        /* phase 3: check for value changes */
        final XWritableField existingNullField = existingObject.getField(DemoModelUtil.EMPTYFIELD_ID);
        final XWritableField existingStringField = existingObject.getField(DemoModelUtil.PHONE_ID);

        existingNullField.setValue(XV.toValue(true));
        final XValue stringFieldValue = existingStringField.getValue();
        existingStringField.setValue(XV.toValue(stringFieldValue + "1"));

        model.discardAllChanges();

        Assert.assertEquals(stringFieldValue, existingStringField.getValue());
        Assert.assertNull(null, existingNullField.getValue());

        /* phase 4: check changed and then removed */
        existingObject.createField(newFieldID);
        model.removeObject(existingObject.getId());

        Assert.assertTrue(!model.hasObject(existingObject.getId()));

        model.discardAllChanges();

        Assert.assertTrue(!existingObject.hasField(newFieldID));
    }

    private static int countItems(@SuppressWarnings("rawtypes") final Iterable model) {
        int count = 0;
        for(@SuppressWarnings("unused") final
        Object xid : model) {
            count++;
        }
        return count;
    }
}
