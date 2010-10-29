package org.xydra.server.impl.newgae;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XType;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


public class KeyStructure {
	
	private static final String KIND_XEVENT = "XEVENT";
	private static final String KIND_XCHANGE = "XCHANGE";
	
	public static Key createCombinedKey(XAddress address) {
		String kind = address.getAddressedType().name();
		Key key = KeyFactory.createKey(kind, address.toURI());
		return key;
	}
	
	public static XAddress toAddress(Key combinedKey) {
		String combinedKeyString = combinedKey.getName();
		XAddress address = XX.toAddress(combinedKeyString);
		assert address.getAddressedType().toString().equals(combinedKey.getKind());
		return address;
	}
	
	public static Key createChangetKey(XAddress modelAddr, long revision) {
		assert modelAddr.getAddressedType() == XType.XMODEL;
		return KeyFactory.createKey(KIND_XCHANGE, modelAddr.toURI() + "/" + revision);
	}
	
	public static Key getEventKey(Key changeKey, int transindex) {
		assert isChangeKey(changeKey);
		return changeKey.getChild(KeyStructure.KIND_XEVENT, Integer.toString(transindex));
	}
	
	public static boolean isChangeKey(Key key) {
		return key.getKind() == KeyStructure.KIND_XCHANGE;
	}
	
}
