package org.xydra.fbsdk4gwt.client;

import org.xydra.fbsdk4gwt.client.examples.Example;
import org.xydra.fbsdk4gwt.client.examples.FriendsExample;
import org.xydra.fbsdk4gwt.client.examples.StreamPublishExample;
import org.xydra.fbsdk4gwt.sdk.FBCore;
import org.xydra.fbsdk4gwt.sdk.FBEvent;
import org.xydra.fbsdk4gwt.sdk.FBXfbml;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * 
 * @author ola
 * 
 */
public class ExampleApp implements EntryPoint, ValueChangeHandler<String> {
	
	/**
	 * FIXME set this
	 */
	public String APPID = "1d81c942b38e2e6b3fc35a147d371ab3";
	
	private DockPanel mainPanel = new DockPanel();
	private SimplePanel mainView = new SimplePanel();
	private SimplePanel sideBarView = new SimplePanel();
	
	private FBCore fbCore = GWT.create(FBCore.class);
	private FBEvent fbEvent = GWT.create(FBEvent.class);
	@SuppressWarnings("unused")
	private FBXfbml fbXfbml = GWT.create(FBXfbml.class);
	
	private boolean status = true;
	private boolean xfbml = true;
	private boolean cookie = true;
	
	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad() {
		
		History.addValueChangeHandler(this);
		
		this.fbCore.init(this.APPID, this.status, this.cookie, this.xfbml);
		
		RootPanel root = RootPanel.get();
		root.getElement().setId("TheApp");
		this.mainView.getElement().setId("MainView");
		this.sideBarView.getElement().setId("SideBarView");
		this.mainPanel.add(new TopMenuPanel(), DockPanel.NORTH);
		this.mainPanel.add(new TopMenuLinksPanel(), DockPanel.NORTH);
		this.mainPanel.add(this.sideBarView, DockPanel.WEST);
		this.mainPanel.add(this.mainView, DockPanel.CENTER);
		root.add(this.mainPanel);
		
		//
		// Callback used when session status is changed
		//
		class SessionChangeCallback extends Callback<JavaScriptObject> {
			@Override
			public void onSuccess(JavaScriptObject response) {
				// Make sure cookie is set so we can use the non async method
				renderHomeView();
			}
		}
		
		//
		// Get notified when user session is changed
		//
		SessionChangeCallback sessionChangeCallback = new SessionChangeCallback();
		this.fbEvent.subscribe("auth.sessionChange", sessionChangeCallback);
		
		// Callback used when checking login status
		class LoginStatusCallback extends Callback<JavaScriptObject> {
			@Override
			public void onSuccess(JavaScriptObject response) {
				renderApp(Window.Location.getHash());
			}
		}
		LoginStatusCallback loginStatusCallback = new LoginStatusCallback();
		
		// Get login status
		this.fbCore.getLoginStatus(loginStatusCallback);
	}
	
	/**
	 * Render GUI
	 */
	private void renderApp(String givenToken) {
		
		String token = givenToken.replace("#", "");
		
		if(token == null || "".equals(token) || "#".equals(token)) {
			token = "home";
		}
		
		if(token.endsWith("home")) {
			renderHomeView();
		} else if(token.endsWith("wave")) {
			renderWaveView();
		} else if(token.startsWith("example")) {
			
			/*
			 * Wrap example, display sourcecode link etc.
			 */
			String example = token.split("/")[1];
			
			Example e = null;
			if("stream.publish".equals(example)) {
				e = new StreamPublishExample(this.fbCore);
			} else if("friends".equals(example)) {
				e = new FriendsExample(this.fbCore);
			} else {
				throw new RuntimeException("Unknown example");
			}
			
			VerticalPanel examplePanel = new VerticalPanel();
			examplePanel.setWidth("700px");
			examplePanel.getElement().setId("ExampleView");
			
			HorizontalPanel headerPanel = new HorizontalPanel();
			headerPanel.addStyleName("header");
			headerPanel.add(new HTML("Method: " + e.getMethod()));
			
			Anchor sourceLink = new Anchor("Source");
			sourceLink.addStyleName("sourceLink");
			sourceLink.setTarget("blank");
			sourceLink
			        .setHref("http://code.google.com/p/gwtfb/source/browse/trunk/GwtFB/src/org.xydra.fbsdk4gwt/client/examples/"
			                + e.getSimpleName() + ".java");
			headerPanel.add(sourceLink);
			examplePanel.add(headerPanel);
			
			examplePanel.addStyleName("example");
			e.addStyleName("example");
			examplePanel.add(e);
			// Add example
			this.mainView.setWidget(examplePanel);
			
		} else {
			Window.alert("Unknown  url " + token);
		}
	}
	
	/**
	 * Render GUI when logged in
	 */
	private void renderWhenLoggedIn() {
		this.mainView.setWidget(new UserInfoViewController(this.fbCore));
		FBXfbml.parse();
	}
	
	/**
	 * Render GUI when not logged in
	 */
	private void renderWhenNotLoggedIn() {
		this.mainView.setWidget(new FrontpageViewController());
		FBXfbml.parse();
	}
	
	/**
	 * Render home view. If user is logged in display welcome message, otherwise
	 * display login dialog.
	 */
	private void renderHomeView() {
		this.sideBarView.clear();
		
		if(this.fbCore.getSession() == null) {
			renderWhenNotLoggedIn();
		} else {
			this.sideBarView.setWidget(new HomeSideBarPanel());
			renderWhenLoggedIn();
		}
	}
	
	/**
	 * Render Wave
	 */
	private void renderWaveView() {
		WaveView waveView = new WaveView();
		this.sideBarView.setWidget(new DocSideBarPanel());
		this.mainView.setWidget(waveView);
	}
	
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		renderApp(event.getValue());
	}
	
}
