package org.xydra.core.index.impl.memory;

import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.index.IIndexFactory;
import org.xydra.core.index.IObjectIndex;
import org.xydra.core.index.IUniqueObjectIndex;


public class IndexFactoryImpl implements IIndexFactory {

	@Override
    public IObjectIndex createObjectIndex(final XId fieldId, final XWritableObject indexObject) {
		return new ObjectIndex(fieldId, indexObject);
	}

	@Override
    public IUniqueObjectIndex createUniqueObjectIndex(final XId fieldId, final XWritableObject indexObject) {
		return new UniqueObjectIndex(fieldId, indexObject);
	}
}
