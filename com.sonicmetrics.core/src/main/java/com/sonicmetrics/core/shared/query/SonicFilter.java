package com.sonicmetrics.core.shared.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xydra.annotations.CanBeNull;

import com.sonicmetrics.core.shared.ISonicEvent;
import com.sonicmetrics.core.shared.ISonicPotentialEvent;
import com.sonicmetrics.core.shared.ISonicPotentialEvent.FilterProperty;
import com.sonicmetrics.core.shared.impl.memory.SonicUtils;


public class SonicFilter implements ISonicFilter {
	
	@CanBeNull
	protected final Map<ISonicEvent.FilterProperty,KeyValueConstraint> keyValueConstraints = new HashMap<ISonicPotentialEvent.FilterProperty,KeyValueConstraint>();
	
	public SonicFilter(KeyValueConstraint ... keyValueConstraints) {
		for(KeyValueConstraint a : Arrays.asList(keyValueConstraints)) {
			this.keyValueConstraints.put(a.key, a);
		}
	}
	
	public Collection<KeyValueConstraint> getKeyValueConstraints() {
		return this.keyValueConstraints.values();
	}
	
	public static abstract class AbstractBuilder<T, B extends AbstractBuilder<T,B>> {
		
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
			if(keyValueConstraint.value == null || keyValueConstraint.value.equals("")
			        || keyValueConstraint.value.equals("*"))
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
			return where(KeyValueConstraint.keyValue(key, value));
		}
		
		/**
		 * @param category if null or '*': silently ignored
		 * @param action if null or '*': silently ignored
		 * @param label if null or '*': silently ignored
		 * @param subject if null or '*': silently ignored
		 * @param source if null or '*': silently ignored
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
	        SonicFilter.AbstractBuilder<SonicFilter,SonicFilter.Builder> {
		
		public Builder() {
			this.b = this;
		}
		
		// FIXME rename
		public SonicFilter done() {
			return new SonicFilter(
			        this.keyValueConstraints
			                .toArray(new KeyValueConstraint[this.keyValueConstraints.size()]));
		}
		
	}
	
	@Override
	public String getSubject() {
		return valueOf(this.keyValueConstraints.get(FilterProperty.Subject));
	}
	
	private static String valueOf(KeyValueConstraint keyValueConstraint) {
		if(keyValueConstraint == null)
			return null;
		
		return keyValueConstraint.value;
	}
	
	@Override
	public String getCategory() {
		return valueOf(this.keyValueConstraints.get(FilterProperty.Category));
	}
	
	@Override
	public String getAction() {
		return valueOf(this.keyValueConstraints.get(FilterProperty.Action));
	}
	
	@Override
	public String getLabel() {
		return valueOf(this.keyValueConstraints.get(FilterProperty.Label));
	}
	
	@Override
	public String getSource() {
		return valueOf(this.keyValueConstraints.get(FilterProperty.Source));
	}
	
	@Override
	public String getDotString() {
		return SonicUtils.toDotString(getCategory(), getAction(), getLabel());
	}
	
}
