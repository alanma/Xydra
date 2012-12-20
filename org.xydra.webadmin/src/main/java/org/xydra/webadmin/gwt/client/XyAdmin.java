package org.xydra.webadmin.gwt.client;

import java.util.HashSet;

import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.RepoWidget;
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
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;


public class XyAdmin extends Composite {
	
	interface ViewUiBinder extends UiBinder<Widget,XyAdmin> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	@UiField
	public FlowPanel repoChoosePanel;
	
	@UiField
	HTML title;
	
	@UiField(provided = true)
	SuggestBox repositoryChooser;
	
	@UiField
	Button loadButton;
	
	@UiField
	FlowPanel repoPanel;
	
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
	
	@UiHandler("loadButton")
	public void onModelIdsClick(ClickEvent e) {
		
		final String selectedRepo = this.repositoryChooser.getText();
		final XID selectedRepoId = XX.toId(selectedRepo);
		log.info("text from SuggestBox: " + selectedRepo);
		
		RepoWidget repoWidget = new RepoWidget(this, selectedRepoId);
		
		// TODO phonebook is in 'repo1', other data in 'gae-repo' - make
		// configurable
		
		this.repoPanel.add(repoWidget);
		
	}
	
	public XyAdminServiceAsync service;
	
	public XyAdmin(XyAdminServiceAsync service) {
		
		// HashSet<String> currentlyUsedRepos = Sets.newHashSet("repo1",
		// "gae-repo");
		HashSet<String> currentlyUsedRepos = new HashSet<String>();
		currentlyUsedRepos.add("repo1");
		currentlyUsedRepos.add("gae-repo");
		
		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
		oracle.addAll(currentlyUsedRepos);
		
		this.repositoryChooser = new SuggestBox(oracle);
		// this.repositoryChooser.addKeyPressHandler(new KeyPressHandler() {
		//
		// @Override
		// public void onKeyPress(KeyPressEvent event) {
		// if(event.() == KeyboardEvent.DOM_VK_ENTER) {
		// this.loadButton.click();
		// }
		// }
		// })
		initWidget(uiBinder.createAndBindUi(this));
		
		this.service = service;
		
		XRepositoryCommand command = X.getCommandFactory().createAddModelCommand(XX.toId("repo1"),
		        XX.toId("newModel"), true);
		
		service.executeCommand(XX.toId("repo1"), command, new AsyncCallback<Long>() {
			
			@Override
			public void onSuccess(Long result) {
				log.info("Server said: " + result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				log.warn("Error", caught);
			}
		});
		
		this.repositoryChooser.setText("repo1");
		
		this.onModelIdsClick(null);
	}
	
}
