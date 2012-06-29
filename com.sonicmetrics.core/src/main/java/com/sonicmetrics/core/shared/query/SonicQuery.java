package com.sonicmetrics.core.shared.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;

import com.sonicmetrics.core.shared.ISonicEvent.IndexedProperty;


/**
 * @author xamde
 * 
 */
@RunsInGWT(true)
public class SonicQuery implements ISonicQuery {
	
	public static class Builder {
		
		private final TimeConstraint timeConstraint;
		private final ArrayList<KeyValueConstraint> keyValueConstraints = new ArrayList<KeyValueConstraint>();
		/** default is 100 */
		private int limit = 100;
		
		public Builder(TimeConstraint timeConstraint) {
			this.timeConstraint = timeConstraint;
		}
		
		/**
		 * If value is null or '*', this contraint is silentyl ignored at
		 * creation time.
		 * 
		 * @param keyValueConstraint
		 * @return this
		 */
		public Builder where(KeyValueConstraint keyValueConstraint) {
			// wildcards
			if(keyValueConstraint.value == null || keyValueConstraint.value.equals("")
			        || keyValueConstraint.value.equals("*"))
				return this;
			
			this.keyValueConstraints.add(keyValueConstraint);
			return this;
		}
		
		public SonicQuery done() {
			return new SonicQuery(this.timeConstraint, this.limit,
			        this.keyValueConstraints
			                .toArray(new KeyValueConstraint[this.keyValueConstraints.size()]));
		}
		
		public Builder limit(int limit) {
			this.limit = limit;
			return this;
		}
		
		/**
		 * If value is null or '*', this contraint is silentyl ignored at
		 * creation time.
		 * 
		 * @param key
		 * @param value
		 * @return this
		 */
		public Builder whereProperty(IndexedProperty key, String value) {
			return where(KeyValueConstraint.keyValue(key, value));
		}
		
		public Builder categoryActionLabelSubjectSource(String category, String action,
		        String label, String subject, String source) {
			whereProperty(IndexedProperty.Category, category);
			
			whereProperty(IndexedProperty.Action, action);
			
			whereProperty(IndexedProperty.Label, label);
			
			whereProperty(IndexedProperty.Subject, subject);
			
			whereProperty(IndexedProperty.Source, source);
			
			return this;
		}
		
	}
	
	@NeverNull
	private final TimeConstraint timeConstraint;
	
	@CanBeNull
	private final List<KeyValueConstraint> keyValueConstraints;
	
	private int limit;
	
	public List<KeyValueConstraint> getKeyValueConstraints() {
		return this.keyValueConstraints;
	}
	
	public SonicQuery(TimeConstraint timeConstraint, int limit,
	        KeyValueConstraint ... keyValueConstraints) {
		this.timeConstraint = timeConstraint;
		this.limit = limit;
		this.keyValueConstraints = Arrays.asList(keyValueConstraints);
	}
	
	public static Builder build(TimeConstraint timeConstraint) {
		return new Builder(timeConstraint);
	}
	
	public boolean isTimeConstrained() {
		return this.timeConstraint != null && this.timeConstraint.isConstraining();
	}
	
	public TimeConstraint getTimeConstraint() {
		return this.timeConstraint;
	}
	
	@Override
	public int getLimit() {
		return this.limit;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.timeConstraint.toString());
		for(KeyValueConstraint c : this.keyValueConstraints) {
			sb.append(" '" + c.key + "'='" + c.value + "'");
		}
		sb.append(" limit:" + this.limit);
		return sb.toString();
	}
	
}
