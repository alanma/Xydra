package org.xydra.core.model;

import org.xydra.base.change.XCommand;
import org.xydra.core.model.impl.memory.IMemoryModel;


public class XUndoFailedLocalChangeCallback implements XLocalChangeCallback {
    private XLocalChangeCallback wrappedOriginalCallback;
    private XCommand command;
    private IMemoryModel model;
    
    /**
     * @return the callback that we need to call
     */
    public XLocalChangeCallback getWrappedOriginalCallback() {
        return this.wrappedOriginalCallback;
    }
    
    /**
     * @return the model in which the undo must take place
     */
    public IMemoryModel getModel() {
        return this.model;
    }
    
    /**
     * @return the command that needs to be undone
     */
    public XCommand getCommand() {
        return this.command;
    }
    
    /**
     * @param command was just executed
     * @param model
     * @param wrappedOriginalCallback
     */
    public XUndoFailedLocalChangeCallback(XCommand command, IMemoryModel model,
            XLocalChangeCallback wrappedOriginalCallback) {
        this.command = command;
        this.model = model;
        this.wrappedOriginalCallback = wrappedOriginalCallback;
    }
    
    @Override
    public void onFailure() {
        if(this.wrappedOriginalCallback != null) {
            this.wrappedOriginalCallback.onFailure();
        }
        // TODO UndoCommand
    }
    
    @Override
    public void onSuccess(long revision) {
        if(this.wrappedOriginalCallback != null) {
            this.wrappedOriginalCallback.onSuccess(revision);
        }
    }
}
