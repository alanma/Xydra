package org.xydra.xgae.memcache.impl.gae;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.utils.DebugFormatter;
import org.xydra.xgae.annotations.XGaeOperation;
import org.xydra.xgae.gaeutils.AboutAppEngine;
import org.xydra.xgae.memcache.api.IMemCache;
import org.xydra.xgae.util.XGaeDebugHelper;
import org.xydra.xgae.util.XGaeDebugHelper.Timing;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.appengine.api.memcache.MemcacheServiceException;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.Stats;

/**
 * An implementation of {@link IMemCache} using the low-level GAE memcache API.
 *
 * @author xamde
 */
public class GaeLowLevelMemCache implements IMemCache {

	private static final Logger log = LoggerFactory.getLogger(GaeLowLevelMemCache.class);

	private static final String MEMCACHE_NAME = "[#=MC]";

	private final MemcacheService memcacheService;
	/* used to prefix all keys */
	@SuppressWarnings("unused")
	private final String appVersion;

	@XGaeOperation()
	public GaeLowLevelMemCache() {
		this.memcacheService = MemcacheServiceFactory.getMemcacheService();
		// format: user-chosen-versionId-from-appengine-xml '.' timestamp
		this.appVersion = AboutAppEngine.getVersion();
	}

	@Override
	@XGaeOperation(memcacheRead = true)
	public String stats() {
		final Stats stats = this.memcacheService.getStatistics();
		return "In-memory, size: " + size()

		+ " maxTimeWithoutAccess: " + stats.getBytesReturnedForHits()

		+ " hits: " + stats.getHitCount()

		+ " misses: " + stats.getMissCount()

		+ " TotalItemBytes: " + stats.getTotalItemBytes()

		+ " BytesReturnedForHits: " + stats.getBytesReturnedForHits();
	}

	@Override
	@XGaeOperation(memcacheRead = true)
	public int size() {
		return (int) this.memcacheService.getStatistics().getItemCount();
	}

	@Override
	@XGaeOperation(memcacheRead = true)
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	@XGaeOperation(memcacheRead = true)
	public boolean containsKey(final Object key) {
		assert key instanceof String;
		final String usedKey = key instanceof String ? (String) key : key.toString();
		return this.memcacheService.contains(keyUniqueForCurrentAppVersion(usedKey));
	}

	@XGaeOperation()
	private static String keyUniqueForCurrentAppVersion(final String key) {
		return key;
		/*
		 * TODO(stability) @Daniel: use a memcache key that is specific for a
		 * certain app-version to avoid conflicts
		 */

		// if(key instanceof String) {
		// return this.appVersion + key;
		// } else {
		// // fall back to binary
		// ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// try {
		// bos.write(this.appVersion.getBytes());
		// ObjectOutputStream oos = new ObjectOutputStream(bos);
		// oos.writeObject(key);
		// oos.close();
		// return bos.toByteArray();
		// } catch(IOException e) {
		// throw new
		// RuntimeException("Error converting memcache key to unique key", e);
		// }
		// }
	}

	@Override
	public boolean containsValue(final Object value) {
		throw new UnsupportedOperationException("GaeMemcache does not support this");
	}

	@Override
	@XGaeOperation(memcacheRead = true)
	public Object get(final Object key) {
		assert key instanceof String;
		final String usedKey = key instanceof String ? (String) key : key.toString();
		final Object o = this.memcacheService.get(keyUniqueForCurrentAppVersion(usedKey));
		log.debug(XGaeDebugHelper.dataGet(MEMCACHE_NAME, usedKey, o, Timing.Now));
		return o;
	}

	@Override
	@XGaeOperation(memcacheRead = true)
	public Map<String, Object> getAll(final Collection<String> keys) {
		final Map<String, Object> result = this.memcacheService.getAll(keys);
		log.debug(XGaeDebugHelper.dataGet(MEMCACHE_NAME, keys, result, Timing.Now));
		return result;
	}

	/**
	 * This implementation violates the API contract. Instead of returning the
	 * previous value, if present, it <em>always</em> returns null. This is
	 * faster.
	 */
	@Override
	@XGaeOperation(memcacheWrite = true)
	public Object put(final String key, final Object value) {
		XyAssert.xyAssert(value != null, "value is null");
		assert value != null;
		log.debug(XGaeDebugHelper.dataPut(MEMCACHE_NAME, key.toString(), value, Timing.Now));
		this.memcacheService.put(keyUniqueForCurrentAppVersion(key), value);
		// does not hold in concurrent environment
		// assert
		// this.memcacheService.get(keyUniqueForCurrentAppVersion(key)).equals(value);
		// assert this.memcacheService.get(key).equals(value);
		return null;
	}

	@Override
	@XGaeOperation(memcacheWrite = true)
	public Object remove(final Object key) {
		assert key instanceof String;
		final String usedKey = key instanceof String ? (String) key : key.toString();
		log.debug(XGaeDebugHelper.dataPut(MEMCACHE_NAME, usedKey, null, Timing.Now));
		return this.memcacheService.delete(keyUniqueForCurrentAppVersion(usedKey));
	}

