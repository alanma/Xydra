package org.xydra.webadmin.gwt.client.widgets;

import org.xydra.core.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.ViewModel;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;


public class XyAdmin extends Composite {
	
	interface ViewUiBinder extends UiBinder<Widget,XyAdmin> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	private Controller controller;
	private DataModel model;
	private ViewModel viewModel;
	
	private EventBus eventbus;
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	private static XyAdmin instance;
	
	public XyAdmin(XyAdminServiceAsync service) {
		instance = this;
		
		this.getController().addService(service);
		this.getModel().addRepoID(XX.toId("repo1"));
		this.getModel().addRepoID(XX.toId("gae-repo"));
		initWidget(uiBinder.createAndBindUi(this));
		
	}
	
	public static XyAdmin getInstance() {
		if(instance == null)
			throw new IllegalStateException("Please init first with a service");
		return instance;
	}
	
	public synchronized Controller getController() {
		if(this.controller == null) {
			this.controller = new Controller();
		}
		return this.controller;
	}
	
	public synchronized DataModel getModel() {
		if(this.model == null) {
			this.model = new DataModel();
		}
		return this.model;
	}
	
	public synchronized ViewModel getViewModel() {
		if(this.viewModel == null) {
			this.viewModel = new ViewModel();
		}
		return this.viewModel;
	}
	
	public EventBus getEventBus() {
		if(this.eventbus == null) {
			this.eventbus = GWT.create(SimpleEventBus.class);
		}
		return this.eventbus;
	}
	
}
