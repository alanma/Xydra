package org.xydra.core.json;

import java.util.LinkedList;
import java.util.List;

import org.xydra.core.X;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.value.TypeSystem;
import org.xydra.core.value.XBooleanListValue;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XDoubleListValue;
import org.xydra.core.value.XDoubleValue;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XIntegerListValue;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XListValue;
import org.xydra.core.value.XLongListValue;
import org.xydra.core.value.XLongValue;
import org.xydra.core.value.XStringListValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XValue;
import org.xydra.json.BroadcastSAJ;
import org.xydra.json.DumpSAJ;
import org.xydra.json.JSONException;
import org.xydra.json.JSONWriter;
import org.xydra.json.JsonParser;
import org.xydra.json.SAJ;
import org.xydra.minio.MiniStringWriter;



public class XJson {
	
	public static String asJsonString(XBaseField xfield) {
		MiniStringWriter mw = new MiniStringWriter();
		JSONWriter jw = new JSONWriter(mw);
		asJsonString(xfield, jw);
		return mw.toString();
	}
	
	private static void asJsonString(XBaseField xfield, JSONWriter jsonWriter) {
		try {
			jsonWriter.objectStart();
			jsonWriter.key(xfield.getID().toURI());
			asJsonString(xfield.getValue(), jsonWriter);
			jsonWriter.objectEnd();
		} catch(JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String asJsonString(XBaseModel xmodel) {
		MiniStringWriter mw = new MiniStringWriter();
		JSONWriter jsonWriter = new JSONWriter(mw);
		asJsonString(xmodel, jsonWriter);
		return mw.toString();
	}
	
	private static void asJsonString(XBaseModel xmodel, JSONWriter jsonWriter) {
		try {
			jsonWriter.objectStart();
			jsonWriter.key(xmodel.getID().toURI());
			jsonWriter.arrayStart();
			for(XID objectID : xmodel) {
				XBaseObject xo = xmodel.getObject(objectID);
				asJsonString(xo, jsonWriter);
			}
			jsonWriter.arrayEnd();
			jsonWriter.objectEnd();
		} catch(JSONException e) {
			throw new AssertionError();
		}
	}
	
	public static String asJsonString(XBaseObject xobject) {
		MiniStringWriter mw = new MiniStringWriter();
		JSONWriter jw = new JSONWriter(mw);
		asJsonString(xobject, jw);
		return mw.toString();
	}
	
	private static void asJsonString(XBaseObject xobject, JSONWriter jsonWriter) {
		try {
			jsonWriter.objectStart();
			jsonWriter.key(xobject.getID().toURI());
			jsonWriter.arrayStart();
			for(XID fieldID : xobject) {
				XBaseField xf = xobject.getField(fieldID);
				asJsonString(xf, jsonWriter);
			}
			jsonWriter.arrayEnd();
			jsonWriter.objectEnd();
		} catch(JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String asJsonString(XValue xvalue) {
		MiniStringWriter mw = new MiniStringWriter();
		JSONWriter jw = new JSONWriter(mw);
		asJsonString(xvalue, jw);
		return mw.toString();
	}
	
	private static void asJsonString(XValue xvalue, JSONWriter jsonWriter) {
		try {
			if(xvalue == null) {
				jsonWriter.nullValue();
			} else if(xvalue instanceof XListValue<?>) {
				asJsonStringFromList((XListValue<?>)xvalue, jsonWriter);
			} else if(xvalue instanceof XBooleanValue) {
				jsonWriter.value(((XBooleanValue)xvalue).contents());
			} else if(xvalue instanceof XDoubleValue) {
				jsonWriter.value(((XDoubleValue)xvalue).contents());
			} else if(xvalue instanceof XIDValue) {
				jsonWriter.value(((XIDValue)xvalue).contents().toURI());
			} else if(xvalue instanceof XIntegerValue) {
				jsonWriter.value(((XIntegerValue)xvalue).contents());
			} else if(xvalue instanceof XLongValue) {
				jsonWriter.value(((XLongValue)xvalue).contents());
			} else if(xvalue instanceof XStringValue) {
				jsonWriter.value(((XStringValue)xvalue).contents());
			}
		} catch(JSONException e) {
			throw new RuntimeException("this never happens", e);
		}
	}
	
	private static void asJsonStringFromList(XListValue<?> xlistvalue, JSONWriter jsonWriter) {
		try {
			jsonWriter.arrayStart();
			if(xlistvalue == null) {
				jsonWriter.nullValue();
			} else if(xlistvalue instanceof XBooleanListValue) {
				for(Boolean b : ((XBooleanListValue)xlistvalue)) {
					jsonWriter.value(b);
				}
			} else if(xlistvalue instanceof XDoubleListValue) {
				for(Double d : ((XDoubleListValue)xlistvalue)) {
					jsonWriter.value(d);
				}
			} else if(xlistvalue instanceof XLongListValue) {
				for(Long l : ((XLongListValue)xlistvalue)) {
					jsonWriter.value(l);
				}
			} else if(xlistvalue instanceof XIDListValue) {
				for(XID xid : ((XIDListValue)xlistvalue)) {
					jsonWriter.value(xid.toURI());
				}
			} else if(xlistvalue instanceof XIntegerListValue) {
				for(Integer i : ((XIntegerListValue)xlistvalue)) {
					jsonWriter.value(i);
				}
			} else if(xlistvalue instanceof XStringListValue) {
				for(String s : ((XStringListValue)xlistvalue)) {
					jsonWriter.value(s);
				}
			} else {
				throw new IllegalArgumentException("unsupported XListValue type: " + xlistvalue);
			}
			jsonWriter.arrayEnd();
		} catch(JSONException e) {
			throw new AssertionError();
		}
	}
	
	private enum ParseState {
		REPO, MODEL, OBJECT, FIELD
	}
	
	public static void addToXRepository(XID actor, String json, XRepository repository)
	        throws JSONException {
		// FIXME
		BroadcastSAJ broadcastSAJ = new BroadcastSAJ();
		DumpSAJ dumpSAJ = new DumpSAJ();
		broadcastSAJ.addSAJ(dumpSAJ);
		
		XEventsSAJ saj = new XEventsSAJ(actor, repository, null, null, null, ParseState.REPO);
		// FIXME
		broadcastSAJ.addSAJ(saj);
		
		// FIXME
		JsonParser jsonParser = new JsonParser(broadcastSAJ);
		jsonParser.parse(json);
	}
	
	public static void addToXModel(XID actor, String json, XModel model) throws JSONException {
		XEventsSAJ saj = new XEventsSAJ(actor, null, model, null, null, ParseState.MODEL);
		JsonParser jsonParser = new JsonParser(saj);
		jsonParser.parse(json);
	}
	
	// TODO the others...
	
	private static class XEventsSAJ implements SAJ {
		
		private ParseState parseState;
		
		private List<Object> tempList;
		private XID actor;
		private XRepository repository;
		private XModel model;
		private XObject object;
		private XField field;
		
		public XEventsSAJ(XID actor, XRepository repository, XModel model, XObject object,
		        XField field, ParseState parseState) {
			this.actor = actor;
			this.repository = repository;
			this.model = model;
			this.object = object;
			this.field = field;
			this.parseState = parseState;
		}
		
		private boolean inValueArray() {
			return this.tempList != null;
		}
		
		public void arrayEnd() {
			if(this.parseState != ParseState.FIELD)
				return;
			
			if(this.tempList.size() == 0) {
				this.onNull();
			} else if(this.tempList.size() == 1) {
				// commit single value
				Object o = this.tempList.get(0);
				if(o instanceof XBooleanValue) {
					this.onBoolean((Boolean)o);
				} else if(o instanceof XDoubleValue) {
					this.onDouble((Double)o);
				} else if(o instanceof XIntegerValue) {
					this.onInteger((Integer)o);
				} else if(o instanceof XLongValue) {
					this.onLong((Long)o);
				} else if(o instanceof XStringValue) {
					this.onString((String)o);
				} else if(o instanceof XID) {
					this.field.setValue(this.actor, X.getValueFactory().createIDValue((XID)o));
				}
			} else {
				// real array
				
				// determine common array type
				Class<?> commonType = this.tempList.get(0).getClass();
				for(int i = 1; i < this.tempList.size(); i++) {
					Class<?> nextType = this.tempList.get(i).getClass();
					if(TypeSystem.canStore(commonType, nextType)) {
						// ok, commonType fits its purpose
					} else if(TypeSystem.canStore(nextType, commonType)) {
						// generalize type
						commonType = nextType;
					} else {
						throw new RuntimeException("Cannot convert " + this.tempList);
					}
				}
				commonType = TypeSystem.getXType(commonType);
				
				XListValue<?> listValue = null;
				if(commonType.equals(XDoubleValue.class)) {
					listValue = X.getValueFactory().createDoubleListValue(
					        this.tempList.toArray(new Double[0]));
				} else if(commonType.equals(XIntegerValue.class)) {
					listValue = X.getValueFactory().createIntegerListValue(
					        this.tempList.toArray(new Integer[0]));
				} else if(commonType.equals(XLongValue.class)) {
					listValue = X.getValueFactory().createLongListValue(
					        this.tempList.toArray(new Long[0]));
				} else if(commonType.equals(XBooleanValue.class)) {
					listValue = X.getValueFactory().createBooleanListValue(
					        this.tempList.toArray(new Boolean[0]));
				} else if(commonType.equals(XStringValue.class)) {
					listValue = X.getValueFactory().createStringListValue(
					        this.tempList.toArray(new String[0]));
				} else if(commonType.equals(XIDValue.class)) {
					listValue = X.getValueFactory().createIDListValue(
					        this.tempList.toArray(new XID[0]));
				} else {
					throw new RuntimeException("Unknown common type " + commonType.getName());
				}
				this.field.setValue(this.actor, listValue);
				
			}
			
			this.tempList = null;
		}
		
		public void arrayStart() {
			if(this.parseState != ParseState.FIELD)
				return;
			
			assert !inValueArray();
			// type of array remains unclear
			this.tempList = new LinkedList<Object>();
		}
		
		public void objectEnd() {
			switch(this.parseState) {
			case REPO:
				// done parsing
				break;
			case MODEL:
				this.model = null;
				this.parseState = ParseState.REPO;
				break;
			case OBJECT:
				this.object = null;
				this.parseState = ParseState.MODEL;
				break;
			case FIELD:
				this.field = null;
				this.parseState = ParseState.OBJECT;
				break;
			}
		}
		
		public void objectStart() {
			// we use onKey() instead
		}
		
		public void onBoolean(boolean b) {
			assert this.parseState == ParseState.FIELD;
			if(inValueArray()) {
				this.tempList.add(b);
			} else {
				assert this.field != null;
				XValue value = X.getValueFactory().createBooleanValue(b);
				this.field.setValue(this.actor, value);
			}
		}
		
		public void onDouble(double d) {
			assert this.parseState == ParseState.FIELD;
			if(inValueArray()) {
				this.tempList.add(d);
			} else {
				assert this.field != null;
				XValue value = X.getValueFactory().createDoubleValue(d);
				this.field.setValue(this.actor, value);
			}
		}
		
		public void onInteger(int i) {
			assert this.parseState == ParseState.FIELD;
			if(inValueArray()) {
				this.tempList.add(i);
			} else {
				assert this.field != null;
				XValue value = X.getValueFactory().createIntegerValue(i);
				this.field.setValue(this.actor, value);
			}
		}
		
		public void onKey(String key) {
			XID xid = X.getIDProvider().fromString(key);
			switch(this.parseState) {
			case REPO: // add model
				assert this.repository != null;
				this.model = this.repository.createModel(this.actor, xid);
				this.parseState = ParseState.MODEL;
				break;
			case MODEL: // add object
				assert this.model != null;
				this.object = this.model.createObject(this.actor, xid);
				this.parseState = ParseState.OBJECT;
				break;
			case OBJECT: // create field
				assert this.object != null;
				this.field = this.object.createField(this.actor, xid);
				this.parseState = ParseState.FIELD;
				break;
			case FIELD:
				throw new XParseException("key() in FIELD is not permitted.");
			}
		}
		
		public void onLong(long l) {
			assert this.parseState == ParseState.FIELD;
			if(inValueArray()) {
				this.tempList.add(l);
			} else {
				assert this.field != null;
				XValue value = X.getValueFactory().createLongValue(l);
				this.field.setValue(this.actor, value);
			}
		}
		
		public void onNull() {
			switch(this.parseState) {
			case REPO:
			case MODEL:
			case OBJECT:
			case FIELD: {// nothing to do
			}
				break;
			}
		}
		
		public void onString(String s) {
			assert this.parseState == ParseState.FIELD : "In " + this.parseState
			        + " while parsing '" + s + "'";
			if(inValueArray()) {
				this.tempList.add(s);
			} else {
				assert this.field != null;
				XValue value = X.getValueFactory().createStringValue(s);
				this.field.setValue(this.actor, value);
			}
		}
		
	}
	
}
