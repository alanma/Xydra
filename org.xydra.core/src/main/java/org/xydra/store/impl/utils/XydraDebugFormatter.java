package org.xydra.store.impl.utils;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.sharedutils.ReflectionUtils;


/**
 * @author xamde
 *
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
public class XydraDebugFormatter implements IDebugFormatter {

    @Override
	public String format(final Object value) {
        assert value != null;
        if(value instanceof XId) {
            return "'" + value.toString() + "'";
        } else if(value instanceof XAddress) {
            return "'" + value.toString() + "'";
        } else if(value instanceof XCommand) {
            final XCommand c = (XCommand)value;
            return "Command {" + DebugFormatter.formatString(c.toString(), 140, false) + "}";
        } else if(value instanceof Long) {
            return "{" + value + "}";
        } else {
            final String s = ReflectionUtils.getCanonicalName(value.getClass());
            final String v = DebugFormatter.formatString(value.toString());
            if(v.length() > 10) {
                return s + " = {" + DebugFormatter.LINE_END + v + "}";
            } else {
                return s + " = {" + v + "}";
            }
        }
    }

}
