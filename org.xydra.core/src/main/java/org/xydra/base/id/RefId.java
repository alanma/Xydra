package org.xydra.base.id;

import java.io.ObjectStreamException;

import org.xydra.base.BaseRuntime;
import org.xydra.base.XId;

public class RefId extends MemoryStringID {

	private static final long serialVersionUID = -1638968666051351868L;

	@Override
	public boolean equals(final Object other) {
		if (other instanceof RefId) {
			return this == other;
		} else if (other instanceof XId) {
			return ((XId) other).toString().equals(this.string);
		} else {
			return false;
		}
	}

	public RefId(final String uriString) {
		super(uriString);
	}

	/**
	 * This is a "magic" method from the Java Object Serialization framework.
	 *
	 * @return
	 * @throws ObjectStreamException
	 */
	private Object readResolve() throws ObjectStreamException {
		final XId id = BaseRuntime.getIDProvider().fromString(this.string);
		return id;
	}

}
