package org.xydra.core.serialize.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.minio.MiniReader;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraParser;

@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class JsonParser implements XydraParser {

	public static class MiniSaj implements SAJ {

		private final Stack<Object> stack = new Stack<Object>();
		public Map<String, Object> map = null;
		public List<Object> list = null;
		private String key = null;

		private void onObject(final Object o) {
			if (this.map != null) {
				this.map.put(this.key, o);
			} else if (this.list != null) {
				this.list.add(o);
			} else if (o != null) {
				throw new IllegalArgumentException("the root must be an object or null, was: " + o);
			}
		}

		private void onContainer(final Object c) {
			if (this.map != null) {
				this.map.put(this.key, c);
				this.stack.push(this.map);
				this.map = null;
			} else if (this.list != null) {
				this.list.add(c);
				this.stack.push(this.list);
				this.list = null;
			}
		}

		@Override
		public void arrayStart() {
			final List<Object> c = new ArrayList<Object>();
			onContainer(c);
			this.list = c;
		}

		@Override
		public void objectStart() {
			final Map<String, Object> c = new HashMap<String, Object>();
			onContainer(c);
			this.map = c;
		}

		@Override
		public void arrayEnd() {
			end();
		}

		@Override
		public void objectEnd() {
			end();
		}

		@SuppressWarnings("unchecked")
		private void end() {
			if (!this.stack.isEmpty()) {
				this.map = null;
				this.list = null;
				final Object last = this.stack.pop();
				if (last instanceof Map<?, ?>) {
					this.map = (Map<String, Object>) last;
				} else {
					this.list = (List<Object>) last;
				}
			}
		}

		@Override
		public void onKey(final String key) {
			this.key = key;
		}

		@Override
		public void onBoolean(final boolean b) {
			onObject(b);
		}

		@Override
		public void onDouble(final double d) {
			onObject(d);
		}

		@Override
		public void onInteger(final int i) {
			onObject(i);
		}

		@Override
		public void onLong(final long l) {
			onObject(l);
		}

		@Override
		public void onNull() {
			onObject(null);
		}

		@Override
		public void onString(final String s) {
			onObject(s);
		}

	}

	@Override
	public XydraElement parse(final String data) throws IllegalArgumentException {

		final MiniSaj saj = new MiniSaj();

		final JsonParserSAJ parser = new JsonParserSAJ(saj);
		try {
			parser.parse(data);
		} catch (final JSONException e) {
			throw new IllegalArgumentException(e);
		}

		if (saj.map == null) {
			return null;
		}
		return new JsonElement(saj.map, null);
	}

	@Override
	public String getContentType() {
		return "application/json";
	}

	@Override
	public XydraElement parse(final MiniReader miniReader) throws IllegalArgumentException {
		final MiniSaj saj = new MiniSaj();

		final JsonParserSAJ parser = new JsonParserSAJ(saj);
		try {
			parser.parse(miniReader);
		} catch (final JSONException e) {
			throw new IllegalArgumentException(e);
		}

		if (saj.map == null) {
			return null;
		}
		return new JsonElement(saj.map, null);
	}
}
