package org.xydra.webadmin.gwt.client;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.version2.MainFrameWidget;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;


public class XyAdmin extends Composite {
	
	interface ViewUiBinder extends UiBinder<Widget,XyAdmin> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	@UiField
	public MainFrameWidget mainPanel;
	
	public XyAdminServiceAsync service;
	
	public XyAdmin(XyAdminServiceAsync service) {
		
		this.service = service;
		
		Controller.getInstance().addService(service);
		Controller.getInstance().getDataModel().addRepoID(XX.toId("repo1"));
		Controller.getInstance().getDataModel().addRepoID(XX.toId("gae-repo"));
		
		initWidget(uiBinder.createAndBindUi(this));
		
		// addDummyData(service);
	}
	
	private static void addDummyData(XyAdminServiceAsync service) {
		
		List<XCommand> commands = new ArrayList<XCommand>();
		
		XID repo1 = XX.toId("repo1");
		XID user1 = XX.toId("user1");
		XID user2 = XX.toId("user2");
		XID model = XX.toId("newModel");
		XID value1 = XX.toId("HI");
		XID value2 = XX.toId("HiHo");
		commands.add(X.getCommandFactory().createAddModelCommand(repo1, model, true));
		
		commands.add(X.getCommandFactory().createAddObjectCommand(
		        XX.toAddress(repo1, model, null, null), user1, true));
		commands.add(X.getCommandFactory().createForcedAddFieldCommand(repo1, model, user1, value1));
		commands.add(X.getCommandFactory().createForcedAddValueCommand(repo1, model, user1, value1,
		        value1));
		commands.add(X.getCommandFactory().createAddObjectCommand(
		        XX.toAddress(repo1, model, null, null), user2, true));
		commands.add(X.getCommandFactory().createForcedAddFieldCommand(repo1, model, user2, value2));
		commands.add(X.getCommandFactory().createForcedAddValueCommand(repo1, model, user2, value2,
		        value2));
		
		commands.add(X.getCommandFactory().createForcedAddFieldCommand(repo1, model, user1, value2));
		commands.add(X.getCommandFactory().createForcedAddValueCommand(repo1, model, user1, value2,
		        value1));
		commands.add(X.getCommandFactory().createForcedAddFieldCommand(repo1, XX.toId("phonebook"),
		        XX.toId("peter"), XX.toId("emptyfield")));
		commands.add(X.getCommandFactory().createForcedAddValueCommand(repo1, XX.toId("phonebook"),
		        XX.toId("peter"), XX.toId("emptyfield"), value1));
		for(int i = 0; i < commands.size(); i++) {
			service.executeCommand(repo1, commands.get(i), new AsyncCallback<Long>() {
				
				@Override
				public void onSuccess(Long result) {
					log.info("Server said: " + result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					log.warn("Error", caught);
				}
			});
		}
		
	}
}
