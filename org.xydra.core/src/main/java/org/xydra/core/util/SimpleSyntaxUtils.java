package org.xydra.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.URIFormatException;
import org.xydra.core.X;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XV;
import org.xydra.core.value.XValue;


/**
 * This interface provides de-serialization support for Xydra from a Java
 * properties files.
 * 
 * Syntax:
 * <ul>
 * <li>Comment lines start with '#'</li>
 * <li>Key-value pairs must be written as key=value</li>
 * <li>Values are written in JSON syntax
 * <dl>
 * <dt>{@link XID}</dt>
 * <dd>":name"</dd>
 * <dt>XString</dt>
 * <dd>"name"</dd>
 * <dt>XInteger</dt>
 * <dd>123</dd>
 * <dt>XLong</dt>
 * <dd>123</dd>
 * <dt>XDouble</dt>
 * <dd>123.45</dd>
 * <dt>XBoolean</dt>
 * <dd>'true' or 'false' (without quotes)</dd>
 * <dt>List of values</dt>
 * <dd>[ a, b, c] where a..c are encoded values</dd>
 * <dl></li>
 * <li>Keys can be declared by just mentioning them on a single line</li>
 * <li>Whitespace in key names is not permitted</li>
 * <li>Encoding must be UTF-8</li>
 * </ul>
 * 
 * 
 * 
 * The following example uses some syntax constructs: <code><pre>
 * # declares the XObject 'hans' and the property 'phone', sets value of hans.phone to '123'.
 * hans.phone=123
 * hans.email=hans@example.com
 * hans.knows=[":peter",":john",":dirk"]
 * # declares 'igor' as an object with no fields
 * igor
 * john.phone=1234
 * </pre></code>
 * 
 * TODO define behaviour for line-breaks in values
 * 
 * @author voelkel
 */
@RunsInJava
@RunsInAppEngine
@RunsInGWT
public class SimpleSyntaxUtils {
	
	private static final XID ACTOR_THIS = X.getIDProvider().fromString("SimpleSyntaxUtils");
	
	/**
	 * @param simpleSyntax a String in a restricted syntax, inspired from Java
	 *            java.util.Properties} syntax.
	 * @return an {@link XModel}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XModel.
	 */
	public static XModel toModel(XID modelID, String simpleSyntax) throws IllegalArgumentException {
		PseudoBufferedReader br = new PseudoBufferedReader(simpleSyntax);
		String line;
		
		XModel model = new MemoryModel(modelID);
		int lineNo = 1;
		line = br.readLine();
		while(line != null) {
			if(line.startsWith("#")) {
				// it's a comment
			} else {
				// parse
				int first = line.indexOf("=");
				String key;
				String value;
				if(first == -1) {
					key = line;
					value = null;
				} else {
					key = line.substring(0, first);
					value = line.substring(first + 1, line.length()).trim();
				}
				// pre-parse key
				String[] keyComponents = key.split("\\.");
				if(keyComponents.length > 2) {
					throw new IllegalArgumentException("Line " + lineNo
					        + ": Found more than one dot in key name '" + key + "'.");
				}
				
				XID objectID = null;
				try {
					objectID = X.getIDProvider().fromString(keyComponents[0].trim());
				} catch(URIFormatException e) {
					throw new IllegalArgumentException("Line " + lineNo + ": Key name syntax '"
					        + keyComponents[0].trim() + "' is not a valid XID.");
				}
				XObject object = model.createObject(ACTOR_THIS, objectID);
				
				XID fieldID = null;
				XField field = null;
				if(keyComponents.length == 2) {
					try {
						fieldID = X.getIDProvider().fromString(keyComponents[1].trim());
					} catch(URIFormatException e) {
						throw new IllegalArgumentException("Line " + lineNo + ": Key name syntax '"
						        + keyComponents[1].trim() + "' is not a valid XID.");
					}
					field = object.createField(ACTOR_THIS, fieldID);
				} else {
					if(value != null) {
						throw new IllegalArgumentException("Line " + lineNo
						        + ": An object cannot have a value.");
					}
				}
				
				if(value != null) {
					assert field != null;
					if(field.getValue() != null) {
						throw new IllegalArgumentException("Line " + lineNo
						        + ": Cannot redefine value for key " + key);
					}
					XValue xvalue;
					if(value.startsWith("[")) {
						// handle ID value/valuelist
						if(!value.endsWith("]")) {
							throw new IllegalArgumentException("Line " + lineNo
							        + ": Starts with '[' but does not end with ']'.");
						}
						String idValue = value.substring(1, value.length() - 1);
						String[] ids = idValue.split(",");
						if(ids.length == 1) {
							xvalue = XV.toValue(X.getIDProvider().fromString(ids[0]));
						} else {
							XID[] xids = new XID[ids.length];
							for(int i = 0; i < xids.length; i++) {
								xids[i] = X.getIDProvider().fromString(ids[i].trim());
							}
							xvalue = XV.toValue(xids);
						}
					} else {
						xvalue = XV.toValue(value);
					}
					field.setValue(ACTOR_THIS, xvalue);
				}
			}
			line = br.readLine();
			lineNo++;
		}
		
		return model;
	}
	
