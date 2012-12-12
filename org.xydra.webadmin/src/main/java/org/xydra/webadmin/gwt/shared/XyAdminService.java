package org.xydra.webadmin.gwt.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


@RemoteServiceRelativePath("ajax")
public interface XyAdminService extends RemoteService {
    
    public String getGreeting(String name);
    
}
