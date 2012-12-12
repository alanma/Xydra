package org.xydra.webadmin.gwt.client;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;


public class XyAdmin extends Composite {
    
    interface ViewUiBinder extends UiBinder<Widget,XyAdmin> {
    }
    
    private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
    
    @UiField
    HTML title;
    
    @UiField
    TextBox name;
    
    @UiField
    Button send;
    
    @UiField
    HTML greeting;
    
    @UiHandler("send")
    public void onClick(ClickEvent e) {
        log.info("Clicked");
    }
    
    private XyAdminServiceAsync service;
    
    public XyAdmin(XyAdminServiceAsync service) {
        initWidget(uiBinder.createAndBindUi(this));
        
        this.title.setHTML("Loaded.");
        
        this.service = service;
    }
    
}
