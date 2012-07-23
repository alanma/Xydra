package com.sonicmetrics.core.shared.impl.memory;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;


public class SonicUtils {
	
	public static String toDotString(@NeverNull String category, @CanBeNull String action,
	        @CanBeNull String label) {
		StringBuilder b = new StringBuilder();
		b.append(category.toLowerCase());
		if(isDefined(action)) {
			b.append(".").append(action.toLowerCase());
			if(isDefined(label)) {
				b.append(".").append(label.toLowerCase());
			}
		}
		return b.toString();
		
	}
	
	public static boolean isDefined(String s) {
		return s != null && !s.equals("") && !s.equals("*");
	}
	
}
