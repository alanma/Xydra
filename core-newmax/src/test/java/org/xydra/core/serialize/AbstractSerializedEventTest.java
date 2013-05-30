package org.xydra.core.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.change.impl.memory.MemoryReversibleFieldEvent;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XModel;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Test serializing {@link XValue} types to/from XML.
 * 
 * @author dscharrer
 * 
 */
abstract public class AbstractSerializedEventTest extends AbstractSerializingTest {
    
    private static final Logger log = getLogger();
    
    private static Logger getLogger() {
        LoggerTestHelper.init();
        return LoggerFactory.getLogger(AbstractSerializedEventTest.class);
    }
    
    private static final XAddress repo = XX.toAddress("/repo");
    private static final XAddress model = XX.toAddress("/repo/model");
    private static final XAddress object = XX.toAddress("/repo/model/object");
    private static final XAddress field = XX.toAddress("/repo/model/object/field");
    private static final XId id = XX.toId("id");
    private static final XValue value = XV.toValue(42);
    private static final XValue oldValue = XV.toValue(4 * 7);
    private static final XId actor = XX.toId("actor");
    
    // field events
    
    @Test
    public void testFieldEventAdd() {
        testEvent(MemoryFieldEvent.createAddEvent(actor, field, value, 33, 23, 2, false));
    }
    
    @Test
    public void testFieldEventAddNoModelRev() {
        testEvent(MemoryFieldEvent.createAddEvent(actor, field, value, 23, 2, false));
    }
    
    @Test
    public void testFieldEventAddNoObjectRev() {
        testEvent(MemoryFieldEvent.createAddEvent(actor, field, value, 33,
                XEvent.REVISION_NOT_AVAILABLE, 2, false));
    }
    
    @Test
    public void testFieldEventAddInTrans() {
        testEvent(MemoryFieldEvent.createAddEvent(actor, field, value, 33, 23, 2, true));
    }
    
    @Test
    public void testFieldEventChange() {
        testEvent(MemoryFieldEvent.createChangeEvent(actor, field, value, 33, 23, 2, false));
    }
    
    @Test
    public void testFieldEventChangeNoModelRev() {
        testEvent(MemoryFieldEvent.createChangeEvent(actor, field, value, 23, 2, false));
    }
    
    @Test
    public void testFieldEventChangeNoObjectRev() {
        testEvent(MemoryFieldEvent.createChangeEvent(actor, field, value, 33,
                XEvent.REVISION_NOT_AVAILABLE, 2, false));
    }
    
    @Test
    public void testFieldEventChangeInTrans() {
        testEvent(MemoryFieldEvent.createChangeEvent(actor, field, value, 33, 23, 2, true));
    }
    
    @Test
    public void testFieldEventRemove() {
        testEvent(MemoryFieldEvent.createRemoveEvent(actor, field, 33, 23, 2, false, false));
    }
    
    @Test
    public void testFieldEventRemoveNoModelRev() {
        testEvent(MemoryFieldEvent.createRemoveEvent(actor, field, 23, 2, false, false));
    }
    
    @Test
    public void testFieldEventRemoveNoObjectRev() {
        testEvent(MemoryFieldEvent.createRemoveEvent(actor, field, 33,
                XEvent.REVISION_NOT_AVAILABLE, 2, false, false));
    }
    
    @Test
    public void testFieldEventRemoveInTrans() {
        testEvent(MemoryFieldEvent.createRemoveEvent(actor, field, 33, 23, 2, true, false));
    }
    
    @Test
    public void testFieldEventRemoveImplied() {
        testEvent(MemoryFieldEvent.createRemoveEvent(actor, field, 33, 23, 2, true, true));
    }
    
    // reversible field events
    
    @Test
    public void testReversibleFieldEventAdd() {
        testEvent(MemoryReversibleFieldEvent.createAddEvent(actor, field, value, 33, 23, 2, false));
    }
    
