package org.xydra.conf.impl;

import org.xydra.annotations.RunsInGWT;
import org.xydra.conf.IConfig;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import com.google.gwt.i18n.client.Dictionary;

@RunsInGWT(true)
public class GwtConfigTool {

	private static final Logger log = LoggerFactory.getLogger(GwtConfigTool.class);

	public static final String DICT_NAME = "XydraConf";

	public static void initFromHostPage(final IConfig conf) {
		final Dictionary dict = Dictionary.getDictionary(DICT_NAME);
		if (dict == null) {
			log.info("No dictionary named '" + DICT_NAME + "' found in hostpage.");
			return;
		}

		for (final String key : dict.keySet()) {
			final String value = dict.get(key);
			conf.set(key, value);
			log.debug("Conf: '" + key + "' = '" + value + "'");
		}
	}

}
