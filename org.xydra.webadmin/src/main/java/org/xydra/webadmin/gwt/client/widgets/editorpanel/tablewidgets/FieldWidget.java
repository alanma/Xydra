package org.xydra.webadmin.gwt.client.widgets.editorpanel.tablewidgets;

import org.xydra.base.XId;
import org.xydra.gwt.editor.XFieldEditor;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * For table fields with existing content
 * 
 * @author Andi
 * 
 */
public class FieldWidget extends Composite implements TableFieldWidget {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,FieldWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel mainPanel;
	
	private XFieldEditor fieldEditor;
	
	public FieldWidget(RowPresenter rowPresenter, XId fieldId) {
		super();
		
		initWidget(uiBinder.createAndBindUi(this));
		
		this.fieldEditor = new XFieldEditor(rowPresenter, fieldId);
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
	
	@Override
	public void scrollToMe() {
		Document.get().setScrollLeft(this.getAbsoluteLeft() - (Window.getClientWidth() / 2 - 50));
		this.addStyleName("fadeOut");
		
		Timer timer1 = new Timer() {
			public void run() {
				FieldWidget.this.addStyleName("highlightStyle");
				// FieldWidget.this.removeStyleName("highlightStyle");
			}
		};
		
		timer1.schedule(500);
		
		Timer timer2 = new Timer() {
			public void run() {
				FieldWidget.this.removeStyleName("highlightStyle");
				FieldWidget.this.removeStyleName("fadeOut");
			}
		};
		
		timer2.schedule(3000);
		
	}
}
