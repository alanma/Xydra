package org.xydra.server.impl.gae;

import org.junit.After;
import org.junit.Before;

import com.google.apphosting.api.ApiProxy;


public abstract class LocalServiceTestCase {
	
	@Before
	public void setUp() throws Exception {
		ApiProxy.setEnvironmentForCurrentThread(new LocalStubEnvironment());
		
		// ApiProxy.setDelegate(new ApiProxyLocalImpl(new File("./target/gae"))
		// {
		// // ApiProxyLocalImpl is not visible otherwise
		// });
	}
	
	@After
	public void tearDown() throws Exception {
		// not strictly necessary to null these out but there's no harm either
		ApiProxy.setDelegate(null);
		ApiProxy.setEnvironmentForCurrentThread(null);
	}
}
