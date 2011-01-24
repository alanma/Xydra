package org.xydra.core.index.impl.memory;

import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.index.IIndexFactory;
import org.xydra.core.index.IObjectIndex;
import org.xydra.core.index.IUniqueObjectIndex;


public class IndexFactoryImpl implements IIndexFactory {
	
	public IObjectIndex createObjectIndex(XID fieldId, XWritableObject indexObject) {
		return new ObjectIndex(fieldId, indexObject);
	}
	
	public IUniqueObjectIndex createUniqueObjectIndex(XID fieldId, XWritableObject indexObject) {
		return new UniqueObjectIndex(fieldId, indexObject);
	}
}
