package org.xydra.store.access.impl.delegate;

import org.xydra.base.value.XIDSetValue;
import org.xydra.store.base.HashUtils;


/**
 * <h4>Data modelling</h4>
 * 
 * Accounts (Passwords, failed login attempts)
 * 
 * <pre>
 * objectId | fieldId                  | value
 * ---------+--------------------------+----------------------------
 * actorId  | "hasPasswordHash"        | the password hash (see {@link HashUtils})
 * actorId  | "hasFailedLoginAttempts" | if present: number of failed login attempts
 * </pre>
 * 
 * Group membership (group->actors)
 * 
 * <pre>
 * objectId | fieldId     | value
 * ---------+-------------+----------------------------
 * groupId  | "hasMember" | {@link XIDSetValue} actors
 * </pre>
 * 
 * 
 * <h3>FUTURE IMPL -- currently index is in memory only</h3> <h4>Indexes for
 * faster access</h4> Group membership (actor->groups)
 * 
 * <pre>
 * objectId | fieldId      | value
 * ---------+--------------+----------------------------
 * actorId  | "isMemberOf" | {@link XIDSetValue} groupIds
 * </pre>
 * 
 * @author xamde
 */
public interface Documentation_AccountModel {
	
}
