/**
 * 
 */
package org.xydra.server.impl.newgae.changes;

import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;

import com.google.appengine.api.datastore.Entity;


/**
 * Internal helper class used by {@link GaeChangesService} to access the current
 * object state.
 * 
 * @author dscharrer
 * 
 */
public class InternalGaeObject extends InternalGaeContainerXEntity<InternalGaeField> implements
        XBaseObject {
	
	private long objectRev = XEvent.RevisionNotAvailable;
	
	protected InternalGaeObject(GaeChangesService changesService, XAddress objectAddr,
	        Set<XAddress> locks) {
		super(changesService, objectAddr, locks);
		assert objectAddr.getAddressedType() == XType.XOBJECT;
	}
	
	public long getRevisionNumber() {
		if(this.objectRev == XEvent.RevisionNotAvailable && getLocks().contains(getAddress())) {
			
			// FIXME we don't always have the locks to get the objectRev
			// this way => some events may be missing the objectRev
			for(XID fieldId : this) {
				XBaseField field = getField(fieldId);
				long fieldRev = field.getRevisionNumber();
				if(fieldRev > this.objectRev) {
					this.objectRev = fieldRev;
				}
			}
			/*
			 * FIXME this won't work for empty objects
			 * 
			 * causes DataApiTestGae#testPostModel to fail
			 */
		}
		return this.objectRev;
	}
	
	public XID getID() {
		return getAddress().getObject();
	}
	
	public InternalGaeField getField(XID fieldId) {
		return getChild(fieldId);
	}
	
	public boolean hasField(XID fieldId) {
		return hasChild(fieldId);
	}
	
	@Override
	protected InternalGaeField loadChild(XAddress childAddr, Entity childEntity) {
		return InternalGaeField.get(getChangesService(), childAddr, childEntity);
	}
	
	@Override
	protected XAddress resolveChild(XAddress addr, XID childId) {
		return XX.resolveField(addr, childId);
	}
	
	@Override
	protected XID getChildId(XAddress childAddr) {
		assert childAddr.getAddressedType() == XType.XFIELD;
		return childAddr.getField();
	}
	
}
