package org.xydra.webadmin.gwt.shared;

import java.util.Set;

import org.xydra.base.XID;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface XyAdminServiceAsync {
    
    public void getGreeting(String name, AsyncCallback<String> asyncCallback);
    
    public void getModelIds(XID repoId, AsyncCallback<Set<XID>> asyncCallback);
    
}