	public static String toSimpleSyntax(XModel model) {
		StringBuffer buf = new StringBuffer();
		
		// TODO add XModel.size() ?
		List<XID> sortedObjectIDS = new ArrayList<XID>();
		for(XID objectID : model) {
			sortedObjectIDS.add(objectID);
		}
		Collections.sort(sortedObjectIDS, new Comparator<XID>() {
			public int compare(XID o1, XID o2) {
				return o1.toURI().compareTo(o2.toURI());
			}
		});
		
		for(XID objectID : sortedObjectIDS) {
			XObject object = model.getObject(objectID);
			if(object.isEmpty()) {
				buf.append(objectID.toURI()).append("\n");
			} else {
				
				// sort
				List<XID> sortedFieldIDS = new ArrayList<XID>();
				for(XID fieldID : object) {
					sortedFieldIDS.add(fieldID);
				}
				Collections.sort(sortedFieldIDS, new Comparator<XID>() {
					public int compare(XID o1, XID o2) {
						return o1.toURI().compareTo(o2.toURI());
					}
				});
				
				for(XID fieldID : sortedFieldIDS) {
					buf.append(objectID.toURI()).append(".").append(fieldID.toURI()).append("=");
					XField field = object.getField(fieldID);
					if(!field.isEmpty()) {
						XValue value = field.getValue();
						buf.append(toSimpleSyntax(value));
					}
					buf.append("\n");
				}
			}
		}
		return buf.toString();
	}
	
	public static String toSimpleSyntax(XIDListValue xidListValue) {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for(int i = 0; i < xidListValue.size(); i++) {
			buf.append(xidListValue.get(i));
			if(i < xidListValue.size()) {
				buf.append(",");
			}
		}
		buf.append("]");
		return buf.toString();
		
	}
	
	public static String toSimpleSyntax(XIDValue xidValue) {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		buf.append(xidValue.toString());
		buf.append("]");
		return buf.toString();
	}
	
	public static String toSimpleSyntax(XValue xvalue) {
		return xvalue.toString();
	}
	
	/**
	 * Like a java.io.BufferedReader but based completely on Strings. GWT
	 * compatible.
	 * 
	 * @author voelkel
	 * 
	 */
	public static class PseudoBufferedReader {
		private String[] lines;
		private int index = 0;
		
		public PseudoBufferedReader(String source) {
			this.lines = source.split("[\\n\\r]");
		}
		
		public String readLine() {
			if(this.index == this.lines.length) {
				return null;
			} else {
				String line = this.lines[this.index];
				this.index++;
				while(line.equals("") && this.index != this.lines.length) {
					line = this.lines[this.index];
					this.index++;
				}
				return line;
			}
		}
		
	}
	
}
