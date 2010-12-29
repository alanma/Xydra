package org.xydra.core.model.tutorial;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.TemporaryStateStore;
import org.xydra.core.test.TestLogger;


public class CalendarTest {
	
	{
		TestLogger.init();
	}
	
	@BeforeClass
	public static void init() {
		XSPI.setStateStore(new TemporaryStateStore());
	}
	
	@Test
	public void testTest() {
		CalendarManager man = new CalendarManager();
		assertTrue(man.registerNewUser("bla", "blub"));
		
		assertTrue(man.addEvent("bla", "blub", "event", "place", 2010, 3, 3, "desc", 1300, 1400));
	}
	
}
