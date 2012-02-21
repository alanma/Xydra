package org.xydra.base.change;

import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableModel;


public interface XSessionModel extends XWritableModel {
	
	XSessionModel loadObject(XID objectId);
	
	XSessionModel loadAllObjects();
	
}
