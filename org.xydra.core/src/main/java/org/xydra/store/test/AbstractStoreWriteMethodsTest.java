package org.xydra.store.test;

import org.junit.Before;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.model.XID;
import org.xydra.store.XydraStore;


public abstract class AbstractStoreWriteMethodsTest extends AbstractStoreTest {
	
	protected XydraStore store;
	protected XCommandFactory factory;
	
	protected XID correctUser, incorrectUser, repoID;
	
	protected String correctUserPass, incorrectUserPass;
	protected long timeout;
	protected long bfQuota;
	protected boolean setUpDone = false, incorrectActorExists = true;
	
	@Before
	public void setUp() {
		
		if(this.setUpDone) {
			return;
			
			/*
			 * This code segment guarantees that the following set-up code will
			 * only be run once. This basically works like an @BeforeClass
			 * method and it not really the most beautiful solution, but
			 * unfortunately we cannot actually use a @BeforeClass method here,
			 * because this is an abstract test and we need to call abstract
			 * methods... but abstract methods cannot be static. There might be
			 * some other kind of workout around this problem, but all I could
			 * think of was/could find on the Internet were even uglier...
			 * ~Bjoern
			 */
		}
		
		this.store = this.getStore();
		this.factory = this.getCommandFactory();
		
		if(this.store == null) {
			throw new RuntimeException(
			        "XydraStore could not be initalized in the setUpClass method!");
		}
		if(this.factory == null) {
			throw new RuntimeException(
			        "XCommandFactory could not be initalized in the setUpClass method!");
		}
		
		this.correctUser = this.getCorrectUser();
		this.correctUserPass = this.getCorrectUserPasswordHash();
		
		if(this.correctUser == null || this.correctUserPass == null) {
			throw new IllegalArgumentException("correctUser or correctUserPass were null");
		}
		
		this.incorrectUser = this.getIncorrectUser();
		this.incorrectUserPass = this.getIncorrectUserPasswordHash();
		this.incorrectActorExists = (this.incorrectUser != null);
		
		this.timeout = getCallbackTimeout();
		
		if(this.timeout <= 0) {
			throw new IllegalArgumentException("Timeout for callbacks must be greater than 0!");
		}
		
		this.bfQuota = getQuotaForBruteForce();
		
		if(this.bfQuota <= 0) {
			throw new IllegalArgumentException("Quota for Login attempts must be greater than 0!");
		}
		
		// get the repository ID of the store
		SynchronousTestCallback<XID> callback = new SynchronousTestCallback<XID>();
		this.store.getRepositoryId(this.correctUser, this.correctUserPass, callback);
		waitOnCallback(callback);
		
		if(callback.getEffect() == null) {
			throw new RuntimeException(
			        "getRepositoryID seems to not work correctly, rendering this test useless!");
		}
		this.repoID = callback.getEffect();
		
		this.setUpDone = true;
	}
	
}
