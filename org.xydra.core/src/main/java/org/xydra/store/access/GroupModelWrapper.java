package org.xydra.store.access;

import java.util.Iterator;

import org.xydra.core.model.XID;
import org.xydra.core.value.XIDSetValue;
import org.xydra.store.XydraStore;


/**
 * Wraps a XydraStore model to model groups and their members.
 * 
 * Data modelling:
 * 
 * <pre>
 * objectId | fieldId     | value
 * ---------+-------------+----------------------------
 * groupId  | "hasMember" | {@link XIDSetValue} actors
 * </pre>
 * 
 * 
 * @author voelkel
 */
public class GroupModelWrapper implements XGroupDatabase {
	
	private static final long serialVersionUID = 3858107275113200924L;
	
	private XydraStore store;
	
	public GroupModelWrapper(XydraStore store) {
		this.store = store;
		
	}
	
	@Override
	public void addToGroup(XID actor, XID group) throws CycleException {
		// force create object, ignore if no change
		// retrieve field
		// add new member
		// store field
	}
	
	@Override
	public Iterator<XID> getAllGroups(XID actor) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Iterator<XID> getAllMembers(XID group) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Iterator<XID> getGroups(XID actor) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Iterator<XID> getGroups() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Iterator<XID> getMembers(XID group) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean hasDirectGroup(XID actor, XID group) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean hasGroup(XID actor, XID group) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void removeFromGroup(XID actor, XID group) {
		// TODO Auto-generated method stub
		
	}
	
}
