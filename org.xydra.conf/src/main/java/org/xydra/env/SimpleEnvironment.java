package org.xydra.env;

import org.xydra.conf.IConfig;

public class SimpleEnvironment implements IEnvironment {

	public SimpleEnvironment(IConfig config) {
		super();
		this.config = config;
	}

	private IConfig config;

	@Override
	public IConfig conf() {
		return this.config;
	}

}
