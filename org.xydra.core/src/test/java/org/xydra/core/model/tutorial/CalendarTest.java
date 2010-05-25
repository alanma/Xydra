package org.xydra.core.model.tutorial;

import junit.framework.TestCase;

import org.junit.Test;


public class CalendarTest extends TestCase {
	@Test
	public void testTest() {
		CalendarManager man = new CalendarManager();
		assertTrue(man.registerNewUser("bla", "blub"));
		
		assertTrue(man.addEvent("bla", "blub", "event", "place", 2010, 3, 3, "desc", 1300, 1400));
	}
}
