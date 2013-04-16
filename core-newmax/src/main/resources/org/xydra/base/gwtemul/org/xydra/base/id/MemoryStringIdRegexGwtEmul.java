package org.xydra.base.id;

import com.google.gwt.regexp.shared.RegExp;


/**
 * Precompiling the regex in a java.util.regex.Pattern is faster, but GWT
 * doesn't support that class. Instead the {@link RegExp} class could be used.
 */
public class MemoryStringIdRegexGwtEmul {
    
    private static final RegExp p = RegExp.compile(MemoryStringIDProvider.nameRegex);
    
    public static boolean matchesXydraId(String uriString) {
        return p.test(uriString);
    }
    
}
