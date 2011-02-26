package org.xydra.store;

import static org.junit.Assert.assertTrue;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommandFactory;
import org.xydra.store.access.HashUtils;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAccessControlManager;
import org.xydra.store.access.XAuthenticationDatabase;
import org.xydra.store.access.XGroupDatabase;


/**
 * @author xamde
 * 
 */
abstract public class AbstractSecureStoreReadMethodsTest extends AbstractStoreReadMethodsTest {
	
	private XAccessControlManager acm;
	protected XAuthenticationDatabase authenticationDb = null;
	protected String correctPass = "Test";
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XID getCorrectUser() {
		if(this.authenticationDb == null) {
			this.authenticationDb = this.store.getXydraStoreAdmin().getAccessControlManager()
			        .getAuthenticationDatabase();
		}
		if(this.acm == null) {
			this.acm = this.store.getXydraStoreAdmin().getAccessControlManager();
		}
		
		// easier in the debugger
		XID actorId = XX.toId("SecureDirk");
		
		if(!this.acm.isAuthenticated(actorId, this.getCorrectUserPasswordHash())) {
			this.authenticationDb.setPasswordHash(actorId,
			        HashUtils.getXydraPasswordHash(this.correctPass));
		}
		assertTrue(this.acm.isAuthenticated(actorId, this.getCorrectUserPasswordHash()));
		
		// TODO IMPROVE give some correct rights without granting EVERYTHING
		/*
		 * I don't think this is a good idea, since the tests relay on the fact
		 * that the given actor is allowed to do everything. ~Bjoern
		 */

		this.acm.getAuthorisationManager().getGroupDatabase()
		        .addToGroup(actorId, XGroupDatabase.ADMINISTRATOR_GROUP_ID);
		XID modelId1 = XX.toId("TestModel1");
		XAddress model1address = XX.toAddress(this.store.getXydraStoreAdmin().getRepositoryId(),
		        modelId1, null, null);
		XAddress repoAddress = XX.toAddress(this.store.getXydraStoreAdmin().getRepositoryId(),
		        null, null, null);
		assertTrue(
		        "admin group is allows to write repo",
		        this.acm.getAuthorisationManager()
		                .getAuthorisationDatabase()
		                .getAccessDefinition(XGroupDatabase.ADMINISTRATOR_GROUP_ID, repoAddress,
		                        XA.ACCESS_WRITE).isAllowed());
		assertTrue(
		        "admin group can write repo",
		        this.acm.getAuthorisationManager().canWrite(XGroupDatabase.ADMINISTRATOR_GROUP_ID,
		                repoAddress));
		assertTrue(
		        "admin group can read repo",
		        this.acm.getAuthorisationManager().canRead(XGroupDatabase.ADMINISTRATOR_GROUP_ID,
		                repoAddress));
		assertTrue(
		        "admin group can read model1",
		        this.acm.getAuthorisationManager().canRead(XGroupDatabase.ADMINISTRATOR_GROUP_ID,
		                model1address));
		
		assertTrue(this.acm.getAuthorisationManager().canKnowAboutModel(actorId, repoAddress,
		        modelId1));
		assertTrue(this.acm.getAuthorisationManager().canRead(actorId, model1address));
		assertTrue(this.acm.getAuthorisationManager().canWrite(actorId, model1address));
		
		return actorId;
	}
	
	@Override
	protected String getCorrectUserPasswordHash() {
		return HashUtils.getXydraPasswordHash(this.correctPass);
	}
	
	@Override
	protected XID getIncorrectUser() {
		/*
		 * By definition of createUniqueID this ID is unknown an is therefore
		 * not registered in the accountDb
		 */
		return XX.createUniqueID();
	}
	
	@Override
	protected String getIncorrectUserPasswordHash() {
		return HashUtils.getXydraPasswordHash("incorrect");
	}
	
	@Override
	protected XID getRepositoryId() {
		return XX.toId("data");
		// repositoryId as set in the standard constructor of {@link
		// MemoryStore}
	}
}
