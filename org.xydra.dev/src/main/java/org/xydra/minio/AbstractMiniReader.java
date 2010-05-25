package org.xydra.minio;

/**
 * Common super-class for all kinds of Mini-Readers. Copied from JDK.
 * 
 * @author voelkel
 * 
 */
public class AbstractMiniReader {
	
	/**
	 * The object used to synchronize operations on this stream. For efficiency,
	 * a character-stream object may use an object other than itself to protect
	 * critical sections. A subclass should therefore use the object in this
	 * field rather than <tt>this</tt> or a synchronized method.
	 */
	protected Object lock;
	
	public AbstractMiniReader() {
		this.lock = this;
	}
	
}
