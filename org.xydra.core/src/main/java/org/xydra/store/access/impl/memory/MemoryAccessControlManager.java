package org.xydra.store.access.impl.memory;

import org.xydra.store.access.XAuthorisationManager;


public class MemoryAccessControlManager extends DelegatingAccessControlManager {

	public MemoryAccessControlManager(final XAuthorisationManager authorisationManager) {
		super(authorisationManager, new CachingOrMemoryAuthenticationDatabase(null));
	}

}
