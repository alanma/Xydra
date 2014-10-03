package org.xydra.xgae.datastore.impl.gae;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xydra.annotations.RunsInGWT;
import org.xydra.store.impl.utils.DebugFormatter;
import org.xydra.store.impl.utils.IDebugFormatter;
import org.xydra.xgae.util.XGaeDebugHelper;

import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;

/**
 * GAE-specific parts of {@link XGaeDebugHelper}. Separating them allows for
 * re-use in GWT.
 * 
 * @author xamde
 * 
 */
@RunsInGWT(false)
public class GaeDebugFormatter implements IDebugFormatter {

	@Override
	public String format(Object value) {
		if (value instanceof IdentifiableValue) {
			return DebugFormatter.format((((IdentifiableValue) value).getValue()));
		} else
			return null;
	}

	/**
	 * @return "2011-10-12 19:05:02.617" UTC time
	 */
	public static String currentTimeInGaeFormat() {
		long ms = System.currentTimeMillis();
		Date d = new Date(ms);
		DateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
		return df.format(d);
	}

}
