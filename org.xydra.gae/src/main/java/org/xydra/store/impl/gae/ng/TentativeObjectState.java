package org.xydra.store.impl.gae.ng;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.core.XCopyUtils;
import org.xydra.sharedutils.XyAssert;


/**
 * An object snapshot that reflects a tentative version. It can be stored in the
 * GAE datastore.
 * 
 * Basically an {@link XRevWritableObject} + {@link #exists()}
 * 
 * @author xamde
 */
public class TentativeObjectState implements Serializable, XRevWritableObject {
	
	private static final long serialVersionUID = -1351865686747885441L;
	
	private long modelRevision;
	
	@NeverNull
	XRevWritableObject object;
	
	private boolean objectExists;
	
	public boolean exists() {
		return this.objectExists;
	}
	
	@Override
	public String toString() {
		return this.getAddress() + " [" + (this.objectExists ? "exists" : "-na-") + "]"
		        + " [model:" + this.modelRevision + "] " + this.object;
	}
	
	public TentativeObjectState(@NeverNull XReadableObject object, boolean objectExists,
	        long modelRev) {
		XyAssert.xyAssert(object != null);
		this.object = XCopyUtils.createSnapshot(object);
		this.objectExists = objectExists;
		this.modelRevision = modelRev;
	}
	
	public XRevWritableObject asRevWritableObject() {
		return this.object;
	}
	
	@Override
	public XAddress getAddress() {
		return this.object.getAddress();
	}
	
	public XRevWritableField getField(XID fieldId) {
		return this.object.getField(fieldId);
	}
	
	@Override
	public XID getId() {
		return this.getAddress().getObject();
	}
	
	public long getModelRevision() {
		return this.modelRevision;
	}
	
	public long getRevisionNumber() {
		return this.object.getRevisionNumber();
	}
	
	@Override
	public XType getType() {
		return XType.XOBJECT;
	}
	
	public boolean hasField(XID fieldId) {
		return this.object.hasField(fieldId);
	}
	
	public boolean isEmpty() {
		return this.object.isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.object.iterator();
	}
	
	public void setModelRev(long modelRev) {
		this.modelRevision = modelRev;
	}
	
	public TentativeObjectState copy() {
		return new TentativeObjectState(XCopyUtils.createSnapshot(this.object), this.objectExists,
		        this.modelRevision);
	}
	
	@Override
	public boolean removeField(XID fieldId) {
		return this.object.removeField(fieldId);
	}
	
	@Override
	public void addField(XRevWritableField field) {
		this.object.addField(field);
	}
	
	@Override
	public XRevWritableField createField(XID fieldId) {
		return this.object.createField(fieldId);
	}
	
	@Override
	public void setRevisionNumber(long rev) {
		this.object.setRevisionNumber(rev);
	}
	
	public void setObjectExists(boolean objectExists) {
		this.objectExists = objectExists;
	}
	
	public void setObjectState(XReadableObject object) {
		this.object = XCopyUtils.createSnapshot(object);
	}
	
}