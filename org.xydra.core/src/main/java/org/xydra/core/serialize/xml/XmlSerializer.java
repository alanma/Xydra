package org.xydra.core.serialize.xml;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.minio.MiniWriter;


/**
 * {@link XydraSerializer} implementations that creates {@link XmlOut}
 * instances.
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class XmlSerializer implements XydraSerializer {
	
	@Override
	public XydraOut create() {
		return new XmlOut();
	}
	
	@Override
	public XydraOut create(MiniWriter writer) {
		return new XmlOut(writer);
	}
	
}
