package org.xydra.core.model.delta;

public enum AtomicChangeType {
	Add, Change, Remove;

	@Override
	public String toString() {
		switch (this) {
		case Add:
			return "+";
		case Remove:
			return "-";
		case Change:
			return "~";
		default:
			throw new AssertionError();
		}
	}
}