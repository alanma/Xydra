package org.xydra.store.impl.gae;

import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.store.impl.delegate.DelegatingAllowAllStore;


public class GaeAllowAllStore extends DelegatingAllowAllStore {
	
	private static final XId REPO_ID = XX.toId("repo_allow_all");
	
	public GaeAllowAllStore() {
		super(new GaePersistence(REPO_ID));
	}
	
}
