package com.sonicmetrics.core.shared.util;

import com.google.gwt.regexp.shared.RegExp;


public class ValidationUtils {
	
	public static RegExp compilePattern(String regex) {
		return RegExp.compile(regex);
	}
	
	public static boolean matches(RegExp regex, String s) {
		return regex.test(s);
	}
	
}
