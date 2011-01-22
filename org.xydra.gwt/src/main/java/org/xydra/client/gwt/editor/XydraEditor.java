package org.xydra.client.gwt.editor;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.client.Callback;
import org.xydra.client.ServiceException;
import org.xydra.client.XChangesService;
import org.xydra.client.XDataService;
import org.xydra.client.gwt.service.GWTChangesService;
import org.xydra.client.gwt.service.GWTDataService;
import org.xydra.client.gwt.xml.impl.GWTMiniXMLParserImpl;
import org.xydra.client.sync.XSynchronizer;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * A minimal CXM client that can load and edit models.
 */
public class XydraEditor implements EntryPoint {
	
	private static final Logger log = LoggerFactory.getLogger(XydraEditor.class);
	
	private final XDataService data = new GWTDataService("/xclient/cxm/data",
	        new GWTMiniXMLParserImpl());
	private final XChangesService changes = new GWTChangesService("/xclient/cxm/changes",
	        new GWTMiniXMLParserImpl());
	
	VerticalPanel panel = new VerticalPanel();
	
	XSynchronizer manager;
	
	Timer timer = new Timer() {
		@Override
		public void run() {
			XydraEditor.this.manager.synchronize();
		}
	};
	
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
				
				log.error(text, t);
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
		
		log.info("starting the xydra editor");
		
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
	
	private XAddress addr;
	
	protected void loadData(String addrStr) {
		
		log.info("editor: loading " + addrStr);
		
		if(this.manager != null) {
			this.timer.cancel();
			this.manager = null;
		}
		
		this.panel.clear();
		
		XID modelId;
		XID objectId;
		try {
			int p = addrStr.indexOf('/');
			if(p >= 0) {
				modelId = XX.toId(addrStr.substring(0, p));
				objectId = XX.toId(addrStr.substring(p + 1));
			} else {
				modelId = XX.toId(addrStr);
				objectId = null;
			}
		} catch(Exception e) {
			this.panel.add(new Label("invalid XID: " + e.getMessage()));
			return;
		}
		
		this.addr = XX.toAddress(null, modelId, objectId, null);
		
		try {
			if(objectId != null) {
				this.data.getObject(modelId, objectId, new Callback<XObject>() {
					public void onSuccess(final XObject object) {
						loadedObject(object);
					}
					
					public void onFailure(Throwable error) {
						handleError(error);
					}
					
				});
			} else {
				this.data.getModel(modelId, new Callback<XModel>() {
					public void onSuccess(final XModel model) {
						loadedModel(model);
					}
					
					public void onFailure(Throwable error) {
						handleError(error);
					}
					
				});
			}
			
		} catch(Exception e) {
			this.panel.add(new Label(e.toString()));
		}
		
	}
	
	private void handleError(Throwable error) {
		if(error instanceof ServiceException) {
			this.panel.add(new Label(error.getMessage()));
		} else {
			this.panel.add(new Label(error.toString()));
		}
	}
	
	protected void loadedModel(XModel model) {
		
		log.info("editor: loaded model, starting synchronizer");
		
		startSynchronizer(model);
		
		this.panel.add(new XModelEditor(model));
	}
	
	protected void loadedObject(XObject object) {
		
		log.info("editor: loaded object, starting synchronizer");
		
		startSynchronizer(object);
		
		this.panel.add(new XObjectEditor(null, object));
	}
	
	private void startSynchronizer(XSynchronizesChanges entity) {
		this.manager = new XSynchronizer(this.addr, entity, XydraEditor.this.changes);
		this.timer.scheduleRepeating(5000);
	}
	
}
