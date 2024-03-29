package org.xydra.core.serialize;

import org.xydra.base.minio.MiniWriter;


/**
 * Factory class to create {@link XydraOut} instances that serialize to a
 * specific encoding.
 *
 * @author dscharrer
 *
 */
public interface XydraSerializer {

	/**
	 * @return a {@link XydraOut} instance that writes to a string buffer.
	 */
	XydraOut create();

	/**
	 * @param writer to which to write
	 * @return a {@link XydraOut} instance that writes to a {@link MiniWriter}.
	 */
	XydraOut create(MiniWriter writer);

	/**
	 * @return the MIME content type of the produced output.
	 */
	String getContentType();

}
