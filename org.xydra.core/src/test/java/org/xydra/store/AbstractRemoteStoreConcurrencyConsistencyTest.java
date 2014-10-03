package org.xydra.store;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XId;
import org.xydra.base.change.XCommandFactory;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


/**
 * Abstract test class for testing a remote Xydra store over the REST API,
 * especially the aspects consistency and concurrency
 * 
 * @author voelkel
 */
public abstract class AbstractRemoteStoreConcurrencyConsistencyTest extends AbstractStoreTest {
	
	static {
		LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
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
	
	@Override
	@Before
	public void setUp() {
		this.store = this.createStore();
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
		        createStore());
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
