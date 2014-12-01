package org.xydra.base.id;

import java.util.HashMap;
import java.util.Map;

import org.xydra.base.XId;
import org.xydra.base.XIdProvider;

public class RefIdProvider extends BaseStringIDProvider implements XIdProvider {

	/** A single map storing ALL ids at runtime */
	private Map<String, XId> allIds = new HashMap<String, XId>();

	@Override
	protected XId createInstance(String string) {
		XId id = this.allIds.get(string);
		if (id == null) {
			id = new RefId(string);
			this.allIds.put(string, id);
		}
		return id;
	}

}
