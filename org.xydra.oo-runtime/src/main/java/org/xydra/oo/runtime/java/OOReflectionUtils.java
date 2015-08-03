package org.xydra.oo.runtime.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import org.xydra.annotations.CanBeNull;
import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.oo.runtime.shared.IBaseType;
import org.xydra.oo.runtime.shared.IType;
import org.xydra.oo.runtime.shared.SharedTypeMapping;

/**
 * Specific reflection for Java proxy objects and generated/annotated methods.
 *
 * @author xamde
 */
public class OOReflectionUtils {

	private static final Logger log = LoggerFactory.getLogger(OOReflectionUtils.class);

	/**
	 * @param type
	 * @param componentType
	 * @param value
	 *            can be a wrapped proxy object (Java or GWT), or any Xydra
	 *            value type
	 * @return ...
	 */
	public static XValue convertToXydra(final Class<?> type, final Class<?> componentType, final Object value) {
		if (value instanceof IHasXId) {
			final IHasXId hasXid = (IHasXId) value;
			return hasXid.getId();
		}

		SharedTypeMapping mapping = OOReflectionUtils.getMapping(type, componentType);
		if (mapping == null) {
			// try via interfaces
			for (final Class<?> interfaze : type.getInterfaces()) {
				mapping = OOReflectionUtils.getMapping(interfaze, componentType);
				if (mapping != null) {
					break;
				}
			}
		}
		if (mapping != null) {
			// assert mapping.getJavaType().equals(paramType);
			final XValue v = mapping.toXydra(value);
			return v;
		}

		// auto-convert Enum<->String
		if (type.isEnum() && componentType == null) {
			final String s = ((Enum<?>) value).name();
			return XV.toValue(s);
		}

		throw new RuntimeException("Not yet handling type=" + type.getCanonicalName()
				+ " compType="
				+ (componentType == null ? "NONE" : componentType.getCanonicalName()));
	}

	/**
	 * @param method
	 * @return the Xydra fieldId extracted from the name of the method. This is
	 *         a bit fragile, so better use {@link Field} annotations.
	 */
	public static String extractFieldIdFromMethod(final Method method) {
		String fieldId = OOReflectionUtils.getAnnotatedFieldId(method);
		if (fieldId == null) {
			final String name = method.getName();
			for (final KindOfMethod m : KindOfMethod.values()) {
				if (m.prefix != null && name.startsWith(m.prefix)) {
					fieldId = name.substring(m.prefix.length());
					fieldId = NameUtils.firstLetterLowercased(fieldId);
					break;
				}
			}
			if (fieldId == null) {
				if (XydraReflectionUtils.isCollectionType(method.getReturnType())) {
					fieldId = method.getName();
				} else {
					log.warn("No fieldId extractable from "
							+ method.getDeclaringClass().getCanonicalName() + "."
							+ method.getName() + "()");
				}
			}
			log.warn("Using fieldId '" + fieldId + "' extracted from method name '" + name
					+ "' - Please @Field annotate");
		}
		return fieldId;
	}

	/**
	 * @param method
	 * @return the kind of a method, i.e. Get, Set, Is, or GetCollection
	 */
	public static KindOfMethod extractKindOfMethod(final Method method) {
		final String name = method.getName();
		assert name != null;
		for (final KindOfMethod m : KindOfMethod.values()) {
			if (m.prefix != null && name.startsWith(m.prefix)) {
				return m;
			}
		}

		if (XydraReflectionUtils.isCollectionType(method.getReturnType())) {
			return KindOfMethod.GetCollection;
		} else {
			return null;
		}
	}

	public static String getAnnotatedFieldId(final Class<?> c) {
		final org.xydra.oo.Field annot = c.getAnnotation(org.xydra.oo.Field.class);
		if (annot == null) {
			return null;
		}
		return annot.value();
	}

	public static String getAnnotatedFieldId(final Method method) {
		final org.xydra.oo.Field annot = method.getAnnotation(org.xydra.oo.Field.class);
		if (annot == null) {
			return null;
		}
		return annot.value();
	}

	/**
	 * @param baseTypeSpec
	 * @return true iff type is mapped indirectly via an XId to the Xydra type
	 *         system (has an 'XId getId()' method)
	 */
	public static boolean hasAnId(final IBaseType baseTypeSpec) {
		final Class<?> c = JavaReflectionUtils.forName(baseTypeSpec);
		if (c == null) {
			return false;
		}
		return hasAnId(c);
	}

