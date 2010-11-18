package org.xydra.store.access;

import java.util.Iterator;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableObject;
import org.xydra.core.value.XIDSetValue;
import org.xydra.core.value.XValue;
import org.xydra.store.XydraStore;
import org.xydra.store.base.Credentials;
import org.xydra.store.base.WritableModel;


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
	
	public static final XID hasMember = XX.toId("hasMember");
	
	private static final long serialVersionUID = 3858107275113200924L;
	
	// FIXME get credentials from config settings
	private Credentials credentials = new Credentials(XX.toId("__accessManager"), "TODO");
	
	private WritableModel writableModel;
	
	public GroupModelWrapper(XydraStore store, XID repositoryId, XID modelId) {
		this.writableModel = new WritableModel(this.credentials, store, X.getIDProvider()
		        .fromComponents(repositoryId, modelId, null, null));
		
	}
	
	@Override
	public void addToGroup(XID actor, XID group) throws CycleException {
		XWritableObject groupObject = this.writableModel.getObject(group);
		if(groupObject == null) {
			groupObject = this.writableModel.createObject(group);
		}
		XWritableField actorsInGroupField = groupObject.getField(hasMember);
		if(actorsInGroupField == null) {
			actorsInGroupField = groupObject.createField(hasMember);
		}
		XValue actorsInGroupValue = actorsInGroupField.getValue();
		if(actorsInGroupValue == null) {
			actorsInGroupValue = X.getValueFactory().createIDSetValue(new XID[] { actor });
		} else {
			actorsInGroupValue = ((XIDSetValue)actorsInGroupValue).add(actor);
		}
		actorsInGroupField.setValue(actorsInGroupValue);
	}
	
	@Override
	public Iterator<XID> getAllGroups(XID actor) {
		// TODO Auto-generated method stub - requires index
		return null;
	}
	
	@Override
	public Iterator<XID> getAllMembers(XID group) {
		XWritableObject groupObject = this.writableModel.getObject(group);
		if(groupObject == null) {
			return null;
		} else {
			return ((XIDSetValue)groupObject.getField(hasMember).getValue()).iterator();
		}
	}
	
	@Override
	public Iterator<XID> getGroups(XID actor) {
		// TODO Auto-generated method stub - requires index
		return null;
	}
	
	@Override
	public Iterator<XID> getGroups() {
		return this.writableModel.iterator();
	}
	
	@Override
	public Iterator<XID> getMembers(XID group) {
		XWritableObject groupObject = this.writableModel.getObject(group);
		if(groupObject == null) {
			return null;
		}
		XWritableField actorsInGroupField = groupObject.getField(hasMember);
		if(actorsInGroupField == null) {
			return null;
		}
		XValue actorsInGroupValue = actorsInGroupField.getValue();
		return ((XIDSetValue)actorsInGroupValue).iterator();
	}
	
	@Override
	public boolean hasDirectGroup(XID actor, XID group) {
		XWritableObject groupObject = this.writableModel.getObject(group);
		if(groupObject == null) {
			throw new IllegalArgumentException("Group " + group + " not found");
		}
		XWritableField actorsInGroupField = groupObject.getField(hasMember);
		if(actorsInGroupField == null) {
			return false;
		}
		XValue actorsInGroupValue = actorsInGroupField.getValue();
		return ((XIDSetValue)actorsInGroupValue).contains(actor);
	}
	
	@Override
	public boolean hasGroup(XID actor, XID group) {
		// TODO Auto-generated method stub - requires index
		return false;
	}
	
	@Override
	public void removeFromGroup(XID actor, XID group) {
		XWritableObject groupObject = this.writableModel.getObject(group);
		if(groupObject == null) {
			return;
		}
		XWritableField actorsInGroupField = groupObject.getField(hasMember);
		if(actorsInGroupField == null) {
			return;
		}
		XValue actorsInGroupValue = actorsInGroupField.getValue();
		if(actorsInGroupValue == null) {
			return;
		} else {
			actorsInGroupValue = ((XIDSetValue)actorsInGroupValue).remove(actor);
			actorsInGroupField.setValue(actorsInGroupValue);
		}
	}
	
}
