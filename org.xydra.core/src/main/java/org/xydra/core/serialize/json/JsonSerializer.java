package org.xydra.core.serialize.json;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.minio.MiniWriter;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraSerializer;


/**
 * {@link XydraSerializer} implementations that creates {@link JsonOut}
 * instances.
 *
 * @author dscharrer
 *
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class JsonSerializer implements XydraSerializer {

	@Override
	public XydraOut create() {
		return new JsonOut();
	}

	@Override
	public XydraOut create(final MiniWriter writer) {
		return new JsonOut(writer);
	}

	@Override
	public String getContentType() {
		return JsonOut.CONTENT_TYPE_JSON;
	}

}
