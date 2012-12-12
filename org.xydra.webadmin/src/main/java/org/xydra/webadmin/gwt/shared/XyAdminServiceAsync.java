package org.xydra.webadmin.gwt.shared;

import java.util.Set;

import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface XyAdminServiceAsync {
    
    public void getGreeting(String name, AsyncCallback<String> asyncCallback);
    
    public void getModelIds(XID repoId, AsyncCallback<Set<XID>> asyncCallback);
    
    public void getModelSnapshot(XID repoId, XID modelId,
            AsyncCallback<XReadableModel> asyncCallback);
    
    public void getObjectSnapshot(XID repoId, XID modelId, XID objectId,
            AsyncCallback<XReadableObject> asyncCallback);
    
    // TODO max needs research - add default constructors to
    // org.xydra.base.change.impl.memory.MemoryRepositoryCommand
    // public void executeCommand(XID repoId, XCommand command,
    // AsyncCallback<Long> asyncCallback);
    
}
