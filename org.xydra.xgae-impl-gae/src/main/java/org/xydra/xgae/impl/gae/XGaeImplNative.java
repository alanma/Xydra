package org.xydra.xgae.impl.gae;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.xgae.IXGae;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.IDatastore;
import org.xydra.xgae.datastore.impl.gae.DatastoreImplGae;
import org.xydra.xgae.gaeutils.AboutAppEngine;
import org.xydra.xgae.gaeutils.GaeTestfixer;
import org.xydra.xgae.impl.AbstractXGaeBaseImpl;
import org.xydra.xgae.memcache.api.IMemCache;
import org.xydra.xgae.memcache.impl.LocalMemcache;
import org.xydra.xgae.memcache.impl.gae.GaeLowLevelMemCache;

import com.google.apphosting.api.ApiProxy;

/**
 * Implements the IXGae abstraction interface simply by calling the real Google
 * Appengine. Includes some tweaks to improve testability, e.g. another local
 * memcache impl.
 * 
 * FIXME deal with {@link GaeTestfixer}
 * 
 * @author xamde
 */
public class XGaeImplNative extends AbstractXGaeBaseImpl implements IXGae {

	private static final Logger log = LoggerFactory.getLogger(XGaeImplNative.class);

	private IDatastore datastore;
	private IMemCache memcache;

	@Override
	public synchronized IDatastore datastore() {
		if (this.datastore == null) {
			this.datastore = new DatastoreImplGae();
		}
		return this.datastore;
	}

	@Override
	public synchronized IMemCache memcache() {
		if (this.memcache == null) {
			log.info("INIT IMemcache instance.");
			if (XGae.get().onAppEngine()) {
				this.memcache = new GaeLowLevelMemCache();
			} else {
				this.memcache = new LocalMemcache();
			}
		}
		return this.memcache;
	}

	@Override
	public boolean inProduction() {
		return AboutAppEngine.inProduction();
	}

	@Override
	public boolean inDevelopment() {
		return AboutAppEngine.inDevelopment();
	}

	@Override
	public String getInstanceId() {
		return AboutAppEngine.getInstanceId();
	}

	@Override
	public long getRuntimeLimitInMillis() {
		return ApiProxy.getCurrentEnvironment().getRemainingMillis();
	}

	@Override
	public String getProviderVersionString() {
		return "GAE-1.7.3";
	}

}
