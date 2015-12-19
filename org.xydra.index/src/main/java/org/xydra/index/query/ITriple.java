package org.xydra.index.query;

import java.io.Serializable;

public interface ITriple<K, L, E> extends HasEntry<E>, Serializable {

	@Override
	E getEntry();

	K getKey1();

	L getKey2();

	/**
	 * @return @NeverNull
	 */
	K s();

	/**
	 * @return @NeverNull
	 */
	L p();

	/**
	 * @return @NeverNull
	 */
	E o();

	@Override
	int hashCode();

	@Override
	boolean equals(Object other);

}
