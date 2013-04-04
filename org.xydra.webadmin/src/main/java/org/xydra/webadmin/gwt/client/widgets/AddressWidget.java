package org.xydra.webadmin.gwt.client.widgets;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.core.XX;
import org.xydra.gwt.editor.value.XAddressEditor;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;


public class AddressWidget extends Composite {
	
	public interface CompoundActionCallback {
		
		void presentModelAndContinue();
		
		void presentObjects();
		
	}
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,AddressWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";
	
	@UiField
	HorizontalPanel mainPanel;
	
	@UiField(provided = true)
	XAddressEditor addressEditor;
	
	@UiField
	Button loadLocationButton;
	
	@UiField
	Button addElementButton;
	
	@UiField
	Button deleteElementButton;
	
	@UiField
	Button clearButton;
	
	public AddressWidget() {
		
		super();
		
		this.addressEditor = new XAddressEditor(XX.toAddress(XX.toId("repo1"), null, null, null),
		        null);
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("loadLocationButton")
	void onClickLoad(ClickEvent e) {
		// showNYIDialog();
		XAddress address = getAddress();
		openAddress(address);
		
	}
	
	private static void openAddress(final XAddress desiredAddress) {
		XId repoID = desiredAddress.getRepository();
		XAddress repoAddress = XX.resolveRepository(repoID);
		// loadRepository(repoAddress);
		//
		// if(address.getModel() != null) {
		// loadModel(address);
		// XyAdmin.getInstance().getController().getTempStorage().register(address);
		// }
		XyAdmin.getInstance().getViewModel().openLocation(desiredAddress);
		XyAdmin.getInstance().getController()
		        .fetchModelIds(repoAddress, new CompoundActionCallback() {
			        
			        @Override
			        public void presentModelAndContinue() {
				        XyAdmin.getInstance().getController().presentModel(desiredAddress);
				        // XyAdmin.getInstance().getController().loadCurrentModelsObjects(this);
			        }
			        
			        @Override
			        public void presentObjects() {
				        // XyAdmin.getInstance()
				        // .getController()
				        // .notifyTableController(desiredAddress,
				        // TableController.Status.Opened);
				        // XyAdmin.getInstance()
				        // .getController()
				        // .notifyTableController(desiredAddress,
				        // TableController.Status.Opened);
				        // XyAdmin.getInstance().getController().getTableController()
				        // .scrollToField(desiredAddress);
			        }
		        });
	}
	
	private XAddress getAddress() {
		XAddress address = null;
		try {
			address = this.addressEditor.getValue();
			
		} catch(Exception ex) {
			showDialog(ex.getLocalizedMessage());
		}
		return address;
	}
	
	@SuppressWarnings("unused")
	private static void loadModel(XAddress address) {
		XyAdmin.getInstance().getController().loadModelsObjects(address, null);
		
	}
	
	@SuppressWarnings("unused")
	private static void loadRepository(XAddress repoAddress) {
		
	}
	
	@UiHandler("clearButton")
	void onClickClear(ClickEvent e) {
		this.addressEditor.setValue(XX.toAddress(XX.toId("repo1"), null, null, null));
	}
	
	@UiHandler("addElementButton")
	void onClickAdd(ClickEvent e) {
		XAddress enteredAddress = getAddress();
		XAddress rootAddress = null;
		@SuppressWarnings("unused")
		XId value = null;
		XType type = enteredAddress.getAddressedType();
		switch(type) {
		case XFIELD:
			break;
		case XMODEL:
			rootAddress = XX.toAddress(enteredAddress.getRepository(), null, null, null);
			value = enteredAddress.getModel();
			break;
		case XOBJECT:
			rootAddress = XX.toAddress(enteredAddress.getRepository(), enteredAddress.getModel(),
			        null, null);
			value = enteredAddress.getObject();
			openAddress(rootAddress);
			break;
		case XREPOSITORY:
			rootAddress = XX.toAddress("/noRepo");
			value = enteredAddress.getRepository();
			break;
		default:
			break;
		}
		// XyAdmin.getInstance().getController().getTempStorage()
		// .processInputFromDialog(rootAddress, value.toString());
		
	}
	
	@UiHandler("deleteElementButton")
	void onClickDelete(ClickEvent e) {
		showDialog("not yet Implemented");
	}
	
	void showDialog(String message) {
		WarningDialog dialog = new WarningDialog(message);
		dialog.show();
	}
}
