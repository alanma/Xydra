package org.xydra.webadmin.gwt.server;

import java.util.Set;

import org.xydra.base.XID;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.webadmin.Utils;
import org.xydra.webadmin.gwt.shared.XyAdminService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;


public class XyAdminServiceImpl extends RemoteServiceServlet implements XyAdminService {
    
    private static final long serialVersionUID = 1L;
    
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
}
