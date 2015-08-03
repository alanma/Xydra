package org.xydra.env;

import org.xydra.conf.IConfig;

public class SimpleEnvironment implements IEnvironment {

	public SimpleEnvironment(final IConfig config) {
		super();
		this.config = config;
	}

	private final IConfig config;

	@Override
	public IConfig conf() {
		return this.config;
	}

}
