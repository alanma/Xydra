package org.xydra.webadmin.gwt.client.util;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.Observable;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;


@Deprecated
public class TempStorage {
    
    private static final Logger log = LoggerFactory.getLogger(DataModel.class);
    
    private Observable dialog;
    
    public void register(Observable committingDialog) {
        this.dialog = committingDialog;
        showWaitCursor();
        
    }
    
    public void remove(XAddress address) {
        DataModel.getInstance().removeItem(address);
        showDefaultCursor();
    }
    
    public void notifyDialog(String message) {
        this.dialog.notifyMe(message);
        showDefaultCursor();
    }
    
    public void allowDialogClose() {
        this.dialog.addCloseOKButton();
        
    }
    
    public void remove(XAddress address, Boolean removeFromRepo) {
        remove(address);
        if(removeFromRepo) {
            log.info("selected Delete model");
            Controller.getInstance().removeModel(address);
        }
        
    }
    
    public static void showWaitCursor() {
        DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "wait");
    }
    
    public static void showDefaultCursor() {
        DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "default");
    }
    
    // private void expandAndLoadModel() {
    // Controller.getInstance().getSelectionTree().expandEntity(this.desiredAddress);
    // // Controller.getInstance().loadCurrentModelsObjects();
    // }
    
}
