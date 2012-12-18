package org.xydra.webadmin.gwt.server;

import java.util.Set;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.store.GetWithAddressRequest;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.webadmin.Utils;
import org.xydra.webadmin.gwt.shared.XyAdminService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;


public class XyAdminServiceImpl extends RemoteServiceServlet implements XyAdminService {
    
    private static final long serialVersionUID = 1L;
    private static final XID ACTOR = XX.toId("_XyAdminGwt");
    
    @Override
    public String getGreeting(String name) {
        if(name.toLowerCase().equals("max")) {
            return "Hello Mighty Coder";
        } else {
            return "Hello " + name;
        }
    }
    
    @Override
    public Set<XID> getModelIds(XID repoId) {
        XydraPersistence p = Utils.createPersistence(repoId);
        return p.getManagedModelIds();
    }
    
    @Override
    public XReadableModel getModelSnapshot(XID repoId, XID modelId) {
        XydraPersistence p = Utils.createPersistence(repoId);
        
        XWritableModel snapshot = p.getModelSnapshot(new GetWithAddressRequest(XX.resolveModel(
                repoId, modelId), false));
        
        return snapshot;
    }
    
    @Override
    public XReadableObject getObjectSnapshot(XID repoId, XID modelId, XID objectId) {
        XydraPersistence p = Utils.createPersistence(repoId);
        
        XWritableObject snapshot = p.getObjectSnapshot(new GetWithAddressRequest(XX.resolveObject(
                repoId, modelId, objectId), false));
        
        return snapshot;
    }
    
    @Override
    public long executeCommand(XID repoId, XCommand command) {
        XydraPersistence p = Utils.createPersistence(repoId);
        long result = p.executeCommand(ACTOR, command);
        return result;
    }
}
