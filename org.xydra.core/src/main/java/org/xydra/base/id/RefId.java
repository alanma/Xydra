package org.xydra.base.id;

import org.xydra.base.XId;

public class RefId extends MemoryStringID {

	private static final long serialVersionUID = -1638968666051351868L;

	@Override
	public boolean equals(Object other) {
		if (other instanceof RefId) {
			return this == other;
		} else if (other instanceof XId) {
			return ((XId) other).toString().equals(this.string);
		} else {
			return false;
		}
	}

	public RefId(String uriString) {
		super(uriString);
	}

}
