package org.xydra.core.serialize.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xydra.core.serialize.MiniElement;
import org.xydra.core.serialize.MiniParser;
import org.xydra.json.JSONException;
import org.xydra.json.JsonParser;
import org.xydra.json.SAJ;


public class MiniParserJson implements MiniParser {
	
	private static class MiniSaj implements SAJ {
		
		private final Stack<Object> stack = new Stack<Object>();
		private Map<String,Object> map = null;
		private List<Object> list = null;
		private String key = null;
		
		private void onObject(Object o) {
			if(this.map != null) {
				this.map.put(this.key, o);
			} else {
				this.list.add(o);
			}
		}
		
		private void onContainer(Object c) {
			if(this.map != null) {
				this.map.put(this.key, c);
				this.stack.push(this.map);
				this.map = null;
			} else if(this.list != null) {
				this.list.add(c);
				this.stack.push(this.list);
				this.list = null;
			}
		}
		
		@Override
		public void arrayStart() {
			System.out.println("arrayStart");
			List<Object> c = new ArrayList<Object>();
			onContainer(c);
			this.list = c;
		}
		
		@Override
		public void objectStart() {
			System.out.println("objectStart");
			Map<String,Object> c = new HashMap<String,Object>();
			onContainer(c);
			this.map = c;
		}
		
		@Override
		public void arrayEnd() {
			System.out.println("arrayEnd");
			end();
		}
		
		@Override
		public void objectEnd() {
			System.out.println("objectEnd");
			end();
		}
		
		@SuppressWarnings("unchecked")
		private void end() {
			if(!this.stack.isEmpty()) {
				this.map = null;
				this.list = null;
				Object last = this.stack.pop();
				if(last instanceof Map<?,?>) {
					this.map = (Map<String,Object>)last;
				} else {
					this.list = (List<Object>)last;
				}
			}
		}
		
		@Override
		public void onKey(String key) {
			System.out.println("onKey " + key);
			this.key = key;
		}
		
		@Override
		public void onBoolean(boolean b) {
			System.out.println("onBoolean " + b);
			onObject(b);
		}
		
		@Override
		public void onDouble(double d) {
			System.out.println("onDouble " + d);
			onObject(d);
		}
		
		@Override
		public void onInteger(int i) {
			System.out.println("onInteger " + i);
			onObject(i);
		}
		
		@Override
		public void onLong(long l) {
			System.out.println("onLong " + l);
			onObject(l);
		}
		
		@Override
		public void onNull() {
			System.out.println("onNull");
			onObject(null);
		}
		
		@Override
		public void onString(String s) {
			System.out.println("onString " + s);
			onObject(s);
		}
		
	}
	
	@Override
	public MiniElement parse(String data) throws IllegalArgumentException {
		
		MiniSaj saj = new MiniSaj();
		
		JsonParser parser = new JsonParser(saj);
		try {
			parser.parse(data);
		} catch(JSONException e) {
			throw new IllegalArgumentException(e);
		}
		
		assert saj.map != null;
		
		return new MiniElementJson(saj.map);
	}
}
