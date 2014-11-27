package org.xydra.oo.runtime.java;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.core.util.DumpUtils;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.oo.runtime.shared.ArrayProxy;
import org.xydra.oo.runtime.shared.CollectionProxy;
import org.xydra.oo.runtime.shared.ListProxy;
import org.xydra.oo.runtime.shared.SetProxy;
import org.xydra.oo.runtime.shared.SharedProxy;
import org.xydra.oo.runtime.shared.SharedTypeMapping;
import org.xydra.oo.runtime.shared.SortedSetProxy;

/**
 * Java dynamic proxy representing a Java Object that is mapped to a Xydra
 * Object.
 * 
 * @author xamde
 */
@RunsInGWT(false)
public class OOJavaOnlyProxy implements InvocationHandler {

	private static final Logger log = LoggerFactory.getLogger(OOJavaOnlyProxy.class);

	private SharedProxy oop;

	public OOJavaOnlyProxy(XWritableModel model, XId objectId) {
		this.oop = new SharedProxy(model, objectId);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		/* handle Object.class methods special */
		String name = method.getName();
		if (Object.class == method.getDeclaringClass()) {
			if ("equals".equals(name)) {
				return proxy == args[0];
			} else if ("hashCode".equals(name)) {
				return System.identityHashCode(proxy);
			} else if ("toString".equals(name)) {
				return proxy.getClass().getName() + "@"
						+ Integer.toHexString(System.identityHashCode(proxy))
						+ ", with InvocationHandler " + this;
			} else {
				throw new IllegalStateException(String.valueOf(method));
			}
		}

		if ("dump".equals(name)) {
			return DumpUtils.toStringBuffer(this.oop.getXObject()).toString();
		}

		try {
			return invokeIntern(proxy, method, args);
		} catch (Exception e) {
			log.error(method.getReturnType() + " " + method.getName() + "(..)", e);
			throw e;
		} catch (Error e) {
			log.error(method.getReturnType() + " " + method.getName() + "(..)", e);
			throw e;
		}
	}

	private Object invokeIntern(Object proxy, Method method, Object[] args) {
		assert args == null && method.getParameterTypes().length == 0
				|| (args != null && method.getParameterTypes().length == args.length) : args == null ? "args null"
				: "args.len=" + args.length;

		/* Determine method */
		String name = method.getName();
		if (name.equals("getId") && args == null) {
			return this.oop.getId();
		}

		KindOfMethod kindOfMethod = OOReflectionUtils.extractKindOfMethod(method);
		if (kindOfMethod == null) {
			throw new RuntimeException("Cannot handle method "
					+ JavaReflectionUtils.toDebug(method));
		}

		if (!this.oop.hasXObject()) {
			throw new RuntimeException("Object '" + this.oop.getId() + "' does not exist in XModel");
		}

		/* Determine fieldId */
		String fieldId = OOReflectionUtils.extractFieldIdFromMethod(method);
		assert fieldId != null;

		/* ================================ GET ================================ */
		if (kindOfMethod == KindOfMethod.Get) {
			if (args != null) {
				throw new RuntimeException("getter " + JavaReflectionUtils.toDebug(method)
						+ " does not take arguments, has " + args.length);
			}
			Class<?> returnType = method.getReturnType();
			if (OOReflectionUtils.isTranslatableSingleType(returnType)) {
				return _get_(returnType, null, fieldId, false);
			}
			Class<?> componentType = JavaReflectionUtils.getComponentType(method);
			if (OOReflectionUtils.isKnownTranslatableCollectionType(returnType, componentType)) {
				return _get_(returnType, componentType, fieldId, false);
			}
			throw new RuntimeException("Cannot handle type " + returnType.getCanonicalName()
					+ " in setter for " + fieldId);
		} else
		/* ================================ IS ================================ */
		if (kindOfMethod == KindOfMethod.Is) {
			if (args != null) {
				throw new RuntimeException("isXXX() method does not take arguments");
			}
			assert method.getReturnType().equals(Boolean.class);
			// TODO deal with old style booleans (missing field = false)
			Boolean b = (Boolean) _get_(Boolean.class, null, fieldId, false);
			return b;
		} else
		/* ============================== GET COLLECTION ======================= */
		if (kindOfMethod == KindOfMethod.GetCollection) {
			Class<?> returnType = method.getReturnType();
			assert XydraReflectionUtils.isCollectionType(returnType);
			Class<?> componentType = JavaReflectionUtils.getComponentType(method);
			return _get_(returnType, componentType, fieldId, true);
		} else
		/* ================================ SET ================================ */
		if (kindOfMethod == KindOfMethod.Set) {
			if (args == null || args.length != 1) {
				throw new RuntimeException("setXXX() method needs *1* argument, has "
						+ (args == null ? "none" : args.length));
			}
			Class<?> paramType = method.getParameterTypes()[0];
			if (OOReflectionUtils.isTranslatableSingleType(paramType)) {
				_set_(args[0], paramType, null, fieldId);
				return proxy;
			}
			Class<?> componentType = JavaReflectionUtils.getComponentType(method
					.getGenericParameterTypes()[0]);
			if (OOReflectionUtils.isKnownTranslatableCollectionType(paramType, componentType)) {
				_set_(args[0], paramType, componentType, fieldId);
				return proxy;
			}
			throw new RuntimeException("Setter for type '" + paramType.getCanonicalName()
					+ "' not yet impl");
		} else
		/* ============================== Unknown ============================== */
		{
			throw new RuntimeException("Don't know how to handle method '" + name + "' with kind '"
					+ kindOfMethod + "'");
		}
	}

