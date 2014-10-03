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
		public Object transform(Object in) {
			if (in instanceof List) {
				List<?> list = (List<?>) in;
				List<Object> xgaeList = new ArrayList<Object>(list.size());
				for (Object o : list) {
					xgaeList.add(GAE_TO_XGAE.transform(o));
				}
				return xgaeList;
			}
			if (in instanceof Text)
				return GText.wrap((Text) in);
			if (in instanceof Key)
				return GKey.wrap((Key) in);
			return in;
		}
	};

	protected static final ITransformer<Entity, SEntity> TRANSFOMER_ENTITY_SENTITY = new ITransformer<Entity, SEntity>() {

		@Override
		public SEntity transform(Entity in) {
			return wrap(in);
		}
	};

	/** S... to G... */
	private static ITransformer<Object, Object> XGAE_TO_GAE = new ITransformer<Object, Object>() {

		@Override
		public Object transform(Object in) {
			if (in instanceof List) {
				List<?> list = (List<?>) in;
				List<Object> gaeList = new ArrayList<Object>(list.size());
				for (Object o : list) {
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

	public static Iterable<Entity> unwrap(Iterable<SEntity> it) {
		return _unwrap(it);
	}

	public static GEntity wrap(Entity raw) {
		if (raw == null)
			return null;

		return new GEntity(raw);
	}

	private GEntity(Entity raw) {
		super(raw);
	}

	@Override
	public Object getAttribute(String name) {
		Object o = raw().getProperty(name);
		return GAE_TO_XGAE.transform(o);
	}

	@Override
	public Map<String, Object> getAttributes() {
		// transform values
		Map<String, Object> map = raw().getProperties();
		return TransformerTool.transformMapValues(map, GAE_TO_XGAE);
	}

	@Override
	public SKey getKey() {
		return GKey.wrap(raw().getKey());
	}

	@Override
	public boolean hasAttribute(String name) {
		return raw().hasProperty(name);
	}

	@Override
	public void removeAttribute(String name) {
		raw().removeProperty(name);
	}

	@Override
	public void setAttribute(String name, boolean value) {
		raw().setUnindexedProperty(name, value);
	}

	@Override
	public void setAttribute(String name, List<?> list) {
		Object gaeList = XGAE_TO_GAE.transform(list);
		raw().setUnindexedProperty(name, gaeList);
	}

	@Override
	public void setAttribute(String name, long value) {
		raw().setUnindexedProperty(name, value);
	}

	@Override
	public void setAttribute(String name, Serializable serializable) {
		raw().setUnindexedProperty(name, serializable);
	}

	@Override
	public void setAttribute(String name, String value) {
		raw().setUnindexedProperty(name, value);
	}

	@Override
	public void setAttribute(String name, SValue value) {
		raw().setUnindexedProperty(name, value.raw());
	}

}
