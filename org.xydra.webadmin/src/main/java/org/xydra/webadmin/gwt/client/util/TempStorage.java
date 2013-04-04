package org.xydra.webadmin.gwt.client.util;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Observable;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;


@Deprecated
public class TempStorage {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DataModel.class);
	
	private Observable dialog;
	
	public void register(Observable committingDialog) {
		this.dialog = committingDialog;
		
	}
	
	public void notifyDialog(String message) {
		this.dialog.notifyMe(message);
	}
	
	public void allowDialogClose() {
		this.dialog.addCloseOKButton();
		
	}
	
	// private void expandAndLoadModel() {
	// Controller.getInstance().getSelectionTree().expandEntity(this.desiredAddress);
	// // Controller.getInstance().loadCurrentModelsObjects();
	// }
	
}
