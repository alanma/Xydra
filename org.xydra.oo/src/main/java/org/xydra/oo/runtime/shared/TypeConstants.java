package org.xydra.oo.runtime.shared;

import org.xydra.base.value.ValueType;

public class TypeConstants {

	public static final String BASE = "org.xydra.base";
	public static final String VALUE = "org.xydra.base.value";

	public static final IBaseType BaseType_XId = new BaseTypeSpec(BASE, "XId");

	public static final TypeSpec Address = new TypeSpec(BASE, "XAddress", null, null,
			"TypeConstants");
	public static final TypeSpec Id = new TypeSpec(BASE, "XId", null, null, "TypeConstants");

	public static final TypeSpec Boolean = new TypeSpec(VALUE, "XBooleanValue", null, null,
			"TypeConstants");
	public static final TypeSpec Binary = new TypeSpec(VALUE, "XBinaryValue", null, null,
			"TypeConstants");
	public static final TypeSpec Double = new TypeSpec(VALUE, "XDoubleValue", null, null,
			"TypeConstants");
	public static final TypeSpec Integer = new TypeSpec(VALUE, "XIntegerValue", null, null,
			"TypeConstants");
	public static final TypeSpec Long = new TypeSpec(VALUE, "XLongValue", null, null,
			"TypeConstants");
	public static final TypeSpec String = new TypeSpec(VALUE, "XStringValue", null, null,
			"TypeConstants");
	public static final TypeSpec AddressList = new TypeSpec(VALUE, "XAddressListValue", null, null,
			"TypeConstants");
	public static final TypeSpec AddressSet = new TypeSpec(VALUE, "XAddressSetValue", null, null,
			"TypeConstants");
	public static final TypeSpec AddressSortedSet = new TypeSpec(VALUE, "XAddressSortedSetValue",
			null, null, "TypeConstants");
	public static final TypeSpec BooleanList = new TypeSpec(VALUE, "XBooleanListValue", null, null,
			"TypeConstants");
	public static final TypeSpec DoubleList = new TypeSpec(VALUE, "XDoubleListValue", null, null,
			"TypeConstants");
	public static final TypeSpec IdList = new TypeSpec(VALUE, "XIdListValue", null, null,
			"TypeConstants");
	public static final TypeSpec IdSet = new TypeSpec(VALUE, "XIdSetValue", null, null,
			"TypeConstants");
	public static final TypeSpec IdSortedSet = new TypeSpec(VALUE, "XIdSortedSetValue", null, null,
			"TypeConstants");
	public static final TypeSpec IntegerList = new TypeSpec(VALUE, "XIntegerListValue", null, null,
			"TypeConstants");
	public static final TypeSpec LongList = new TypeSpec(VALUE, "XLongListValue", null, null,
			"TypeConstants");
	public static final TypeSpec StringList = new TypeSpec(VALUE, "XStringListValue", null, null,
			"TypeConstants");
	public static final TypeSpec StringSet = new TypeSpec(VALUE, "XStringSetValue", null, null,
			"TypeConstants");

	// java.util.collections on BASE

	public static final TypeSpec Address_List = new TypeSpec("java.util", "List", BASE, "XAddress",
			"TypeConstants");
	public static final TypeSpec Address_Set = new TypeSpec("java.util", "Set", BASE, "XAddress",
			"TypeConstants");
	public static final TypeSpec Address_SortedSet = new TypeSpec("java.util", "SortedSet", BASE,
			"XAddress", "TypeConstants");

	public static final TypeSpec Id_List = new TypeSpec("java.util", "List", BASE, "XId",
			"TypeConstants");
	public static final TypeSpec Id_Set = new TypeSpec("java.util", "Set", BASE, "XId",
			"TypeConstants");
	public static final TypeSpec Id_SortedSet = new TypeSpec("java.util", "SortedSet", BASE, "XId",
			"TypeConstants");

	// java.util.Collections on java.lang wrapper types

	public static final TypeSpec Boolean_List = new TypeSpec("java.util", "List", "java.lang",
			"Boolean", "TypeConstants");
	public static final TypeSpec Double_List = new TypeSpec("java.util", "List", "java.lang",
			"Double", "TypeConstants");
	public static final TypeSpec Integer_List = new TypeSpec("java.util", "List", "java.lang",
			"Integer", "TypeConstants");
	public static final TypeSpec Long_List = new TypeSpec("java.util", "List", "java.lang", "Long",
			"TypeConstants");
	public static final TypeSpec String_Set = new TypeSpec("java.util", "Set", "java.lang",
			"String", "TypeConstants");
	public static final TypeSpec String_List = new TypeSpec("java.util", "List", "java.lang",
			"String", "TypeConstants");
	public static final TypeSpec String_SortedSet = new TypeSpec("java.util", "SortedSet",
			"java.lang", "String", "TypeConstants");

	// primitive types
	public static final TypeSpec Javaboolean = new TypeSpec(null, "boolean", null, null,
			"TypeConstants");
	public static final TypeSpec Javadouble = new TypeSpec(null, "double", null, null,
			"TypeConstants");
	public static final TypeSpec Javaint = new TypeSpec(null, "int", null, null, "TypeConstants");
	public static final TypeSpec Javalong = new TypeSpec(null, "long", null, null, "TypeConstants");

	// java.lang wrapper
	public static final TypeSpec JavaBoolean = new TypeSpec("java.lang", "Boolean", null, null,
			"TypeConstants");
	public static final TypeSpec JavaDouble = new TypeSpec("java.lang", "Double", null, null,
			"TypeConstants");
	public static final TypeSpec JavaInteger = new TypeSpec("java.lang", "Integer", null, null,
			"TypeConstants");
	public static final TypeSpec JavaLong = new TypeSpec("java.lang", "Long", null, null,
			"TypeConstants");

	// String
	public static final TypeSpec JavaString = new TypeSpec("java.lang", "String", null, null,
			"TypeConstants");

	// byte[]
	public static final TypeSpec JavabyteArray = new TypeSpec(BaseTypeSpec.ARRAY.getPackageName(),
			BaseTypeSpec.ARRAY.getSimpleName(), null, "byte", "TypeConstants");
	public static final TypeSpec JavabyteList = new TypeSpec("java.util", "List", null, "byte",
			"TypeConstants");

	// Collections of primitives
	public static final TypeSpec JavabooleanList = new TypeSpec("java.util", "List", null,
			"boolean", "TypeConstants");
	public static final TypeSpec JavadoubleList = new TypeSpec("java.util", "List", null, "double",
			"TypeConstants");
	public static final TypeSpec JavaintList = new TypeSpec("java.util", "List", null, "int",
			"TypeConstants");
	public static final TypeSpec JavalongList = new TypeSpec("java.util", "List", null, "long",
			"TypeConstants");

	public static final java.lang.String XV = VALUE + ".XV";

	// public static final TypeSpec Boolean_List =
	// List.class,
	// Boolean.class;

	public static void main(final String[] args) {

		for (final ValueType v : ValueType.values()) {
			for (final String coll : new String[] { "Set", "List", "SortedSet" }) {
				System.out.println("    public static final TypeSpec " + v + "_" + coll
						+ " = new TypeSpec(\"java.util\",\"" + coll + "\", VALUES, \"X" + v
						+ "Value\", \"TypeConstants\");");
			}
		}
	}
}
