package org.xydra.store;

import org.junit.BeforeClass;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommandFactory;
import org.xydra.core.LoggerTestHelper;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class GaeAllowAllStoreReadMethodsTest extends AbstractAllowAllStoreReadMethodsTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}
	
	private XID repositoryID = XX.createUniqueID();
	
	@Override
	protected XydraStore getStore() {
		GaeTestfixer.enable();
		
		if(this.store == null) {
			this.store = getNewStore(new GaePersistence(this.repositoryID));
		}
		
		return this.store;
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XID getRepositoryId() {
		return this.repositoryID;
	}
	
}
