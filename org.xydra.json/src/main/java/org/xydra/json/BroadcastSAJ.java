package org.xydra.json;

import java.util.HashSet;
import java.util.Set;

import org.xydra.core.serialize.json.JSONException;
import org.xydra.core.serialize.json.SAJ;


/**
 * Sends SAJ events to multiple SAJs. Mostly used for debugging.
 * 
 * @author voelkel
 */
public class BroadcastSAJ implements SAJ {
	
	private Set<SAJ> sajs = new HashSet<SAJ>();
	
	public void arrayEnd() throws JSONException {
		for(SAJ saj : this.sajs) {
			saj.arrayEnd();
		}
	}
	
	public void arrayStart() throws JSONException {
		for(SAJ saj : this.sajs) {
			saj.arrayStart();
		}
	}
	
	public void objectEnd() throws JSONException {
		for(SAJ saj : this.sajs) {
			saj.objectEnd();
		}
	}
	
	public void objectStart() throws JSONException {
		for(SAJ saj : this.sajs) {
			saj.objectStart();
		}
	}
	
	public void onBoolean(boolean b) throws JSONException {
		for(SAJ saj : this.sajs) {
			saj.onBoolean(b);
		}
	}
	
	public void onDouble(double d) throws JSONException {
		for(SAJ saj : this.sajs) {
			saj.onDouble(d);
		}
	}
	
	public void onInteger(int i) throws JSONException {
		for(SAJ saj : this.sajs) {
			saj.onInteger(i);
		}
	}
	
	public void onKey(String key) throws JSONException {
		for(SAJ saj : this.sajs) {
			saj.onKey(key);
		}
	}
	
	public void onLong(long l) throws JSONException {
		for(SAJ saj : this.sajs) {
			saj.onLong(l);
		}
	}
	
	public void onNull() throws JSONException {
		for(SAJ saj : this.sajs) {
			saj.onNull();
		}
	}
	
	public void onString(String s) throws JSONException {
		for(SAJ saj : this.sajs) {
			saj.onString(s);
		}
	}
	
	public void addSAJ(SAJ saj) {
		this.sajs.add(saj);
	}
	
}
