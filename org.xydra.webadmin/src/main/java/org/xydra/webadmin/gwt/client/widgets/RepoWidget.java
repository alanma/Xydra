package org.xydra.webadmin.gwt.client.widgets;

import java.util.Set;

import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;
import org.xydra.webadmin.gwt.client.util.ModelConfiguration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;


public class RepoWidget extends Composite {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,RepoWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	FlowPanel modelButtonPanel;
	
	@UiField
	FlowPanel modelPanel;
	
	private XID repoId;
	
	private XyAdmin adminObject;
	
	public RepoWidget(XyAdmin xyAdmin, XID selectedRepoId) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		this.adminObject = xyAdmin;
		
		this.repoId = selectedRepoId;
		
		this.loadRepo();
		
		TreeItem root = new TreeItem();
		root.setText("root");
		root.addTextItem("item0");
		root.addTextItem("item1");
		root.addTextItem("item2");
		
	}
	
	private void loadRepo() {
		this.adminObject.service.getModelIds(this.repoId, new AsyncCallback<Set<XID>>() {
			
			@Override
			public void onSuccess(Set<XID> result) {
				log.info("Server said: " + result);
				
				Label descriptionLabel = new Label("models in " + RepoWidget.this.repoId
				        + " <REVISION>:");
				RepoWidget.this.modelButtonPanel.add(descriptionLabel);
				
				for(final XID modelId : result) {
					
					Anchor modelAnchor = new Anchor(modelId.toString());
					RepoWidget.this.modelButtonPanel.add(modelAnchor);
					
					modelAnchor.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							
							ModelConfiguration modelConfig = new ModelConfiguration(
							        RepoWidget.this.adminObject, RepoWidget.this.repoId, modelId);
							ModelWidget modelWidget = new ModelWidget(modelConfig);
							RepoWidget.this.modelPanel.add(modelWidget);
						}
					});
					
				}
				
				Button addButton = new Button(" + ");
				Button removeButton = new Button(" - ");
				
				RepoWidget.this.modelButtonPanel.add(addButton);
				RepoWidget.this.modelButtonPanel.add(removeButton);
				
				addButton.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						RepoWidget.this.onAddButtonClick(event);
						
					}
				});
			}
			
			@Override
			public void onFailure(Throwable caught) {
				log.warn("Error", caught);
			}
		});
	}
	
	void onAddButtonClick(ClickEvent e) {
		
		final DialogBox popUpPanel = new DialogBox();
		FlowPanel contentPanel = new FlowPanel();
		
		final TextBox tb = new TextBox();
		tb.setText("new model name");
		Button okButton = new Button("OK");
		contentPanel.add(tb);
		contentPanel.add(okButton);
		popUpPanel.add(contentPanel);
		
		okButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				popUpPanel.removeFromParent();
				String modelName = tb.getText();
				XID modelId = XX.toId(modelName);
				
				XRepositoryCommand command = X.getCommandFactory().createAddModelCommand(
				
				RepoWidget.this.repoId, modelId, true);
				
				RepoWidget.this.adminObject.service.executeCommand(RepoWidget.this.repoId, command,
				        new AsyncCallback<Long>() {
					        
					        @Override
					        public void onSuccess(Long result) {
						        log.info("Server said: " + result);
					        }
					        
					        @Override
					        public void onFailure(Throwable caught) {
						        log.warn("Error", caught);
					        }
				        });
				RepoWidget.this.reload();
				
			}
		});
		
		popUpPanel.show();
		tb.selectAll();
		
	}
	
	void reload() {
		this.modelButtonPanel.clear();
		this.loadRepo();
	}
}
