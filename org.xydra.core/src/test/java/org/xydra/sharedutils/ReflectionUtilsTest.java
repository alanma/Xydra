package org.xydra.sharedutils;

import org.junit.Test;


public class ReflectionUtilsTest {

	@Test
	public void testExceptionWithCause() {
		try {
			final RuntimeException e = new RuntimeException("a_test_exception");
			throw e;
		} catch(final Exception e) {
			final RuntimeException e2 = new RuntimeException(e);
			try {
				throw e2;
			} catch(final Exception e3) {
				final String s = ReflectionUtils.firstNLines(e3, 50);
				// should have 5 lines
				System.out.println("--------");
				System.out.println(s);
			}
		}
	}

}
