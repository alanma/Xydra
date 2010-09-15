package org.xydra.schema.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SList extends SValue implements ISyntax {
	
	public static final char START = '[';
	public static final char SEP = ',';
	public static final char END = ']';
	
	public List<ISyntax> list = new ArrayList<ISyntax>();
	
	public void toSyntax(StringBuffer buf) {
		buf.append(START);
		Iterator<ISyntax> it = this.list.iterator();
		while(it.hasNext()) {
			ISyntax iSyntax = it.next();
			iSyntax.toSyntax(buf);
			if(it.hasNext()) {
				buf.append(SEP);
			}
		}
		buf.append(END);
	}
	
	public static SValue parse(String fullList) {
		if(!fullList.startsWith("" + START)) {
			throw new IllegalArgumentException("Found no " + START + " in list");
		}
		if(!fullList.endsWith("" + END)) {
			throw new IllegalArgumentException("Found no " + END + " in list");
		}
		String coreList = fullList.substring(1, fullList.length() - 1);
		SList list = new SList();
		String[] parts = coreList.split("" + SEP);
		for(String part : parts) {
			SValue value = SValue.parse(part);
			list.list.add(value);
		}
		return list;
	}
	
}
