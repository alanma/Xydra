package org.xydra.webadmin.gwt.client;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;


public class XyAdmin extends Composite {
    
    interface ViewUiBinder extends UiBinder<Widget,XyAdmin> {
    }
    
    private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
    
    @UiField
    HTML title;
    
    private XyAdminServiceAsync service;
    
    public XyAdmin() {
        initWidget(uiBinder.createAndBindUi(this));
        
        this.title.setHTML("Loaded.");
        
    }
    
    public XyAdmin(XyAdminServiceAsync service) {
        this.service = service;
    }
    
}
