package org.xydra.schema.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SModel implements ISModel, ISyntax {
	
	public static final char EQUAL = '=';
	public static final char START = '{';
	public static final char SEP = ';';
	public static final char END = '}';
	
	public SName name;
	
	public SModel(SName name) {
		super();
		this.name = name;
	}
	
	public List<SObject> objects = new ArrayList<SObject>();
	
	public void toSyntax(StringBuffer buf) {
		this.name.toSyntax(buf);
		buf.append(EQUAL);
		buf.append(START);
		Iterator<SObject> it = this.objects.iterator();
		while(it.hasNext()) {
			SObject object = it.next();
			object.toSyntax(buf);
			buf.append(SEP);
		}
		buf.append(END);
	}
	
}
