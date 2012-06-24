package com.sonicmetrics.core;

import org.junit.Before;

import com.sonicmetrics.core.shared.impl.memory.SonicMemoryDB;


public class TestMemorySonicDb extends AbstractTestSonicDb {
	
	@Before
	public void before() {
		super.db = new SonicMemoryDB();
	}
	
}
