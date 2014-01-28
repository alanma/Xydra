package org.xydra.gwt.editor;

import java.util.HashMap;
import java.util.Map;

import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.value.XValue;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.gwt.editor.value.XIdEditor;
import org.xydra.gwt.editor.value.XValueEditor.EditListener;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

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
    
    private final XModel model;
    private final VerticalPanel outer = new VerticalPanel();
    private final HorizontalPanel inner = new HorizontalPanel();
    private final Button add = new Button("Add Object");
    
    private final Map<XId,XObjectEditor> objects = new HashMap<XId,XObjectEditor>();
    
    public XModelEditor(XModel model) {
        super();
        
        this.model = model;
        this.model.addListenerForModelEvents(this);
        
        this.outer.add(this.inner);
        
        this.inner.add(new Label(this.model.getId().toString()));
        this.inner.add(this.add);
        
        this.add.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                final PopupPanel pp = new PopupPanel(false, true);
                HorizontalPanel layout = new HorizontalPanel();
                final Button add = new Button("Add Object");
                final XIdEditor editor = new XIdEditor(null, new EditListener() {
                    @Override
                    public void newValue(XValue value) {
                        add.setEnabled(value != null && value instanceof XId
                                && !XModelEditor.this.model.hasObject(((XId)value)));
                    }
                });
                Button cancel = new Button("Cancel");
                layout.add(new Label("XID:"));
                layout.add(editor);
                layout.add(add);
                layout.add(cancel);
                pp.add(layout);
                cancel.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent e) {
                        pp.hide();
                        pp.removeFromParent();
                    }
                });
                add.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent e) {
                        // TODO simplify this code block now that XID=XIDValue
                        XId value = editor.getValue();
                        if(value == null)
                            return;
                        XId id = value;
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
        
        for(XId objectId : this.model)
            newObject(objectId);
        
    }
    
    private void newObject(XId objectId) {
        XObject object = this.model.getObject(objectId);
        if(object == null) {
            log.warn("editor: asked to add object " + objectId + ", which doesn't exist (anymore)");
            return;
        }
        XObjectEditor editor = new XObjectEditor(this.model, object);
        this.objects.put(objectId, editor);
        // TODO sort
        this.outer.add(editor);
    }
    
    private void objectRemoved(XId objectId) {
        XObjectEditor editor = XModelEditor.this.objects.remove(objectId);
        if(editor == null) {
            log.warn("editor: asked to remove object " + objectId + ", which isn't there");
            return;
        }
        editor.removeFromParent();
    }
    
    @Override
    public void onChangeEvent(XModelEvent event) {
        log.info("editor: got " + event);
        if(event.getChangeType() == ChangeType.ADD) {
            newObject(event.getObjectId());
        } else {
            objectRemoved(event.getObjectId());
        }
    }
    
    private void add(XId id) {
        this.model.executeCommand(MemoryModelCommand.createAddCommand(this.model.getAddress(),
                true, id));
    }
    
}
