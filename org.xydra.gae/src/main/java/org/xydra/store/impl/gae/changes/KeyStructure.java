package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.value.XValue;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * Translates {@link XAddress} to GAE {@link Key Keys}.
 * 
 * @author scharrer
 * 
 */
public class KeyStructure {
	
	private static final String KIND_XVALUE = "XVALUE";
	private static final String KIND_XCHANGE = "XCHANGE";
	
	/**
	 * @param address The {@link XAddress} of the entity that the key should
	 *            point to.
	 * @return a GAE {@link Key} addressing an internal Xydra entity (not a
	 *         snapshot entity)
	 */
	public static Key createEntityKey(XAddress address) {
		String kind = address.getAddressedType().name();
		Key key = KeyFactory.createKey(kind, address.toURI());
		return key;
	}
	
	/**
	 * @param entityKey A key as returned by {@link #createEntityKey(XAddress)}
	 * @return a Xydra {@link XAddress} from a entity (via
	 *         {@link #createEntityKey(XAddress)}) GAE {@link Key}
	 */
	public static XAddress toAddress(Key entityKey) {
		String combinedKeyString = entityKey.getName();
		XAddress address = XX.toAddress(combinedKeyString);
		assert address.getAddressedType().toString().equals(entityKey.getKind());
		return address;
	}
	
	/**
	 * @param modelAddr The address of the model containing the entity modified
	 *            by the change.
	 * @param revision The revision number of the change.
	 * @return a GAE {@link Key} representing a Xydra change entity
	 */
	public static Key createChangeKey(XAddress modelAddr, long revision) {
		assert modelAddr.getAddressedType() == XType.XMODEL;
		return KeyFactory.createKey(KIND_XCHANGE, modelAddr.toURI() + "/" + revision);
	}
	
	/**
	 * @param modelAddr The address of the model containing the {@link XValue}.
	 * @param rev The revision number of the change that set the value.
	 * @param transindex The index of the event that set the value in the
	 *            change.
	 * @return a GAE {@link Key} representing an internal part of a Xydra change
	 *         entity
	 */
	public static Key createValueKey(XAddress modelAddr, long rev, int transindex) {
		assert modelAddr.getAddressedType() == XType.XMODEL;
		return KeyFactory.createKey(KIND_XVALUE, modelAddr.toURI() + "/" + rev + "+" + transindex);
	}
	
	/**
	 * @param key The GAE {@link Key} who'se type to check.
	 * @return true if the given GAE {@link Key} represents a Xydra change
	 *         entity
	 */
	public static boolean isChangeKey(Key key) {
		return key.getKind() == KIND_XCHANGE;
	}
	
	/**
	 * Check that the revision encoded in the given key matches the given
	 * revision.
	 * 
	 * @param key A key for a change entity as returned by
	 *            {@link createChangeKey}.
	 * @param key The key to check
	 */
	static boolean assertRevisionInKey(Key key, long rev) {
		assert isChangeKey(key);
		String keyStr = key.getName();
		int p = keyStr.lastIndexOf("/");
		assert p > 0;
		String revStr = keyStr.substring(p + 1);
		return (Long.parseLong(revStr) == rev);
	}
	
}
