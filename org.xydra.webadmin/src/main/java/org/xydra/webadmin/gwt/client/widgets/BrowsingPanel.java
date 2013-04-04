package org.xydra.webadmin.gwt.client.widgets;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanel;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.SelectionTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;


public class BrowsingPanel extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,BrowsingPanel> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	HorizontalPanel panel;
	
	@UiField
	SelectionTree selectionTree;
	
	@UiField
	EditorPanel editorPanel;
	
	public BrowsingPanel() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
	}
}
