package org.xydra.store.impl.utils;

import java.util.Collection;


public class GenericDebugFormatter implements IDebugFormatter {
    
    @Override
    public String format(Object object) {
        if(object instanceof String) {
            return (String)object;
            
        } else if(object instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>)object;
            StringBuffer buf = new StringBuffer("{" + coll.size() + "=");
            for(Object o : coll) {
                buf.append(DebugFormatter.format(o) + "; ");
            }
            buf.append("}");
            return DebugFormatter.formatString(buf.toString(), 500, false);
        }
        
        return null;
    }
    
}
