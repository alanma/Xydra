package org.xydra.client.gwt.editor;

import java.util.HashMap;
import java.util.Map;

import org.xydra.client.gwt.editor.value.XIDEditor;
import org.xydra.client.gwt.editor.value.XValueEditor.EditListener;
import org.xydra.client.sync.XSynchronizer;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedField;
import org.xydra.core.model.XLoggedObject;
import org.xydra.core.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


public class XObjectEditor extends VerticalPanel implements XObjectEventListener {
	
	private static final Logger log = LoggerFactory.getLogger(XObjectEditor.class);
	
	private final XSynchronizer manager;
	private final XLoggedObject object;
	private final HorizontalPanel inner = new HorizontalPanel();
	private final Button add = new Button("Add Field");
	private final Map<XID,XFieldEditor> fields = new HashMap<XID,XFieldEditor>();
	
	public XObjectEditor(XLoggedObject object, XSynchronizer manager) {
		super();
		
		this.manager = manager;
		this.object = object;
		this.object.addListenerForObjectEvents(this);
		
		add(this.inner);
		
		this.inner.add(new Label(object.getID().toString()));
		this.inner.add(this.add);
		
		if(this.object.getAddress().getModel() != null) {
			Button delete = new Button("Remove Object");
			this.inner.add(delete);
			delete.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent e) {
					delete();
				}
			});
		}
		
		this.add.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				final PopupPanel pp = new PopupPanel(false, true);
				HorizontalPanel layout = new HorizontalPanel();
				final Button add = new Button("Add Field");
				final XIDEditor editor = new XIDEditor(null, new EditListener() {
					public void newValue(XValue value) {
						add.setEnabled(value != null && value instanceof XID
						        && !XObjectEditor.this.object.hasField(((XID)value)));
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
						// TODO simply this block now that XID=XIDValue
						XID value = editor.getValue();
						if(value == null)
							return;
						XID id = value;
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
		
		addStyleName("editor-xobject");
		addStyleName("editor");
		
		for(XID fieldId : this.object)
			newField(fieldId);
	}
	
	private void newField(XID fieldId) {
		XLoggedField field = this.object.getField(fieldId);
		if(field == null) {
			log.info("editor: asked to add field " + fieldId + ", which doesn't exist (anymore)");
			return;
		}
		XFieldEditor editor = new XFieldEditor(field, this.manager);
		this.fields.put(fieldId, editor);
		// TODO sort
		add(editor);
	}
	
	private void fieldRemoved(XID fieldId) {
		XFieldEditor editor = XObjectEditor.this.fields.remove(fieldId);
		if(editor == null) {
			log.warn("editor: asked to remove field " + fieldId + ", which isn't there");
			return;
		}
		editor.removeFromParent();
	}
	
	public void onChangeEvent(XObjectEvent event) {
		log.info("editor: got " + event);
		if(event.getChangeType() == ChangeType.ADD) {
			newField(event.getFieldID());
		} else {
			fieldRemoved(event.getFieldID());
		}
	}
	
	private void add(XID id) {
		this.manager.executeCommand(MemoryObjectCommand.createAddCommand(this.object.getAddress(),
		        true, id), null);
	}
	
	protected void delete() {
		this.manager.executeCommand(MemoryModelCommand.createRemoveCommand(this.object.getAddress()
		        .getParent(), this.object.getRevisionNumber(), this.object.getID()), null);
	}
	
}
