package org.xydra.store.impl.gae.changes;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.value.XValue;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SKey;

/**
 * Translates {@link XAddress} to GAE {@link SKey Keys}.
 *
 * Changes are persisted as {@link #KIND_XCHANGE} and {@link #KIND_XVALUE}
 * types.
 *
 * @author dscharrer
 *
 */
public class KeyStructure {

	public static final String KIND_XVALUE = "XVALUE";
	public static final String KIND_XCHANGE = "XCHANGE";

	/**
	 * Note: Only used by InternalGae(MOF)Entity
	 *
	 * @param address
	 *            The {@link XAddress} of the entity that the key should point
	 *            to.
	 * @return a GAE {@link SKey} addressing an internal Xydra entity (not a
	 *         snapshot entity)
	 */
	public static SKey createEntityKey(final XAddress address) {
		final String kind = address.getAddressedType().name();
		return XGae.get().datastore().createKey(kind, address.toURI());
	}

	/**
	 * @param entityKey
	 *            A key as returned by {@link #createEntityKey(XAddress)}
	 * @return a Xydra {@link XAddress} from a entity (via
	 *         {@link #createEntityKey(XAddress)}) GAE {@link SKey}
	 */
	public static XAddress toAddress(final SKey entityKey) {
		final String combinedKeyString = entityKey.getName();
		final XAddress address = Base.toAddress(combinedKeyString);
		assert address.getAddressedType().toString().equals(entityKey.getKind());
		return address;
	}

	/**
	 * @param modelAddr
	 *            The address of the model containing the entity modified by the
	 *            change.
	 * @param revision
	 *            The revision number of the change.
	 * @return a GAE {@link SKey} representing a Xydra change entity
	 */
	public static SKey createChangeKey(final XAddress modelAddr, final long revision) {
		assert modelAddr.getAddressedType() == XType.XMODEL;
		return XGae.get().datastore().createKey(KIND_XCHANGE, revision + modelAddr.toURI());
	}

	/**
	 * @param key
	 *            must be a change key created via
	 *            {@link #createChangeKey(XAddress, long)}
	 * @return the XAddress part of the change key
	 */
	public static XAddress getAddressFromChangeKey(final SKey key) {
		assert key.getKind().equals(KIND_XCHANGE);
		final String name = key.getName();
		final int firstSlash = name.indexOf("/");
		final String address = name.substring(firstSlash);
		final XAddress xa = Base.toAddress(address);
		return xa;
	}

	/**
	 * @param modelAddr
	 *            The address of the model containing the {@link XValue}.
	 * @param rev
	 *            The revision number of the change that set the value.
	 * @param transindex
	 *            The index of the event that set the value in the change.
	 * @return a GAE {@link Key} representing an internal part of a Xydra change
	 *         entity
	 */
	static SKey createValueKey(final XAddress modelAddr, final long rev, final int transindex) {
		assert modelAddr.getAddressedType() == XType.XMODEL;
		return XGae.get().datastore()
				.createKey(KIND_XVALUE, rev + "+" + transindex + "/" + modelAddr.toURI());
	}

	/**
	 * @param key
	 *            The GAE {@link Key} who'se type to check.
	 * @return true if the given GAE {@link Key} represents a Xydra change
	 *         entity
	 */
	private static boolean isChangeKey(final SKey key) {
		return key.getKind().equals(KIND_XCHANGE);
	}

	/**
	 * Check that the revision encoded in the given key matches the given
	 * revision.
	 *
	 * @param key
	 *            A key for a change entity as returned by
	 *            {@link createChangeKey}.
	 * @param key
	 *            The key to check
	 */
	static boolean assertRevisionInKey(final SKey key, final long rev) {
		assert isChangeKey(key) : "key = " + key + " with kind " + key.getKind();
		return getRevisionFromChangeKey(key) == rev;
	}

	/**
	 * @param key
	 *            a non-null change key that has been created via
	 *            {@link #createChangeKey(XAddress, long)}.
	 * @return the revision number encoded in a change key.
	 */
	public static long getRevisionFromChangeKey(final SKey key) {
		assert isChangeKey(key) : key.toString();
		final String keyStr = key.getName();
		return getRevisionFromChangeKey(keyStr);
	}

	public static long getRevisionFromChangeKey(final String keyStr) {
		final int p = keyStr.indexOf("/");
		assert p > 0 : keyStr;
		final String revStr = keyStr.substring(0, p);
		return Long.parseLong(revStr);
	}

	public static String toString(final SKey key) {
		return key.getKind() + "|" + key.getName();
	}

	/**
	 * @param key
	 *            must have format 'KIND|NAME'
	 * @return a gae key
	 */
	public static SKey toKey(final String key) {
		final int index = key.indexOf("|");
		assert index > 0;
		final String kind = key.substring(0, index);
		final String name = key.substring(index + 1, key.length());
		return XGae.get().datastore().createKey(kind, name);
	}

}
