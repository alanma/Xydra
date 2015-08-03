package org.xydra.conf.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.Setting;
import org.xydra.conf.ConfBuilder;
import org.xydra.conf.ConfigException;
import org.xydra.conf.IConfig;
import org.xydra.conf.IResolver;
import org.xydra.index.impl.MapSetIndex;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

@RunsInGWT(true)
public class MemoryConfig implements IConfig {

	// TODO need to check if this one runs in GWT‚
	private static class ClassResolver<T> implements IResolver<T> {

		private final Class<? extends T> clazz;

		/**
		 * @param clazz
		 * @NeverNull
		 */
		public ClassResolver(final Class<? extends T> clazz) {
			this.clazz = clazz;
		}

		@Override
		public boolean canResolve() {
			return true;
		}

		@Override
		public T resolve() {
			return MemoryConfig_GwtEmul.newInstance(this.clazz);
		}

	}

	private static class InstanceResolver<T> implements IResolver<T> {

		private final T instance;

		/**
		 * @param instance
		 * @CanBeNull
		 */
		public InstanceResolver(final T instance) {
			this.instance = instance;
		}

		@Override
		public boolean canResolve() {
			return this.instance != null;
		}

		@Override
		public T resolve() {
			return this.instance;
		}

	}

	private static Logger log;

	@Setting("Compile-time flag, should be false for high performance")
	private static boolean traceOrigins = true;

	/**
	 * @param className
	 * @NeverNull
	 * @return
	 * @throws RuntimeException
	 *             when class could not be loaded or has wrong type
	 */
	private static <T> IResolver<T> createResolverFromClassName(final String className) {
		assert className != null;
		assert className.length() > 0;

		// try dynamic class loading
		final Class<?> clazz = MemoryConfig_GwtEmul.classForName(className);
		if (clazz == null) {
			throw new RuntimeException("Class '" + className + "' could not be loaded");
		}
		final Object instance = MemoryConfig_GwtEmul.newInstance(clazz);
		try {
			@SuppressWarnings("unchecked")
			final
			T t = (T) instance;
			return new InstanceResolver<T>(t);
		} catch (final ClassCastException e) {
			throw new RuntimeException("Defined class '" + clazz.getName()
			+ "' does not implement required type", e);
		}

	}

	public static String[] decodeList(final String s) {
		return s.split("[|]");
	}

