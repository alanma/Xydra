package org.xydra.oo.generator.codespec.impl;

public class AnnotationSpec<T> {

	public Class<?> annot;

	private final T[] values;

	/**
	 * @param annot
	 *            which annotation is made
	 * @param values
	 *            of the annotation
	 */
	// @SafeVarargs
	AnnotationSpec(final Class<?> annot, @SuppressWarnings("unchecked") final T... values) {
		this.annot = annot;
		this.values = values;
	}

	/**
	 * @return the first of the values or null
	 */
	public T getValue() {
		return this.values == null ? null : this.values.length == 0 ? null : this.values[0];
	}

	public T[] getValues() {
		return this.values;
	}

}
