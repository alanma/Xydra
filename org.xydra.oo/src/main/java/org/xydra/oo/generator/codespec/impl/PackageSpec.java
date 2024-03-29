package org.xydra.oo.generator.codespec.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Packagename + classes, sub-packages
 *
 * @author xamde
 */
public class PackageSpec {

	public Set<ClassSpec> classes = new HashSet<ClassSpec>();

	private final String fullPackageName;

	public Class<?> generatedFrom;

	private final boolean isBuiltIn;

	public List<PackageSpec> subPackages = new ArrayList<PackageSpec>();

	public PackageSpec(final String fullPackageName, final boolean isBuiltIn) {
		this.fullPackageName = fullPackageName;
		this.isBuiltIn = isBuiltIn;
	}

	public ClassSpec addAbstractClass(final String name) {
		return new ClassSpec(this, "abstract class", name);
	}

	public ClassSpec addClass(final String name) {
		return new ClassSpec(this, "class", name);
	}

	/**
	 * Don't always create a new one, also return existing one with same name
	 *
	 * @param name
	 * @return an existing or new classSpec with the given name
	 */
	public ClassSpec addInterface(final String name) {
		// slow, but who cares
		for (final ClassSpec cs : this.classes) {
			if (cs.getName().equals(name)) {
				return cs;
			}
		}
		// else
		return new ClassSpec(this, "interface", name);
	}

	public void dump() {
		System.out.println("PackageSpec (" + (isBuiltIn() ? "builtIn" : "generated") + ") "
				+ this.fullPackageName);
		System.out.println("Generated from: "
				+ (this.generatedFrom == null ? "UNKNOWN" : this.generatedFrom.getCanonicalName()));
		for (final ClassSpec c : this.classes) {
			c.dump();
		}
	}

	public String getFQPackageName() {
		return this.fullPackageName;
	}

	public boolean isBuiltIn() {
		return this.isBuiltIn;
	}

}
