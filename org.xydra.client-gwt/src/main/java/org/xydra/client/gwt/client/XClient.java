package org.xydra.client.gwt.client;

import org.xydra.client.gwt.Callback;
import org.xydra.client.gwt.XChangesService;
import org.xydra.client.gwt.XDataService;
import org.xydra.client.gwt.client.editor.DeleteCallback;
import org.xydra.client.gwt.client.editor.XModelEditor;
import org.xydra.client.gwt.service.GWTChangesService;
import org.xydra.client.gwt.service.GWTDataService;
import org.xydra.client.gwt.xml.impl.GWTMiniXMLParserImpl;
import org.xydra.client.sync.XModelSynchronizer;
import org.xydra.core.X;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * A minimal CXM client that can load and edit models.
 */
public class XClient implements EntryPoint {
	
	private final XDataService data = new GWTDataService("/xclient/cxm/data",
	        new GWTMiniXMLParserImpl());
	private final XChangesService changes = new GWTChangesService("/xclient/cxm/changes",
	        new GWTMiniXMLParserImpl());
	
	VerticalPanel panel = new VerticalPanel();
	
	public void onModuleLoad() {
		// set uncaught exception handler
		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
			public void onUncaughtException(Throwable throwable) {
				
				Throwable t = throwable;
				
				String text = "Uncaught exception: ";
				while(t != null) {
					StackTraceElement[] stackTraceElements = t.getStackTrace();
					text += t.toString() + "\n";
					for(int i = 0; i < stackTraceElements.length; i++) {
						text += "    at " + stackTraceElements[i] + "\n";
					}
					t = t.getCause();
					if(t != null) {
						text += "Caused by: ";
					}
				}
				
				Log.error(text, t);
			}
		});
		
		// use a deferred command so that the handler catches init() exceptions
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				init();
			}
		});
	}
	
	public void init() {
		
		VerticalPanel layout = new VerticalPanel();
		HorizontalPanel header = new HorizontalPanel();
		layout.add(header);
		layout.add(this.panel);
		RootPanel.get().add(layout);
		
		final TextBox address = new TextBox();
		
		address.setText("phonebook");
		
		Button load = new Button("Load");
		
		header.add(address);
		header.add(load);
		
		load.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent arg0) {
				loadData(address.getText());
			}
			
		});
		
	}
	
	abstract private class AbstractCallback<T> implements Callback<T> {
		
		public void onFailure(Throwable t) {
			XClient.this.panel.add(new Label(t.getMessage()));
		}
		
	}
	
	protected void loadData(String modelIdStr) {
		
		this.panel.clear();
		
		XID modelId;
		try {
			modelId = X.getIDProvider().fromString(modelIdStr);
		} catch(Exception e) {
			this.panel.add(new Label("invalid model XID: " + e.getMessage()));
			return;
		}
		
		try {
			this.data.getModel(modelId, new AbstractCallback<XModel>() {
				public void onSuccess(final XModel model) {
					
					XModelSynchronizer manager = new XModelSynchronizer(model, XClient.this.changes);
					
					XClient.this.panel.add(new XModelEditor(manager, new DeleteCallback() {
						public void delete(XID entity) {
							XClient.this.data.deleteModel(model.getID(),
							        new AbstractCallback<Void>() {
								        public void onSuccess(Void object) {
									        // model sucessfully removed
								        }
							        });
						}
					}));
				}
			});
			
		} catch(Exception e) {
			this.panel.add(new Label(e.toString()));
		}
		
	}
}