	@Override
	@XGaeOperation(memcacheWrite = true)
	public void putAll(final Map<? extends String, ? extends Object> m) {
		if (m.isEmpty()) {
			return;
		}
		log.debug(XGaeDebugHelper.dataPut(MEMCACHE_NAME, m, Timing.Now));
		// transform keys
		final Map<String, Object> keyTransformedMap = new HashMap<String, Object>();
		for (final java.util.Map.Entry<? extends String, ? extends Object> mapEntry : m.entrySet()) {
			XyAssert.xyAssert(mapEntry.getValue() != null, "mapEntry.getValue() is null");
			assert mapEntry.getValue() != null;
			// TODO memcache: relaxed assert to allow caching null-entities --
			// good idea?
			// !Memcache.NULL_ENTITY.equals(mapEntry.getValue());
			keyTransformedMap.put(keyUniqueForCurrentAppVersion(mapEntry.getKey()),
					mapEntry.getValue());
		}
		// IMPROVE consider doing this in an async tread
		this.memcacheService.putAll(keyTransformedMap);
	}

	@Override
	@XGaeOperation(memcacheWrite = true)
	public void clear() {
		log.debug(XGaeDebugHelper.clear(MEMCACHE_NAME));
		this.memcacheService.clearAll();
	}

	@Override
	public Set<String> keySet() {
		throw new UnsupportedOperationException("GaeMemcache does not support this");
	}

	@Override
	public Collection<Object> values() {
		throw new UnsupportedOperationException("GaeMemcache does not support this");
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		throw new UnsupportedOperationException("GaeMemcache does not support this");
	}

	@Override
	// Expires in 10 days. There is no default.
	public void putIfValueIsNull(final String key, final Object value) {
		XyAssert.xyAssert(value != null, "value is null");
		assert value != null;
		// TODO memcache: reenable? assert
		// !(KeyStructure.toKey(key).getKind().equals("XCHANGE") &&
		// Memcache.NULL_ENTITY
		// .equals(value)) : KeyStructure.toKey(key);
		log.debug(XGaeDebugHelper.dataPutIfNull(MEMCACHE_NAME, key, value, Timing.Now));
		this.memcacheService.put(key, value, Expiration.byDeltaSeconds(60 * 60 * 24 * 10),
				SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
		if (log.isTraceEnabled()) {
			log.debug(MEMCACHE_NAME + " now "
					+ DebugFormatter.format(this.memcacheService.get(key)));
		}
	}

	private static class IdentifiableValueImpl implements IdentifiableValue {

		private final com.google.appengine.api.memcache.MemcacheService.IdentifiableValue id;

		public IdentifiableValueImpl(
				final com.google.appengine.api.memcache.MemcacheService.IdentifiableValue id) {
			this.id = id;
		}

		@Override
		public Object getValue() {
			return this.id == null ? null : this.id.getValue();
		}

		public com.google.appengine.api.memcache.MemcacheService.IdentifiableValue getAppEngineInternal() {
			return this.id;
		}
	}

	@Override
	@XGaeOperation(memcacheRead = true)
	public IdentifiableValue getIdentifiable(final String key) {
		final com.google.appengine.api.memcache.MemcacheService.IdentifiableValue id = this.memcacheService
				.getIdentifiable(keyUniqueForCurrentAppVersion(key));
		log.debug(XGaeDebugHelper.dataGet(MEMCACHE_NAME, key, id, Timing.Now));
		return new IdentifiableValueImpl(id);
	}

	@Override
	@XGaeOperation(memcacheWrite = true)
	public boolean putIfUntouched(final String key, final IdentifiableValue oldValue, final Object newValue) {
		XyAssert.xyAssert(newValue != null, "newValue is null");
		assert newValue != null;
		assert oldValue instanceof IdentifiableValueImpl : "this cache can only handly its own impls "
				+ oldValue.getClass().getCanonicalName();
		final IdentifiableValueImpl idImpl = (IdentifiableValueImpl) oldValue;
		final com.google.appengine.api.memcache.MemcacheService.IdentifiableValue gaeId = idImpl
				.getAppEngineInternal();
		final Expiration expiration = Expiration.byDeltaSeconds(60 * 60 * 24 * 10);
		boolean result;
		if (gaeId == null) {
			log.debug(XGaeDebugHelper.dataPutIfNull(MEMCACHE_NAME, key, newValue, Timing.Now));
			result = this.memcacheService.put(key, newValue, expiration,
					SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
		} else {
			log.debug(XGaeDebugHelper.dataPutIfUntouched(MEMCACHE_NAME, key, oldValue, newValue,
					Timing.Now));
			result = this.memcacheService.putIfUntouched(key, gaeId, newValue, expiration);
		}
		if (log.isTraceEnabled()) {
			log.debug(MEMCACHE_NAME + " now "
					+ DebugFormatter.format(this.memcacheService.get(key)));
		}
		return result;

	}

	@Override
	public Map<String, Long> incrementAll(final Map<String, Long> offsets, final long initialValue) {
		return this.memcacheService.incrementAll(offsets, initialValue);
	}

	@Override
	public Object putChecked(final String key, final Object value) throws IOException {
		try {
			return put(key, value);
		} catch (final MemcacheServiceException e) {
			throw new IOException(e);
		}
	}
}
