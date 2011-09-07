package org.xydra.json;

import org.xydra.core.serialize.json.SAJ;

public class DumpSAJ implements SAJ {
	
	private String indent = "";
	
	@Override
    public void arrayEnd() {
		decreaseIndent();
		System.out.println(this.indent + "]");
	}
	
	@Override
    public void arrayStart() {
		System.out.println(this.indent + "[");
		increaseIndent();
	}
	
	private void decreaseIndent() {
		this.indent = this.indent.substring(0, this.indent.length() - 2);
	}
	
	private void increaseIndent() {
		this.indent = this.indent + "  ";
	}
	
	@Override
    public void objectEnd() {
		decreaseIndent();
		System.out.println(this.indent + "}");
	}
	
	@Override
    public void objectStart() {
		System.out.println(this.indent + "{");
		increaseIndent();
	}
	
	@Override
    public void onBoolean(boolean b) {
		System.out.println(this.indent + "bool : " + b);
	}
	
	@Override
    public void onDouble(double d) {
		System.out.println(this.indent + "double : " + d);
	}
	
	@Override
    public void onInteger(int i) {
		System.out.println(this.indent + "int : " + i);
	}
	
	@Override
    public void onKey(String key) {
		System.out.println(this.indent + "key : '" + key + "'");
	}
	
	@Override
    public void onLong(long l) {
		System.out.println(this.indent + "long : " + l);
	}
	
	@Override
    public void onNull() {
		System.out.println(this.indent + "null");
	}
	
	@Override
    public void onString(String s) {
		System.out.println(this.indent + "string : '" + s + "'");
	}
	
}
