package com.sonicmetrics.core.shared.util;

import org.junit.Test;

public class JsonUtilsTest {

	@Test
	public void test() {
		String json = JsonUtils.string("Hello\"&we have\nstuff here") + "";
		System.out.println(json);
		System.out.println(JsonUtils.string(""));
		System.out.println(JsonUtils.string(null));
	}

}
