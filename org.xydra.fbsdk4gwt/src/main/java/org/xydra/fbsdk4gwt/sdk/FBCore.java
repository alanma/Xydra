package org.xydra.fbsdk4gwt.sdk;

import org.xydra.fbsdk4gwt.client.Callback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * Class that wraps facebook Javascript SDK
 * 
 * @author ola
 */
public class FBCore {
	
	public static final String CONNECT_JS_PRE = "http://connect.facebook.net/";
	public static final String CONNECT_JS_DEFAULT_MIDDLE = "en_US";
	public static final String CONNECT_JS_POST = "/all.js";
	
	public native boolean isLoaded()
	/*-{
	 return typeof $wnd.FB !== 'undefined' && !!$wnd.FB;
	 }-*/;
	
	/**
	 * Wrapper method
	 */
	public native void api(String path, AsyncCallback<JavaScriptObject> callback)
	/*-{ var app=this;
	$wnd.FB.api (path, function(response){
	app.@org.xydra.fbsdk4gwt.sdk.FBCore::callbackSuccess(Lcom/google/gwt/user/client/rpc/AsyncCallback;Lcom/google/gwt/core/client/JavaScriptObject;)(callback,response);
	});
	}-*/;
	
	public native void api(String path, String params, AsyncCallback<JavaScriptObject> callback)
	/*-{
	   var app=this;
	   $wnd.FB.api (path, params, function(response){
	   app.@org.xydra.fbsdk4gwt.sdk.FBCore::callbackSuccess(Lcom/google/gwt/user/client/rpc/AsyncCallback;Lcom/google/gwt/core/client/JavaScriptObject;)(callback,response);
	   });
	   }-*/;
	
	/**
	 * Wrapper method
	 */
	public native void api(String path, JavaScriptObject params,
	        AsyncCallback<JavaScriptObject> callback)
	/*-{
	     var app=this;
	     $wnd.FB.api (path, params, function(response){
	     app.@org.xydra.fbsdk4gwt.sdk.FBCore::callbackSuccess(Lcom/google/gwt/user/client/rpc/AsyncCallback;Lcom/google/gwt/core/client/JavaScriptObject;)(callback,response);
	     });
	     }-*/;
	
	/**
	 * Wrapper method
	 */
	public native void api(String path, String method, JavaScriptObject params,
	        AsyncCallback<JavaScriptObject> callback)
	/*-{
	     var app=this;
	     $wnd.FB.api (path,method, params, function(response){
	     app.@org.xydra.fbsdk4gwt.sdk.FBCore::callbackSuccess(Lcom/google/gwt/user/client/rpc/AsyncCallback;Lcom/google/gwt/core/client/JavaScriptObject;)(callback,response);
	     });
	     }-*/;
	
	/**
	 * Wrapper method for
	 * "http://developers.facebook.com/docs/reference/javascript/fb.getloginstatus/"
	 */
	public native void getLoginStatus(Callback<JavaScriptObject> callback)
	/*-{
	  var app=this;
	  $wnd.FB.getLoginStatus(function(response) {
	    app.@org.xydra.fbsdk4gwt.sdk.FBCore::callbackSuccess(Lcom/google/gwt/user/client/rpc/AsyncCallback;Lcom/google/gwt/core/client/JavaScriptObject;)(callback,response);
	  });
	}-*/;
	
	/**
	 * Wrapper method
	 */
	public native JavaScriptObject getSession()
	/*-{
	  return $wnd.FB.getSession();
	}-*/;
	
	/**
	 * @see "http://developers.facebook.com/docs/reference/javascript/FB.init"
	 */
	public native void init(String appId, boolean status, boolean cookie, boolean xfbml)
	/*-{
	  $wnd.FB.init({
	  'appId': appId, 
	  'status': status,
	  'cookie': cookie,
	  'xfbml' : xfbml
	  });
	  }-*/;
	
	/**
	 * Wrapper method
	 */
	public native void login(AsyncCallback<JavaScriptObject> callback)
	/*-{
	  var app=this;
	  $wnd.FB.login (function(response){
	    app.@org.xydra.fbsdk4gwt.sdk.FBCore::callbackSuccess(Lcom/google/gwt/user/client/rpc/AsyncCallback;Lcom/google/gwt/core/client/JavaScriptObject;)(callback,response);
	  });
	}-*/;
	
	/**
	 * Wrapper method
	 */
	public native void login(AsyncCallback<JavaScriptObject> callback, String permissions)
	/*-{
	    var app=this;
	    $wnd.FB.login (function(response){
	      app.@org.xydra.fbsdk4gwt.sdk.FBCore::callbackSuccess(Lcom/google/gwt/user/client/rpc/AsyncCallback;Lcom/google/gwt/core/client/JavaScriptObject;)(callback,response);
	    },{perms:permissions});
	}-*/;
	
	/**
	 * Wrapper method
	 */
	public native void logout(AsyncCallback<JavaScriptObject> callback)
	/*-{
	   $wnd.FB.logout(function(response){
	     app.@org.xydra.fbsdk4gwt.sdk.FBCore::callbackSuccess(Lcom/google/gwt/user/client/rpc/AsyncCallback;Lcom/google/gwt/core/client/JavaScriptObject;)(callback,response);
	   });
	}-*/;
	
	/**
	 * Wrapper method
	 */
	public native void ui(JavaScriptObject params, AsyncCallback<JavaScriptObject> callback)
	/*-{
	    var app=this;
	    $wnd.FB.ui(params,function(response){
	      app.@org.xydra.fbsdk4gwt.sdk.FBCore::callbackSuccess(Lcom/google/gwt/user/client/rpc/AsyncCallback;Lcom/google/gwt/core/client/JavaScriptObject;)(callback,response);
	    });
	}-*/;
	
	/**
	 * This code inspired from
	 * "http://stackoverflow.com/questions/4249030/load-javascript-async-then-check-dom-loaded-before-executing-callback"
	 * 
	 * @param uri FIXME should contain the current date to be cached for only 1
	 *            day, not forever
	 * @param callback ...
	 */
	public native void loadScriptAsync(String uri, AsyncCallback<Void> callback)
	/*-{
		
	var script = document.createElement('script'); 
	script.type = "text/javascript";
	script.async = true;
	script.src = uri;
	script.onload = script.onreadystatechange = function() {
	this.@org.xydra.fbsdk4gwt.sdk.FBCore::callbackSuccess(Lcom/google/gwt/user/client/rpc/AsyncCallback;)(callback);
	script.onload = null;
	script.onreadystatechange = null;
	};
	var head = document.getElementsByTagName('head')[0];
	head.appendChild(script);

		}-*/;
	
	/*
	 * Called when method succeeded.
	 */
	protected void callbackSuccess(AsyncCallback<JavaScriptObject> callback, JavaScriptObject obj) {
		callback.onSuccess(obj);
	}
	
	protected void callbackSuccess(AsyncCallback<Void> callback) {
		callback.onSuccess(null);
	}
	
	/**
	 * @param locale like 'en_US'
	 * @param callback ..
	 */
	public void loadConnectJs(String locale, AsyncCallback<Void> callback) {
		loadScriptAsync(CONNECT_JS_PRE + (locale == null ? CONNECT_JS_DEFAULT_MIDDLE : locale)
		        + CONNECT_JS_POST, callback);
	}
	
}
