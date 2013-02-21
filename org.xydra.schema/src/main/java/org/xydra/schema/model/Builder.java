package org.xydra.schema.model;

import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.MemoryModel;


public class Builder {
	
	public static final XID ACTOR = X.getIDProvider().fromString(Builder.class.getCanonicalName());
	
	public static XModel toXModel(SModel smodel) {
		XID id = X.getIDProvider().fromString(smodel.name.name);
		XModel xmodel = new MemoryModel(ACTOR, "", id);
		for(SObject sobject : smodel.objects) {
			XID objId = X.getIDProvider().fromString(sobject.name.name);
			XObject xo = xmodel.createObject(objId);
			
		}
		return xmodel;
	}
	
}