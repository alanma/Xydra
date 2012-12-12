package org.xydra.webadmin.gwt.shared;

import java.util.Set;

import org.xydra.base.XID;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


@RemoteServiceRelativePath("ajax")
public interface XyAdminService extends RemoteService {
    
    public String getGreeting(String name);
    
    public Set<XID> getModelIds(XID repoId);
    
}
