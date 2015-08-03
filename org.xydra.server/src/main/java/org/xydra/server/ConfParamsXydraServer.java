package org.xydra.server;

import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.conf.IConfig;
import org.xydra.conf.IConfigProvider;
import org.xydra.conf.annotations.ConfDoc;
import org.xydra.conf.annotations.ConfType;

public class ConfParamsXydraServer implements IConfigProvider {

	/** The config key */
	@ConfType(XId.class)
	@ConfDoc("The default ID for a repository in the current XydraRuntime")
	public static final String repoId = "xydra-repoId";

	@Override
	public void configureDefaults(final IConfig conf) {
		conf.setDefault(ConfParamsXydraServer.repoId, Base.toId("data"), true);
	}

}
