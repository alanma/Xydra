package org.xydra.store.access;

import org.xydra.base.XId;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


public class GroupUtils {

	private static final Logger log = LoggerFactory.getLogger(GroupUtils.class);

	public static void dump(final XGroupDatabase groupDb) {
		log.info("All groups:");
		for(final XId groupId : groupDb.getGroups()) {
			dumpGroupId(groupDb, groupId);
		}
	}

	public static void dumpGroupId(final XGroupDatabase groupDb, final XId groupId) {
		log.info("=== " + groupId);
		log.info("*     All groups: " + groupDb.getGroupsOf(groupId));
		log.info("*    All members: " + groupDb.getMembersOf(groupId));
	}

}
