package org.xydra.core.index.impl.memory;

import org.xydra.core.index.IIndexFactory;
import org.xydra.core.index.IObjectIndex;
import org.xydra.core.index.IUniqueObjectIndex;
import org.xydra.core.model.XID;
import org.xydra.core.model.XObject;


public class IndexFactoryImpl implements IIndexFactory {
	
	public IObjectIndex createObjectIndex(XID fieldID, XObject indexObject) {
		return new ObjectIndex(fieldID, indexObject);
	}
	
	public IUniqueObjectIndex createUniqueObjectIndex(XID fieldID, XObject indexObject) {
		return new UniqueObjectIndex(fieldID, indexObject);
	}
}
