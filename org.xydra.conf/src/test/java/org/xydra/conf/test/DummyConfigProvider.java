package org.xydra.conf.test;

import org.xydra.conf.IConfig;
import org.xydra.conf.IConfigProvider;

public class DummyConfigProvider implements IConfigProvider {

	public static final String FOO = "example.field-foo";

	@Override
	public void configure(IConfig conf) {
	}

}
