package org.xydra.conf.impl;

import static org.junit.Assert.assertTrue;
import static org.xydra.conf.escape.EscapingTest.a;
import static org.xydra.conf.escape.EscapingTest.b;
import static org.xydra.conf.escape.EscapingTest.c;
import static org.xydra.conf.escape.EscapingTest.classicWindowsPath;
import static org.xydra.conf.escape.EscapingTest.d;
import static org.xydra.conf.escape.EscapingTest.e;
import static org.xydra.conf.escape.EscapingTest.eC1;
import static org.xydra.conf.escape.EscapingTest.eC2;
import static org.xydra.conf.escape.EscapingTest.eC3;
import static org.xydra.conf.escape.EscapingTest.f;
import static org.xydra.conf.escape.EscapingTest.keys;
import static org.xydra.conf.escape.EscapingTest.strangeUnicodeSign;
import static org.xydra.conf.escape.EscapingTest.weirdWindowsPathWithEscapedBackslashes;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xydra.conf.escape.Escaping;

public class SimpleIOTest {

	private MemoryConfig memoryConfig;

	private File targetFile;

	/**
	 * create config file, write it, read it again
	 *
	 * special regard to *escaping*
	 *
	 */
	@Before
	public void setUp() {
		this.memoryConfig = new MemoryConfig();

		final String dotSlashTarget = "./target/testConfig.conf";
		this.targetFile = new File(dotSlashTarget);
		try {
			this.targetFile.createNewFile();
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testEscapingWithFile() {
		this.memoryConfig.set(a, eC1);
		this.memoryConfig.set(b, eC2);
		this.memoryConfig.set(c, eC3);
		this.memoryConfig.set(d, classicWindowsPath);
		this.memoryConfig.set(e, weirdWindowsPathWithEscapedBackslashes);
		this.memoryConfig.set(f, strangeUnicodeSign);

		this.memoryConfig.set(eC1, a);
		this.memoryConfig.set(eC2, b);
		this.memoryConfig.set(eC3, c);
		this.memoryConfig.set(classicWindowsPath, d);
		this.memoryConfig.set(weirdWindowsPathWithEscapedBackslashes, e);
		this.memoryConfig.set(strangeUnicodeSign, f);

		try {
			ConfigFiles.write(this.memoryConfig, this.targetFile);
		} catch (final IOException e1) {
			e1.printStackTrace();
		}

		final MemoryConfig confAgain = new MemoryConfig();
		try {
			ConfigFiles.read(this.targetFile, confAgain);
		} catch (final IOException e1) {
			e1.printStackTrace();
		}

		System.out.println("keySet original: " + this.memoryConfig.toString() + "\n");
		System.out.println("keySet duplicate: " + confAgain.toString());

		for (final String key : keys) {
			String original = this.memoryConfig.getString(key);
			System.out.println("successfully gotten value from original");
			final String reRead = confAgain.getString(key);
			System.out.println("key: '" + key + "', expected \n'" + Escaping.toCodepoints(original)
					+ "', got \n'" + Escaping.toCodepoints(reRead) + "'\n\n");

			if (original.equals("")) {
				original = "\uF8FF";
			}
			assertTrue(original.equals(reRead));
		}

	}

	@After
	public void tearDown() {
		this.memoryConfig = null;

	}

}
