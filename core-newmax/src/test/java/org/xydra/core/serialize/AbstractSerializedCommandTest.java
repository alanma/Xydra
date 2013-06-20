package org.xydra.core.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.index.XI;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Test serializing {@link XValue} types to/from XML.
 * 
 * @author dscharrer
 * 
 */
abstract public class AbstractSerializedCommandTest extends AbstractSerializingTest {
    
    private static final Logger log = getLogger();
    
    private static Logger getLogger() {
        LoggerTestHelper.init();
        return LoggerFactory.getLogger(AbstractSerializedCommandTest.class);
    }
    
    private static final XAddress repo = XX.toAddress("/repo");
    private static final XAddress model = XX.toAddress("/repo/model");
    private static final XAddress object = XX.toAddress("/repo/model/object");
    private static final XAddress field = XX.toAddress("/repo/model/object/field");
    private static final XId id = XX.toId("id");
    private static final XValue value = XV.toValue(42);
    
    // field commands
    
    @Test
    public void testFieldCommandAddSafe() {
        testCommand(MemoryFieldCommand.createAddCommand(field, 30, value));
    }
    
    @Test
    public void testFieldCommandAddForced() {
        testCommand(MemoryFieldCommand.createAddCommand(field, XCommand.FORCED, value));
    }
    
    @Test
    public void testFieldCommandAddRelative() {
        testCommand(MemoryFieldCommand.createAddCommand(field, XCommand.RELATIVE_REV + 2, value));
    }
    
    @Test
    public void testFieldCommandChangeSafe() {
        testCommand(MemoryFieldCommand.createChangeCommand(field, 30, value));
    }
    
    @Test
    public void testFieldCommandChangeForced() {
        testCommand(MemoryFieldCommand.createChangeCommand(field, XCommand.FORCED, value));
    }
    
    @Test
    public void testFieldCommandChangeRelative() {
        testCommand(MemoryFieldCommand.createChangeCommand(field, XCommand.RELATIVE_REV + 2, value));
    }
    
    @Test
    public void testFieldCommandRemoveSafe() {
        testCommand(MemoryFieldCommand.createRemoveCommand(field, 30));
    }
    
    @Test
    public void testFieldCommandRemoveForced() {
        testCommand(MemoryFieldCommand.createRemoveCommand(field, XCommand.FORCED));
    }
    
    @Test
    public void testFieldCommandRemoveRelative() {
        testCommand(MemoryFieldCommand.createRemoveCommand(field, XCommand.RELATIVE_REV + 2));
    }
    
    // object commands
    
    @Test
    public void testObjectCommandAddSafe() {
        testCommand(MemoryObjectCommand.createAddCommand(object, XCommand.SAFE_STATE_BOUND, id));
    }
    
    @Test
    public void testObjectCommandAddForced() {
        testCommand(MemoryObjectCommand.createAddCommand(object, XCommand.FORCED, id));
    }
    
    @Test
    public void testObjectCommandRemoveSafe() {
        testCommand(MemoryObjectCommand.createRemoveCommand(object, 34, id));
    }
    
    @Test
    public void testObjectCommandRemoveForced() {
        testCommand(MemoryObjectCommand.createRemoveCommand(object, XCommand.FORCED, id));
    }
    
    @Test
    public void testObjectCommandRemoveRelative() {
        testCommand(MemoryObjectCommand.createRemoveCommand(object, XCommand.RELATIVE_REV + 2, id));
    }
    
    // model commands
    
    @Test
    public void testModelCommandAddSafe() {
        testCommand(MemoryModelCommand.createAddCommand(model, XCommand.SAFE_STATE_BOUND, id));
    }
    
    @Test
    public void testModelCommandAddForced() {
        testCommand(MemoryModelCommand.createAddCommand(model, XCommand.FORCED, id));
    }
    
    @Test
    public void testModelCommandRemoveSafe() {
        testCommand(MemoryModelCommand.createRemoveCommand(model, 34, id));
    }
    
    @Test
    public void testModelCommandRemoveForced() {
        testCommand(MemoryModelCommand.createRemoveCommand(model, XCommand.FORCED, id));
    }
    
