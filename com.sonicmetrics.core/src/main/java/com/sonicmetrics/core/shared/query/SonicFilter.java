package com.sonicmetrics.core.shared.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;

import com.sonicmetrics.core.shared.ISonicPotentialEvent.FilterProperty;
import com.sonicmetrics.core.shared.impl.memory.SonicUtils;

/**
 * @author xamde
 */
public class SonicFilter implements ISonicFilter, Serializable {

	private static final long serialVersionUID = 1L;

	@CanBeNull
	protected final Map<String, KeyValueConstraint> keyValueConstraints = new HashMap<String, KeyValueConstraint>();

	public SonicFilter(KeyValueConstraint... keyValueConstraints) {
		for (KeyValueConstraint a : Arrays.asList(keyValueConstraints)) {
			this.keyValueConstraints.put(a.getKey(), a);
		}
	}

	@Override
	@NeverNull
	public Collection<KeyValueConstraint> getKeyValueConstraints() {
		return this.keyValueConstraints.values();
	}

	public static abstract class AbstractBuilder<T, B extends AbstractBuilder<T, B>> {

		protected final ArrayList<KeyValueConstraint> keyValueConstraints = new ArrayList<KeyValueConstraint>();

		protected B b;

		/**
		 * If value is null or '*', this constraint is silently ignored at
		 * creation time.
		 * 
		 * @param keyValueConstraint
		 * @return this
		 */
		public B where(KeyValueConstraint keyValueConstraint) {
			// wildcards
			if (keyValueConstraint.getValue() == null || keyValueConstraint.getValue().equals("")
					|| keyValueConstraint.getValue().equals("*"))
				return this.b;

			this.keyValueConstraints.add(keyValueConstraint);
			return this.b;
		}

		/**
		 * If value is null or '*', this constraint is silently ignored at
		 * creation time.
		 * 
		 * @param key
		 * @param value
		 * @return this
		 */
		public B whereProperty(FilterProperty key, String value) {
			return where(KeyValueConstraint.keyValue(key.name(), value));
		}

		/**
		 * @param category
		 *            if null or '*': silently ignored
		 * @param action
		 *            if null or '*': silently ignored
		 * @param label
		 *            if null or '*': silently ignored
		 * @param subject
		 *            if null or '*': silently ignored
		 * @param source
		 *            if null or '*': silently ignored
		 * @return this
		 */
		public B categoryActionLabelSubjectSource(String category, String action, String label,
				String subject, String source) {
			whereProperty(FilterProperty.Category, category);

			whereProperty(FilterProperty.Action, action);

			whereProperty(FilterProperty.Label, label);

			whereProperty(FilterProperty.Subject, subject);

			whereProperty(FilterProperty.Source, source);

			return this.b;
		}

	}

	public static Builder create() {
		return new Builder();
	}

	public static class Builder extends
			SonicFilter.AbstractBuilder<SonicFilter, SonicFilter.Builder> {

		public Builder() {
			this.b = this;
		}

		// FIXME rename to build();
		public SonicFilter done() {
			return new SonicFilter(
					this.keyValueConstraints
							.toArray(new KeyValueConstraint[this.keyValueConstraints.size()]));
		}

	}

	@Override
	@NeverNull
	public String getSubject() {
		return valueOf(this.keyValueConstraints.get(FilterProperty.Subject.name()));
	}

	private static String valueOf(KeyValueConstraint keyValueConstraint) {
		if (keyValueConstraint == null)
			return null;

		return keyValueConstraint.getValue();
	}

	@Override
	@NeverNull
	public String getCategory() {
		return valueOf(this.keyValueConstraints.get(FilterProperty.Category.name()));
	}

	@Override
	@NeverNull
	public String getAction() {
		return valueOf(this.keyValueConstraints.get(FilterProperty.Action.name()));
	}

	@Override
	public String getLabel() {
		return valueOf(this.keyValueConstraints.get(FilterProperty.Label.name()));
	}

	@Override
	@NeverNull
	public String getSource() {
		return valueOf(this.keyValueConstraints.get(FilterProperty.Source.name()));
	}

	@Override
	public @NeverNull String getDotString() {
		return SonicUtils.toDoStringWithWildcards(getCategory(), getAction(), getLabel());
	}

	public static boolean equals(ISonicFilter a, ISonicFilter b) {
		if (!SonicUtils.bothNullOrEqual(a.getSubject(), b.getSubject())) {
			return false;
		}
		if (!SonicUtils.bothNullOrEqual(a.getCategory(), b.getCategory())) {
			return false;
		}
		if (!SonicUtils.bothNullOrEqual(a.getAction(), b.getAction())) {
			return false;
		}
		if (!SonicUtils.bothNullOrEqual(a.getLabel(), b.getLabel())) {
			return false;
		}
		if (!SonicUtils.bothNullOrEqual(a.getSource(), b.getSource())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ISonicFilter))
			return false;

		ISonicFilter o = (ISonicFilter) other;
		return equals(this, o);
	}

	/**
	 * @param other
	 * @return true if this filter includes the other filter, i.e. this filter
	 *         is more general (or equal) to the other filter and the other
	 *         filter requests no events that this filter won't return.
	 */
	public boolean includes(ISonicFilter other) {
		if (!SonicUtils.moreGeneralThanOrEqualTo(this.getSubject(), other.getSubject())) {
			return false;
		}
		if (!SonicUtils.moreGeneralThanOrEqualTo(this.getCategory(), other.getCategory())) {
			return false;
		}
		if (!SonicUtils.moreGeneralThanOrEqualTo(this.getAction(), other.getAction())) {
			return false;
		}
		if (!SonicUtils.moreGeneralThanOrEqualTo(this.getLabel(), other.getLabel())) {
			return false;
		}
		if (!SonicUtils.moreGeneralThanOrEqualTo(this.getSource(), other.getSource())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return SonicUtils.hashCode(getSubject()) + SonicUtils.hashCode(getSource())
				+ SonicUtils.hashCode(getCategory()) + SonicUtils.hashCode(getAction())
				+ SonicUtils.hashCode(getLabel());
	}

	@Override
	public String toString() {
		return "cat.act.lab.sou.sub=" + this.getCategory() + "." + this.getAction() + "."
				+ this.getLabel() + "." + this.getSource() + "." + this.getSubject();
	}

}
