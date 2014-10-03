package org.xydra.conf;

import org.xydra.annotations.RunsInGWT;

@RunsInGWT(true)
public class ConfigException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConfigException(String msg) {
		super(msg);
	}

	public ConfigException(String msg, Throwable t) {
		super(msg, t);
	}

}
