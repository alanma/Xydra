package org.xydra.core.model.tutorial;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.core.LoggerTestHelper;


public class CalendarTest {
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}
	
	@Test
	public void testTest() {
		CalendarManager man = new CalendarManager();
		assertTrue(man.registerNewUser("bla", "blub"));
		
		assertTrue(man.addEvent("bla", "blub", "event", "place", 2010, 3, 3, "desc", 1300, 1400));
	}
	
}
