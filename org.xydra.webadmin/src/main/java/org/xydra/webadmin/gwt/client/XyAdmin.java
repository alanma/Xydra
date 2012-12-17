package org.xydra.webadmin.gwt.client;

import java.util.Set;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.util.ModelConfiguration;
import org.xydra.webadmin.gwt.client.widgets.ModelWidget;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;


public class XyAdmin extends Composite {
	
	interface ViewUiBinder extends UiBinder<Widget,XyAdmin> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	private static final XID REPO1 = XX.toId("repo1");
	
	@UiField
	HTML title;
	
	@UiField
	Button repo1;
	
	@UiField
	FlowPanel modelIdResult;
	
	// XRepositoryCommand command =
	// X.getCommandFactory().createAddModelCommand(REPO1, XX.toId("model1"),
	// true);
	
	// this.service.executeCommand(REPO1, command, new AsyncCallback<Long>()
	// {
	//
	// @Override
	// public void onSuccess(Long result) {
	// log.info("Server said: " + result);
	// }
	//
	// @Override
	// public void onFailure(Throwable caught) {
	// log.warn("Error", caught);
	// }
	// });
	
	@UiHandler("repo1")
	public void onModelIdsClick(ClickEvent e) {
		
		// TODO phonebook is in 'repo1', other data in 'gae-repo' - make
		// configurable
		this.service.getModelIds(REPO1, new AsyncCallback<Set<XID>>() {
			
			@Override
			public void onSuccess(Set<XID> result) {
				log.info("Server said: " + result);
				
				for(final XID modelId : result) {
					
					ModelConfiguration modelConfig = new ModelConfiguration(XyAdmin.this, REPO1,
					        modelId);
					
					ModelWidget modelWidget = new ModelWidget(modelConfig);
					XyAdmin.this.modelIdResult.add(modelWidget);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				log.warn("Error", caught);
			}
		});
		
	}
	
	public XyAdminServiceAsync service;
	
	public XyAdmin(XyAdminServiceAsync service) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.title.setHTML("Select Repo!");
		
		this.service = service;
	}
	
}
