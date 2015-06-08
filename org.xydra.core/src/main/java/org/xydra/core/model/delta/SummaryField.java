package org.xydra.core.model.delta;


public class SummaryField extends SummaryEntity {
	public SummaryValue summaryValue = null;

	public SummaryValue createOrGet() {
		if (this.summaryValue == null) {
			this.summaryValue = new SummaryValue();
		}
		return this.summaryValue;
	}

	@Override
	public String toString() {
		return "F-" + this.change + "\n" + this.summaryValue.toString();
	}

}