	/**
	 * @param javaType
	 * @param javaComponentType
	 *            @CanBeNull is only set if javaType is a collection type
	 * @param fieldName
	 * @return
	 */
	private Object _get_(Class<?> javaType, Class<?> javaComponentType, String fieldName,
			boolean returnCollectionsAsLiveViews) {
		XValue v = this.oop.getValue(fieldName);
		if (v == null) {
			if (returnCollectionsAsLiveViews && XydraReflectionUtils.isCollectionType(javaType)) {
				return liveCollection(javaType, javaComponentType, fieldName, this.oop.getXModel(),
						this.oop.getXObject());
			} else {
				// handle primitive return values
				if (javaType.equals(byte.class))
					return 0;
				if (javaType.equals(int.class))
					return 0;
				if (javaType.equals(double.class))
					return 0d;
				if (javaType.equals(long.class))
					return 0l;
				if (javaType.equals(boolean.class))
					return false;

				return null;
			}
		}

		Object j = convertToJava(fieldName, v, javaType, javaComponentType, this.oop.getXModel(),
				this.oop.getXObject());
		return j;
	}

	/**
	 * @param param
	 * @param paramComponentType
	 * @param xydraType
	 * @param fieldId
	 */
	private void _set_(Object param, Class<?> paramType, Class<?> paramComponentType,
			String fieldName) {
		XValue v = OOReflectionUtils.convertToXydra(param.getClass(), paramComponentType, param);

		this.oop.setValue(fieldName, v);
	}

