package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.value.XValue;
import org.xydra.store.XydraStore;


/**
 * Uses an internal {@link XReadableField} snapshot or -- after write access --
 * an internal {@link SimpleField}.
 *
 * This class is mostly useful for testing purposes. For production use it has
 * not been tested enough and performance is expected to be rather bad.
 *
 * @author xamde
 */
@RunsInGWT(false)
public class WritableFieldOnStore extends ReadableFieldOnStore implements XWritableField,
        Serializable {

    private static final long serialVersionUID = -4510683969960714307L;

    public WritableFieldOnStore(final Credentials credentials, final XydraStore store, final XAddress address) {
        super(credentials, store, address);
    }

    @Override
    public boolean setValue(final XValue value) {
        // send change command
        XCommand command;
        if(value != null) {
            command = BaseRuntime.getCommandFactory().createAddValueCommand(this.address,
                    getRevisionNumber(), value, true);
        } else {
            command = BaseRuntime.getCommandFactory().createRemoveValueCommand(this.address,
                    getRevisionNumber(), true);
        }
        final long result = ExecuteCommandsUtils.executeCommand(this.credentials, this.store, command);
        if(result >= 0) {
            this.baseField = new SimpleField(this.address, result, value);
            return true;
        } else {
            // something went wrong
            if(result == XCommand.FAILED) {
                throw new RuntimeException("Command failed");
            } else if(result == XCommand.NOCHANGE) {
                return false;
            } else {
                throw new AssertionError("Cannot happen");
            }
        }
    }

}
