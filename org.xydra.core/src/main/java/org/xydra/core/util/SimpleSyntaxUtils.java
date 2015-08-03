package org.xydra.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.Base;
import org.xydra.base.URIFormatException;
import org.xydra.base.XId;
import org.xydra.base.value.XIdListValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.sharedutils.XyAssert;


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
 * <dt>{@link XId}</dt>
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
 * @author xamde
 */
@RequiresAppEngine(false)
@RunsInAppEngine(true)
@RunsInGWT(true)
public class SimpleSyntaxUtils {

	/**
	 * Like a java.io.BufferedReader but based completely on Strings. GWT
	 * compatible.
	 *
	 * @author xamde
	 *
	 */
	public static class PseudoBufferedReader {
		private int index = 0;
		private final String[] lines;

		public PseudoBufferedReader(final String source) {
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

	private static final XId ACTOR_THIS = XX.toId("SimpleSyntaxUtils");

	private static final String PSW_THIS = null;

	/**
	 * @param modelId
	 * @param simpleSyntax a String in a restricted syntax, inspired from Java
	 *            java.util.Properties} syntax.
	 * @return an {@link XModel}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XModel.
	 */
	public static XModel toModel(final XId modelId, final String simpleSyntax) throws IllegalArgumentException {
		final PseudoBufferedReader br = new PseudoBufferedReader(simpleSyntax);
		String line;

		final XModel model = new MemoryModel(ACTOR_THIS, PSW_THIS, modelId);
		int lineNo = 1;
		line = br.readLine();
		while(line != null) {
			if(line.startsWith("#")) {
				// it's a comment
			} else {
				// parse
				final int first = line.indexOf("=");
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
				final String[] keyComponents = key.split("\\.");
				if(keyComponents.length > 2) {
					throw new IllegalArgumentException("Line " + lineNo
					        + ": Found more than one dot in key name '" + key + "'.");
				}

				XId objectId = null;
				try {
					objectId = Base.toId(keyComponents[0].trim());
				} catch(final URIFormatException e) {
					throw new IllegalArgumentException("Line " + lineNo + ": Key name syntax '"
					        + keyComponents[0].trim() + "' is not a valid XId.");
				}
				final XObject object = model.createObject(objectId);

				XId fieldId = null;
				XField field = null;
				if(keyComponents.length == 2) {
					try {
						fieldId = Base.toId(keyComponents[1].trim());
					} catch(final URIFormatException e) {
						throw new IllegalArgumentException("Line " + lineNo + ": Key name syntax '"
						        + keyComponents[1].trim() + "' is not a valid XId.");
					}
					field = object.createField(fieldId);
				} else {
					if(value != null) {
						throw new IllegalArgumentException("Line " + lineNo
						        + ": An object cannot have a value.");
					}
				}

				if(value != null) {
					XyAssert.xyAssert(field != null); assert field != null;
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
						final String idValue = value.substring(1, value.length() - 1);
						final String[] ids = idValue.split(",");
						if(ids.length == 1) {
							xvalue = Base.toId(ids[0]);
						} else {
							final XId[] xids = new XId[ids.length];
							for(int i = 0; i < xids.length; i++) {
								xids[i] = Base.toId(ids[i].trim());
							}
							xvalue = XV.toValue(xids);
						}
					} else {
						xvalue = XV.toValue(value);
					}
					field.setValue(xvalue);
				}
			}
			line = br.readLine();
			lineNo++;
		}

		return model;
	}

	public static String toSimpleSyntax(final XId xid) {
		final StringBuffer buf = new StringBuffer();
		buf.append("[");
		buf.append(xid.toString());
		buf.append("]");
		return buf.toString();
	}

	public static String toSimpleSyntax(final XIdListValue xidListValue) {
		final StringBuffer buf = new StringBuffer();
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

	public static String toSimpleSyntax(final XModel model) {
		final StringBuffer buf = new StringBuffer();

		// TODO add XModel.size() ?
		final List<XId> sortedObjectIdS = new ArrayList<XId>();
		for(final XId objectId : model) {
			sortedObjectIdS.add(objectId);
		}
		Collections.sort(sortedObjectIdS, new Comparator<XId>() {
			@Override
			public int compare(final XId o1, final XId o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		for(final XId objectId : sortedObjectIdS) {
			final XObject object = model.getObject(objectId);
			if(object.isEmpty()) {
				buf.append(objectId.toString()).append("\n");
			} else {

				// sort
				final List<XId> sortedFieldIdS = new ArrayList<XId>();
				for(final XId fieldId : object) {
					sortedFieldIdS.add(fieldId);
				}
				Collections.sort(sortedFieldIdS, new Comparator<XId>() {
					@Override
					public int compare(final XId o1, final XId o2) {
						return o1.toString().compareTo(o2.toString());
					}
				});

				for(final XId fieldId : sortedFieldIdS) {
					buf.append(objectId.toString()).append(".").append(fieldId.toString())
					        .append("=");
					final XField field = object.getField(fieldId);
					if(!field.isEmpty()) {
						final XValue value = field.getValue();
						buf.append(toSimpleSyntax(value));
					}
					buf.append("\n");
				}
			}
		}
		return buf.toString();
	}

	public static String toSimpleSyntax(final XValue xvalue) {
		return xvalue.toString();
	}

}
