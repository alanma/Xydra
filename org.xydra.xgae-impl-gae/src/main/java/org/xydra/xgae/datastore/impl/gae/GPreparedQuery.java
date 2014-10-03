package org.xydra.xgae.datastore.impl.gae;

import java.util.Iterator;
import java.util.List;

import org.xydra.index.TransformerTool;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.SPreparedQuery;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;

public class GPreparedQuery extends RawWrapper<PreparedQuery, SPreparedQuery> implements
		SPreparedQuery, Iterable<SEntity> {

	private int limit;
	private int chunkSize;

	private GPreparedQuery(PreparedQuery raw) {
		super(raw);
	}

	public static GPreparedQuery wrap(PreparedQuery raw) {
		if (raw == null)
			return null;

		return new GPreparedQuery(raw);
	}

	@Override
	public Iterable<SEntity> asIterable() {
		return this;
	}

	@Override
	@Deprecated
	public List<SEntity> asListWithChunkSize(int chunkSize) {
		setChunkSize(chunkSize);
		return asList();
	}

	@Override
	public Iterator<SEntity> iterator() {
		return new TransformingIterator<Entity, SEntity>(raw().asIterator(),
				new ITransformer<Entity, SEntity>() {

					@Override
					public SEntity transform(Entity in) {
						return GEntity.wrap(in);
					}
				});
	}

	private class KeyIterable implements Iterable<SKey> {

		@Override
		public Iterator<SKey> iterator() {
			return new TransformingIterator<Entity, SKey>(raw().asIterator(),
					new ITransformer<Entity, SKey>() {

						@Override
						public SKey transform(Entity in) {
							return GKey.wrap(in.getKey());
						}
					});
		}

	}

	@Override
	public Iterable<SKey> asKeysIterable() throws IllegalArgumentException {
		return new KeyIterable();
	}

	@Override
	public void setLimit(int limit) {
		this.limit = limit;
	}

	private FetchOptions toFetchOptions() {
		FetchOptions fo = FetchOptions.Builder.withDefaults();
		if (this.limit > 0) {
			fo.limit(this.limit);
		}
		if (this.chunkSize > 0) {
			fo.chunkSize(this.chunkSize);
		}
		return fo;
	}

	@Override
	public List<SEntity> asList() {
		List<Entity> rawList = raw().asList(toFetchOptions());
		return TransformerTool.transformListEntries(rawList, GEntity.TRANSFOMER_ENTITY_SENTITY);
	}

	@Override
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

}
