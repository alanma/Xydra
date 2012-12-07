package org.xydra.core.util;

import org.xydra.core.model.XModel;


public class QueryUtils {
	
	public static boolean matches(XModel model, XidOrVariable object, XidOrVariable field,
	        XvalueOrVariable value) {
		ModelIndex modelIndex = new ModelIndex(model);
		return modelIndex.matches(object, field, value);
	}
	
}
