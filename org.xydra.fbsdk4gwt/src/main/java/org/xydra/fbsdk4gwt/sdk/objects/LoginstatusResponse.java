package org.xydra.fbsdk4gwt.sdk.objects;

import com.google.gwt.core.client.JavaScriptObject;


/**
 * Wrapper class
 * 
 * @see "http://developers.facebook.com/docs/reference/api/post"
 * 
 */
public class LoginstatusResponse extends JavaScriptObject {
	
	protected LoginstatusResponse() {
	}
	
	public final native JavaScriptObject getSession() /*-{ return this.session; }-*/;
	
}
