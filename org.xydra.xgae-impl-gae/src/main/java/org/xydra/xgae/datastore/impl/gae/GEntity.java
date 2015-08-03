package org.xydra.xgae.datastore.impl.gae;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xydra.index.TransformerTool;
import org.xydra.index.iterator.ITransformer;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.SValue;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

public class GEntity extends RawWrapper<Entity, GEntity> implements SEntity {

	/** G... to S... */
	private static ITransformer<Object, Object> GAE_TO_XGAE = new ITransformer<Object, Object>() {

		@Override
		public Object transform(final Object in) {
			if (in instanceof List) {
				final List<?> list = (List<?>) in;
				final List<Object> xgaeList = new ArrayList<Object>(list.size());
				for (final Object o : list) {
					xgaeList.add(GAE_TO_XGAE.transform(o));
				}
				return xgaeList;
			}
			if (in instanceof Text) {
				return GText.wrap((Text) in);
			}
			if (in instanceof Key) {
				return GKey.wrap((Key) in);
			}
			return in;
		}
	};

	protected static final ITransformer<Entity, SEntity> TRANSFOMER_ENTITY_SENTITY = new ITransformer<Entity, SEntity>() {

		@Override
		public SEntity transform(final Entity in) {
			return wrap(in);
		}
	};

	/** S... to G... */
	private static ITransformer<Object, Object> XGAE_TO_GAE = new ITransformer<Object, Object>() {

		@Override
		public Object transform(final Object in) {
			if (in instanceof List) {
				final List<?> list = (List<?>) in;
				final List<Object> gaeList = new ArrayList<Object>(list.size());
				for (final Object o : list) {
					gaeList.add(XGAE_TO_GAE.transform(o));
				}
				return gaeList;
			}
			if (in instanceof SValue) {
				return ((SValue) in).raw();
			} else {
				return in;
			}
		}
	};

	public static Iterable<Entity> unwrap(final Iterable<SEntity> it) {
		return _unwrap(it);
	}

	public static GEntity wrap(final Entity raw) {
		if (raw == null) {
			return null;
		}

		return new GEntity(raw);
	}

	private GEntity(final Entity raw) {
		super(raw);
	}

	@Override
	public Object getAttribute(final String name) {
		final Object o = raw().getProperty(name);
		return GAE_TO_XGAE.transform(o);
	}

	@Override
	public Map<String, Object> getAttributes() {
		// transform values
		final Map<String, Object> map = raw().getProperties();
		return TransformerTool.transformMapValues(map, GAE_TO_XGAE);
	}

	@Override
	public SKey getKey() {
		return GKey.wrap(raw().getKey());
	}

	@Override
	public boolean hasAttribute(final String name) {
		return raw().hasProperty(name);
	}

	@Override
	public void removeAttribute(final String name) {
		raw().removeProperty(name);
	}

	@Override
	public void setAttribute(final String name, final boolean value) {
		raw().setUnindexedProperty(name, value);
	}

	@Override
	public void setAttribute(final String name, final List<?> list) {
		final Object gaeList = XGAE_TO_GAE.transform(list);
		raw().setUnindexedProperty(name, gaeList);
	}

	@Override
	public void setAttribute(final String name, final long value) {
		raw().setUnindexedProperty(name, value);
	}

	@Override
	public void setAttribute(final String name, final Serializable serializable) {
		raw().setUnindexedProperty(name, serializable);
	}

	@Override
	public void setAttribute(final String name, final String value) {
		raw().setUnindexedProperty(name, value);
	}

	@Override
	public void setAttribute(final String name, final SValue value) {
		raw().setUnindexedProperty(name, value.raw());
	}

}
