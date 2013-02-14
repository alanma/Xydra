package org.xydra.webadmin.gwt.client.widgets.tablewidgets;

import org.xydra.base.rmof.XReadableField;
import org.xydra.gwt.editor.XFieldEditor;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * For table fields with existing content
 * 
 * @author Andi
 * 
 */
public class FieldWidget extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,FieldWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel mainPanel;
	
	@UiField
	Label revisionNumberLabel;
	
	public FieldWidget(XReadableField field) {
		
		super();
		
		initWidget(uiBinder.createAndBindUi(this));
		
		XFieldEditor fieldEditor = new XFieldEditor(field);
		this.mainPanel.insert(fieldEditor, 0);
		
		this.revisionNumberLabel.setText("rev.: " + field.getRevisionNumber());
		
	}
	
}
