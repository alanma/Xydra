package org.xydra.webadmin.gwt.server;

import java.util.Set;

import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.XX;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.XydraPersistence;
import org.xydra.webadmin.Utils;
import org.xydra.webadmin.gwt.shared.XyAdminService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;


public class XyAdminServiceImpl extends RemoteServiceServlet implements XyAdminService {
    
    private static final long serialVersionUID = 1L;
    private static final XId ACTOR = XX.toId("_XyAdminGwt");
    
    @Override
    public String getGreeting(String name) {
        if(name.toLowerCase().equals("max")) {
            return "Hello Mighty Coder";
        } else {
            return "Hello " + name;
        }
    }
    
    @Override
    public Set<XId> getModelIds(XId repoId) {
        XydraPersistence p = Utils.createPersistence(repoId);
        return p.getManagedModelIds();
    }
    
    @Override
    public XReadableModel getModelSnapshot(XId repoId, XId modelId) {
        XydraPersistence p = Utils.createPersistence(repoId);
        
        XWritableModel snapshot = p.getModelSnapshot(new GetWithAddressRequest(XX.resolveModel(
                repoId, modelId), false));
        
        return snapshot;
    }
    
    @Override
    public XReadableObject getObjectSnapshot(XId repoId, XId modelId, XId objectId) {
        XydraPersistence p = Utils.createPersistence(repoId);
        
        XWritableObject snapshot = p.getObjectSnapshot(new GetWithAddressRequest(XX.resolveObject(
                repoId, modelId, objectId), false));
        
        return snapshot;
    }
    
    @Override
    public long executeCommand(XId repoId, XCommand command) {
        XydraPersistence p = Utils.createPersistence(repoId);
        long result = p.executeCommand(ACTOR, command);
        return result;
    }
}
