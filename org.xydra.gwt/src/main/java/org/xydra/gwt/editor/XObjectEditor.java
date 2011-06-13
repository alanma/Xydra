package org.xydra.gwt.editor;

import java.util.HashMap;
import java.util.Map;

import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.value.XValue;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.gwt.editor.value.XIDEditor;
import org.xydra.gwt.editor.value.XValueEditor.EditListener;
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
	
	private final XModel model;
	private final XObject object;
	private final HorizontalPanel inner = new HorizontalPanel();
	private final Button add = new Button("Add Field");
	private final Map<XID,XFieldEditor> fields = new HashMap<XID,XFieldEditor>();
	
	public XObjectEditor(XObject object) {
		this(null, object);
	}
	
	public XObjectEditor(XModel model, XObject object) {
		super();
		
		this.model = model;
		this.object = object;
		this.object.addListenerForObjectEvents(this);
		
		add(this.inner);
		
		this.inner.add(new Label(object.getID().toString()));
		this.inner.add(this.add);
		
		if(this.model != null) {
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
		XField field = this.object.getField(fieldId);
		if(field == null) {
			log.info("editor: asked to add field " + fieldId + ", which doesn't exist (anymore)");
			return;
		}
		XFieldEditor editor = new XFieldEditor(this.object, field);
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
			newField(event.getFieldId());
		} else {
			fieldRemoved(event.getFieldId());
		}
	}
	
	private void add(XID id) {
		this.object.executeCommand(MemoryObjectCommand.createAddCommand(this.object.getAddress(),
		        true, id), null);
	}
	
	protected void delete() {
		assert this.model != null;
		this.model.executeCommand(MemoryModelCommand.createRemoveCommand(this.object.getAddress()
		        .getParent(), this.object.getRevisionNumber(), this.object.getID()), null);
	}
	
}
