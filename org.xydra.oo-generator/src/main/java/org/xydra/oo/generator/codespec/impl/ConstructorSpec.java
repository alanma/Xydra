package org.xydra.oo.generator.codespec.impl;

import org.xydra.oo.generator.codespec.IMember;

public class ConstructorSpec extends AbstractConstructorOrMethodSpec implements IMember {

	ConstructorSpec(final ClassSpec classSpec, final String generatedFrom) {
		super(classSpec.getName(), generatedFrom);
	}

	@Override
	public String toString() {
		String s = "";
		s += "CONSTRUCTOR\n";
		s += "  name:" + getName() + "\n";
		s += "  comment:" + getComment() + "\n";
		for (final FieldSpec p : this.params) {
			s += "  PARAM " + p.toString() + "\n";
		}
		for (final String l : this.sourceLines) {
			s += "  CODE " + l + "\n";
		}
		return s;
	}

}
