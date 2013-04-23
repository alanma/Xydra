package org.xydra.base.minio;

import java.io.Reader;


public class MiniStringReader extends AbstractMiniReader implements MiniReader {
	
	private String str;
	private int length;
	private int next = 0;
	private int mark = 0;
	
	/**
	 * Create a new string reader.
	 * 
	 * @param s String providing the character stream.
	 */
	public MiniStringReader(String s) {
		super();
		this.str = s;
		this.length = s.length();
	}
	
	/** Check to make sure that the stream has not been closed */
	private void ensureOpen() throws MiniIOException {
		if(this.str == null)
			throw new MiniIOException("Stream closed");
	}
	
	/**
	 * Read a single character.
	 * 
	 * @return The character read, or -1 if the end of the stream has been
	 *         reached
	 * 
	 * @throws MiniIOException If an I/O error occurs
	 */
	@Override
	public int read() throws MiniIOException {
		synchronized(this.lock) {
			ensureOpen();
			if(this.next >= this.length)
				return -1;
			return this.str.charAt(this.next++);
		}
	}
	
	/**
	 * Read characters into a portion of an array.
	 * 
	 * @param cbuf Destination buffer
	 * @param off Offset at which to start writing characters
	 * @param len Maximum number of characters to read
	 * 
	 * @return The number of characters read, or -1 if the end of the stream has
	 *         been reached
	 * 
	 * @exception MiniIOException If an I/O error occurs
	 */
	@Override
	public int read(char cbuf[], int off, int len) throws MiniIOException {
		synchronized(this.lock) {
			ensureOpen();
			if((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length)
			        || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			} else if(len == 0) {
				return 0;
			}
			if(this.next >= this.length)
				return -1;
			int n = Math.min(this.length - this.next, len);
			this.str.getChars(this.next, this.next + n, cbuf, off);
			this.next += n;
			return n;
		}
	}
	
	/**
	 * Skips the specified number of characters in the stream.
	 * 
	 * @param ns may be negative, even though the <code>skip</code> method of
	 *            the {@link Reader} superclass throws an exception in this
	 *            case. Negative values of <code>ns</code> cause the stream to
	 *            skip backwards. Negative return values indicate a skip
	 *            backwards. It is not possible to skip backwards past the
	 *            beginning of the string.
	 * @return the number of characters that were skipped. If the entire string
	 *         has been read or skipped, then this method has no effect and
	 *         always returns 0.
	 * 
	 * @exception MiniIOException If an I/O error occurs
	 */
	public long skip(long ns) throws MiniIOException {
		synchronized(this.lock) {
			ensureOpen();
			if(this.next >= this.length)
				return 0;
			// Bound skip by beginning and end of the source
			long n = Math.min(this.length - this.next, ns);
			n = Math.max(-this.next, n);
			this.next += n;
			return n;
		}
	}
	
	/**
	 * Tell whether this stream is ready to be read.
	 * 
	 * @return True if the next read() is guaranteed not to block for input
	 * 
	 * @exception MiniIOException If the stream is closed
	 */
	@Override
	public boolean ready() throws MiniIOException {
		synchronized(this.lock) {
			ensureOpen();
			return true;
		}
	}
	
	/**
	 * Tell whether this stream supports the mark() operation, which it does.
	 */
	@Override
	public boolean markSupported() {
		return true;
	}
	
	/**
	 * Mark the present position in the stream. Subsequent calls to reset() will
	 * reposition the stream to this point.
	 * 
	 * @param readAheadLimit Limit on the number of characters that may be read
	 *            while still preserving the mark. Because the stream's input
	 *            comes from a string, there is no actual limit, so this
	 *            argument must not be negative, but is otherwise ignored.
	 * 
	 * @exception IllegalArgumentException If readAheadLimit is < 0
	 * @exception MiniIOException If an I/O error occurs
	 */
	@Override
	public void mark(int readAheadLimit) throws MiniIOException {
		if(readAheadLimit < 0) {
			throw new IllegalArgumentException("Read-ahead limit < 0");
		}
		synchronized(this.lock) {
			ensureOpen();
			this.mark = this.next;
		}
	}
	
	/**
	 * Reset the stream to the most recent mark, or to the beginning of the
	 * string if it has never been marked.
	 * 
	 * @exception MiniIOException If an I/O error occurs
	 */
	@Override
	public void reset() throws MiniIOException {
		synchronized(this.lock) {
			ensureOpen();
			this.next = this.mark;
		}
	}
	
	/**
	 * Close the stream.
	 */
	@Override
	public void close() {
		this.str = null;
	}
	
}