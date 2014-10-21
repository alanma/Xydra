package org.xydra.oo.runtime.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.oo.runtime.shared.BaseTypeSpec;
import org.xydra.oo.runtime.shared.IBaseType;
import org.xydra.oo.runtime.shared.IType;
import org.xydra.oo.runtime.shared.TypeSpec;

import com.google.gwt.dom.client.Style.Float;

/**
 * Generic tool for Java reflection, knows nothing about Xydra stuff except the
 * {@link TypeSpec} and {@link BaseTypeSpec} type abstractions.
 * 
 * @author xamde
 */
@RunsInGWT(false)
public class JavaReflectionUtils {

	public static final IBaseType BASETYPE_boolean = JavaTypeSpecUtils
			.createBaseTypeSpec(boolean.class);
	public static final IBaseType BASETYPE_byte = JavaTypeSpecUtils.createBaseTypeSpec(byte.class);
	public static final IBaseType BASETYPE_char = JavaTypeSpecUtils.createBaseTypeSpec(char.class);
	public static final IBaseType BASETYPE_double = JavaTypeSpecUtils
			.createBaseTypeSpec(double.class);
	public static final IBaseType BASETYPE_float = JavaTypeSpecUtils
			.createBaseTypeSpec(float.class);
	public static final IBaseType BASETYPE_int = JavaTypeSpecUtils.createBaseTypeSpec(int.class);
	public static final IBaseType BASETYPE_long = JavaTypeSpecUtils.createBaseTypeSpec(long.class);
	public static final IBaseType BASETYPE_short = JavaTypeSpecUtils
			.createBaseTypeSpec(short.class);

	/**
	 * @param method
	 * @return the component type of a generic type or null
	 */
	public static Class<?> getComponentType(Method method) {
		return getComponentType(method.getGenericReturnType());
	}

