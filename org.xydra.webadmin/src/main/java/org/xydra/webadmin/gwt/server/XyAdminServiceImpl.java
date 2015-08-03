package org.xydra.webadmin.gwt.server;

import java.util.Set;

import org.xydra.base.Base;
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
	public String getGreeting(final String name) {
		if (name.toLowerCase().equals("max")) {
			return "Hello Mighty Coder";
		} else {
			return "Hello " + name;
		}
	}

	@Override
	public Set<XId> getModelIds(final XId repoId) {
		final XydraPersistence p = Utils.createPersistence(repoId);
		return p.getManagedModelIds();
	}

	@Override
	public XReadableModel getModelSnapshot(final XId repoId, final XId modelId) {
		final XydraPersistence p = Utils.createPersistence(repoId);

		final XWritableModel snapshot = p.getModelSnapshot(new GetWithAddressRequest(Base.resolveModel(
				repoId, modelId), false));

		return snapshot;
	}

	@Override
	public XReadableObject getObjectSnapshot(final XId repoId, final XId modelId, final XId objectId) {
		final XydraPersistence p = Utils.createPersistence(repoId);

		final XWritableObject snapshot = p.getObjectSnapshot(new GetWithAddressRequest(Base.resolveObject(
				repoId, modelId, objectId), false));

		return snapshot;
	}

	@Override
	public long executeCommand(final XId repoId, final XCommand command) {
		final XydraPersistence p = Utils.createPersistence(repoId);
		final long result = p.executeCommand(ACTOR, command);
		return result;
	}
}
