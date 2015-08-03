package org.xydra.base.minio;

public interface MiniReader {

	void close();

	boolean markSupported();

	int read();

	/**
	 * @param cbuf Destination buffer
	 * @param off Offset at which to start writing characters
	 * @param len Maximum number of characters to read
	 *
	 * @return The number of characters read, or -1 if the end of the stream has
	 *         been reached
	 */
	int read(char[] cbuf, int off, int len);

	void mark(int maxValue);

	void reset();

	boolean ready();

}
