package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.core.XX;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * Translates {@link XAddress} to GAE {@link Key Keys}.
 * 
 * @author scharrer
 * 
 */
public class KeyStructure {
	
	private static final String KIND_XEVENT = "XEVENT";
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
	 * @param changeKey The key of the "change" (as returned by
	 *            {@link #createChangeKey(XAddress, long)}) that contains the
	 *            desired event.
	 * @param transindex The index of the event in the change.
	 * @return a GAE {@link Key} representing an internal part of a Xydra change
	 *         entity
	 */
	public static Key createEventKey(Key changeKey, int transindex) {
		assert isChangeKey(changeKey);
		return changeKey.getChild(KeyStructure.KIND_XEVENT, Integer.toString(transindex));
	}
	
	/**
	 * @param key The GAE {@link Key} who'se type to check.
	 * @return true if the given GAE {@link Key} represents a Xydra change
	 *         entity
	 */
	public static boolean isChangeKey(Key key) {
		return key.getKind() == KeyStructure.KIND_XCHANGE;
	}
	
}
