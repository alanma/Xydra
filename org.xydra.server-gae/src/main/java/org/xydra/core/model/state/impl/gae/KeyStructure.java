package org.xydra.core.model.state.impl.gae;

import org.xydra.core.X;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XType;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


public class KeyStructure {
	
	public static String KIND_XCHANGE = "XCHANGE";
	
	public static Key createCombinedKey(XAddress address) {
		String kind = address.getAddressedType().name();
		Key key = KeyFactory.createKey(kind, address.toURI());
		return key;
	}
	
	public static XAddress toAddress(Key combinedKey) {
		String combinedKeyString = combinedKey.getName();
		XAddress address = X.getIDProvider().fromAddress(combinedKeyString);
		return address;
	}
	
	public static Key createChangetKey(XAddress modelAddr, long revision) {
		assert modelAddr.getAddressedType() == XType.XMODEL;
		return KeyFactory.createKey(KIND_XCHANGE, modelAddr.toURI() + "/" + revision);
	}
	
}
