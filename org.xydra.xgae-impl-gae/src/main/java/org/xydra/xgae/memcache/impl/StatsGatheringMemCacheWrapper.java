package org.xydra.xgae.memcache.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.minio.MiniIOException;
import org.xydra.base.minio.MiniStreamWriter;
import org.xydra.perf.MapStats;
import org.xydra.sharedutils.XyAssert;
import org.xydra.xgae.memcache.api.IMemCache;
import org.xydra.xgae.memcache.impl.LocalMemcache.IdentifiableImpl;

/**
 * A wrapper around an IMemCache to gather runtime access statistics.
 *
 * Records every cache get and put, as well as all values a key ever has.
 *
 * Very memory expensive!
 *
 * @author xamde
 *
 */
@RunsInGWT(false)
public class StatsGatheringMemCacheWrapper implements IMemCache {

	private final MapStats mapstats = new MapStats();
	private final Map<String, Long> stats = new HashMap<String, Long>();
	/** Allows any other process easy runtime-access to the stats */
	public static StatsGatheringMemCacheWrapper INSTANCE;

	public StatsGatheringMemCacheWrapper(final IMemCache memcache) {
		this.base = memcache;
		INSTANCE = this;
	}

	@Override
	public int size() {
		return this.base.size();
	}

	@Override
	public boolean isEmpty() {
		return this.base.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		XyAssert.xyAssert(key instanceof String);
		count("containsKey");
		return this.base.containsKey(key instanceof String ? key : key.toString());
	}

	private void count(final String action) {
		final Long l = this.stats.get(action);
		if (l == null) {
			this.stats.put(action, 1L);
		} else {
			this.stats.put(action, l + 1);
		}
	}

	@Override
	public boolean containsValue(final Object value) {
		count("containsValue");
		return this.base.containsValue(value);
	}

	@Override
	public Object get(final Object key) {
		XyAssert.xyAssert(key instanceof String);
		count("get");
		String usedKey;
		if (key instanceof String) {
			usedKey = (String) key;
		} else {
			usedKey = key.toString();
		}
		final Object value = this.base.get(usedKey);
		this.mapstats.recordGet(key.toString(), value != null, 1);
		return value;
	}

	@Override
	public Map<String, Object> getAll(final Collection<String> keys) {
		count("getAll");
		final Map<String, Object> result = new HashMap<String, Object>();
		for (final String key : keys) {
			final Object value = this.base.get(key);
			this.mapstats.recordGet(key.toString(), value != null, keys.size());
			if (value != null) {
				result.put(key, value);
			}
		}
		return result;
	}

	@Override
	public Object put(final String key, final Object value) {
		count("put");
		this.mapstats.recordPut(key.toString(), value);
		return this.base.put(key, value);
	}

	@Override
	public void putIfValueIsNull(final String key, final Object value) {
		count("putIfValueIsNull");
		this.mapstats.recordPut(key.toString(), value);
		this.base.putIfValueIsNull(key, value);
	}

	@Override
	public Object remove(final Object key) {
		count("remove");
		return this.base.remove(key);
	}

	@Override
	public void putAll(final Map<? extends String, ? extends Object> m) {
		count("putAll");
		this.base.putAll(m);
	}

	@Override
	public void clear() {
		count("clear");
		this.base.clear();
		// clear also stats
		this.stats.clear();
		this.mapstats.clear();
	}

	@Override
	public Set<String> keySet() {
		count("keySet");
		return this.base.keySet();
	}

	@Override
	public Collection<Object> values() {
		count("values");
		return this.base.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		count("entrySet");
		return this.base.entrySet();
	}

	@Override
	public boolean equals(final Object o) {
		return this.base.equals(o);
	}

	@Override
	public int hashCode() {
		return this.base.hashCode();
	}

	private final IMemCache base;

	@Override
	public String stats() {
		final StringBuffer buf = new StringBuffer();
		for (final String s : this.stats.keySet()) {
			buf.append(s + " = " + this.stats.get(s) + "<br />\n");
		}
		buf.append("Access stats ===================================================== <br />\n");
		try {
			final StringWriter sw = new StringWriter();
			this.mapstats.writeStats(new MiniStreamWriter(sw));
			buf.append(sw.toString());
		} catch (final MiniIOException e) {
			throw new RuntimeException(e);
		}

		buf.append("BASE stats<br />\n");
		buf.append(this.base.stats() + "<br />\n");

		return buf.toString();
	}

	@Override
	public boolean putIfUntouched(@NeverNull final String key, @NeverNull final IdentifiableValue oldValue,
			@CanBeNull final Object newValue) {
		count("putIfUntouched");
		synchronized (this.base) {
			final Object current = get(key);
			if (current.equals(oldValue.getValue())) {
				// indirect count
				put(key, newValue);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public IdentifiableValue getIdentifiable(final String key) {
		// no counting needed
		final Object o = get(key);
		return new IdentifiableImpl(o);
	}

	@Override
	public Map<String, Long> incrementAll(final Map<String, Long> offsets, final long initialValue) {
		count("incrementAll");
		Map<String, Long> result = new HashMap<String, Long>();
		result = this.base.incrementAll(offsets, initialValue);
		for (final java.util.Map.Entry<String, Long> entry : result.entrySet()) {
			this.mapstats.recordPut(entry.getKey(), entry.getValue());
		}
		return result;
	}

	@Override
	public Object putChecked(final String key, final Object value) throws IOException {
		return put(key, value);
	}

}