	/**
	 * @param type
	 * @return true iff type is mapped indirectly via an XId to the Xydra type
	 *         system (has an 'XId getId()' method)
	 */
	public static boolean hasAnId(final Class<?> type) {
		assert type != null;
		Method m;
		try {
			m = type.getMethod("getId");
			return m != null && m.getReturnType().equals(XId.class);
		} catch (final NoSuchMethodException e) {
			return false;
		} catch (final SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param type
	 * @return true iff type is a Java proxy type mapped via XId to a Xydra
	 *         object
	 */
	public static boolean isProxyType(@CanBeNull final Class<?> type) {
		if (type == null) {
			return false;
		}
		try {
			final Method method = type.getMethod("getId");
			return method != null && method.getReturnType().equals(XId.class);
		} catch (final NoSuchMethodException e) {
			return false;
		} catch (final SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isProxyType(final IType type) {
		assert type != null;
		try {
			final Class<?> c = Class.forName(type.getBaseType().getCanonicalName());
			return isProxyType(c);
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}

	public static boolean isProxyType(final IBaseType baseTypeSpec) {
		final Class<?> c = JavaReflectionUtils.forName(baseTypeSpec);
		if (c == null) {
			return false;
		}
		return isProxyType(c);
	}

	/**
	 * @param type
	 * @param componentType
	 *            can be a proxy object or any XValue type
	 * @return a mapping or null
	 */
	public static SharedTypeMapping getMapping(final Class<?> type, final Class<?> componentType) {
		assert type != null;
		if (isProxyType(componentType)) {
			// mapping is always a kind of XId
			return JavaTypeSpecUtils.getMapping(type, XId.class);
		} else {
			// try as simple type
			return JavaTypeSpecUtils.getMapping(type, componentType);
		}
	}

	public static void main(final String[] args) {
		System.out.println("All types");
		for (final ValueType v : ValueType.values()) {
			System.out.println(v.name());
		}
		assert OOReflectionUtils.isTranslatableSingleType(byte[].class);
		assert XydraReflectionUtils.isXydraValueType(JavaTypeSpecUtils
				.createBaseTypeSpec(XId.class));
	}

	/**
	 * @param type
	 * @param componentType
	 * @return ...
	 */
	public static XCollectionValue<XValue> createCollection(final Class<?> type, final Class<?> componentType) {
		if (XydraReflectionUtils.isCollectionType(type)) {
			final SharedTypeMapping mapping = getMapping(type, componentType);
			if (mapping == null) {
				throw new RuntimeException("Not yet handling type=" + type.getCanonicalName()
						+ " compType=" + componentType);
			}
			return mapping.createEmptyXydraCollection();
		} else {
			throw new RuntimeException("Not yet handling type=" + type.getCanonicalName()
					+ " compType=" + componentType);
		}
	}

	/**
	 * @param type
	 * @return true iff we can generate getters and setters that map the given
	 *         type to a defined Xydra type, includes all enum types
	 */
	public static boolean isTranslatableSingleType(final Class<?> type) {
		assert type != null;

		if (type.isEnum()) {
			assertEnumHasRightPackage(type);
		}

		return XydraReflectionUtils.isTranslatableSingleType(type) || isProxyType(type)
				|| type.isEnum() || JavaTypeSpecUtils.getMapping(type, null) != null;
	}

	static void assertEnumHasRightPackage(final Class<?> enumClass) {
		assert enumClass.isEnum() : "check applies only to enums";
		if (!enumClass.getPackage().getName().contains(".shared")) {
			log.error("Enum type " + enumClass.getCanonicalName()
					+ " must be moved to shared package to be reachable at GWT runtime.\n"
					+ "      Typically named .shared relative to .gwt.xml file.");
		}
	}

	public static boolean isKnownTranslatableCollectionType(final Class<?> type, final Class<?> componentType) {
		// check collection type
		if (XydraReflectionUtils.isCollectionType(type)) {
			// check component type of collection
			if (isTranslatableSingleType(componentType)) {
				return true;
			} else {
				throw new RuntimeException("Dont know how to translate collection of type "
						+ type.getCanonicalName() + " with element type "
						+ componentType.getCanonicalName());
			}
		}
		return false;
	}

	/**
	 * @param interfaze
	 * @param model
	 * @param id
	 * @return a Java instance of type T backed by a dynamic proxy that
	 *         delegates persistence to a Xydra object
	 */
	public static <T> T toJavaInstance(final Class<T> interfaze, final XWritableModel model, final XId id) {
		if (log.isTraceEnabled()) {
			log.trace("Creating proxy for " + interfaze.getCanonicalName());
		}

		@SuppressWarnings("unchecked")
		final
		T instance = (T) Proxy.newProxyInstance(interfaze.getClassLoader(), new Class<?>[] {
				interfaze, ICanDump.class }, new OOJavaOnlyProxy(model, id));
		return instance;
	}

	public static boolean isTranslatableCollectionType(final Class<?> type, final Class<?> compType) {
		return XydraReflectionUtils.isCollectionType(type) && isTranslatableSingleType(compType);
	}

	/**
	 * @param type
	 * @param componentType
	 * @param toBeGeneratedTypes
	 * @return false for array types
	 */
	static boolean isToBeGeneratedCollectionType(final Class<?> type, final Class<?> componentType,
			final Set<Class<?>> toBeGeneratedTypes) {
		if (!type.isArray() && XydraReflectionUtils.isCollectionType(type)) {
			final boolean componentOk = toBeGeneratedTypes.contains(componentType);
			if (!componentOk) {
				log.warn("Translatable collection type (" + type
						+ ") with untranslatable component type: " + componentType);
			}
			return componentOk;
		} else {
			return false;
		}
	}

	/**
	 * @param field
	 * @param toBeGeneratedTypes
	 * @return false for array types
	 */
	public static boolean isToBeGeneratedCollectionType(final Field field,
			final Set<Class<?>> toBeGeneratedTypes) {
		final Class<?> type = field.getType();
		final Class<?> compType = JavaReflectionUtils.getComponentType(field);
		return isToBeGeneratedCollectionType(type, compType, toBeGeneratedTypes);
	}

}
