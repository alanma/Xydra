package org.xydra.gwt.editor;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.impl.ISyncableState;
import org.xydra.core.StoreException;
import org.xydra.core.XX;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.impl.memory.IMemoryModel;
import org.xydra.core.model.impl.memory.IMemoryObject;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;
import org.xydra.gwt.store.GwtXydraStoreRestClient;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.XydraStore;
import org.xydra.store.sync.NewSyncer;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

	private static final String PSW = "secret";

	private static final XId ACTOR = XX.toId("tester");

	private static final Logger log = LoggerFactory.getLogger(XydraEditor.class);

	private static final XydraStore store = new GwtXydraStoreRestClient("/cxm/store/v1/",
			new JsonSerializer(), new JsonParser());

	VerticalPanel panel = new VerticalPanel();

	NewSyncer manager;

	Timer timer = new Timer() {
		@Override
		public void run() {
			XydraEditor.this.manager.startSync(null);
		}
	};

	@Override
	public void onModuleLoad() {

		// set uncaught exception handler
		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
			@Override
			public void onUncaughtException(Throwable throwable) {

				Throwable t = throwable;

				String text = "Uncaught exception: ";
				while (t != null) {
					StackTraceElement[] stackTraceElements = t.getStackTrace();
					text += t.toString() + "\n";
					for (int i = 0; i < stackTraceElements.length; i++) {
						text += "    at " + stackTraceElements[i] + "\n";
					}
					t = t.getCause();
					if (t != null) {
						text += "Caused by: ";
					}
				}

				log.error(text, t);
			}
		});

		// use a deferred command so that the handler catches init() exceptions
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
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

			@Override
			public void onClick(ClickEvent arg0) {
				loadData(address.getText());
			}

		});

	}

	private XAddress addr;

	protected void loadData(String addrStr) {

		log.info("editor: loading " + addrStr);

		if (this.manager != null) {
			this.timer.cancel();
			this.manager = null;
		}

		this.panel.clear();

		XId modelId;
		XId objectId;
		try {
			int p = addrStr.indexOf('/');
			if (p >= 0) {
				modelId = XX.toId(addrStr.substring(0, p));
				objectId = XX.toId(addrStr.substring(p + 1));
			} else {
				modelId = XX.toId(addrStr);
				objectId = null;
			}
		} catch (Exception e) {
			this.panel.add(new Label("invalid XId: " + e.getMessage()));
			return;
		}

		this.addr = XX.toAddress(XX.toId("data"), modelId, objectId, null);

		try {
			if (objectId != null) {
				Callback<BatchedResult<XReadableObject>[]> cb = new Callback<BatchedResult<XReadableObject>[]>() {

					@Override
					public void onFailure(Throwable error) {
						handleError(error);
					}

					@Override
					public void onSuccess(BatchedResult<XReadableObject>[] object) {
						assert object.length == 1;
						if (object[0].getException() != null) {
							handleError(object[0].getException());
						} else if (object[0].getResult() == null) {
							handleError(new RuntimeException("object not found: "
									+ XydraEditor.this.addr));
						} else {
							loadedObject(object[0].getResult());
						}
					}

				};
				store.getObjectSnapshots(ACTOR, PSW,
						new GetWithAddressRequest[] { new GetWithAddressRequest(this.addr) }, cb);

			} else {
				Callback<BatchedResult<XReadableModel>[]> cb = new Callback<BatchedResult<XReadableModel>[]>() {

					@Override
					public void onFailure(Throwable error) {
						handleError(error);
					}

					@Override
					public void onSuccess(BatchedResult<XReadableModel>[] object) {
						assert object.length == 1;
						if (object[0].getException() != null) {
							handleError(object[0].getException());
						} else if (object[0].getResult() == null) {
							handleError(new RuntimeException("model not found: "
									+ XydraEditor.this.addr));
						} else {
							loadedModel(object[0].getResult());
						}
					}

				};
				store.getModelSnapshots(ACTOR, PSW,
						new GetWithAddressRequest[] { new GetWithAddressRequest(this.addr) }, cb);
			}

		} catch (Exception e) {
			this.panel.add(new Label(e.toString()));
		}

	}

	private void handleError(Throwable error) {
		if (error instanceof StoreException) {
			this.panel.add(new Label(error.getMessage()));
		} else {
			this.panel.add(new Label(error.toString()));
		}
	}

	protected void loadedModel(XReadableModel modelSnapshot) {

		log.info("editor: loaded model, starting synchronizer");

		XModel model = XX.wrap(ACTOR, PSW, modelSnapshot);

		startSynchronizer(model);

		this.panel.add(new Label(model.getAddress().toString()));
		this.panel.add(new XModelEditor(model));
	}

	protected void loadedObject(XReadableObject objectSnapshot) {

		log.info("editor: loaded object, starting synchronizer");

		XObject object = XX.wrap(ACTOR, PSW, objectSnapshot);

		startSynchronizer(object);

		this.panel.add(new Label(object.getAddress().toString()));
		this.panel.add(new XObjectEditor(object));
	}

	private void startSynchronizer(XSynchronizesChanges entity) {
		ISyncableState syncableState;
		if (entity instanceof IMemoryObject) {
			syncableState = ((IMemoryObject) entity).getState();
		} else if (entity instanceof IMemoryModel) {
			syncableState = ((IMemoryModel) entity).getState();
		} else {
			throw new IllegalArgumentException("Cannot sync this");
		}

		this.manager = new NewSyncer(store, entity, syncableState);
		this.timer.scheduleRepeating(5000);
	}

}
