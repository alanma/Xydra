package org.xydra.schema.model;

import org.xydra.base.XID;
import org.xydra.core.X;


public abstract class SValue implements ISyntax {
	
	/**
	 * Could be: {@link SBoolean}, {@link SDouble}, {@link SList},
	 * {@link SString} or {@link SID}.
	 */
	public static SValue parse(String string) {
		if(string.startsWith("" + SList.START)) {
			return SList.parse(string);
		} else if(string.startsWith("" + SString.QUOTE)) {
			return SString.parse(string);
		} else {
			if(string.equals(SBoolean.TRUE)) {
				return new SBoolean(true);
			} else if(string.equals(SBoolean.TRUE)) {
				return new SBoolean(false);
			}
			
			// try parsing as number or XID
			try {
				Integer i = Integer.parseInt(string);
				return new SNumber(i);
			} catch(NumberFormatException e) {
			}
			
			try {
				Long l = Long.parseLong(string);
				return new SNumber(l);
			} catch(NumberFormatException e) {
			}
			
			try {
				Double d = Double.parseDouble(string);
				return new SDouble(d);
			} catch(NumberFormatException e) {
			}
			
			try {
				XID xid = X.getIDProvider().fromString(string);
				return new SID(xid);
			} catch(IllegalArgumentException e) {
			}
			
			throw new IllegalArgumentException("Could not parse <" + string + "> as a value");
		}
	}
}
