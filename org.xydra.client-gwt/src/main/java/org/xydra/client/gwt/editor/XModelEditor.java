package org.xydra.client.gwt.editor;

import java.util.HashMap;
import java.util.Map;

import org.xydra.client.gwt.editor.value.XIDEditor;
import org.xydra.client.gwt.editor.value.XValueEditor.EditListener;
import org.xydra.client.sync.XSynchronizer;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.impl.memory.MemoryModelCommand;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedModel;
import org.xydra.core.model.XLoggedObject;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


public class XModelEditor extends Composite implements XModelEventListener {
	
	private static final Logger log = LoggerFactory.getLogger(XModelEditor.class);
	
	private final XSynchronizer manager;
	private final XLoggedModel model;
	private final VerticalPanel outer = new VerticalPanel();
	private final HorizontalPanel inner = new HorizontalPanel();
	private final Button add = new Button("Add Object");
	
	private final Map<XID,XObjectEditor> objects = new HashMap<XID,XObjectEditor>();
	
	public XModelEditor(XLoggedModel model, XSynchronizer manager) {
		super();
		
		this.manager = manager;
		this.model = model;
		this.model.addListenerForModelEvents(this);
		
		this.outer.add(this.inner);
		
		this.inner.add(new Label(this.model.getID().toString()));
		this.inner.add(this.add);
		
		this.add.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				final PopupPanel pp = new PopupPanel(false, true);
				HorizontalPanel layout = new HorizontalPanel();
				final Button add = new Button("Add Object");
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
						XIDValue value = editor.getValue();
						if(value == null)
							return;
						XID id = value.contents();
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
		
		initWidget(this.outer);
		
		setStyleName("editor-xmodel");
		
		for(XID objectId : this.model)
			newObject(objectId);
		
	}
	
	private void newObject(XID objectId) {
		XLoggedObject object = this.model.getObject(objectId);
		if(object == null) {
			log.warn("editor: asked to add object " + objectId + ", which doesn't exist (anymore)");
			return;
		}
		XObjectEditor editor = new XObjectEditor(object, this.manager);
		this.objects.put(objectId, editor);
		// TODO sort
		this.outer.add(editor);
	}
	
	private void objectRemoved(XID objectId) {
		XObjectEditor editor = XModelEditor.this.objects.remove(objectId);
		if(editor == null) {
			log.warn("editor: asked to remove object " + objectId + ", which isn't there");
			return;
		}
		editor.removeFromParent();
	}
	
	public void onChangeEvent(XModelEvent event) {
		log.info("editor: got " + event);
		if(event.getChangeType() == ChangeType.ADD) {
			newObject(event.getObjectID());
		} else {
			objectRemoved(event.getObjectID());
		}
	}
	
	private void add(XID id) {
		this.manager.executeCommand(MemoryModelCommand.createAddCommand(this.model.getAddress(),
		        true, id), null);
	}
	
}
