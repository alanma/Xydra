package org.xydra.fbsdk4gwt.client.examples;

import org.xydra.fbsdk4gwt.client.Callback;
import org.xydra.fbsdk4gwt.sdk.FBCore;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * Stream publish example
 */
public class StreamPublishExample extends Example {
	
	private VerticalPanel mainPanel = new VerticalPanel();
	private Anchor publishLink = new Anchor("Click to execute stream.publish method");
	private FBCore fbCore;
	
	/*
	 * Constructor
	 */
	public StreamPublishExample(FBCore fbCore) {
		
		this.fbCore = fbCore;
		
		this.publishLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				publish();
			}
		});
		
		this.mainPanel.add(this.publishLink);
		
		initWidget(this.mainPanel);
	}
	
	/**
	 * Show publish dialog.
	 */
	public void publish() {
		JSONObject data = new JSONObject();
		data.put("method", new JSONString("stream.publish"));
		data.put("message", new JSONString("Getting education about Facebook Connect and GwtFB"));
		
		JSONObject attachment = new JSONObject();
		attachment.put("name", new JSONString("GwtFB"));
		attachment.put("caption", new JSONString("The Facebook Connect Javascript SDK and GWT"));
		attachment
		        .put("description",
		                new JSONString(
		                        "A small GWT library that allows you to interact with Facebook Javascript SDK in GWT "));
		attachment.put("href", new JSONString("http://www.gwtfb.com"));
		data.put("attachment", attachment);
		
		JSONObject actionLink = new JSONObject();
		actionLink.put("text", new JSONString("Code"));
		actionLink.put("href", new JSONString("http://www.gwtfb.com"));
		
		JSONArray actionLinks = new JSONArray();
		actionLinks.set(0, actionLink);
		data.put("action_links", actionLinks);
		
		data.put("user_message_prompt", new JSONString("Share your thoughts about Connect and GWT"));
		
		/*
		 * Execute facebook method
		 */
		this.fbCore.ui(data.getJavaScriptObject(), new Callback<JavaScriptObject>());
		
	}
	
	@Override
	public String getMethod() {
		return "fbCore.ui(...), method:stream.publish";
	}
	
	@Override
	public String getSimpleName() {
		return "StreamPublishExample";
	}
	
}
