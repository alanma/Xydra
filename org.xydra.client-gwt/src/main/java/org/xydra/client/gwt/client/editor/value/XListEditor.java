package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.client.gwt.client.editor.value.XValueEditor.EditListener;
import org.xydra.core.value.XValue;
import org.xydra.index.iterator.AbstractTransformingIterator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;



abstract public class XListEditor extends XValueEditor implements EditListener {
	
	private final EditListener listener;
	private final FlowPanel list = new FlowPanel();
	private final List<AtomicXValueEditor> editors = new ArrayList<AtomicXValueEditor>();
	
	public XListEditor(EditListener listener) {
		super();
		this.listener = listener;
		
		initWidget(this.list);
		
		setStyleName("editor-xvalue");
		addStyleName("editor-xvalue-list");
	}
	
	public void add(final AtomicXValueEditor editor) {
		final HorizontalPanel entry = new HorizontalPanel();
		entry.add(editor);
		Button remove = new Button("-");
		entry.add(remove);
		remove.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				remove(entry, editor);
			}
		});
		this.list.add(entry);
		this.editors.add(editor);
	}
	
	protected void remove(HorizontalPanel entry, AtomicXValueEditor editor) {
		this.list.remove(entry);
		this.editors.remove(editor);
		changed();
	}
	
	public void newValue(XValue value) {
		changed();
	}
	
	@Override
	public XValue getValue() {
		return asListValue(new AbstractTransformingIterator<AtomicXValueEditor,XValue>(this.editors
		        .iterator()) {
			@Override
			public XValue transform(AtomicXValueEditor in) {
				return in.getValue();
			}
		});
	}
	
	protected void changed() {
		this.listener.newValue(getValue());
	}
	
	abstract protected XValue asListValue(Iterator<XValue> entries);
	
	abstract public void add();
	
}
