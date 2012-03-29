package org.xydra.sharedutils;

import org.junit.Test;


public class ReflectionUtilsTest {
	
	@Test
	public void testExceptionWithCause() {
		try {
			RuntimeException e = new RuntimeException("a_test_exception");
			throw e;
		} catch(Exception e) {
			RuntimeException e2 = new RuntimeException(e);
			try {
				throw e2;
			} catch(Exception e3) {
				String s = ReflectionUtils.firstNLines(e3, 50);
				// should have 5 lines
				System.out.println("--------");
				System.out.println(s);
			}
		}
	}
	
}
