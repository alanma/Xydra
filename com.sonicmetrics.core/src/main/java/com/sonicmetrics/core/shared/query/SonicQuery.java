package com.sonicmetrics.core.shared.query;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;


/**
 * @author xamde
 * 
 */
@RunsInGWT(true)
public class SonicQuery extends SonicFilter implements ISonicQuery {
	
	private static final long serialVersionUID = 1L;
	
	public static class Builder extends SonicFilter.AbstractBuilder<SonicQuery,Builder> {
		
		private final TimeConstraint timeConstraint;
		/** default is 100 */
		private int limit = 100;
		
		public Builder(TimeConstraint timeConstraint) {
			this.b = this;
			this.timeConstraint = timeConstraint;
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
	
	private int limit;
	
	public SonicQuery(TimeConstraint timeConstraint, int limit,
	        KeyValueConstraint ... keyValueConstraints) {
		super(keyValueConstraints);
		this.timeConstraint = timeConstraint;
		this.limit = limit;
	}
	
	public static SonicQuery.Builder build(TimeConstraint timeConstraint) {
		return new SonicQuery.Builder(timeConstraint);
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
		for(KeyValueConstraint c : this.keyValueConstraints.values()) {
			sb.append(" '" + c.key + "'='" + c.value + "'");
		}
		sb.append(" limit:" + this.limit);
		return sb.toString();
	}
	
}
