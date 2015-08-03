package org.xydra.core.serialize;

/**
 * Base class for serialization tests.
 *
 * @author dscharrer
 *
 */
abstract public class AbstractSerializingTest {

	abstract protected XydraSerializer getSerializer();

	abstract protected XydraParser getParser();

	private final XydraSerializer serializer;
	private final XydraParser parser;

	protected AbstractSerializingTest() {
		this.serializer = getSerializer();
		this.parser = getParser();
	}

	protected XydraOut create() {
		return this.serializer.create();
	}

	protected XydraElement parse(final String data) {
		return this.parser.parse(data);
	}

}
