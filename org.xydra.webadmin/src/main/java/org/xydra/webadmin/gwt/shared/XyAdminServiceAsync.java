package org.xydra.webadmin.gwt.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface XyAdminServiceAsync {
    
    public void getGreeting(String name, AsyncCallback<String> asyncCallback);
    
}
