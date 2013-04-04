package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent.IModelChangedEventHandler;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class EditorPanel extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,EditorPanel> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel mainPanel;
	
	private EditorPanelPresenter presenter;
	
	public EditorPanel() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		this.presenter = new EditorPanelPresenter(this);
		XyAdmin.getInstance().getController().registerEditorPanelPresenter(this.presenter);
		
	}
	
	public void buildModelView(SessionCachedModel selectedModel) {
		this.mainPanel.clear();
		
		this.mainPanel.add(new ModelControlPanel(this.presenter));
		final ModelInformationPanel modelInformationPanel = new ModelInformationPanel(
		        this.presenter, selectedModel);
		this.mainPanel.add(modelInformationPanel);
		
		EventHelper.addModelChangeListener(this.presenter.getCurrentModelAddress(),
		        new IModelChangedEventHandler() {
			        
			        @Override
			        public void onModelChange(ModelChangedEvent event) {
				        if(event.getStatus().equals(EntityStatus.DELETED)) {
					        resetView();
					        
				        } else if(event.getStatus().equals(EntityStatus.EXTENDED)) {
					        EditorPanel.this.presenter.presentNewInformation(modelInformationPanel);
				        }
			        }
			        
		        });
	}
	
	private void resetView() {
		this.mainPanel.clear();
		this.mainPanel.add(new Label("choose model via selection tree"));
		
	}
}
