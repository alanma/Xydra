package org.xydra.store.access.impl.delegate;

/**
 * Data modelling:
 * 
 * <pre>
 * objectId | fieldId                        | value
 * ---------+--------------------------------+----------
 * actorId  | "enc(address)+"_."+enc(rightId) | boolean
 * </pre>
 * 
 * Rights can be READ, WRITE, ADMIN.
 * 
 * Non-existing field: right not defined.
 * 
 * 
 * @author xamde
 */
public interface Documentation_AccessModel {
	
}
