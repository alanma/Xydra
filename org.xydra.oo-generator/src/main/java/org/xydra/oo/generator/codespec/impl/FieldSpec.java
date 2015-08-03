package org.xydra.oo.generator.codespec.impl;

import java.lang.reflect.Type;
import java.util.Set;

import org.xydra.oo.generator.codespec.IMember;
import org.xydra.oo.runtime.java.JavaReflectionUtils;
import org.xydra.oo.runtime.java.JavaTypeSpecUtils;
import org.xydra.oo.runtime.shared.IType;
import org.xydra.oo.runtime.shared.TypeSpec;

/**
 * specifies a field
 *
 * @author xamde
 */
public class FieldSpec extends AbstractMember implements IMember {

	public IType t;

	public FieldSpec(final String name, final Class<?> type, final Class<?> componentType, final String generatedFrom) {
		super(name, generatedFrom);
		this.t = JavaTypeSpecUtils.createTypeSpec(type, componentType, generatedFrom);
	}

	public FieldSpec(final String name, final Class<?> type, final String componentPackageName,
			final String componentTypeName, final String generatedFrom) {
		super(name, generatedFrom);
		this.t = JavaReflectionUtils.createTypeSpec(type, componentPackageName, componentTypeName,
				generatedFrom);
	}

	public FieldSpec(final String name, final String typePackageName, final String typeName, final String generatedFrom) {
		super(name, generatedFrom);
		this.t = new TypeSpec(typePackageName, typeName, generatedFrom);
	}

	public FieldSpec(final String name, final Type t, final String generatedFrom) {
		super(name, generatedFrom);
		this.t = JavaTypeSpecUtils.createTypeSpec(JavaReflectionUtils.getRawType(t),
				JavaReflectionUtils.getComponentType(t), generatedFrom);
	}

	FieldSpec(final String name, final IType typeSpec, final String generatedFrom) {
		super(name, generatedFrom);
		this.t = typeSpec;
	}

	@Override
	public int compareTo(final IMember o) {
		if (o instanceof FieldSpec) {
			return id().compareTo(((FieldSpec) o).id());
		} else {
			return getName().compareTo(o.getName());
		}

	}

	@Override
	public void dump() {
		System.out.println(toString());
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof FieldSpec && ((FieldSpec) other).id().equals(id());
	}

	@Override
	public Set<String> getRequiredImports() {
		final Set<String> set = this.t.getRequiredImports();
		set.addAll(super.getRequiredImports());
		return set;
	}

	@Override
	public int hashCode() {
		return id().hashCode();
	}

	public String id() {
		return getName() + this.t.id();
	}

	@Override
	public String toString() {
		return "FIELD\n" + "  " + getName() + " " + this.t.toString();
	}

	public String getTypeString() {
		return this.t.getTypeString();
	}

	public IType getType() {
		return this.t;
	}

}
