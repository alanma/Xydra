package org.xydra.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.xydra.server.impl.IMemCache;
import org.xydra.server.impl.InfrastructureServiceFactory;


public class InfrastructureProviderTest {
	
	@Test
	public void test() throws UnsupportedEncodingException {
		IMemCache cache1 = InfrastructureServiceFactory.getMemCache();
		IMemCache cache2 = InfrastructureServiceFactory.getMemCache();
		assertTrue(cache1 == cache2);
		byte[] test;
		test = "test".getBytes("utf-8");
		cache1.put("a", test);
		byte[] test2 = cache1.get("a");
		assertEquals(test, test2);
	}
	
}
