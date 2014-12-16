package org.xydra.base.minio;

import java.io.IOException;
import java.io.Reader;

import org.xydra.annotations.RunsInGWT;

@RunsInGWT(false)
public class MiniReaderToReader extends Reader {

	private MiniReader miniReader;

	public MiniReaderToReader(MiniReader miniReader) {
		this.miniReader = miniReader;
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return this.miniReader.read(cbuf, off, len);
	}

	@Override
	public void close() throws IOException {
		this.miniReader.close();
	}

}
