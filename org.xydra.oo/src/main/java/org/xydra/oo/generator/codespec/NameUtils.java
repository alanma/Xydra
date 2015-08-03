package org.xydra.oo.generator.codespec;

public class NameUtils {

	public static String toJavaName(final String name) {
		return firstLetterUppercased(name);
	}

	public static String firstLetterUppercased(final String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static String firstLetterLowercased(final String s) {
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	public static String toXFieldName(final String name) {
		return firstLetterLowercased(name);
	}

	public static String toClassName(final Class<?> clazz) {
		final String s = clazz.getSimpleName();
		return "I" + s;
	}

	public static String withoutPackages(final String typeName) {
		if (typeName.equals("void")) {
			return "void";
		}

		assert typeName.contains(".") : "typeName must be a FQ name";

		final String[] s = typeName.split("[.]");
		return s[s.length - 1];
	}

}
