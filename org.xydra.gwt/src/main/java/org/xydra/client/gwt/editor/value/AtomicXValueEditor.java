package org.xydra.client.gwt.editor.value;

import org.xydra.core.value.XValue;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Widget;


abstract public class AtomicXValueEditor<E extends XValue> extends XValueEditor implements Command,
        ChangeHandler {
	
	private final EditListener listener;
	private boolean commandQueued;
	
	public AtomicXValueEditor(EditListener listener) {
		super();
		this.listener = listener;
	}
	
	@Override
	public void initWidget(Widget widget) {
		super.initWidget(widget);
		if(this.listener != null && widget instanceof HasChangeHandlers)
			((HasChangeHandlers)widget).addChangeHandler(this);
		
		setStyleName("editor-xvalue");
		addStyleName("editor-xvalue-atomic");
	}
	
	public void execute() {
		this.commandQueued = false;
		this.listener.newValue(getValue());
	}
	
	public void onChange(ChangeEvent e) {
		if(this.commandQueued)
			return;
		DeferredCommand.addCommand(this);
		this.commandQueued = true;
	}
	
	@Override
	public abstract E getValue();
	
}
