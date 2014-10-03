package org.xydra.xgae;

import org.xydra.store.impl.utils.IDebugFormatter;
import org.xydra.xgae.memcache.api.IMemCache.IdentifiableValue;


public class XGaeDebugFormatter implements IDebugFormatter {
    
    @Override
	public String format(Object value) {
        assert value != null;
        if(value instanceof IdentifiableValue) {
            Object o = ((IdentifiableValue)value).getValue();
            return format(o);
        }
        return null;
    }
    
}
