package org.xydra.schema.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SObject implements ISModel, ISyntax {
	
	public static final char EQUAL = '=';
	public static final char START = '{';
	public static final char SEP = ';';
	public static final char END = '}';
	
	public SName name;
	
	public SObject(SName name) {
		super();
		this.name = name;
	}
	
	public List<SField> fields = new ArrayList<SField>();
	
	public void toSyntax(StringBuffer buf) {
		this.name.toSyntax(buf);
		buf.append(EQUAL);
		buf.append(START);
		Iterator<SField> it = this.fields.iterator();
		while(it.hasNext()) {
			SField field = it.next();
			field.toSyntax(buf);
			buf.append(SEP);
		}
		buf.append(END);
	}
	
	/**
	 * @param objectDef
	 * @return
	 * @throws IllegalArgumentException if parsing fails
	 */
	public static SObject parse(String objectDef) {
		int pos = 0;
		// whitespace
		while(objectDef.charAt(pos) == ' ') {
			pos++;
		}
		// name, equal sign
		int equalPos = objectDef.indexOf(EQUAL, pos);
		if(equalPos < 0) {
			throw new IllegalArgumentException("Expected <" + EQUAL + "> in <" + objectDef);
		}
		String nameString = objectDef.substring(pos, equalPos);
		SName name = SName.parse(nameString.trim());
		pos = equalPos + 1;
		// whitespace
		while(objectDef.charAt(pos) == ' ') {
			pos++;
		}
		// start
		if(objectDef.charAt(pos) != START) {
			throw new IllegalArgumentException("Expected <" + START + "> found <"
			        + objectDef.substring(0, pos));
		}
		pos++;
		// in list
		int sepPos = objectDef.indexOf(SEP, pos);
		while(sepPos >= 0) {
			
			pos = sepPos + 1;
			sepPos = objectDef.indexOf(SEP, pos);
		}
		
		// whitespace
		while(objectDef.charAt(pos) == ' ') {
			pos++;
		}
		// end
		if(objectDef.charAt(pos) != END) {
			throw new IllegalArgumentException("Expected <" + END + "> found <"
			        + objectDef.substring(0, pos));
		}
		pos++;
		// whitespace
		while(objectDef.charAt(pos) == ' ') {
			pos++;
		}
		
		int equalPost = objectDef.indexOf(EQUAL, pos);
		
		String[] equals = objectDef.split("=");
		if(equals.length != 2) {
			throw new IllegalArgumentException("Found no equal sign in <" + objectDef + ">");
		}
		// SName name = SName.parse(equals[0]);
		// SObject object = new SObject(name);
		// // parse children
		// String fullList = equals[1];
		// if(!fullList.startsWith("" + START)) {
		// throw new IllegalArgumentException("Found no " + START +
		// " after name declaration");
		// }
		// if(!fullList.endsWith("" + END)) {
		// throw new IllegalArgumentException("Found no " + END +
		// " after field declarations");
		// }
		// String coreList = fullList.substring(1, fullList.length() - 1);
		// String[] fieldDefs = coreList.split("" + SEP);
		// for(String fieldDef : fieldDefs) {
		// SField field = SField.parse(fieldDef);
		// object.fields.add(field);
		// }
		// return object;
		return null;
	}
	
}
