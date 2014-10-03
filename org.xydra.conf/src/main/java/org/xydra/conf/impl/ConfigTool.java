package org.xydra.conf.impl;

import static org.reflections.ReflectionUtils.withName;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.Setting;
import org.xydra.conf.IConfig;
import org.xydra.conf.IConfigProvider;
import org.xydra.conf.annotations.ConfDoc;
import org.xydra.conf.annotations.ConfType;
import org.xydra.conf.annotations.RequireConf;
import org.xydra.conf.annotations.RequireConfInstance;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.scanners.TypesScanner;
import org.reflections.util.ClasspathHelper;

import com.google.common.base.Predicate;

/**
 * Scans annotation of a given source code base and summarises: (1) Which
 * hard-coded compile/runtime flags are provided (via @Setting) and (2) Which
 * runtime configuration keys are expected (via @RequireConf)
 * 
 * @author xamde
 */
@RunsInGWT(false)
public class ConfigTool {

	private static final Logger log = LoggerFactory.getLogger(ConfigTool.class);

	static class AnnotationSpot {

		/**
		 * @param clazz
		 * @param subName
		 *            is null if a class is annotated
		 * @param annotation
		 */
		public AnnotationSpot(Class<?> clazz, String subName, Annotation annotation) {
			super();
			this.annotation = annotation;
			this.clazz = clazz;
			this.subName = subName;
		}

		private Annotation annotation;
		private Class<?> clazz;
		private String subName;

		@Override
		public String toString() {
			return this.annotation.getClass().getSimpleName() + " at '" + this.clazz
					+ (this.subName == null ? "" : "." + this.subName) + "': "
					+ getValue(this.annotation) + " (" + this.clazz.getSimpleName()
					+ ".java:1)-fake";
		}
	}

