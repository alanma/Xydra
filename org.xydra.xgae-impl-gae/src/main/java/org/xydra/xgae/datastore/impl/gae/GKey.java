package org.xydra.xgae.datastore.impl.gae;

import org.xydra.index.TransformerTool;
import org.xydra.index.iterator.ITransformer;
import org.xydra.xgae.datastore.api.SKey;

import com.google.appengine.api.datastore.Key;

public class GKey extends RawWrapper<Key, GKey> implements SKey {

	protected static final ITransformer<Key, SKey> TRANSFOMER_KEY_SKEY = new ITransformer<Key, SKey>() {

		@Override
		public SKey transform(final Key in) {
			return wrap(in);
		}
	};

	private GKey(final Key raw) {
		super(raw);
	}

	public static Iterable<Key> unwrap(final Iterable<SKey> it) {

		return TransformerTool.transformIterable(it, new ITransformer<SKey, Key>() {

			@Override
			public Key transform(final SKey in) {
				return GKey.unwrap(in);
			}
		});
	}

	protected static Key unwrap(final SKey in) {
		if (in == null) {
			return null;
		}

		return (Key) in.raw();
	}

	public static GKey wrap(final Key raw) {
		if (raw == null) {
			return null;
		}

		return new GKey(raw);
	}

	@Override
	public String getKind() {
		return raw().getKind();
	}

	@Override
	public String getName() {
		return raw().getName();
	}

}
