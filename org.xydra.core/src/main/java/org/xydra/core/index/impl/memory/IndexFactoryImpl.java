package org.xydra.core.index.impl.memory;

import org.xydra.base.XID;
import org.xydra.core.index.IIndexFactory;
import org.xydra.core.index.IObjectIndex;
import org.xydra.core.index.IUniqueObjectIndex;
import org.xydra.core.model.XObject;


public class IndexFactoryImpl implements IIndexFactory {
	
	public IObjectIndex createObjectIndex(XID fieldId, XObject indexObject) {
		return new ObjectIndex(fieldId, indexObject);
	}
	
	public IUniqueObjectIndex createUniqueObjectIndex(XID fieldId, XObject indexObject) {
		return new UniqueObjectIndex(fieldId, indexObject);
	}
}