	static class ProvidedConfKey {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.key == null) ? 0 : this.key.hashCode());
			result = prime * result
					+ ((this.whereDefined == null) ? 0 : this.whereDefined.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProvidedConfKey other = (ProvidedConfKey) obj;
			if (this.key == null) {
				if (other.key != null)
					return false;
			} else if (!this.key.equals(other.key))
				return false;
			if (this.whereDefined == null) {
				if (other.whereDefined != null)
					return false;
			} else if (!this.whereDefined.equals(other.whereDefined))
				return false;
			return true;
		}

		/**
		 * A ConfParams.... class which typically implements
		 * {@link IConfigProvider}
		 */
		@NeverNull
		Class<?> whereDefined;

		/**
		 * @CanBeNull
		 */
		Class<?> type;

		@NeverNull
		String key;

		@CanBeNull
		String doc;

		@Override
		public String toString() {
			return this.whereDefined + "." + this.key + " "
					+ (this.type == null ? "noType" : this.type.getName()) + " "
					+ (this.doc == null ? "noDoc" : this.doc);
		}

	}

	/**
	 * @param annotation
	 * @param packageNames
	 * @return the list of all spots where the given annotation has been found
	 */
	public static List<AnnotationSpot> findAnnotationSpots(Class<? extends Annotation> annotation,
			String... packageNames) {
		Reflections reflections = createReflectionsOnPackages(packageNames);
		List<AnnotationSpot> list = new ArrayList<AnnotationSpot>();

		// types (classes, interfaces)
		for (Class<?> type : reflections.getTypesAnnotatedWith(annotation)) {
			list.add(new AnnotationSpot(type, null, type.getAnnotation(annotation)));
		}

		// fields
		for (Field field : reflections.getFieldsAnnotatedWith(annotation)) {
			list.add(new AnnotationSpot(field.getDeclaringClass(), null, field
					.getAnnotation(annotation)));
		}

		// methods
		for (Method method : reflections.getMethodsAnnotatedWith(annotation)) {
			if (method == null)
				continue;

			assert method != null;
			assert method.getDeclaringClass() != null;
			list.add(new AnnotationSpot(method.getDeclaringClass(), null, method
					.getAnnotation(annotation)));
		}
		return list;
	}

	private static Reflections createReflectionsOnPackages(String... packageNames) {
		Set<URL> packageUrls = new HashSet<URL>();
		for (int i = 0; i < packageNames.length; i++) {
			packageUrls.addAll(ClasspathHelper.forPackage(packageNames[i]));
		}

		Reflections reflections = new Reflections(packageUrls, new TypesScanner(),
				new TypeAnnotationsScanner(), new FieldAnnotationsScanner(),
				new MethodAnnotationsScanner(), new TypeElementsScanner(), new SubTypesScanner()
		// ,new ResourcesScanner()
		);

		return reflections;
	}

	/**
	 * @param annotationInstance
	 * @return @NeverNull
	 */
	private static String getValue(Annotation annotationInstance) {
		if (annotationInstance == null) {
			return "";
		}

		Class<? extends Annotation> annotationClass = annotationInstance.annotationType();
		Set<Method> methods = Reflections.getAllMethods(annotationClass, withName("value"));
		Object result;
		try {
			assert methods.size() == 1;
			Method method = methods.iterator().next();
			result = method.invoke(annotationInstance);

			if (result instanceof Object[]) {
				Object[] a = (Object[]) result;
				if (a.length == 1)
					return a[0].toString();

				return Arrays.toString(a);
			}

			return result.toString();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		/* Make sure the classpath is set up correctly */
		try {
			log.trace("Found Reflections in classpath: " + Reflections.class.getCanonicalName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		List<AnnotationSpot> list = findAnnotationSpots(Setting.class, "com.calpano", "org.xydra");
		for (AnnotationSpot a : list) {
			System.out.println(a);
		}
		list = findAnnotationSpots(RequireConf.class, "com.calpano", "org.xydra");
		for (AnnotationSpot a : list) {
			System.out.println(a);
		}

		analyzeConfiguration();
	}

	/**
	 * Slow.
	 * 
	 * @param packageNames
	 * @return @NeverNull
	 */
	public static Set<ProvidedConfKey> findProvidedKeys(String... packageNames) {
		Set<ProvidedConfKey> provided = new HashSet<>();

		Reflections reflections = createReflectionsOnPackages(packageNames);
		Set<Class<? extends IConfigProvider>> subTypes = reflections
				.getSubTypesOf(IConfigProvider.class);

		for (Class<?> configProvider : subTypes) {
			log.info("Found " + configProvider.getCanonicalName());
			Set<Field> fields = ReflectionUtils.getAllFields(configProvider,
					new Predicate<Field>() {

						@Override
						public boolean apply(Field f) {
							return true;
						}

					});
			for (Field f : fields) {
				ProvidedConfKey p = new ProvidedConfKey();

				p.whereDefined = configProvider;

				ConfDoc doc = f.getAnnotation(ConfDoc.class);
				if (doc != null) {
					String docString = getValue(doc);
					p.doc = docString;
				}

				ConfType type = f.getAnnotation(ConfType.class);
				if (type != null) {
					Class<?> typeClass = type.value();
					p.type = typeClass;
				}

				try {
					p.key = (String) f.get(null);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				provided.add(p);

				// if(docString.length() > 0)
				// System.out.println("/** " + docString + " */");
				// System.out.println(configProvider.getSimpleName() + "." +
				// f.getName() + " "
				// + typeString);
			}
		}
		return provided;
	}

	/**
	 * Look at config keys. Which of them are defined in {@link IConfigProvider}
	 * s? Which keys are requested somewhere? Where is a mismatch? I.e. which
	 * keys are never requested? Which are requested but not defined?
	 */
	public static void analyzeConfiguration() {
		Map<String, ProvidedConfKey> provMap = new TreeMap<>();
		Map<String, AnnotationSpot> reqMap = new TreeMap<>();

		for (AnnotationSpot r : findAnnotationSpots(RequireConf.class, "")) {
			reqMap.put(getValue(r.annotation), r);
		}
		for (AnnotationSpot r : findAnnotationSpots(RequireConfInstance.class, "")) {
			reqMap.put(getValue(r.annotation), r);
		}

		Set<ProvidedConfKey> prov = findProvidedKeys("");
		for (ProvidedConfKey p : prov) {
			provMap.put(p.key, p);
		}

		System.out.println("=== Requested but never defined");
		for (Entry<String, AnnotationSpot> e : reqMap.entrySet()) {
			if (!provMap.containsKey(e.getKey())) {
				System.out.println(e.getKey() + " --> " + e.getValue());
			}
		}

		System.out.println("=== Defined but never used");
		for (Entry<String, ProvidedConfKey> e : provMap.entrySet()) {
			if (!reqMap.containsKey(e.getKey())) {
				System.out.println(e.getKey() + " --> " + e.getValue());
			}
		}
	}

	/**
	 * To be used together with {@link GwtConfigTool}
	 * 
	 * @param conf
	 * @param clientKeys
	 *            which config values to copy over
	 * @return a string to be used in a template
	 */
	public static String generateHostPageDictionary(IConfig conf, String... clientKeys) {
		StringBuffer buf = new StringBuffer();
		buf.append("<script type=\"text/javascript\"><!--\n");
		buf.append("var ");
		buf.append(GwtConfigTool.DICT_NAME);
		buf.append(" = {\n");
		for (String key : clientKeys) {
			Object value = conf.get(key);
			if (!jsonSerialisable(value)) {
				throw new IllegalArgumentException("Client key '" + key + "' results in type "
						+ value.getClass().getName() + " which cannot be JSOnified");
			}
			buf.append("  ");
			buf.append(key);
			buf.append(": \"");
			buf.append(value.toString());
			buf.append("\",\n");
		}
		buf.append("};\n");
		buf.append("//-->\n");
		buf.append("    </script>");
		return buf.toString();
	}

	private static boolean jsonSerialisable(Object value) {
		return value instanceof String || value instanceof Boolean;
	}

	public static void addAll(IConfig conf, Map<String, ?> map) {
		for (Entry<String, ?> e : map.entrySet()) {
			conf.set(e.getKey(), e.getValue());
		}
	}
}
