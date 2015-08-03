package org.xydra.conf;

import org.xydra.annotations.RunsInGWT;

@RunsInGWT(true)
public class ConfBuilder {

	private final String key;

	private final IConfig config;

	public ConfBuilder(final IConfig config, final String key) {
		this.config = config;
		this.key = key;
	}

	/**
	 * Allows callers to set doc at runtime.
	 *
	 * @param doc
	 * @return this, for a fluent API
	 */
	public IConfig setDoc(final String doc) {
		this.config.setDocumentation(this.key, doc);
		return this.config;
	}

	/**
	 * Allows callers to set desired type at runtime.
	 *
	 * @param typeClass
	 * @return this, for a fluent API
	 */
	public IConfig setType(final Class<?> typeClass) {
		this.config.setType(this.key, typeClass);
		return this.config;
	}

}
