package org.xydra.store;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.rest.XydraStoreRestClient;


/**
 * Abstract test framework for Xydra REST APIs.
 * 
 * Subclasses must overwrite {@link #getServerConfig()}.
 * 
 * @author dscharrer
 * 
 */
public abstract class AbstractRestClientWriteMethodsTest extends AbstractStoreWriteMethodsTest {
	
	protected static ServerConfig serverconfig;
	
	abstract protected XydraSerializer getSerializer();
	
	abstract protected XydraParser getParser();
	
	private final XydraSerializer serializer;
	private final XydraParser parser;
	
	public static ServerConfig getServerConfig() {
		if(serverconfig == null) {
			throw new IllegalStateException(
			        "Sublcasses must set the serverconfig in a static{...} block.");
		}
		return serverconfig;
	}
	
	protected AbstractRestClientWriteMethodsTest() {
		this.serializer = getSerializer();
		this.parser = getParser();
	}
	
	protected XydraOut create() {
		return this.serializer.create();
	}
	
	protected XydraElement parse(String data) {
		return this.parser.parse(data);
	}
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
	        .getLogger(AbstractRestClientWriteMethodsTest.class);
	
	private XydraStore clientStore;
	
	@Override
	@Before
	public void before() {
		this.clientStore = new XydraStoreRestClient(getServerConfig().absoluteURI, this.serializer,
		        this.parser);
		super.before();
	}
	
	@After
	public void after() {
		SynchronousCallbackWithOneResult<Set<XID>> mids = new SynchronousCallbackWithOneResult<Set<XID>>();
		this.store.getModelIds(getCorrectUser(), getCorrectUserPasswordHash(), mids);
		assertEquals(SynchronousCallbackWithOneResult.SUCCESS, mids.waitOnCallback(Long.MAX_VALUE));
		XAddress repoAddr = XX.toAddress(getRepositoryId(), null, null, null);
		for(XID modelId : mids.effect) {
			if(modelId.toString().startsWith("internal--")) {
				continue;
			}
			XCommand removeCommand = MemoryRepositoryCommand.createRemoveCommand(repoAddr,
			        XCommand.FORCED, modelId);
			this.store.executeCommands(getCorrectUser(), getCorrectUserPasswordHash(),
			        new XCommand[] { removeCommand }, null);
		}
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XID getCorrectUser() {
		return getServerConfig().testerActor;
	}
	
	@Override
	protected String getCorrectUserPasswordHash() {
		return getServerConfig().testerPasswordHash;
	}
	
	@Override
	protected XID getIncorrectUser() {
		return XX.toId("bob");
	}
	
	@Override
	protected String getIncorrectUserPasswordHash() {
		return "wrong";
	}
	
	@Override
	protected XID getRepositoryId() {
		return getServerConfig().mainRepositoryId;
	}
	
	@Override
	protected XydraStore getStore() {
		return this.clientStore;
	}
	
}