    @Test
    public void testReversibleFieldEventAddNoModelRev() {
        testEvent(MemoryReversibleFieldEvent.createAddEvent(actor, field, value, 23, 2, false));
    }
    
    @Test
    public void testReversibleFieldEventAddNoObjectRev() {
        testEvent(MemoryReversibleFieldEvent.createAddEvent(actor, field, value, 33,
                XEvent.REVISION_NOT_AVAILABLE, 2, false));
    }
    
    @Test
    public void testReversibleFieldEventAddInTrans() {
        testEvent(MemoryReversibleFieldEvent.createAddEvent(actor, field, value, 33, 23, 2, true));
    }
    
    @Test
    public void testReversibleFieldEventChange() {
        testEvent(MemoryReversibleFieldEvent.createChangeEvent(actor, field, value, 33, 23, 2,
                false));
    }
    
    @Test
    public void testReversibleFieldEventChangeNoModelRev() {
        testEvent(MemoryReversibleFieldEvent.createChangeEvent(actor, field, value, 23, 2, false));
    }
    
    @Test
    public void testReversibleFieldEventChangeNoObjectRev() {
        testEvent(MemoryReversibleFieldEvent.createChangeEvent(actor, field, value, 33,
                XEvent.REVISION_NOT_AVAILABLE, 2, false));
    }
    
    @Test
    public void testReversibleFieldEventChangeInTrans() {
        testEvent(MemoryReversibleFieldEvent
                .createChangeEvent(actor, field, value, 33, 23, 2, true));
    }
    
    @Test
    public void testReversibleFieldEventChangeOldValue() {
        testEvent(MemoryReversibleFieldEvent.createChangeEvent(actor, field, oldValue, value, 33,
                23, 2, false));
    }
    
    @Test
    public void testReversibleFieldEventRemove() {
        testEvent(MemoryReversibleFieldEvent.createRemoveEvent(actor, field, 33, 23, 2, false,
                false));
    }
    
    @Test
    public void testReversibleFieldEventRemoveNoModelRev() {
        testEvent(MemoryReversibleFieldEvent.createRemoveEvent(actor, field, 23, 2, false, false));
    }
    
    @Test
    public void testReversibleFieldEventRemoveNoObjectRev() {
        testEvent(MemoryReversibleFieldEvent.createRemoveEvent(actor, field, 33,
                XEvent.REVISION_NOT_AVAILABLE, 2, false, false));
    }
    
    @Test
    public void testReversibleFieldEventRemoveInTrans() {
        testEvent(MemoryReversibleFieldEvent
                .createRemoveEvent(actor, field, 33, 23, 2, true, false));
    }
    
    @Test
    public void testReversibleFieldEventRemoveImplied() {
        testEvent(MemoryReversibleFieldEvent.createRemoveEvent(actor, field, 33, 23, 2, true, true));
    }
    
    // object events
    
    @Test
    public void testObjectEventAdd() {
        testEvent(MemoryObjectEvent.createAddEvent(actor, object, id, 463, 34, false));
    }
    
    @Test
    public void testObjectEventAddNoModelRev() {
        testEvent(MemoryObjectEvent.createAddEvent(actor, object, id, 34, false));
    }
    
    @Test
    public void testObjectEventAddNoObjectRev() {
        testEvent(MemoryObjectEvent.createAddEvent(actor, object, id, 463,
                XEvent.REVISION_NOT_AVAILABLE, false));
    }
    
    @Test
    public void testObjectEventAddNoModelObjectRev() {
        testEvent(MemoryObjectEvent.createAddEvent(actor, object, id,
                XEvent.REVISION_NOT_AVAILABLE, false));
    }
    
    @Test
    public void testObjectEventAddInTrans() {
        testEvent(MemoryObjectEvent.createAddEvent(actor, object, id, 463, 34, true));
    }
    
    @Test
    public void testObjectEventRemove() {
        testEvent(MemoryObjectEvent.createRemoveEvent(actor, object, id, 463, 34, 20, false, false));
    }
    
