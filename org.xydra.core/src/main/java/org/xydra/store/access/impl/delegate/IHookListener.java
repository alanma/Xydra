package org.xydra.store.access.impl.delegate;

public interface IHookListener {
	
	void beforeRead();
	
	void beforeWrite();
	
}
