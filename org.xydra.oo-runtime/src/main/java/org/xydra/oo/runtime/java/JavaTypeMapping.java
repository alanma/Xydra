package org.xydra.oo.runtime.java;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.oo.runtime.shared.IMapper;
import org.xydra.oo.runtime.shared.IXydraCollectionFactory;
import org.xydra.oo.runtime.shared.SharedTypeMapping;

/**
 * @author xamde
 *
 */
public class JavaTypeMapping {

	/**
	 * Creates a new mapping and adds it to the list of known mappings.
	 *
	 * @param javaBaseType
	 *            Mapping from this Java type @NeverNull
	 * @param javaComponentType
	 *            potentially with a component type @NeverNull
	 * @param xydraBaseValueType
	 *            @NeverNull
	 * @param mapper
	 *            @NeverNull
	 * @param factory
	 *            @NeverNull
	 */
	public static <J, C, X extends XValue> void addCollectionTypeMapping(final Class<J> javaBaseType,
			@NeverNull final Class<C> javaComponentType, final ValueType xydraBaseValueType,
			@NeverNull final IMapper<C, X> mapper, final IXydraCollectionFactory factory) {
		createMapping(javaBaseType, javaComponentType, xydraBaseValueType, mapper, factory);
	}

	/**
	 * Creates a new mapping and adds it to the list of known mappings.
	 *
	 * @param javaBaseType
	 *            Mapping from this Java type @NeverNull
	 * @param xydraBaseValueType
	 *            @NeverNull
	 * @param mapper
	 *            @NeverNull for Xydra types
	 */
	public static <J, C, X extends XValue> void addSingleTypeMapping(final Class<J> javaBaseType,
			final ValueType xydraBaseValueType, @NeverNull final IMapper<J, X> mapper) {
		createMapping(javaBaseType, null, xydraBaseValueType, mapper, null);
	}

	/**
	 * Creates a new mapping and adds it to the list of known mappings.
	 *
	 * @param enumClass
	 *            @NeverNull
	 */
	public static <T extends Enum<T>> void addSingleTypeEnumMapping(final Class<T> enumClass) {
		assert enumClass.isEnum();
		createMapping(enumClass, null, ValueType.String, new IMapper<T, XStringValue>() {

			@Override
			public T toJava(final XStringValue x) {
				return Enum.valueOf(enumClass, XV.toString(x));
			}

			@Override
			public XStringValue toXydra(final T j) {
				return XV.toValue(((Enum<T>) j).name());
			}

			@Override
			public java.lang.String toJava_asSourceCode() {
				return enumClass.getCanonicalName() + ".valueOf( x.contents() )";
			}

			@Override
			public java.lang.String toXydra_asSourceCode() {
				return XV.class.getCanonicalName() + ".toValue(j.name())";
			}

		}, null);
	}

	/**
	 * @param mapping
	 * @return the component type as a Java class or null if none defined
	 */
	public static Class<?> getJavaComponentType(final SharedTypeMapping mapping) {
		return JavaReflectionUtils.forName(mapping.getTypeSpec().getComponentType());
	}

	/**
	 * @param mapping
	 * @return the Java class of the base type. @NeverNull
	 */
	public static Class<?> getJavaBaseType(final SharedTypeMapping mapping) {
		return JavaReflectionUtils.forName(mapping.getTypeSpec().getBaseType());
	}

	/**
	 * Creates a new mapping and adds it to the list of known mappings.
	 *
	 * @param javaBaseType
	 *            Mapping from this Java type @NeverNull
	 * @param javaComponentType
	 *            potentially with a component type @CanBeNull
	 * @param xydraBaseValueType
	 *            @NeverNull
	 * @param mapper
	 *            @CanBeNull for Xydra types
	 * @param factory
	 *            @CanBeNull for non-collection types
	 * @return ...
	 */
	public static SharedTypeMapping createMapping(final Class<?> javaBaseType,
			@CanBeNull final Class<?> javaComponentType, final ValueType xydraBaseValueType,
			@CanBeNull final IMapper<?, ? extends XValue> mapper, final IXydraCollectionFactory factory) {
		return SharedTypeMapping.createAndAdd(
				JavaTypeSpecUtils.createTypeSpec(javaBaseType, javaComponentType),
				xydraBaseValueType, mapper, factory);
	}

}
