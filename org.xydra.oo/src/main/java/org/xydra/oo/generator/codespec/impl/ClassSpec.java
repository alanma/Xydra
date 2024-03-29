package org.xydra.oo.generator.codespec.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.oo.generator.codespec.IMember;
import org.xydra.oo.runtime.shared.IBaseType;
import org.xydra.oo.runtime.shared.IType;

/**
 * state: required imports, implemented intefaces, kind, members, package,
 * superclass
 *
 * @author xamde
 */
public class ClassSpec extends NamedElement {

	/** Make sure to add also as required imports */
	public List<String> implementedInterfaces = new ArrayList<String>();

	/** "class" or "interface" or "abstract class" */
	@NeverNull
	public String kind;

	public List<IMember> members = new ArrayList<IMember>();

	/** back-ref */
	@NeverNull
	private final PackageSpec packageSpec;

	private final Set<String> requiredImports = new HashSet<String>();

	@CanBeNull
	public ClassSpec superClass;

	@CanBeNull
	private String comment;

	/**
	 * @param packageSpec
	 * @param kind
	 *            "interface" or "class" or "abstract class"
	 * @param name
	 */
	ClassSpec(final PackageSpec packageSpec, final String kind, final String name) {
		super(name);
		this.packageSpec = packageSpec;
		packageSpec.classes.add(this);
		this.kind = kind;
	}

	public ConstructorSpec addConstructor(final String generatedFrom) {
		final ConstructorSpec constructorSpec = new ConstructorSpec(this, generatedFrom);
		this.members.add(constructorSpec);
		return constructorSpec;
	}

	public MethodSpec addMethod(final Method m, final String generatedFrom) {
		final MethodSpec methodSpec = new MethodSpec(m, generatedFrom);
		this.members.add(methodSpec);
		return methodSpec;
	}

	public MethodSpec addMethod(final String name, final String typePackageName, final String typeName,
			final String generatedFrom) {
		final MethodSpec methodSpec = new MethodSpec(name, typePackageName, typeName, generatedFrom);
		this.members.add(methodSpec);
		return methodSpec;
	}

	/**
	 * @param name
	 *            of method
	 * @param returnType
	 *            type
	 * @param generatedFrom
	 *            debug comment
	 * @return new MethodSpec
	 */
	public MethodSpec addMethod(final String name, final IType returnType, final String generatedFrom) {
		final MethodSpec methodSpec = new MethodSpec(name, returnType, generatedFrom);
		this.members.add(methodSpec);
		return methodSpec;
	}

	public void addRequiredImports(final Class<?> c) {
		addRequiredImports(c.getCanonicalName());
	}

	public void addRequiredImports(final String canonicalName) {
		assert !canonicalName.equals("void");
		this.requiredImports.add(canonicalName);
	}

	public MethodSpec addVoidMethod(final String name, final String generatedFrom) {
		final MethodSpec methodSpec = MethodSpec.createVoid(name, generatedFrom);
		this.members.add(methodSpec);
		return methodSpec;
	}

	public void dump() {
		System.out.println("Class "
				+ getName()
				+ (this.superClass != null ? " extends " + this.superClass.getCanonicalName()
						: ""));
		for (final IMember t : this.members) {
			t.dump();
		}
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof ClassSpec && ((ClassSpec) other).id().equals(id());
	}

	public String getCanonicalName() {
		return this.packageSpec.getFQPackageName() + "." + getName();
	}

	public String getPackageName() {
		return this.packageSpec.getFQPackageName();
	}

	public Set<String> getRequiredImports() {
		final Set<String> imports = new HashSet<String>();
		imports.addAll(this.requiredImports);
		if (this.superClass != null) {
			imports.add(this.superClass.getCanonicalName());
		}
		for (final IMember t : this.members) {
			for (final String req : t.getRequiredImports()) {
				imports.add(req);
			}
		}
		return imports;
	}

	@Override
	public int hashCode() {
		return id().hashCode();
	}

	public String id() {
		return getName();
	}

	public boolean isBuiltIn() {
		return this.packageSpec.isBuiltIn();
	}

	public void setComment(final String s) {
		this.comment = s;
	}

	public String getComment() {
		return this.comment;
	}

	public String getTypeString() {
		return getName();
	}

	class Wrapper implements IType, IBaseType {

		@Override
		@NeverNull
		public IBaseType getBaseType() {
			return this;
		}

		@Override
		public boolean isArray() {
			return false;
		}

		@Override
		@CanBeNull
		public IBaseType getComponentType() {
			return null;
		}

		@Override
		public String getGeneratedFrom() {
			return "ClassSpec";
		}

		@Override
		public String getSimpleName() {
			return getName();
		}

		@Override
		public String getRequiredImport() {
			return getCanonicalName();
		}

		@Override
		public String getCanonicalName() {
			return ClassSpec.this.getCanonicalName();
		}

		@Override
		public String getPackageName() {
			return ClassSpec.this.getPackageName();
		}

		@Override
		public String getTypeString() {
			return getName();
		}

		@Override
		public Set<String> getRequiredImports() {
			// TODO Auto-generated method stub
			return Collections.singleton(getCanonicalName());
		}

		@Override
		public String id() {
			return ClassSpec.this.id();
		}

		@Override
		public String getComment() {
			return ClassSpec.this.getComment();
		}

		@Override
		public void setComment(final String comment) {
			ClassSpec.this.setComment(comment);
		}

	}

	public IType asType() {
		return new Wrapper();
	}

	@Override
	public String toString() {
		return this.kind + " " + getCanonicalName();
	}
}
