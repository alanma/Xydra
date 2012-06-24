package com.sonicmetrics.core.shared.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;


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
		
		public Builder where(KeyValueConstraint keyValueConstraint) {
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
