package org.xydra.webadmin.gwt.client.widgets.tablewidgets;

import org.xydra.base.rmof.XReadableField;
import org.xydra.gwt.editor.XFieldEditor;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
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
	
	private XFieldEditor fieldEditor;
	
	public FieldWidget(XReadableField field) {
		
		super();
		
		initWidget(uiBinder.createAndBindUi(this));
		
		this.fieldEditor = new XFieldEditor(field);
		this.mainPanel.insert(this.fieldEditor, 0);
		this.fieldEditor.hideButtons();
		this.addDomHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				FieldWidget.this.fieldEditor.showButtons();
				
			}
		}, MouseOverEvent.getType());
		
		this.addDomHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				FieldWidget.this.fieldEditor.hideButtons();
				
			}
		}, MouseOutEvent.getType());
	}
}
