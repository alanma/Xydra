package org.xydra.restless;

/**
 * A boolean flag, for call by reference
 *
 * @author xamde
 */
public class Flag {

	public Flag(final boolean value) {
		this.value = value;
	}

	private boolean value;

	public void setTrue() {
		this.value = true;
	}

	public void setFalse() {
		this.value = false;
	}

	public boolean getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return Boolean.toString(getValue());
	}
}
