package org.xydra.xgae.util;

import java.util.concurrent.Future;

import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;

/**
 * Wraps a Future<Entity> and remember the result of {@link Future#get()}
 * 
 * @author dscharrer
 */
// TODO remove this class or what purpose does it serve?
public class AsyncEntity {

	private Future<SEntity> future;
	private SEntity entity;
	@SuppressWarnings("unused")
	private SKey key;

	public AsyncEntity(SKey key, Future<SEntity> future) {
		this.future = future;
		this.entity = null;
		this.key = key;
	}

	public AsyncEntity(SEntity entity) {
		this.future = null;
		this.entity = entity;
		this.key = null;
	}

	public SEntity get() {
		if (this.future != null) {
			this.entity = FutureUtils.waitFor(this.future);
			this.future = null;
		}
		return this.entity;
	}

}
