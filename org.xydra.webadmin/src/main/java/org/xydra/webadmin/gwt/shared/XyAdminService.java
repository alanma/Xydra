package org.xydra.webadmin.gwt.shared;

import java.util.Set;

import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


@RemoteServiceRelativePath("ajax")
public interface XyAdminService extends RemoteService {
    
    public String getGreeting(String name);
    
    public Set<XID> getModelIds(XID repoId);
    
    public XReadableModel getModelSnapshot(XID repoId, XID modelId);
    
    public XReadableObject getObjectSnapshot(XID repoId, XID modelId, XID objectId);
    
    public long executeCommand(XID repoId, XCommand command);
    
}
