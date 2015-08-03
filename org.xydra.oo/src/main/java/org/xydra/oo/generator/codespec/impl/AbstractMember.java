package org.xydra.oo.generator.codespec.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xydra.oo.generator.codespec.IMember;

public abstract class AbstractMember extends NamedElement implements IMember {

	public List<AnnotationSpec<?>> annotations = new ArrayList<AnnotationSpec<?>>();

	private String comment;

	private final String generatedFrom;

	protected AbstractMember(final String name, final String generatedFrom) {
		super(name);
		this.generatedFrom = generatedFrom;
	}

	public <T> AbstractMember annotateWith(final Class<?> annotationClass,
			@SuppressWarnings("unchecked") final T... values) {
		this.annotations.add(new AnnotationSpec<T>(annotationClass, values));
		return this;
	}

	@Override
	public String getComment() {
		return (this.comment == null ? "" : this.comment)
				+ (this.generatedFrom == null ? "" : " [generated from: '" + this.generatedFrom
						+ "']");
	}

	@Override
	public String getGeneratedFrom() {
		return this.generatedFrom;
	}

	@Override
	public Set<String> getRequiredImports() {
		final HashSet<String> req = new HashSet<String>();
		for (final AnnotationSpec<?> ann : this.annotations) {
			req.add(ann.annot.getCanonicalName());
		}
		return req;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

}
