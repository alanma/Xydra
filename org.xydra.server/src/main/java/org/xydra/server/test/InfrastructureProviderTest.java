package org.xydra.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.xydra.server.impl.IGaeMemCache;
import org.xydra.server.impl.InfrastructureServiceFactory;


@SuppressWarnings("deprecation")
@Deprecated
public class InfrastructureProviderTest {
	
	@Test
	public void test() throws UnsupportedEncodingException {
		IGaeMemCache cache1 = InfrastructureServiceFactory.getMemCache();
		IGaeMemCache cache2 = InfrastructureServiceFactory.getMemCache();
		assertTrue(cache1 == cache2);
		byte[] test;
		test = "test".getBytes("utf-8");
		cache1.put("a", test);
		byte[] test2 = cache1.get("a");
		assertEquals(test, test2);
	}
	
}
