package org.xydra.core.model.impl.memory;

import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XIDProvider;
import org.xydra.core.URIFormatException;


/**
 * An implementation of {@link XIDProvider}
 * 
 * @author voelkel
 * @author Kaidel
 */

@RunsInGWT
@RunsInJava
public class MemoryStringIDProvider implements XIDProvider {
	
	private static final String nameStartChar = "A-Z_a-z\\xC0-\\xD6\\xD8-\\xF6"
	        + "\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D"
	        + "\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF" + "\\uFDF0-\\uFFFD";
	// the XML spec also allows 5-digit unicode characters
	// (\u10000-\uEFFFF) but Java can't handle them as char datatype is 16-bit
	// only.
	private static final String nameChar = nameStartChar
	        + "\\-\\.0-9\\xB7\\u0300-\u036F\\u203F-\\u2040";
	private static final String nameRegex = "[" + nameStartChar + "][" + nameChar + "]*";
	
	/*
	 * IMPROVE precompiling the regex in a java.util.regex.Pattern is faster,
	 * but GWT doesn't support that class
	 */

	public XID createUniqueID() {
		/* leading 'a' ensures legal XML name */
		return new MemoryStringID("a" + UUID.uuid());
	}
	
	public XAddress fromAddress(String address) {
		
		if(address == null) {
			throw new IllegalArgumentException("address may not be null");
		}
		String[] components = address.split("/");
		// TODO this strips any trailing slashes - is that OK?
		
		if(components.length > 5) {
			throw new URIFormatException("The address \"" + address
			        + "\" contains too many components.");
		}
		
		if(components.length < 2 || components[0].length() > 0) {
			throw new URIFormatException("The address \"" + address
			        + "\" does not start with a slash ('/').");
		}
		
		XID repository = null;
		if(components.length >= 2 && !components[1].equals("-")) {
			repository = fromString(components[1]);
		}
		
		XID model = null;
		if(components.length >= 3 && !components[2].equals("-")) {
			model = fromString(components[2]);
		}
		
		XID object = null;
		if(components.length >= 4 && !components[3].equals("-")) {
			object = fromString(components[3]);
		}
		
		XID field = null;
		if(components.length >= 5 && !components[4].equals("-")) {
			field = fromString(components[4]);
		}
		
		return new MemoryAddress(repository, model, object, field);
	}
	
	public XAddress fromComponents(XID repositoryId, XID modelId, XID objectId, XID fieldId) {
		return new MemoryAddress(repositoryId, modelId, objectId, fieldId);
	}
	
	public XID fromString(String uriString) {
		if(!uriString.matches(nameRegex)) {
			throw new IllegalArgumentException("'" + uriString
			        + "' is not a valid XML name or contains ':', cannot create XID");
		}
		return new MemoryStringID(uriString);
	}
	
}
