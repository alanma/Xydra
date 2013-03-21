package org.xydra.store.access;

import org.xydra.base.XId;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class GroupUtils {
	
	private static final Logger log = LoggerFactory.getLogger(GroupUtils.class);
	
	public static void dump(XGroupDatabase groupDb) {
		log.info("All groups:");
		for(XId groupId : groupDb.getGroups()) {
			dumpGroupId(groupDb, groupId);
		}
	}
	
	public static void dumpGroupId(XGroupDatabase groupDb, XId groupId) {
		log.info("=== " + groupId);
		log.info("*     All groups: " + groupDb.getGroupsOf(groupId));
		log.info("*    All members: " + groupDb.getMembersOf(groupId));
	}
	
}
