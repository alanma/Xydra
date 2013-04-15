package org.xydra.core.index.impl.memory;

import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.index.IIndexFactory;
import org.xydra.core.index.IObjectIndex;
import org.xydra.core.index.IUniqueObjectIndex;


public class IndexFactoryImpl implements IIndexFactory {
	
	@Override
    public IObjectIndex createObjectIndex(XId fieldId, XWritableObject indexObject) {
		return new ObjectIndex(fieldId, indexObject);
	}
	
	@Override
    public IUniqueObjectIndex createUniqueObjectIndex(XId fieldId, XWritableObject indexObject) {
		return new UniqueObjectIndex(fieldId, indexObject);
	}
}
