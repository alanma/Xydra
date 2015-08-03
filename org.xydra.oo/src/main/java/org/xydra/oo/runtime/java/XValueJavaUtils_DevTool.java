package org.xydra.oo.runtime.java;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XValueJavaUtils;

/**
 * Use during development of {@link XValueJavaUtils}
 *
 * @author xamde
 *
 */
public class XValueJavaUtils_DevTool {

	public static void main(final String[] args) {
		for (final ValueType v : ValueType.values()) {
			final String X = v.getXydraInterface().getSimpleName();
			final String J = v.getJavaClass().getSimpleName();

			System.out.println();
			System.out.println("// " + v);
			final String XXX = v.name();

			if (v.isSingle()) {
				// from ...
				System.out.println("public static " + J + " from" + XXX + "(" + X
						+ " xydraValue ) {");
				if (J.equals(X)) {
					System.out.println("    return xydraValue;");
				} else {
					if (!v.getJavaClass().isPrimitive()) {
						System.out.println("    if(xydraValue == null) {");
						System.out.println("        return (" + J
								+ ") XValueJavaUtils.getUninitializedValue(" + J + ".class);");
						System.out.println("    }");
					}
					System.out.println("    return xydraValue.getValue();");
				}
				System.out.println("}");
				System.out.println();

				// to...
				System.out.println("public static " + X + " to" + XXX + "(" + J + " javaValue) {");
				if (J.equals(X)) {
					System.out.println("    return javaValue;");
				} else {
					if (!v.getJavaClass().isPrimitive()) {
						System.out.println("    if(javaValue == null)");
						System.out.println("        return null;");
					}
					System.out.println("    return XV.toValue(javaValue);");
				}
				System.out.println("}");
			} else {
				// e.g. X = XAddressSetValue
				final String C = v.getComponentType().getJavaClass().getSimpleName();

				// from ...
				System.out.println("public static " + J + "<" + C + "> from" + XXX + "(" + X
						+ " xydraValue) {");
				System.out.println("    if(xydraValue == null) {");
				System.out.println("        return null;");
				System.out.println("    }");
				System.out.println("    return as" + J + "(xydraValue.contents());");
				System.out.println("}");
				System.out.println();

				// to ...
				System.out.println("public static " + X + " to" + XXX + "(" + J + "<" + C
						+ "> javaValue) {");
				System.out.println("    if(javaValue == null)");
				System.out.println("        return null;");
				System.out.println("    return XV.to" + XXX + "Value(javaValue);");
				System.out.println("}");
			}
		}
	}

}