	public static String encodeList(final String[] strings) {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			b.append(strings[i]);
			if (i + 1 < strings.length) {
				b.append("|");
			}
		}
		return b.toString();
	}

	// delayed log init for improved participation in boot sequences
	private static void ensureLogInit() {
		if (log == null) {
			log = LoggerFactory.getLogger(MemoryConfig.class);
		}
	}

	/** default values, are used if no explicit value has been defined */
	private final TreeMap<String, Object> defaults = new TreeMap<String, Object>();

	/** human-readable */
	private HashMap<String, String> docs = new HashMap<String, String>();

	/** values that override the defaults */
	private final TreeMap<String, Object> explicit = new TreeMap<String, Object>();

	/** for debugging */
	private final String internalId;

	private final MapSetIndex<String, Class<?>> required = MapSetIndex.createWithFastEntrySets();

	private final Set<Exception> setOrigins = new HashSet<Exception>();

	/** informative */
	private final HashMap<String, Class<?>> types = new HashMap<String, Class<?>>();

	public MemoryConfig() {
		this.internalId = "" + (int) (Math.random() * 10000d);
	}

	private MemoryConfig(final String internalId) {
		this.internalId = internalId;
	}

	@Override
	public void addRequiredSetting(final Enum<?> key, final Class<?> caller) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		addRequiredSetting(key.name(), caller);
	}

	@Override
	public void addRequiredSetting(final String key, final Class<?> caller) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		this.required.index(key, caller);
	}

	@Override
	public Map<String, Object> asMap() {
		final Map<String, Object> map = new TreeMap<String, Object>();

		for (final String s : this.defaults.keySet()) {
			map.put(s, this.defaults.get(s));
		}
		for (final String s : this.explicit.keySet()) {
			map.put(s, this.explicit.get(s));
		}

		return map;
	}

	@Override
	public void assertDefined(final Enum<?> key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		assertDefined(key.name());
	}

	@Override
	public void assertDefined(final String key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		get(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IConfig copy() {
		final MemoryConfig copy = new MemoryConfig(this.internalId + "-copy");
		copy.defaults.putAll(this.defaults);
		copy.docs = (HashMap<String, String>) this.docs.clone();
		copy.explicit.putAll(this.explicit);
		copy.required.clear();
		final Iterator<KeyEntryTuple<String, Class<?>>> it = this.required.tupleIterator(
				new Wildcard<String>(), new Wildcard<Class<?>>());
		while (it.hasNext()) {
			final KeyEntryTuple<String, Class<?>> keyEntryTuple = it.next();
			copy.required.index(keyEntryTuple.getKey(), keyEntryTuple.getEntry());
		}
		copy.setOrigins.clear();
		copy.setOrigins.addAll(this.setOrigins);
		return copy;
	}

	@Override
	public Object get(final Enum<?> key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return get(key.name());
	}

	@Override
	public Object get(final String key) {
		final Object value = tryToGet(key);
		if (value == null) {
			throw new ConfigException("Config key '" + key
					+ "' requested but not defined - and no default defined either. " + idStr()

					+ " \nDoc:" + getDocumentation(key));
		}
		return value;
	}

	@Override
	public <T> T getAs(final Enum<?> key, final Class<T> clazz) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return getAs(key.name(), clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAs(final String key, final Class<T> clazz) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		final Object o = get(key);
		if (o == null) {
			return null;
		}
		return (T) o;
	}

	@Override
	public boolean getBoolean(final Enum<?> key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return getBoolean(key.name());
	}

	@Override
	public boolean getBoolean(final String key) {
		final Object o = getInternal(key, Boolean.class, null);
		if (o instanceof String) {
			return Boolean.parseBoolean((String) o);
		} else {
			return (Boolean) o;
		}
	}

	@Override
	public Iterable<String> getDefinedKeys() {
		final SortedSet<String> keys = new TreeSet<String>();
		keys.addAll(this.explicit.keySet());
		keys.addAll(this.defaults.keySet());
		return keys;
	}

	@Override
	public String getDocumentation(final Enum<?> key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return getDocumentation(key.name());
	}

	@Override
	public String getDocumentation(final String key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return this.docs.get(key);
	}

	@Override
	public Iterable<String> getExplicitlyDefinedKeys() {
		return this.explicit.keySet();
	}

	@Override
	public int getInt(final Enum<?> key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return getInt(key.name());
	}

	@Override
	public int getInt(final String key) {
		final Object o = get(key);
		if (o instanceof Integer) {
			return (Integer) o;
		}
		if (o instanceof Long) {
			final long l = (Long) o;
			assert l <= Integer.MAX_VALUE;
			assert l >= Integer.MIN_VALUE;
			return (int) l;
		}
		if (o instanceof String) {
			return Integer.parseInt((String) o);
		}
		throw new ConfigException("Value with key '" + key + "' not a int but '"
				+ o.getClass().getName());
	}


	/**
	 * @param key
	 * @param requestedType
	 *            used only to generate better error messages
	 * @return
	 */
	private Object getInternal(final String key, final Class<?> requestedType, final String callContext) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}

		final Object value = tryToGet(key);

		if (value == null) {
			throw new ConfigException("Config key '" + key + "' requested as '"
					+ requestedType.getName()
					+ "' but not defined - and no default defined either. " + idStr() + " \n"
					+ getDocumentation(key));
		}
		return value;
	}

	/**
	 * @return a short 4-character marker string to help identify which config
	 *         is which.
	 */
	@Override
	public String getInternalId() {
		return this.internalId;
	}

	@Override
	public long getLong(final Enum<?> key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return getLong(key.name());
	}

	@Override
	public long getLong(final String key) {
		final Object o = get(key);
		if (o instanceof Integer) {
			return (Integer) o;
		}
		if (o instanceof Long) {
			return (Long) o;
		}
		if (o instanceof String) {
			return Long.parseLong((String) o);
		}
		throw new ConfigException("Value with key '" + key + "' not a long but '"
				+ o.getClass().getName());
	}

	@Override
	public Set<String> getMissingRequiredKeys() {
		final Set<String> open = new HashSet<String>(this.required.keySet());
		open.removeAll(this.explicit.keySet());
		open.removeAll(this.defaults.keySet());
		return open;
	}

	@Override
	public Set<String> getRequiredKeys() {
		return this.required.keySet();
	}

	@Override
	public <T> IResolver<T> getResolver(final Class<T> interfaze) {
		return getResolver(interfaze.getName());
	}

	@Override
	public <T> IResolver<T> getResolver(final Enum<?> key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return getResolver(key.name());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IResolver<T> getResolver(final String key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		final Object o = get(key);

		if (o instanceof String) {
			try {
				return createResolverFromClassName((String) o);
			} catch (final Exception e) {
				throw new RuntimeException("Could not resolve from '" + key + "'='" + o + "'", e);
			}
		}

		if (o instanceof IResolver) {
			return (IResolver<T>) o;
		}
		// default:
		throw new ConfigException("instance at key '" + key + "' could not be used. Type="
				+ o.getClass().getName());
	}

	@Override
	public String getString(final Enum<?> key) {
		return getString(key.name());
	}

	@Override
	public String getString(final String key) {
		final Object o = get(key);
		if (!(o instanceof String)) {
			throw new ConfigException("instance at key '" + key + "' was not String but "
					+ o.getClass().getName());
		}
		return (String) o;
	}

	@Override
	public String[] getStringArray(final String key) {
		final String s = getString(key);
		if (s == null) {
			return null;
		}
		return decodeList(s);
	}

	@Override
	public Set<String> getStringSet(final Enum<?> key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return getStringSet(key.name());
	}

	@Override
	public Set<String> getStringSet(final String key) {
		String[] array = getStringArray(key);
		if (array == null) {
			array = new String[0];
		}
		return new HashSet<String>(Arrays.asList(array));
	}

	private String idStr() {
		return "[confId=" + getInternalId() + "]";
	}

	@Override
	public boolean isComplete() {
		return getMissingRequiredKeys().isEmpty();
	}

	@Override
	public <T> T resolve(final Class<T> interfaze) {
		final IResolver<T> resolver = getResolver(interfaze);
		if (resolver == null) {
			return null;
		}
		return resolver.resolve();
	}

	@Override
	public void revertToDefault(final Enum<?> key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		revertToDefault(key.name());
	}

	@Override
	public void revertToDefault(final String key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		this.explicit.remove(key);
	}

	@Override
	public ConfBuilder set(final Enum<?> key, final Object value) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return set(key.name(), value);
	}

	@Override
	public ConfBuilder set(final String key, final Object value) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}

		ensureLogInit();
		if (log.isTraceEnabled()) {
			log.trace("Setting '" + key + "' to object value");
		}

		this.explicit.put(key, value);
		return new ConfBuilder(this, key);
	}

	@Override
	public <T> void setAs(final Enum<?> key, final T value) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		setAs(key.name(), value);
	}

	@Override
	public <T> void setAs(final String key, final T value) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		set(key, value);
	}

	@Override
	public ConfBuilder setBoolean(final Enum<?> key, final boolean b) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return setBoolean(key.name(), b);
	}

	@Override
	public ConfBuilder setBoolean(final String key, final boolean b) {
		set(key, "" + b);
		return new ConfBuilder(this, key);
	}

	@Override
	public <T> void setClass(final Class<T> interfaze, final Class<? extends T> clazz) {
		if (interfaze == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		if (clazz == null) {
			throw new IllegalArgumentException("Class may not be null");
		}

		setResolver(interfaze, new ClassResolver<T>(clazz));
	}

	@Override
	public ConfBuilder setDefault(final Enum<?> key, final Object value, final boolean initial) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return setDefault(key.name(), value, initial);
	}

	@Override
	public ConfBuilder setDefault(final String key, final Object value, final boolean initial) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		if (!initial && this.defaults.containsKey(key)) {
			throw new ConfigException("Config key '" + initial + "' had already a default value "
					+ idStr());
		}
		this.defaults.put(key, value);
		if (traceOrigins) {
			try {
				throw new RuntimeException("MARKER");
			} catch (final RuntimeException e) {
				e.fillInStackTrace();
				this.setOrigins.add(e);
			}
		}
		return new ConfBuilder(this, key);
	}

	@Override
	public IConfig setDocumentation(final Enum<?> key, final String documentation) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return setDocumentation(key.name(), documentation);
	}

	@Override
	public IConfig setDocumentation(final String key, final String documentation) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		this.docs.put(key, documentation);
		return this;
	}

	@Override
	public <T> void setInstance(final Class<T> interfaze, final T instance) {
		if (interfaze == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		setResolver(interfaze, new InstanceResolver<T>(instance));
	}

	@Override
	public <T> void setInstance(final String key, final T instance) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		setResolver(key, new InstanceResolver<T>(instance));
	}

	@Override
	public ConfBuilder setLong(final Enum<?> key, final long value) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return setLong(key.name(), value);
	}

	@Override
	public ConfBuilder setLong(final String key, final long l) {
		set(key, "" + l);
		return new ConfBuilder(this, key);
	}

	@Override
	public <T> ConfBuilder setResolver(final Class<T> interfaze, final IResolver<T> resolver) {
		return setResolver(interfaze.getName(), resolver);
	}

	@Override
	public <T> ConfBuilder setResolver(final Enum<?> key, final IResolver<T> resolver) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return setResolver(key.name(), resolver);
	}

	@Override
	public <T> ConfBuilder setResolver(final String key, final IResolver<T> resolver) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		if (resolver == null) {
			throw new IllegalArgumentException("resolver may not be null");
		}
		set(key, resolver);
		return new ConfBuilder(this, key);
	}

	@Override
	public ConfBuilder setStrings(final Enum<?> key, final String... values) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return setStrings(key.name(), values);
	}

	@Override
	public ConfBuilder setStrings(final String key, final String... values) {
		assert values != null;
		final String s = encodeList(values);
		set(key, s);
		return new ConfBuilder(this, key);
	}

	@Override
	public IConfig setType(final Enum<?> key, final Class<?> type) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return setType(key.name(), type);
	}

	@Override
	public IConfig setType(final String key, final Class<?> type) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		this.types.put(key, type);
		return this;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("DEFINED\n");
		final List<String> defined = new ArrayList<String>();
		for (final String s : getExplicitlyDefinedKeys()) {
			defined.add(s);
		}
		Collections.sort(defined);
		for (final String s : defined) {
			b.append(s + "=" + get(s) + "\n");
		}
		return b.toString();
	}

	@Override
	public Object tryToGet(final Enum<?> key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		return tryToGet(key.name());
	}

	@Override
	public Object tryToGet(final String key) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		Object value = this.explicit.get(key);
		if (value == null) {
			value = this.defaults.get(key);
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T tryToGetAs(final String key, final Class<T> clazz) {
		if (key == null) {
			throw new IllegalArgumentException("Key may not be null");
		}
		final Object o = tryToGet(key);
		if (o == null) {
			if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
				return (T) Boolean.FALSE;
			} else {
				return null;
			}
		}

		if (o instanceof IResolver) {
			return ((IResolver<T>) o).resolve();
		}

		return (T) o;
	}

	@Override
	public <T> T tryToResolve(final Class<T> interfaze) {
		try {
			return resolve(interfaze);
		} catch (final Exception e) {
			return null;
		}
	}

	@Override
	public <T> T tryToResolve(final String key) {
		try {
			final IResolver<T> resolver = getResolver(key);
			if (resolver == null) {
				return null;
			}
			return resolver.resolve();
		} catch (final Exception e) {
			return null;
		}
	}

}
