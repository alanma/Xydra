package org.xydra.base.minio;

import java.io.IOException;
import java.io.Reader;

import org.xydra.annotations.RunsInGWT;

@RunsInGWT(false)
public class MiniReaderToReader extends Reader {

	private final MiniReader miniReader;

	public MiniReaderToReader(final MiniReader miniReader) {
		this.miniReader = miniReader;
	}

	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {
		return this.miniReader.read(cbuf, off, len);
	}

	@Override
	public void close() throws IOException {
		this.miniReader.close();
	}

}