    @Test
    public void testModelCommandRemoveRelative() {
        testCommand(MemoryModelCommand.createRemoveCommand(model, XCommand.RELATIVE_REV + 2, id));
    }
    
    // object commands
    
    @Test
    public void testRepositoryCommandAddSafe() {
        testCommand(MemoryRepositoryCommand.createAddCommand(repo, XCommand.SAFE_STATE_BOUND, id));
    }
    
    @Test
    public void testRepositoryCommandAddForced() {
        testCommand(MemoryRepositoryCommand.createAddCommand(repo, XCommand.FORCED, id));
    }
    
    @Test
    public void testRepositoryCommandRemoveSafe() {
        testCommand(MemoryRepositoryCommand.createRemoveCommand(repo, 34, id));
    }
    
    @Test
    public void testRepositoryCommandRemoveForced() {
        testCommand(MemoryRepositoryCommand.createRemoveCommand(repo, XCommand.FORCED, id));
    }
    
    @Test
    public void testRepositoryCommandRemoveRelative() {
        testCommand(MemoryRepositoryCommand
                .createRemoveCommand(repo, XCommand.RELATIVE_REV + 2, id));
    }
    
    // transactions
    
    @Test
    public void testBigTransaction() {
        
        XTransactionBuilder tb = new XTransactionBuilder(model);
        DemoModelUtil.setupPhonebook(model, tb, true);
        
        XTransaction trans = tb.build();
        testCommand(trans);
        
        // now also test the command list
        XydraOut out = create();
        SerializedCommand.serialize(trans.iterator(), out, null);
        assertTrue(out.isClosed());
        String data = out.getData();
        
        log.debug(data);
        
        XydraElement e = parse(data);
        List<XCommand> commandAgain = SerializedCommand.toCommandList(e, null);
        assertNotNull(commandAgain);
        XI.equalsIterator(trans.iterator(), commandAgain.iterator());
        
        // now test with a different context
        
        out = create();
        SerializedCommand.serialize(trans.iterator(), out, model);
        assertTrue(out.isClosed());
        data = out.getData();
        
        log.debug(data);
        
        e = parse(data);
        commandAgain = SerializedCommand.toCommandList(e, model);
        assertNotNull(commandAgain);
        XI.equalsIterator(trans.iterator(), commandAgain.iterator());
    }
    
    @Test
    public void testEmptyCommandList() {
        
        // now also test the command list
        XydraOut out = create();
        SerializedCommand.serialize(new NoneIterator<XCommand>(), out, null);
        assertTrue(out.isClosed());
        String data = out.getData();
        
        log.debug(data);
        
        XydraElement e = parse(data);
        List<XCommand> commandAgain = SerializedCommand.toCommandList(e, null);
        assertNotNull(commandAgain);
        assertTrue(commandAgain.isEmpty());
        
        // now test with a different context
        
        out = create();
        SerializedCommand.serialize(new NoneIterator<XCommand>(), out, model);
        assertTrue(out.isClosed());
        data = out.getData();
        
        log.debug(data);
        
        e = parse(data);
        commandAgain = SerializedCommand.toCommandList(e, model);
        assertNotNull(commandAgain);
        assertTrue(commandAgain.isEmpty());
    }
    
    // helper functions
    
    private void testCommand(XCommand command) {
        
        XydraOut out = create();
        SerializedCommand.serialize(command, out, null);
        assertTrue(out.isClosed());
        String data = out.getData();
        
        log.debug(data);
        
        XydraElement e = parse(data);
        XCommand commandAgain = SerializedCommand.toCommand(e, null);
        assertEquals(command, commandAgain);
        
        // now test with a different context
        
        out = create();
        SerializedCommand.serialize(command, out, command.getTarget());
        assertTrue(out.isClosed());
        data = out.getData();
        
        log.debug(data);
        
        e = parse(data);
        commandAgain = SerializedCommand.toCommand(e, command.getTarget());
        assertEquals(command, commandAgain);
        
    }
    
}
