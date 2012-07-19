package com.sonicmetrics.core.shared.impl.memory;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;


public class SonicUtils {
	
	public static String toDotString(@NeverNull String category, @CanBeNull String action,
	        @CanBeNull String label) {
		StringBuilder b = new StringBuilder();
		b.append(category.toLowerCase());
		if(action != null) {
			b.append(".").append(action.toLowerCase());
		}
		if(label != null) {
			b.append(".").append(label.toLowerCase());
		}
		return b.toString();
		
	}
	
}
