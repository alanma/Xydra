package org.xydra.base;

import java.io.Serializable;

import org.xydra.base.value.XValue;

/**
 * A identification object that can be serialised.
 *
 * See {@link XIdProvider} for limits.
 *
 * <em>Important:</em> Implementations of XId must implement <tt>equals()</tt>
 * and <tt>hashCode</tt> correctly.
 *
 * @author xamde
 */
public interface XId extends XValue, Serializable, Comparable<XId>, IHasXId {

	/**
	 * A default Id to be used in cases where a single default Id solves the
	 * problem. E.g. to be used as the repository-Id of a model that has no
	 * repository.
	 */
	XId DEFAULT = Base.toId("_default");

	/**
	 * To convert this XId to a byte[] representation use the ByteUtils (TBD).
	 *
	 * @return a compact String which can be turned into an XId again via. The
	 *         length of the string SHOULD be at most 100 characters for maximal
	 *         compatibility with all kinds of back-ends such as Google
	 *         AppEngine. {@link XIdProvider#fromString(String)}
	 */
	@Override
	String toString();

	/**
	 * The id as a binary representation
	 *
	 * @return a byte array, in which no byte is 00 (NUL)
	 */
	byte[] toBytes();

	@Override
	int hashCode();

	@Override
	boolean equals(Object o);

}
