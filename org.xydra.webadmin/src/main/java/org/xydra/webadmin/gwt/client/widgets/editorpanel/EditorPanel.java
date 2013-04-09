package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class EditorPanel extends Composite implements IEditorPanel {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,EditorPanel> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel mainPanel;
	
	@SuppressWarnings("unused")
	private EditorPanelPresenter presenter;
	
	public EditorPanel() {
		
		this.presenter = new EditorPanelPresenter(this);
		
	}
	
	@Override
	public void init() {
		initWidget(uiBinder.createAndBindUi(this));
		
	}
	
	@Override
	public void add(Widget widget) {
		this.mainPanel.add(widget);
		
	}
	
	@Override
	public void clear() {
		this.mainPanel.clear();
	}
}
