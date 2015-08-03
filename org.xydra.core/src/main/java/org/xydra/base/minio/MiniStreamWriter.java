package org.xydra.base.minio;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;


/**
 * Delegates to a standard java Writer.
 *
 * @author xamde
 *
 */
@RunsInGWT(false)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class MiniStreamWriter implements MiniWriter {

	private final Writer w;

	public MiniStreamWriter(final Writer w) {
		this.w = w;
	}

	public MiniStreamWriter(final OutputStream os) {
		this(wrap(os));
	}

	private static Writer wrap(final OutputStream os) {
		try {
			return new OutputStreamWriter(os, "UTF-8");
		} catch(final UnsupportedEncodingException e) {
			throw new RuntimeException("missing UTF-8 charset", e);
		}
	}

	@Override
    public void close() throws MiniIOException {
		try {
			this.w.close();
		} catch(final IOException e) {
			throw new MiniIOException(e);
		}
	}

	@Override
    public void flush() throws MiniIOException {
		try {
			this.w.flush();
		} catch(final IOException e) {
			throw new MiniIOException(e);
		}
	}

	@Override
    public void write(final String string) throws MiniIOException {
		try {
			this.w.write(string);
		} catch(final IOException e) {
			throw new MiniIOException(e);
		}
	}

	@Override
    public void write(final char c) throws MiniIOException {
		try {
			this.w.write(c);
		} catch(final IOException e) {
			throw new MiniIOException(e);
		}

	}

}
