package org.xydra.store.impl.gae.ng;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.sharedutils.XyAssert;


/**
 * An object snapshot that reflects a tentative version. It can be stored in the
 * GAE datastore.
 * 
 * Basically an {@link XReadableObject} + {@link #getModelRevision()} +
 * {@link #asRevWritableObject()}. And the address and ID are @NeverNull, even
 * if the object itself is null.
 * 
 * @author xamde
 */
public class TentativeObjectSnapshot implements Serializable, XReadableObject {
	
	private static final long serialVersionUID = -1351865686747885441L;
	
	private long modelRevision;
	
	private @CanBeNull
	XRevWritableObject object;
	
	private transient XAddress objectAddress;
	
	@Override
	public String toString() {
		return this.objectAddress + " [model:" + this.modelRevision + "] " + this.object;
	}
	
	public TentativeObjectSnapshot(@CanBeNull XRevWritableObject object,
	        @NeverNull XAddress objectAddress, long modelRev) {
		this.object = object;
		XyAssert.xyAssert(objectAddress != null);
		this.objectAddress = objectAddress;
		this.modelRevision = modelRev;
	}
	
	public XRevWritableObject asRevWritableObject() {
		return this.object;
	}
	
	@Override
	public XAddress getAddress() {
		return this.objectAddress;
	}
	
	public XReadableField getField(XID fieldId) {
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
	
	public boolean isObjectExists() {
		return this.object != null;
	}
	
	public Iterator<XID> iterator() {
		return this.object.iterator();
	}
	
	public void setModelRev(long modelRev) {
		this.modelRevision = modelRev;
	}
	
}
