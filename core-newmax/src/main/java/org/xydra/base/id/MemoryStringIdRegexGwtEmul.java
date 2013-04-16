package org.xydra.base.id;

import java.util.regex.Pattern;

import com.google.gwt.regexp.shared.RegExp;


/**
 * Precompiling the regex in a java.util.regex.Pattern is faster, but GWT
 * doesn't support that class. Instead the {@link RegExp} class could be used.
 */
public class MemoryStringIdRegexGwtEmul {
    
    private static final Pattern p = Pattern.compile(MemoryStringIDProvider.nameRegex);
    
    public static boolean matchesXydraId(String uriString) {
        return p.matcher(uriString).matches();
    }
    
}
