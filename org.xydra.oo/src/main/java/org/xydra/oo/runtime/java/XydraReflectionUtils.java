package org.xydra.oo.runtime.java;

import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.value.ValueType;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.oo.runtime.shared.IBaseType;
import org.xydra.oo.runtime.shared.IType;
import org.xydra.oo.runtime.shared.SharedTypeMapping;

/**
 * Reflection helpers for Xydra. Specific to Xydras type system.
 * 
 * Knows nothing about wrapped proxy types. See {@link OOReflectionUtils} for
 * that.
 * 
 * @author xamde
 */
@RunsInGWT(false)
public class XydraReflectionUtils {

	/** Not all collections can be used with all types */
	private static final Set<Class<?>> javaMappedCollectionTypes = new HashSet<Class<?>>();

	/** Can not be used in collections */
	private static final Set<Class<?>> javaMappedPrimitiveTypes = new HashSet<Class<?>>();

	private static final Set<Class<?>> javaMappedSingleObjectTypes = new HashSet<Class<?>>();

	private static final Logger log = LoggerFactory.getLogger(XydraReflectionUtils.class);

	/** Can not be used in collections */
	private static final Set<Class<?>> xydraTypes = new HashSet<Class<?>>();

	/* create indexes on the type system */
	static {
		log.trace("Init type system indexes");
		for (ValueType v : ValueType.values()) {
			xydraTypes.add(v.getXydraInterface());
			Class<?> javaClass = v.getJavaClass();
			if (v.isCollection()) {
				javaMappedCollectionTypes.add(javaClass);
			} else {
				javaMappedSingleObjectTypes.add(javaClass);
				Class<?> primitive = JavaReflectionUtils.getPrimitiveClass(javaClass);
				if (primitive != null) {
					javaMappedPrimitiveTypes.add(primitive);
				}
			}
		}
	}

	/**
	 * @param type
	 * @return true for all Java collection classes used in OOTypeMappings AND
	 *         true for all Array types
	 */
	public static boolean isCollectionType(Class<?> type) {
		return javaMappedCollectionTypes.contains(type) || type.isArray();
	}

	/**
	 * @param type
	 * @return true iff the given BaseTypeSpec represents a Xydra interface that
	 *         is a known Xydra value type, i.e. a subclass of XValue.
	 */
	public static boolean isXydraValueType(IBaseType type) {
		Class<?> c;
		try {
			c = Class.forName(type.getCanonicalName());
			return xydraTypes.contains(c);
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * @param type
	 * @return true iff the given type is a known type that maps natively to a
	 *         Xydra type.
	 */
	public static boolean isTranslatableSingleType(Class<?> type) {
		return // trivially translatable 1:!
		XydraReflectionUtils.xydraTypes.contains(type)

		|| XydraReflectionUtils.javaMappedPrimitiveTypes.contains(type)

		|| XydraReflectionUtils.javaMappedSingleObjectTypes.contains(type);
	}

	public static ValueType getValueType(IType typeSpec) {
		return SharedTypeMapping.getValueType(typeSpec.getBaseType(), typeSpec.getComponentType());
	}

	public static ValueType getValueType(IBaseType baseType) {
		return SharedTypeMapping.getValueType(baseType, null);
	}

}
