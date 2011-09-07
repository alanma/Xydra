package org.xydra.core.json;

import java.util.LinkedList;
import java.util.List;

import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XListValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.value.TypeSystem;
import org.xydra.json.BroadcastSAJ;
import org.xydra.json.DumpSAJ;
import org.xydra.json.JSONException;
import org.xydra.json.JSONWriter;
import org.xydra.json.JsonParser;
import org.xydra.json.SAJ;
import org.xydra.minio.MiniStringWriter;


public class XJson {
	
	public static String asJsonString(XReadableField xfield) {
		MiniStringWriter mw = new MiniStringWriter();
		JSONWriter jw = new JSONWriter(mw);
		asJsonString(xfield, jw);
		return mw.toString();
	}
	
	private static void asJsonString(XReadableField xfield, JSONWriter jsonWriter) {
		try {
			jsonWriter.objectStart();
			jsonWriter.key(xfield.getID().toString());
			asJsonString(xfield.getValue(), jsonWriter);
			jsonWriter.objectEnd();
		} catch(JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String asJsonString(XReadableModel xmodel) {
		MiniStringWriter mw = new MiniStringWriter();
		JSONWriter jsonWriter = new JSONWriter(mw);
		asJsonString(xmodel, jsonWriter);
		return mw.toString();
	}
	
	private static void asJsonString(XReadableModel xmodel, JSONWriter jsonWriter) {
		try {
			jsonWriter.objectStart();
			jsonWriter.key(xmodel.getID().toString());
			jsonWriter.arrayStart();
			for(XID objectID : xmodel) {
				XReadableObject xo = xmodel.getObject(objectID);
				asJsonString(xo, jsonWriter);
			}
			jsonWriter.arrayEnd();
			jsonWriter.objectEnd();
		} catch(JSONException e) {
			throw new AssertionError();
		}
	}
	
	public static String asJsonString(XReadableObject xobject) {
		MiniStringWriter mw = new MiniStringWriter();
		JSONWriter jw = new JSONWriter(mw);
		asJsonString(xobject, jw);
		return mw.toString();
	}
	
	private static void asJsonString(XReadableObject xobject, JSONWriter jsonWriter) {
		try {
			jsonWriter.objectStart();
			jsonWriter.key(xobject.getID().toString());
			jsonWriter.arrayStart();
			for(XID fieldID : xobject) {
				XReadableField xf = xobject.getField(fieldID);
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
			} else if(xvalue instanceof XID) {
				jsonWriter.value(((XID)xvalue).toString());
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
					jsonWriter.value(xid.toString());
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
		@SuppressWarnings("unused")
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
		
		@Override
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
					this.field.setValue((XID)o);
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
					List<Double> doubleList = new LinkedList<Double>();
					for(Object o : this.tempList) {
						doubleList.add((Double)o);
					}
					listValue = X.getValueFactory().createDoubleListValue(doubleList);
				} else if(commonType.equals(XIntegerValue.class)) {
					List<Integer> intList = new LinkedList<Integer>();
					for(Object o : this.tempList) {
						intList.add((Integer)o);
					}
					listValue = X.getValueFactory().createIntegerListValue(intList);
				} else if(commonType.equals(XLongValue.class)) {
					List<Long> longList = new LinkedList<Long>();
					for(Object o : this.tempList) {
						longList.add((Long)o);
					}
					listValue = X.getValueFactory().createLongListValue(longList);
				} else if(commonType.equals(XBooleanValue.class)) {
					List<Boolean> booleanList = new LinkedList<Boolean>();
					for(Object o : this.tempList) {
						booleanList.add((Boolean)o);
					}
					listValue = X.getValueFactory().createBooleanListValue(booleanList);
				} else if(commonType.equals(XStringValue.class)) {
					listValue = X.getValueFactory().createStringListValue(
					        this.tempList.toArray(new String[0]));
				} else if(commonType.equals(XID.class)) {
					listValue = X.getValueFactory().createIDListValue(
					        this.tempList.toArray(new XID[0]));
				} else {
					throw new RuntimeException("Unknown common type " + commonType.getName());
				}
				this.field.setValue(listValue);
				
			}
			
			this.tempList = null;
		}
		
		@Override
        public void arrayStart() {
			if(this.parseState != ParseState.FIELD)
				return;
			
			assert !inValueArray();
			// type of array remains unclear
			this.tempList = new LinkedList<Object>();
		}
		
		@Override
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
		
		@Override
        public void objectStart() {
			// we use onKey() instead
		}
		
		@Override
        public void onBoolean(boolean b) {
			assert this.parseState == ParseState.FIELD;
			if(inValueArray()) {
				this.tempList.add(b);
			} else {
				assert this.field != null;
				XValue value = X.getValueFactory().createBooleanValue(b);
				this.field.setValue(value);
			}
		}
		
		@Override
        public void onDouble(double d) {
			assert this.parseState == ParseState.FIELD;
			if(inValueArray()) {
				this.tempList.add(d);
			} else {
				assert this.field != null;
				XValue value = X.getValueFactory().createDoubleValue(d);
				this.field.setValue(value);
			}
		}
		
		@Override
        public void onInteger(int i) {
			assert this.parseState == ParseState.FIELD;
			if(inValueArray()) {
				this.tempList.add(i);
			} else {
				assert this.field != null;
				XValue value = X.getValueFactory().createIntegerValue(i);
				this.field.setValue(value);
			}
		}
		
		@Override
        public void onKey(String key) {
			XID xid = X.getIDProvider().fromString(key);
			switch(this.parseState) {
			case REPO: // add model
				assert this.repository != null;
				this.model = this.repository.createModel(xid);
				this.parseState = ParseState.MODEL;
				break;
			case MODEL: // add object
				assert this.model != null;
				this.object = this.model.createObject(xid);
				this.parseState = ParseState.OBJECT;
				break;
			case OBJECT: // create field
				assert this.object != null;
				this.field = this.object.createField(xid);
				this.parseState = ParseState.FIELD;
				break;
			case FIELD:
				throw new XParseException("key() in FIELD is not permitted.");
			}
		}
		
		@Override
        public void onLong(long l) {
			assert this.parseState == ParseState.FIELD;
			if(inValueArray()) {
				this.tempList.add(l);
			} else {
				assert this.field != null;
				XValue value = X.getValueFactory().createLongValue(l);
				this.field.setValue(value);
			}
		}
		
		@Override
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
		
		@Override
        public void onString(String s) {
			assert this.parseState == ParseState.FIELD : "In " + this.parseState
			        + " while parsing '" + s + "'";
			if(inValueArray()) {
				this.tempList.add(s);
			} else {
				assert this.field != null;
				XValue value = X.getValueFactory().createStringValue(s);
				this.field.setValue(value);
			}
		}
		
	}
	
}
