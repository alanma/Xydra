package org.xydra.server.impl.newgae.changes;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XType;

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
	 * @param address
	 * @return a GAE {@link Key} representing a Xydra {@link XAddress}
	 */
	public static Key createCombinedKey(XAddress address) {
		String kind = address.getAddressedType().name();
		Key key = KeyFactory.createKey(kind, address.toURI());
		return key;
	}
	
	/**
	 * @param combinedKey
	 * @return a Xydra {@link XAddress} from a combined (via
	 *         {@link #createCombinedKey(XAddress)}) GAE {@link Key}
	 */
	public static XAddress toAddress(Key combinedKey) {
		String combinedKeyString = combinedKey.getName();
		XAddress address = XX.toAddress(combinedKeyString);
		assert address.getAddressedType().toString().equals(combinedKey.getKind());
		return address;
	}
	
	/**
	 * @param modelAddr
	 * @param revision
	 * @return a GAE {@link Key} representing a Xydra change entity
	 */
	public static Key createChangeKey(XAddress modelAddr, long revision) {
		assert modelAddr.getAddressedType() == XType.XMODEL;
		return KeyFactory.createKey(KIND_XCHANGE, modelAddr.toURI() + "/" + revision);
	}
	
	/**
	 * @param changeKey
	 * @param transindex
	 * @return a GAE key representing an internal part of a Xydra change entity
	 */
	public static Key getEventKey(Key changeKey, int transindex) {
		assert isChangeKey(changeKey);
		return changeKey.getChild(KeyStructure.KIND_XEVENT, Integer.toString(transindex));
	}
	
	/**
	 * @param key
	 * @return true if the given key represents a Xydra change entity (which is
	 *         also true, if the key represents a specific a part within a Xydra
	 *         change entity)
	 */
	public static boolean isChangeKey(Key key) {
		return key.getKind() == KeyStructure.KIND_XCHANGE;
	}
	
}
