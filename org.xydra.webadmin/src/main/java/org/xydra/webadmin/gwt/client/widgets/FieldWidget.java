package org.xydra.webadmin.gwt.client.widgets;

import org.xydra.base.XID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


public class FieldWidget extends Composite {
    
    interface ViewUiBinder extends UiBinder<Widget,FieldWidget> {
    }
    
    private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    
    @SuppressWarnings("unused")
    private XID fieldID;
    @SuppressWarnings("unused")
    private Object value;
    
    @UiField
    FlowPanel fieldPanel;
    @UiField
    Label fieldLabel;
    @UiField
    Anchor valueAnchor;
    
    public FieldWidget(XID fieldID, Object value) {
        
        initWidget(uiBinder.createAndBindUi(this));
        
        this.fieldID = fieldID;
        this.value = value;
        
        this.fieldLabel.setText(fieldID.toString());
        this.valueAnchor.setText(" : " + value);
        
        // TODO clean this up
//        if(value != null) {
//            EditListener listener = new EditListener() {
//                
//                @Override
//                public void newValue(XValue value) {
//                    // TODO send to server or wait while we build a txn
//                }
//            };
//            XValueEditor editor = XValueEditor.get(value, listener);
//            this.fieldPanel.add(editor);
//        }
    }
}
