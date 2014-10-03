package org.xydra.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.index.iterator.AbstractTransformingIterator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

abstract public class XCollectionEditor<E extends XValue, V extends XCollectionValue<?>> extends
		XValueEditor implements XValueEditor.EditListener {

	private final EditListener listener;
	private final FlowPanel list = new FlowPanel();
	private final List<AtomicXValueEditor<E>> editors = new ArrayList<AtomicXValueEditor<E>>();
	protected XAddress dummyAddress = XX.toAddress("/a/-/-/-");

	public XCollectionEditor(EditListener listener) {
		super();
		this.listener = listener;

		initWidget(this.list);

		setStyleName("editor-xvalue");
		addStyleName("editor-xvalue-list");
	}

	public void add(final AtomicXValueEditor<E> editor) {
		final HorizontalPanel entry = new HorizontalPanel();
		entry.add(editor);
		Button remove = new Button("-");
		entry.add(remove);
		remove.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				remove(entry, editor);
			}
		});
		this.list.add(entry);
		this.editors.add(editor);
	}

	protected void remove(HorizontalPanel entry, AtomicXValueEditor<E> editor) {
		this.list.remove(entry);
		this.editors.remove(editor);
		changed();
	}

	@Override
	public void newValue(XValue value) {
		changed();
	}

	@Override
	public V getValue() {
		return asCollectionValue(new AbstractTransformingIterator<AtomicXValueEditor<E>, E>(
				this.editors.iterator()) {
			@Override
			public E transform(AtomicXValueEditor<E> in) {
				return in.getValue();
			}
		});
	}

	protected EditListener getListenerForEntry() {
		return this.listener == null ? null : this;
	}

	protected void changed() {
		if (this.listener != null) {
			this.listener.newValue(getValue());
		}
	}

	abstract protected V asCollectionValue(Iterator<E> entries);

	abstract public void add();

}