	/**
	 * @param fieldName
	 *            @NeverNull
	 * @param v
	 *            @CanBeNull ?
	 * @param type
	 *            @NeverNull
	 * @param componentType
	 *            @CanBeNull
	 * @param model
	 *            @NeverNull
	 * @param object
	 * @return null or Java type
	 */
	public static Object convertToJava(String fieldName, XValue v, Class<?> type,
			Class<?> componentType, XWritableModel model, XWritableObject object) {

		/* 1) Proxy type */
		if (OOReflectionUtils.isProxyType(type)) {
			assert v instanceof XId;
			return OOReflectionUtils.toJavaInstance(type, model, (XId) v);
		}

		/* 2) Collection of Proxy type, i.e. List<IPerson> */
		if (XydraReflectionUtils.isCollectionType(type)
				&& OOReflectionUtils.isProxyType(componentType)) {
			if (v == null || v.getType().isCollection())
				return liveCollection(type, componentType, fieldName, model, object);
			else {
				assert v != null;
				assert !v.getType().isCollection();
				// return casted component type
				assert v instanceof XId : v.getType();
				return OOReflectionUtils.toJavaInstance(type, model, (XId) v);
			}
		}

		/* 3) Auto-convert Enum <-> String */
		if (type.isEnum() && componentType == null) {
			assert v instanceof XStringValue;
			String s = ((XStringValue) v).getValue();

			@SuppressWarnings({ "unchecked", "rawtypes" })
			Class<Enum> enumClass = (Class<Enum>) type;

			@SuppressWarnings("unchecked")
			Object enumValue = Enum.valueOf(enumClass, s);
			return enumValue;
		}

		/* 4) Mapped types */
		SharedTypeMapping mapping = JavaTypeSpecUtils.getMapping(type, componentType);
		if (mapping != null) {
			assert JavaTypeMapping.getJavaBaseType(mapping) != null : "" + mapping;
			assert JavaTypeMapping.getJavaBaseType(mapping).equals(type);
			assert JavaTypeMapping.getJavaComponentType(mapping) == null;
			// Class<?> x = mapping.getXydraType();
			// assert x.isAssignableFrom(x);
			Object o = mapping.toJava((XValue) v);
			return o;
		}

		// unknown
		throw new RuntimeException("Found no mapping for type=" + type.getCanonicalName()
				+ " compType="
				+ (componentType == null ? "NONE" : componentType.getCanonicalName()));
	}

	/**
	 * @param javaCollectionType
	 *            (J)
	 * @param componentType
	 *            (C) object-oriented interface type
	 * @param fieldName
	 * @param model
	 * @param xobject
	 * @return a {@link CollectionProxy} or subclass of it
	 */
	public static <J, C> Object liveCollection(final Class<J> javaCollectionType,
			final Class<C> componentType, final String fieldName, final XWritableModel model,
			final XWritableObject xobject) {
		assert XydraReflectionUtils.isCollectionType(javaCollectionType) : "type="
				+ javaCollectionType.getCanonicalName() + ", compTyp="
				+ componentType.getCanonicalName();

		/* ComponentTransformer: C (Java _C_omponent Type) <--> XValue */
		CollectionProxy.IComponentTransformer<XCollectionValue<XValue>, XValue, J, C> componentTransformer

		= new CollectionProxy.IComponentTransformer<XCollectionValue<XValue>, XValue, J, C>() {

			@SuppressWarnings("unchecked")
			@Override
			public C toJavaComponent(XValue xydraValue) {
				Object o = convertToJava(fieldName, xydraValue, componentType, null, model, xobject);
				return (C) o;
			}

			@Override
			public XValue toXydraComponent(C javaValue) {
				return OOReflectionUtils.convertToXydra(componentType, null, javaValue);
			}

			@Override
			public XCollectionValue<XValue> createCollection() {
				// FIXME create collection

				return OOReflectionUtils.createCollection(javaCollectionType, componentType);
			}
		};

		if (javaCollectionType.equals(List.class)) {
			return new ListProxy<XCollectionValue<XValue>, XValue, J, C>(xobject,
					XX.toId(fieldName), componentTransformer);
		} else if (javaCollectionType.equals(Set.class)) {
			return new SetProxy<XCollectionValue<XValue>, XValue, J, C>(xobject,
					XX.toId(fieldName), componentTransformer);
		} else if (javaCollectionType.equals(SortedSet.class)) {
			return new SortedSetProxy<XCollectionValue<XValue>, XValue, J, C>(xobject,
					XX.toId(fieldName), componentTransformer);
		} else if (javaCollectionType.isArray()) {
			return new ArrayProxy<XCollectionValue<XValue>, XValue, J, C>(xobject,
					XX.toId(fieldName), componentTransformer);
		} else
			throw new IllegalArgumentException(
					"Cannot create a live view for a collection of type "
							+ javaCollectionType.getCanonicalName());
	}
}