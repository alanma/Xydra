package org.xydra.core.model.state.impl.gae;

import org.xydra.core.X;
import org.xydra.core.model.XAddress;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


public class KeyStructure {
	
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
	
}
