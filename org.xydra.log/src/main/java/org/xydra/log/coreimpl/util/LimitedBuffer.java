package org.xydra.log.coreimpl.util;

public class LimitedBuffer {

	private final String lineEnd;

	/**
	 * @param maxLen
	 *            in 16-bit chars
	 * @param lineEnd
	 */
	public LimitedBuffer(int maxLen, String lineEnd) {
		this.maxLen = maxLen;
		this.lineEnd = lineEnd;
	}

	/* Max 100 KB */
	private final int maxLen;

	private StringBuffer buf = new StringBuffer();

	public void append(String s) {
		this.buf.append(s);
		if (this.buf.length() > this.maxLen) {
			this.buf = new StringBuffer("(too many logs, deleted past)" + this.lineEnd);
		}
	}

	@Override
	public String toString() {
		return this.buf.toString();
	}

}
