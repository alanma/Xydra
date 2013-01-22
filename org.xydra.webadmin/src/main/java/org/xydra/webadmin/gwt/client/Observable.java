package org.xydra.webadmin.gwt.client;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;


public interface Observable {
	
	public void notifyMe(XAddress address, Iterator<XID> iterator);
	
}
