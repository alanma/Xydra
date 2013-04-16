package org.xydra.base.id;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.URIFormatException;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XIdProvider;


/**
 * An implementation of {@link XIdProvider}
 * 
 * @author voelkel
 * @author Kaidel
 */

@RunsInGWT(true)
@RequiresAppEngine(false)
public class MemoryStringIDProvider implements XIdProvider {
    
    private static final String nameStartChar = "A-Z_a-z\\xC0-\\xD6\\xD8-\\xF6"
            + "\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D"
            + "\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF" + "\\uFDF0-\\uFFFD";
    // the XML spec also allows 5-digit unicode characters
    // (\u10000-\uEFFFF) but Java can't handle them as char datatype is 16-bit
    // only.
    private static final String nameChar = nameStartChar
            + "\\-\\.0-9\\xB7\\u0300-\u036F\\u203F-\\u2040";
    static final String nameRegex = "[" + nameStartChar + "][" + nameChar + "]*";
    
    @Override
    public XId createUniqueId() {
        /* leading 'a' ensures legal XML name */
        return new MemoryStringID("a" + UUID.uuid());
    }
    
    @Override
    public XAddress fromAddress(String address) {
        
        if(address == null) {
            throw new IllegalArgumentException("address may not be null");
        }
        String[] components = address.split("/");
        // Note: this strips any trailing slashes
        
        if(components.length > 5) {
            throw new URIFormatException("The address \"" + address
                    + "\" contains too many components.");
        }
        
        if(components.length < 2 || components[0].length() > 0) {
            throw new URIFormatException("The address \"" + address
                    + "\" does not start with a slash ('/').");
        }
        
        XId repository = null;
        if(components.length >= 2 && !components[1].equals("-")) {
            repository = fromString(components[1]);
        }
        
        XId model = null;
        if(components.length >= 3 && !components[2].equals("-")) {
            model = fromString(components[2]);
        }
        
        XId object = null;
        if(components.length >= 4 && !components[3].equals("-")) {
            object = fromString(components[3]);
        }
        
        XId field = null;
        if(components.length >= 5 && !components[4].equals("-")) {
            field = fromString(components[4]);
        }
        
        return new MemoryAddress(repository, model, object, field);
    }
    
    @Override
    public XAddress fromComponents(XId repositoryId, XId modelId, XId objectId, XId fieldId) {
        return new MemoryAddress(repositoryId, modelId, objectId, fieldId);
    }
    
    @Override
    public XId fromString(String uriString) {
        if(uriString == null) {
            throw new IllegalArgumentException("'" + uriString + "' is null - cannot create XId");
        }
        if(!MemoryStringIdRegexGwtEmul.matchesXydraId(uriString)) {
            throw new IllegalArgumentException("'" + uriString
                    + "' is not a valid XML name or contains ':', cannot create XId");
        }
        return new MemoryStringID(uriString);
    }
    
}
