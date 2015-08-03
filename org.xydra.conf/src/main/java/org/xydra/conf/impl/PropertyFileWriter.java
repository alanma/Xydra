package org.xydra.conf.impl;

import java.io.IOException;
import java.io.Writer;

import org.xydra.conf.escape.Escaping;

/**
 * Writes java property files with comments.
 *
 * @author xamde
 */
public class PropertyFileWriter {

	private final Writer w;
	private final String lineEnd;

	/**
	 * Uses the platform line ending
	 *
	 * @param w
	 */
	public PropertyFileWriter(final Writer w) {
		this(w, System.getProperty("line.separator") == null ? "\n" : System
				.getProperty("line.separator"));
	}

	/**
	 * @param w
	 * @param lineEnd
	 */
	public PropertyFileWriter(final Writer w, final String lineEnd) {
		this.w = w;
		this.lineEnd = lineEnd;
	}

	/**
	 * @param key
	 * @param value
	 *            @CanBeNull
	 * @throws IOException
	 */
	public void keyValue(final String key, final String value) throws IOException {
		assert key != null;
		this.w.write(Escaping.escape(key, true, false));
		this.w.write("=");
		this.w.write(value == null ? "" : Escaping.escape(value, true, false));
		this.w.write(this.lineEnd);
	}

	/**
	 * @param comment
	 *            @NeverNull
	 * @throws IOException
	 */
	public void comment(final String comment) throws IOException {
		assert comment != null;
		this.w.write("# " + comment + this.lineEnd);
	}

}
