package org.xydra.json;

import java.util.Stack;

import org.xydra.core.serialize.json.JSONException;
import org.xydra.core.serialize.json.SAJ;


public class BuilderSAJ implements SAJ {
	
	private Stack<Object> parsed = new Stack<Object>();
	private String key = null;
	private Object result;
	
	public BuilderSAJ() {
	}
	
	@Override
    public void arrayEnd() {
		assert !this.parsed.isEmpty() && this.parsed.peek().getClass().equals(JSONArray.class);
		if(this.parsed.size() > 1) {
			this.result = this.parsed.peek();
		}
		this.parsed.pop();
	}
	
	@Override
    public void arrayStart() throws JSONException {
		JSONArray jsonArray = new JSONArray();
		linkToParent(jsonArray);
		this.parsed.push(jsonArray);
	}
	
	@Override
    public void objectEnd() {
		assert !this.parsed.isEmpty() && this.parsed.peek().getClass().equals(JSONObject.class);
		if(this.parsed.size() > 1) {
			this.result = this.parsed.peek();
		}
		this.parsed.pop();
	}
	
	@Override
    public void objectStart() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		linkToParent(jsonObject);
		this.parsed.push(jsonObject);
	}
	
	private void linkToParent(Object jsonThing) throws JSONException {
		if(this.parsed.isEmpty()) {
			// we are the first
			this.parsed.push(jsonThing);
		} else {
			Object top = this.parsed.peek();
			if(top instanceof JSONObject && this.key != null) {
				JSONObject jo = (JSONObject)top;
				jo.put(this.key, jsonThing);
				this.key = null;
			} else if(top instanceof JSONArray) {
				JSONArray ja = (JSONArray)top;
				ja.put(jsonThing);
			} else {
				throw new JSONException("Unexpected top stack element " + this.parsed);
			}
		}
	}
	
	@Override
    public void onBoolean(boolean b) throws JSONException {
		Boolean bool = new Boolean(b);
		linkToParent(bool);
	}
	
	@Override
    public void onDouble(double d) throws JSONException {
		Double doub = new Double(d);
		linkToParent(doub);
	}
	
	@Override
    public void onInteger(int i) throws JSONException {
		Integer in = new Integer(i);
		linkToParent(in);
	}
	
	@Override
    public void onKey(String key) {
		assert this.key == null;
		this.key = key;
	}
	
	@Override
    public void onLong(long l) throws JSONException {
		Long lo = new Long(l);
		linkToParent(lo);
	}
	
	@Override
    public void onNull() throws JSONException {
		linkToParent(null);
	}
	
	@Override
    public void onString(String s) throws JSONException {
		linkToParent(s);
	}
	
	public Object getParsed() {
		if(this.result != null) {
			return this.result;
		} else {
			// result is a primitive value on top of the stack
			assert this.parsed.size() == 1;
			return this.parsed.peek();
		}
	}
}
