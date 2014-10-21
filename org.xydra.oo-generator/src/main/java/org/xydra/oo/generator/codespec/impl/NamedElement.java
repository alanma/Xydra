package org.xydra.oo.generator.codespec.impl;

import org.xydra.annotations.NeverNull;
import org.xydra.oo.generator.codespec.IMember;

/**
 * Such as a {@link ClassSpec} or an {@link IMember} or such a class.
 * 
 * @author xamde
 */
public class NamedElement {

	@NeverNull
	private String name;

	NamedElement(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

}