	/**
	 * @param genericType
	 * @return raw type of a generic type
	 */
	public static Class<?> getRawType(Type genericType) {
		if (genericType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) genericType;
			return parameterizedType.getRawType().getClass();
		}
		assert genericType instanceof Class<?> : "generic type is " + genericType;
		return (Class<?>) genericType;
	}

	public static Class<?> getComponentType(Type genericType) {
		if (genericType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) genericType;
			Type[] fieldArgTypes = parameterizedType.getActualTypeArguments();
			if (fieldArgTypes.length == 0)
				return null;
			if (fieldArgTypes.length > 1)
				throw new IllegalArgumentException(
						"Multiple generic types found - which is the component type?");
			return (Class<?>) fieldArgTypes[0];
		} else if (genericType instanceof Class) {
			Class<?> type = (Class<?>) genericType;
			if (type.isArray()) {
				return type.getComponentType();
			} else {
				return null;
			}
		} else
			return null;
	}

	/**
	 * @param baseTypeSpec
	 * @return true iff array or any implementation of java.util.Collection
	 */
	public static boolean isJavaCollectionType(IBaseType baseTypeSpec) {
		assert baseTypeSpec != null;
		if (baseTypeSpec.isArray())
			return true;

		try {
			Class<?> c = Class.forName(baseTypeSpec.getCanonicalName());
			Set<Class<?>> interfaces = JavaReflectionUtils.getAllInterfaces(c);
			return interfaces.contains(java.util.Collection.class);
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static Class<?> getComponentType(Field field) {
		return getComponentType(field.getGenericType());
	}

	public static Set<Class<?>> getAllInterfaces(Class<?> c) {
		Set<Class<?>> set = new HashSet<Class<?>>();
		for (Class<?> i : c.getInterfaces()) {
			set.add(i);
			set.addAll(getAllInterfaces(i));
		}
		Class<?> superClass = c.getSuperclass();
		if (superClass != null) {
			set.addAll(getAllInterfaces(superClass));
		}
		return set;
	}

	public static boolean isJavaCollectionType(IType type) {
		return isJavaCollectionType(type.getBaseType());
	}

	public static boolean isJavaPrimitiveType(IBaseType baseType) {
		return baseType.getPackageName() == null;
	}

	public static String toDebug(Method method) {
		return method.getDeclaringClass().getCanonicalName() + "." + method.getName() + "(..)";
	}

	public static Object returnDefaultValueOfPrimitiveType(String primitiveType) {
		if (primitiveType.equals("boolean"))
			return false;
		if (primitiveType.equals("byte"))
			return 0;
		if (primitiveType.equals("char"))
			return 0;
		if (primitiveType.equals("double"))
			return 0d;
		if (primitiveType.equals("float"))
			return 0f;
		if (primitiveType.equals("int"))
			return 0;
		if (primitiveType.equals("long"))
			return 0l;
		if (primitiveType.equals("short"))
			return 0;

		throw new IllegalArgumentException("Not known primitive type: " + primitiveType);
	}

	/**
	 * @param c
	 * @return the corresponding primitive type, if it exists, or null. E.g.
	 *         returns "int.class" for "java.lang.Integer".
	 */
	public static Class<?> getPrimitiveClass(Class<?> c) {
		if (c.equals(Boolean.class))
			return boolean.class;
		if (c.equals(Byte.class))
			return byte.class;
		if (c.equals(Character.class))
			return char.class;
		if (c.equals(Double.class))
			return double.class;
		if (c.equals(Float.class))
			return float.class;
		if (c.equals(Integer.class))
			return int.class;
		if (c.equals(Long.class))
			return long.class;
		if (c.equals(Short.class))
			return short.class;
		return null;
	}

	/**
	 * @param primitiveType
	 * @return the corresponding wrapper type or null, if none exists. E.g.
	 *         returns "java.lang.Boolean" for "boolean".
	 */
	public static Class<?> getWrapperClass(Class<?> primitiveType) {
		if (primitiveType.equals(boolean.class))
			return Boolean.class;
		if (primitiveType.equals(byte.class))
			return Byte.class;
		if (primitiveType.equals(char.class))
			return Character.class;
		if (primitiveType.equals(double.class))
			return Double.class;
		if (primitiveType.equals(float.class))
			return Float.class;
		if (primitiveType.equals(int.class))
			return Integer.class;
		if (primitiveType.equals(long.class))
			return Long.class;
		if (primitiveType.equals(short.class))
			return Short.class;
		return null;
	}

	public static String returnDefaultValueOfPrimitiveTypeAsSourceCodeLiteral(String primitiveType) {
		if (primitiveType.equals("boolean"))
			return "false";
		if (primitiveType.equals("byte"))
			return "0";
		if (primitiveType.equals("char"))
			return "0";
		if (primitiveType.equals("double"))
			return "0d";
		if (primitiveType.equals("float"))
			return "0f";
		if (primitiveType.equals("int"))
			return "0";
		if (primitiveType.equals("long"))
			return "0l";
		if (primitiveType.equals("short"))
			return "0";

		throw new IllegalArgumentException("Not known primitive type: " + primitiveType);
	}

	public static Class<?> forName(IBaseType baseTypeSpec) {
		if (baseTypeSpec == null)
			return null;

		if (baseTypeSpec.getPackageName() == null) {
			// might be a primitive type
			if (baseTypeSpec.getSimpleName().equals("boolean")) {
				return boolean.class;
			}
			if (baseTypeSpec.getSimpleName().equals("byte")) {
				return byte.class;
			}
			if (baseTypeSpec.getSimpleName().equals("int")) {
				return int.class;
			}
			if (baseTypeSpec.getSimpleName().equals("double")) {
				return double.class;
			}
			if (baseTypeSpec.getSimpleName().equals("long")) {
				return long.class;
			}
		}

		try {
			Class<?> c = Class.forName(baseTypeSpec.getCanonicalName());
			return c;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static boolean isPrimitiveArrayOf(TypeSpec type, Class<?> componentType) {
		return type.getBaseType().isArray()
				&& JavaReflectionUtils.equalsClass(type.getComponentType(), componentType);
	}

	public static boolean isEnumType(IType type) {
		assert type != null;
		if (type.getComponentType() != null)
			return false;

		Class<?> c = forName(type.getBaseType());
		if (c == null)
			return false;

		return c.isEnum();
	}

	/**
	 * @param type
	 * @return null or primitive type
	 */
	public static IBaseType getPrimitiveTypeForWrapperClass(IBaseType type) {
		if (JavaReflectionUtils.equalsClass(type, Boolean.class))
			return BASETYPE_boolean;
		if (JavaReflectionUtils.equalsClass(type, Byte.class))
			return BASETYPE_byte;
		if (JavaReflectionUtils.equalsClass(type, Character.class))
			return BASETYPE_char;
		if (JavaReflectionUtils.equalsClass(type, Double.class))
			return BASETYPE_double;
		if (JavaReflectionUtils.equalsClass(type, Float.class))
			return BASETYPE_float;
		if (JavaReflectionUtils.equalsClass(type, Integer.class))
			return BASETYPE_int;
		if (JavaReflectionUtils.equalsClass(type, Long.class))
			return BASETYPE_long;
		if (JavaReflectionUtils.equalsClass(type, Short.class))
			return BASETYPE_short;
		return null;
	}

	public static boolean equalsClass(IType t, Class<?> c) {
		if (c.isArray()) {
			return t.isArray()
					&& JavaReflectionUtils.equalsClass(t.getComponentType(), c.getComponentType());
		} else {
			return JavaReflectionUtils.equalsClass(t.getBaseType(), c);
		}
	}

	public static boolean equalsClass(IBaseType b, Class<?> c) {
		assert c != null;
		return (c.getPackage() == null ? b.getPackageName() == null : c.getPackage().getName()
				.equals(b.getPackageName()))

				&& c.getSimpleName().equals(b.getSimpleName());
	}

	public static TypeSpec createTypeSpec(Class<?> type, String componentPackageName,
			String componentTypeName, String generatedFrom) {
		return new TypeSpec(JavaTypeSpecUtils.createBaseTypeSpec(type), new BaseTypeSpec(
				componentPackageName, componentTypeName), generatedFrom);
	}

	public static TypeSpec createTypeSpec(@CanBeNull Class<?> c, String generatedFrom) {
		if (c == null)
			return null;
		return JavaTypeSpecUtils.createTypeSpec(c, null, generatedFrom);
	}

	public static void main(String[] args) throws ClassNotFoundException {
		TypeSpec t = new TypeSpec(BaseTypeSpec.ARRAY, new BaseTypeSpec(null, "byte"), "");
		assert JavaReflectionUtils.equalsClass(t, byte[].class);

		for (Class<?> c : getAllInterfaces(TreeSet.class))
			System.out.println(c.getCanonicalName());

		assert isJavaCollectionType(JavaReflectionUtils.createTypeSpec(SortedSet.class, "foo"));

		System.out.println(forName(BASETYPE_boolean));

	}

}
