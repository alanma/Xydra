package org.xydra.core.value;

import org.xydra.core.model.XID;


public class TypeSystem {
	
	/**
	 * @param x
	 * @param y
	 * @return true iff the class of type x can hold instances of type y
	 */
	public static boolean canStore(Class<?> x, Class<?> y) {
		return

		// trivial
		x.equals(y) ||

		// XModels super-class axioms
		        (x.equals(XLongValue.class) && y.equals(XIntegerValue.class)) ||

		        // mapping to Javas type system
		        (x.equals(XStringValue.class) && y.equals(String.class)) ||

		        (x.equals(XIntegerValue.class) && y.equals(Integer.class)) ||

		        (x.equals(XDoubleValue.class) && y.equals(Double.class)) ||

		        (x.equals(XLongValue.class) && y.equals(Long.class)) ||

		        (x.equals(XBooleanValue.class) && y.equals(Boolean.class)) ||

		        (x.equals(XIDValue.class) && y.equals(XID.class));
	}
	
	public static Class<?> getXType(Class<?> javaTypeOrXType) {
		if(javaTypeOrXType.equals(Integer.class)) {
			return XIntegerValue.class;
		} else if(javaTypeOrXType.equals(String.class)) {
			return XStringValue.class;
		} else if(javaTypeOrXType.equals(Long.class)) {
			return XLongValue.class;
		} else if(javaTypeOrXType.equals(Double.class)) {
			return XDoubleValue.class;
		} else if(javaTypeOrXType.equals(XID.class)) {
			return XIDValue.class;
		} else if(javaTypeOrXType.equals(Boolean.class)) {
			return XBooleanValue.class;
			
		} else
		// already an XType
		if(javaTypeOrXType.equals(XIntegerValue.class) || javaTypeOrXType.equals(XLongValue.class)
		        || javaTypeOrXType.equals(XDoubleValue.class)
		        || javaTypeOrXType.equals(XBooleanValue.class)
		        || javaTypeOrXType.equals(XStringValue.class)
		        || javaTypeOrXType.equals(XIDValue.class)) {
			return javaTypeOrXType;
		} else {
			throw new RuntimeException("Unknon type " + javaTypeOrXType.getName());
		}
	}
}
