package org.xydra.client.gwt.client.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xydra.client.gwt.client.editor.value.XIDEditor;
import org.xydra.client.gwt.client.editor.value.XValueEditor.EditListener;
import org.xydra.client.gwt.sync.XModelSynchronizer;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedModel;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XValue;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;



public class XModelEditor extends Composite implements XModelEventListener, XObjectEventListener,
        XFieldEventListener {
	
	private final XModelSynchronizer manager;
	private final XLoggedModel model;
	private final Label revision = new Label();
	private final VerticalPanel outer = new VerticalPanel();
	private final HorizontalPanel inner = new HorizontalPanel();
	private final Button add = new Button("Add Object");
	private final Button delete = new Button("Remove Model");
	private final DeleteCallback callback;
	private final Map<XID,XObjectEditor> objects = new HashMap<XID,XObjectEditor>();
	private final Set<XID> orphans = new HashSet<XID>();
	
	public XModelEditor(XModelSynchronizer manager, DeleteCallback callback) {
		super();
		
		this.manager = manager;
		this.model = manager.getModel();
		
		this.callback = callback;
		
		this.outer.add(this.inner);
		
		this.inner.add(new Label(this.model.getID().toString() + " ["));
		this.inner.add(this.revision);
		this.inner.add(new Label("] "));
		this.inner.add(this.add);
		this.inner.add(this.delete);
		
		this.revision.setText(Long.toString(this.model.getRevisionNumber()));
		
		this.add.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				final PopupPanel pp = new PopupPanel(false, true);
				HorizontalPanel layout = new HorizontalPanel();
				final Button add = new Button("Add Object");
				add.setEnabled(false);
				final XIDEditor editor = new XIDEditor(null, new EditListener() {
					public void newValue(XValue value) {
						add
						        .setEnabled(value != null
						                && value instanceof XIDValue
						                && !XModelEditor.this.model.hasObject(((XIDValue)value)
						                        .contents()));
					}
				});
				Button cancel = new Button("Cancel");
				layout.add(new Label("XID:"));
				layout.add(editor);
				layout.add(add);
				layout.add(cancel);
				pp.add(layout);
				cancel.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent e) {
						pp.hide();
						pp.removeFromParent();
					}
				});
				add.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent e) {
						XValue value = editor.getValue();
						if(value == null || !(value instanceof XIDValue))
							return;
						XID id = ((XIDValue)value).contents();
						if(XModelEditor.this.model.hasObject(id))
							return;
						pp.hide();
						pp.removeFromParent();
						add(id);
					}
				});
				pp.show();
				pp.center();
			}
		});
		
		this.delete.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				delete();
			}
		});
		
		initWidget(this.outer);
		
		setStyleName("editor-xmodel");
		
		for(XID objectId : this.model)
			newObject(objectId);
		
	}
	
	private void newObject(XID objectId) {
		XObjectEditor editor = this.objects.get(objectId);
		if(editor == null) {
			editor = new XObjectEditor(objectId, this.manager);
			this.objects.put(objectId, editor);
			// TODO sort
			this.outer.add(editor);
		}
		editor.setObject(this.model.getObject(objectId));
		this.orphans.remove(objectId);
	}
	
	private void objectRemoved(XID objectId) {
		boolean orphaned = this.orphans.isEmpty();
		this.orphans.add(objectId);
		if(orphaned) {
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					for(XID id : XModelEditor.this.orphans) {
						XObjectEditor editor = XModelEditor.this.objects.remove(id);
						editor.removeFromParent();
					}
					XModelEditor.this.orphans.clear();
				}
			});
		}
	}
	
	public void onChangeEvent(XModelEvent event) {
		if(event.getChangeType() == ChangeType.ADD) {
			newObject(event.getObjectID());
		} else {
			objectRemoved(event.getObjectID());
		}
		this.revision.setText(Long.toString(event.getModelRevisionNumber()));
	}
	
	private void add(XID id) {
		this.manager.executeCommand(MemoryModelCommand.createAddCommand(this.model.getAddress(),
		        true, id), null);
	}
	
	protected void delete() {
		removeFromParent();
		if(this.callback != null)
			this.callback.delete(this.model.getID());
	}
	
	public void onChangeEvent(XObjectEvent event) {
		this.revision.setText(Long.toString(event.getModelRevisionNumber()));
	}
	
	public void onChangeEvent(XFieldEvent event) {
		this.revision.setText(Long.toString(event.getModelRevisionNumber()));
	}
	
}
