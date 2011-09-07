package org.xydra.schema.model;

public class SField implements ISyntax {
	
	public static final char EQUAL = '=';
	
	public SField(SType type, SName name, SValue value) {
		super();
		this.type = type;
		this.name = name;
		this.value = value;
	}
	
	public SType type;
	public SName name;
	public SValue value;
	
	@Override
    public void toSyntax(StringBuffer buf) {
		this.type.toSyntax(buf);
		buf.append(" ");
		this.name.toSyntax(buf);
		buf.append(EQUAL);
		this.value.toSyntax(buf);
	}
	
	public static SField parse(String fieldDef) {
		String[] equals = fieldDef.split("" + EQUAL);
		if(equals.length != 2) {
			throw new IllegalArgumentException("Found no equal sign");
		}
		String[] spaced = equals[0].split(" ");
		if(spaced.length != 2) {
			throw new IllegalArgumentException("Found no space in part before equal sign");
		}
		SType type = SType.parse(spaced[0]);
		SName name = SName.parse(spaced[1]);
		SValue value = SValue.parse(equals[1]);
		SField field = new SField(type, name, value);
		return field;
	}
	
}
