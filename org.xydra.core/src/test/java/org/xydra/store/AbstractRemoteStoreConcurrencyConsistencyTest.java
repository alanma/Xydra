package org.xydra.store;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.base.change.XCommandFactory;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


/**
 * Abstract test class for testing a remote Xydra store over the REST API,
 * especially the aspects consistency and concurrency
 * 
 * @author voelkel
 */
public abstract class AbstractRemoteStoreConcurrencyConsistencyTest extends AbstractStoreTest {
	
	static {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
	}
	
	private static final Logger log = LoggerFactory.getLogger(AbstractStoreReadMethodsTest.class);
	
	private XId correctUser;
	protected String correctUserPass;
	
	protected XCommandFactory factory;
	protected XydraStore store;
	
	protected PersistenceOnStore persistence;
	protected WritableRepositoryOnPersistence repo;
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XId getIncorrectUser() {
		return null;
	}
	
	@Override
	protected String getIncorrectUserPasswordHash() {
		return null;
	}
	
	@Before
	public void before() {
		this.store = this.getStore();
		this.factory = this.getCommandFactory();
		
		if(this.store == null) {
			throw new RuntimeException("XydraStore could not be initalized in the setUp method!");
		}
		if(this.factory == null) {
			throw new RuntimeException(
			        "XCommandFactory could not be initalized in the setUp method!");
		}
		
		this.correctUser = this.getCorrectUser();
		this.correctUserPass = this.getCorrectUserPasswordHash();
		
		if(this.correctUser == null || this.correctUserPass == null) {
			throw new IllegalArgumentException("correctUser or correctUserPass were null");
		}
		
		this.persistence = new PersistenceOnStore(getCorrectUser(), getCorrectUserPasswordHash(),
		        getStore());
		this.repo = new WritableRepositoryOnPersistence(this.persistence, getCorrectUser());
	}
	
	@Test
	public void testConnectionWorks() {
		this.repo.createModel(XX.toId("justThere"));
		for(XId modelId : this.repo) {
			log.info("Found model " + modelId);
		}
	}
	
}
