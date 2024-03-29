package org.xydra.oo.runtime.shared;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;

/**
 * A single type with a package and a simple name. Is either a collection type
 * (List, Set, ...) OR a component type (String, int, boolean, ... ) OR a
 * generic (typeless) array type (Array).
 *
 * To represent generic collection types or normal typed arrays, use a
 * {@link TypeSpec}.
 *
 * @author xamde
 */
@RunsInGWT(true)
public class BaseTypeSpec implements Comparable<BaseTypeSpec>, IBaseType {

	public static final IBaseType ARRAY = new BaseTypeSpec("org.xydra.oo.runtime.shared", "Array");

	@CanBeNull
	private String packageName;

	@NeverNull
	private String simpleName;

	public BaseTypeSpec(@CanBeNull final String packageName, @NeverNull final String simpleName) {
		init(packageName, simpleName);
	}

	/**
	 * Create a clone
	 *
	 * @param baseType
	 */
	public BaseTypeSpec(final IBaseType baseType) {
		this(baseType.getPackageName(), baseType.getSimpleName());
	}

	/**
	 * @param packageName
	 *            can be null for primitive Java types
	 * @param simpleName
	 *            can contain dots to represent inner classes as
	 *            outercclass.innerclass
	 */
	private void init(@CanBeNull final String packageName, @NeverNull final String simpleName) {
		assert simpleName != null;
		assert simpleName.length() > 0;
		assert !simpleName.equals("void");
		if (simpleName.endsWith("[]")) {
			throw new IllegalArgumentException("type cannot be an array type");
		}
		this.packageName = packageName;
		this.simpleName = simpleName;
		assert packageName != null || simpleName.equals("byte") || simpleName.equals("boolean")
				|| simpleName.equals("char") || simpleName.equals("double")
				|| simpleName.equals("float") || simpleName.equals("int")
				|| simpleName.equals("long") || simpleName.equals("short");
	}

	@Override
	public int compareTo(final BaseTypeSpec o) {
		final int i = this.packageName.compareTo(o.getPackageName());
		if (i != 0) {
			return i;
		}

		return this.simpleName.compareTo(o.getSimpleName());
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof BaseTypeSpec) {
			final BaseTypeSpec obts = (BaseTypeSpec) o;
			return obts.packageName.equals(this.packageName)
					&& obts.simpleName.equals(this.simpleName);
		}
		return false;
	}


	@Override
	public String getCanonicalName() {
		return (this.packageName == null ? "" : this.packageName + ".") + this.simpleName;
	}


	@Override
	public String getPackageName() {
		return this.packageName;
	}


	@Override
	public String getSimpleName() {
		return this.simpleName;
	}

	@Override
	public int hashCode() {
		return (this.packageName == null ? 0 : this.packageName.hashCode())
				+ this.simpleName.hashCode();
	}

	@Override
	public String toString() {
		return getCanonicalName();
	}


	@Override
	public boolean isArray() {
		return equals(ARRAY);
	}


	@Override
	public String getRequiredImport() {
		if (getSimpleName().contains(".")) {
			// require enclosing class
			final String[] parts = getSimpleName().split("[.]");
			assert parts.length == 2 : "cannot handle multiple level deep nested inner classes";
			return getPackageName() + "." + parts[0];
		} else {
			return getCanonicalName();
		}
	}

	/**
	 * @param packageName
	 *            @CanBeNull
	 * @param simpleName
	 *            @CanBeNull
	 * @return null if simpleName is null
	 */
	public static IBaseType create(final java.lang.String packageName, final java.lang.String simpleName) {
		if (simpleName == null) {
			return null;
		}
		return new BaseTypeSpec(packageName, simpleName);
	}

}
