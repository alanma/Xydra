package org.xydra.webadmin.gwt.shared;

import java.util.Set;

import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("ajax")
public interface XyAdminService extends RemoteService {

	public String getGreeting(String name);

	public Set<XId> getModelIds(XId repoId);

	public XReadableModel getModelSnapshot(XId repoId, XId modelId);

	public XReadableObject getObjectSnapshot(XId repoId, XId modelId, XId objectId);

	public long executeCommand(XId repoId, XCommand command);

}
