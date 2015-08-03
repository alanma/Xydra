package org.xydra.oo.generator.codespec.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xydra.oo.generator.codespec.IMember;

public class AbstractConstructorOrMethodSpec extends AbstractMember implements IMember {

	private String access = "";

	private boolean isAbstract = false;

	private boolean isStatic = false;

	public List<FieldSpec> params = new ArrayList<FieldSpec>();

	public List<String> sourceLines = new ArrayList<String>();

	AbstractConstructorOrMethodSpec(final String name, final String generatedFrom) {
		super(name, generatedFrom);
	}

	/**
	 * @param name
	 * @param type
	 * @param generatedFrom
	 * @return this = fluent API pattern
	 */
	public AbstractConstructorOrMethodSpec addParam(final String name, final Class<?> type, final String generatedFrom) {
		final FieldSpec fs = new FieldSpec(name, type, null, generatedFrom);
		this.params.add(fs);
		return this;
	}

	/**
	 * @param line
	 * @return this = fluent API pattern
	 */
	public AbstractConstructorOrMethodSpec addSourceLine(final String line) {
		this.sourceLines.add(line);
		return this;
	}

	@Override
	public final <T> AbstractConstructorOrMethodSpec annotateWith(final Class<?> annotationClass,
			@SuppressWarnings("unchecked") final T... values) {
		super.annotateWith(annotationClass, values);
		return this;
	}

	@Override
	public int compareTo(final IMember o) {
		if (o instanceof AbstractConstructorOrMethodSpec) {
			return id().compareTo(((AbstractConstructorOrMethodSpec) o).id());
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
		return other instanceof AbstractConstructorOrMethodSpec
				&& ((AbstractConstructorOrMethodSpec) other).id().equals(id());
	}

	public String getModifiers() {
		assert !(this.isAbstract && this.isStatic);
		String m = this.access;
		if (this.isAbstract) {
			if (m.length() > 0) {
				m += " ";
			}
			m += "abstract";
		}
		if (this.isStatic) {
			if (m.length() > 0) {
				m += " ";
			}
			m += "static";
		}
		return m;
	}

	@Override
	public Set<String> getRequiredImports() {
		final Set<String> req = new HashSet<String>();
		for (final FieldSpec p : this.params) {
			req.addAll(p.getRequiredImports());
		}
		req.addAll(super.getRequiredImports());
		return req;
	}

	@Override
	public int hashCode() {
		return id().hashCode();
	}

	public String id() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getName());
		for (final FieldSpec p : this.params) {
			sb.append(p.id());
		}
		return sb.toString();
	}

	public boolean isAbstract() {
		return this.isAbstract;
	}

	public AbstractConstructorOrMethodSpec setAbstract(final boolean b) {
		this.isAbstract = true;
		return this;
	}

	public AbstractConstructorOrMethodSpec setAccess(final String access) {
		this.access = access;
		return this;
	}

	public AbstractConstructorOrMethodSpec setStatic(final boolean b) {
		this.isStatic = b;
		return this;
	}

}
