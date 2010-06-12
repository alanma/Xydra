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
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedObject;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XValue;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;



public class XObjectEditor extends VerticalPanel implements XObjectEventListener,
        XFieldEventListener {
	
	private final XModelSynchronizer manager;
	private XLoggedObject object;
	private final Label revision = new Label();
	private final HorizontalPanel inner = new HorizontalPanel();
	private final Button add = new Button("Add Field");
	private final Button delete = new Button("Remove Object");
	private final Map<XID,XFieldEditor> fields = new HashMap<XID,XFieldEditor>();
	private final Set<XID> orphans = new HashSet<XID>();
	
	public XObjectEditor(XID objectId, XModelSynchronizer manager) {
		super();
		
		this.manager = manager;
		
		add(this.inner);
		
		this.inner.add(new Label(objectId.toString() + " ["));
		this.inner.add(this.revision);
		this.inner.add(new Label("] "));
		this.inner.add(this.add);
		this.inner.add(this.delete);
		
		this.add.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				final PopupPanel pp = new PopupPanel(false, true);
				HorizontalPanel layout = new HorizontalPanel();
				final Button add = new Button("Add Field");
				add.setEnabled(false);
				final XIDEditor editor = new XIDEditor(null, new EditListener() {
					public void newValue(XValue value) {
						add.setEnabled(value != null
						        && value instanceof XIDValue
						        && !XObjectEditor.this.object
						                .hasField(((XIDValue)value).contents()));
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
						if(XObjectEditor.this.object.hasField(id))
							return;
						add(id);
						pp.hide();
						pp.removeFromParent();
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
		
		setStyleName("editor-xobject");
		
	}
	
	private void newField(XID fieldId) {
		XFieldEditor editor = this.fields.get(fieldId);
		if(editor == null) {
			editor = new XFieldEditor(fieldId, this.manager);
			this.fields.put(fieldId, editor);
			// TODO sort
			add(editor);
		}
		editor.setField(this.object.getField(fieldId));
		this.orphans.remove(fieldId);
	}
	
	private void fieldRemoved(XID objectId) {
		boolean orphaned = this.orphans.isEmpty();
		this.orphans.add(objectId);
		if(orphaned) {
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					for(XID id : XObjectEditor.this.orphans) {
						XFieldEditor editor = XObjectEditor.this.fields.remove(id);
						editor.removeFromParent();
					}
					XObjectEditor.this.orphans.clear();
				}
			});
		}
	}
	
	public void onChangeEvent(XObjectEvent event) {
		if(event.getChangeType() == ChangeType.ADD) {
			newField(event.getObjectID());
		} else {
			fieldRemoved(event.getObjectID());
		}
		this.revision.setText(Long.toString(event.getObjectRevisionNumber()));
	}
	
	private void add(XID id) {
		this.manager.executeCommand(MemoryObjectCommand.createAddCommand(this.object.getAddress(),
		        true, id), null);
	}
	
	protected void delete() {
		this.manager.executeCommand(MemoryModelCommand.createRemoveCommand(this.object.getAddress()
		        .getParent(), this.object.getRevisionNumber(), this.object.getID()), null);
	}
	
	public void setObject(XLoggedObject object) {
		this.object = object;
		for(XID fieldId : this.object)
			newField(fieldId);
		this.revision.setText(Long.toString(object.getRevisionNumber()));
	}
	
	public void onChangeEvent(XFieldEvent event) {
		this.revision.setText(Long.toString(event.getObjectRevisionNumber()));
	}
	
}
