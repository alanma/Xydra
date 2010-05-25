package org.xydra.json;

public class DumpSAJ implements SAJ {
	
	private String indent = "";
	
	public void arrayEnd() {
		decreaseIndent();
		System.out.println(this.indent + "]");
	}
	
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
	
	public void objectEnd() {
		decreaseIndent();
		System.out.println(this.indent + "}");
	}
	
	public void objectStart() {
		System.out.println(this.indent + "{");
		increaseIndent();
	}
	
	public void onBoolean(boolean b) {
		System.out.println(this.indent + "bool : " + b);
	}
	
	public void onDouble(double d) {
		System.out.println(this.indent + "double : " + d);
	}
	
	public void onInteger(int i) {
		System.out.println(this.indent + "int : " + i);
	}
	
	public void onKey(String key) {
		System.out.println(this.indent + "key : '" + key + "'");
	}
	
	public void onLong(long l) {
		System.out.println(this.indent + "long : " + l);
	}
	
	public void onNull() {
		System.out.println(this.indent + "null");
	}
	
	public void onString(String s) {
		System.out.println(this.indent + "string : '" + s + "'");
	}
	
}
