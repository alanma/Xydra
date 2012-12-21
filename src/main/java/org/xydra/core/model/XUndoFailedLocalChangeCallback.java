package org.xydra.core.model;

import org.xydra.base.change.XCommand;
import org.xydra.core.model.impl.memory.MemoryModel;


public class XUndoFailedLocalChangeCallback implements XLocalChangeCallback {
	private XLocalChangeCallback wrappedOriginalCallback;
	private XCommand command;
	private MemoryModel model;
	
	public XUndoFailedLocalChangeCallback(XCommand command, MemoryModel model,
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
