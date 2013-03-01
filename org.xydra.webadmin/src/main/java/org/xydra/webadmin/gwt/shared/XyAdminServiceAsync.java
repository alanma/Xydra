package org.xydra.webadmin.gwt.shared;

import java.util.Set;

import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface XyAdminServiceAsync {
    
    public void getGreeting(String name, AsyncCallback<String> asyncCallback);
    
    public void getModelIds(XId repoId, AsyncCallback<Set<XId>> asyncCallback);
    
    public void getModelSnapshot(XId repoId, XId modelId,
            AsyncCallback<XReadableModel> asyncCallback);
    
    public void getObjectSnapshot(XId repoId, XId modelId, XId objectId,
            AsyncCallback<XReadableObject> asyncCallback);
    
    // TODO max needs research - add default constructors to
    // org.xydra.base.change.impl.memory.MemoryRepositoryCommand
    public void executeCommand(XId repoId, XCommand command, AsyncCallback<Long> asyncCallback);
    
}
