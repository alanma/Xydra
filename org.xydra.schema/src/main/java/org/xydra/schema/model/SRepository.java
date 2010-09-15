package org.xydra.schema.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author voelkel
 * 
 */
public class SRepository implements ISModel {
	
	public static final char EQUAL = '=';
	public static final char START = '{';
	public static final char SEP = ';';
	public static final char END = '}';
	
	public SName name;
	
	public SRepository(SName name) {
		super();
		this.name = name;
	}
	
	public List<SModel> models = new ArrayList<SModel>();
	
	public void toSyntax(StringBuffer buf) {
		this.name.toSyntax(buf);
		buf.append(EQUAL);
		buf.append(START);
		Iterator<SModel> it = this.models.iterator();
		while(it.hasNext()) {
			SModel model = it.next();
			model.toSyntax(buf);
			buf.append(SEP);
		}
		buf.append(END);
	}
	
}
