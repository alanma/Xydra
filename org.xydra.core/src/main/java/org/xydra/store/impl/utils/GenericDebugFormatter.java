package org.xydra.store.impl.utils;

import java.util.Collection;


public class GenericDebugFormatter implements IDebugFormatter {

    @Override
    public String format(final Object object) {
        if(object instanceof String) {
            return (String)object;

        } else if(object instanceof Collection<?>) {
            final Collection<?> coll = (Collection<?>)object;
            final StringBuffer buf = new StringBuffer("{" + coll.size() + "=");
            for(final Object o : coll) {
                buf.append(DebugFormatter.format(o) + "; ");
            }
            buf.append("}");
            return DebugFormatter.formatString(buf.toString(), 500, false);
        }

        return null;
    }

}
