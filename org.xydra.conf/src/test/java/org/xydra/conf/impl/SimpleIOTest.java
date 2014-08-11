package org.xydra.conf.impl;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gwt.thirdparty.guava.common.collect.Lists;


public class SimpleIOTest {
	
	private MemoryConfig memoryConfig;
	
	private String a = "a";
	private String b = "b";
	private String c = "c";
	private String d = "d";
	private String e = "e";
	private String f = "f";
	
	// escapedCharacter#
	private String eC1 = "\n";
	private String eC2 = ":";
	private String eC3 = "=";
	private String classicWindowsPath = "C:/Users/andre_000/Desktop/";
	private String weirdWindowsPathWithEscapedBackslashes = "C:\\ners\\andre_000\\Desktop\\Usersandre_000Denkwerkzeug Knowledge Files\\my";
	private String strangeUnicodeSign = "";
	
	private ArrayList<String> keys = Lists.newArrayList(this.a, this.b, this.c, this.d, this.e,
	        this.f, this.eC1, this.eC2, this.eC3, this.classicWindowsPath,
	        this.weirdWindowsPathWithEscapedBackslashes, this.strangeUnicodeSign);
	
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
		
		String dotSlashTarget = "./target/testConfig.conf";
		this.targetFile = new File(dotSlashTarget);
		try {
			this.targetFile.createNewFile();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testEscapingWithFile() {
		this.memoryConfig.set(this.a, this.eC1);
		this.memoryConfig.set(this.b, this.eC2);
		this.memoryConfig.set(this.c, this.eC3);
		this.memoryConfig.set(this.d, this.classicWindowsPath);
		this.memoryConfig.set(this.e, this.weirdWindowsPathWithEscapedBackslashes);
		this.memoryConfig.set(this.f, this.strangeUnicodeSign);
		
		this.memoryConfig.set(this.eC1, this.a);
		this.memoryConfig.set(this.eC2, this.b);
		this.memoryConfig.set(this.eC3, this.c);
		this.memoryConfig.set(this.classicWindowsPath, this.d);
		this.memoryConfig.set(this.weirdWindowsPathWithEscapedBackslashes, this.e);
		this.memoryConfig.set(this.strangeUnicodeSign, this.f);
		
		try {
			ConfigFiles.write(this.memoryConfig, this.targetFile);
		} catch(IOException e1) {
			e1.printStackTrace();
		}
		
		MemoryConfig confAgain = new MemoryConfig();
		try {
			ConfigFiles.read(this.targetFile, confAgain);
		} catch(IOException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("keySet original: " + this.memoryConfig.toString() + "\n");
		System.out.println("keySet duplicate: " + confAgain.toString());
		
		for(String key : this.keys) {
			Object original = this.memoryConfig.get(key);
			System.out.println("successfully gotten value from original");
			Object reRead = confAgain.get(key);
			System.out.println("key: '" + key + "', expected \n'" + original + "', got \n'"
			        + reRead + "'\n\n");
			assertTrue(original.equals(reRead));
		}
		
	}
	
	@After
	public void tearDown() {
		this.memoryConfig = null;

	}
	
}