    @Test
    public void testObjectEventRemoveNoModelRev() {
        testEvent(MemoryObjectEvent.createRemoveEvent(actor, object, id, 34, 20, false, false));
    }
    
    @Test
    public void testObjectEventRemoveNoObjectRev() {
        testEvent(MemoryObjectEvent.createRemoveEvent(actor, object, id, 463,
                XEvent.REVISION_NOT_AVAILABLE, 20, false, false));
    }
    
    @Test
    public void testObjectEventRemoveNoModelObjectRev() {
        testEvent(MemoryObjectEvent.createRemoveEvent(actor, object, id,
                XEvent.REVISION_NOT_AVAILABLE, 20, false, false));
    }
    
    @Test
    public void testObjectEventRemoveInTrans() {
        testEvent(MemoryObjectEvent.createRemoveEvent(actor, object, id, 463, 34, 20, true, false));
    }
    
    @Test
    public void testObjectEventRemoveImplied() {
        testEvent(MemoryObjectEvent.createRemoveEvent(actor, object, id, 463, 34, 20, true, true));
    }
    
    // model events
    
    @Test
    public void testModelEventAdd() {
        testEvent(MemoryModelEvent.createAddEvent(actor, model, id, 34, false));
    }
    
    @Test
    public void testModelEventAddInTrans() {
        testEvent(MemoryModelEvent.createAddEvent(actor, model, id, 463, true));
    }
    
    @Test
    public void testModelEventRemove() {
        testEvent(MemoryModelEvent.createRemoveEvent(actor, model, id, 463, 34, false, false));
    }
    
    @Test
    public void testModelEventRemoveInTrans() {
        testEvent(MemoryModelEvent.createRemoveEvent(actor, model, id, 34, 20, true, false));
    }
    
    @Test
    public void testModelEventRemoveImplied() {
        testEvent(MemoryModelEvent.createRemoveEvent(actor, model, id, 34, 20, true, true));
    }
    
    // repo events
    
    @Test
    public void testRepositoryEventAdd() {
        testEvent(MemoryRepositoryEvent.createAddEvent(actor, repo, id, 34, false));
    }
    
    @Test
    public void testRepositoryEventAddInTrans() {
        testEvent(MemoryRepositoryEvent.createAddEvent(actor, repo, id, 463, true));
    }
    
    @Test
    public void testRepositoryEventRemove() {
        testEvent(MemoryRepositoryEvent.createRemoveEvent(actor, repo, id, 463, false));
    }
    
    @Test
    public void testRepositoryEventRemoveInTrans() {
        testEvent(MemoryRepositoryEvent.createRemoveEvent(actor, repo, id, 34, true));
    }
    
    // transaction events
    
    @Test
    public void testBigTransactionEvent() {
        
        XModel model = new MemoryModel(actor, "-", id);
        
        XTransactionBuilder tb = new XTransactionBuilder(model.getAddress());
        DemoModelUtil.setupPhonebook(model.getAddress(), tb, false);
        
        model.executeCommand(tb.build());
        
        Iterator<XEvent> it = model.getChangeLog().getEventsSince(0);
        while(it.hasNext()) {
            testEvent(it.next());
        }
        
    }
    
    // helper functions
    
    private void testEvent(XEvent event) {
        
        XydraOut out = create();
        SerializedEvent.serialize(event, out, null);
        assertTrue(out.isClosed());
        String data = out.getData();
        
        log.debug(data);
        
        XydraElement e = parse(data);
        XEvent eventAgain = SerializedEvent.toEvent(e, null);
        assertEquals(event, eventAgain);
        
        // now test with a different context
        
        out = create();
        SerializedEvent.serialize(event, out, event.getTarget());
        assertTrue(out.isClosed());
        data = out.getData();
        
        log.debug(data);
        
        e = parse(data);
        eventAgain = SerializedEvent.toEvent(e, event.getTarget());
        assertEquals(event, eventAgain);
        
    }
    
}
