package org.xydra.base.minio;

import java.io.IOException;
import java.io.Reader;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;


/**
 * Delegates to a standard java Reader.
 *
 * @author xamde
 *
 */
@RunsInGWT(false)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class MiniStreamReader implements MiniReader {

	private final Reader r;

	public MiniStreamReader(final Reader reader) {
		this.r = reader;
	}

	@Override
	public void close() throws MiniIOException {
		try {
			this.r.close();
		} catch(final IOException e) {
			throw new MiniIOException(e);
		}
	}

	@Override
	public boolean markSupported() {
		return this.r.markSupported();
	}

	@Override
	public int read() {
		try {
			return this.r.read();
		} catch(final IOException e) {
			throw new MiniIOException(e);
		}
	}

	@Override
	public int read(final char[] cbuf, final int off, final int len) {
		try {
			return this.r.read(cbuf, off, len);
		} catch(final IOException e) {
			throw new MiniIOException(e);
		}
	}

	@Override
	public void mark(final int maxValue) {
		try {
			this.r.mark(maxValue);
		} catch(final IOException e) {
			throw new MiniIOException(e);
		}
	}

	@Override
	public void reset() {
		try {
			this.r.reset();
		} catch(final IOException e) {
			throw new MiniIOException(e);
		}
	}

	@Override
	public boolean ready() {
		try {
			return this.r.ready();
		} catch(final IOException e) {
			throw new MiniIOException(e);
		}
	}

}
