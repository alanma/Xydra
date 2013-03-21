package org.xydra.base.rmof.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.util.DumpUtils;
import org.xydra.sharedutils.XyAssert;


/**
 * A simple data container for {@link XWritableObject}.
 * 
 * Minimal memory footprint, can be used as data transfer object.
 * 
 * @author voelkel
 */
public class SimpleObject implements Serializable, XRevWritableObject {
	
	private static final long serialVersionUID = 5593443685935758227L;
	
	// not final for GWT serialisation
	private XAddress address;
	// not final for GWT serialisation
	private Map<XId,XRevWritableField> fields;
	private long revisionNumber;
	
	/** For GWT only! */
	protected SimpleObject() {
	}
	
	public SimpleObject(XAddress address) {
		this(address, XCommand.NEW);
	}
	
	public SimpleObject(XAddress address, long revisionNumber) {
		XyAssert.xyAssert(address.getAddressedType() == XType.XOBJECT);
		this.address = address;
		this.revisionNumber = revisionNumber;
		this.fields = new HashMap<XId,XRevWritableField>(2);
	}
	
	@Override
	public void addField(XRevWritableField field) {
		XyAssert.xyAssert(field != null);
		assert field != null;
		this.fields.put(field.getId(), field);
	}
	
	@Override
	public XRevWritableField createField(XId fieldId) {
		XRevWritableField field = this.fields.get(fieldId);
		if(field != null) {
			return field;
		}
		SimpleField newField = new SimpleField(XX.resolveField(this.address, fieldId));
		this.fields.put(fieldId, newField);
		return newField;
	}
	
	@Override
	public XAddress getAddress() {
		return this.address;
	}
	
	@Override
	public XRevWritableField getField(XId fieldId) {
		return this.fields.get(fieldId);
	}
	
	@Override
	public XId getId() {
		return this.address.getObject();
	}
	
	@Override
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	@Override
	public boolean hasField(XId fieldId) {
		return this.fields.containsKey(fieldId);
	}
	
	@Override
	public boolean isEmpty() {
		return this.fields.isEmpty();
	}
	
	@Override
	public Iterator<XId> iterator() {
		return this.fields.keySet().iterator();
	}
	
	@Override
	public boolean removeField(XId fieldId) {
		XRevWritableField oldField = this.fields.remove(fieldId);
		return oldField != null;
	}
	
	@Override
	public void setRevisionNumber(long rev) {
		this.revisionNumber = rev;
	}
	
	@Override
	public XType getType() {
		return XType.XOBJECT;
	}
	
	@Override
	public String toString() {
		return DumpUtils.toStringBuffer(this).toString();
	}
	
	/**
	 * @param object An object to copy.
	 * @return A copy of the object. Both objects share the same fields but not
	 *         the same field list or revision number.
	 */
	public static SimpleObject shallowCopy(XRevWritableObject object) {
		
		if(object == null) {
			return null;
		}
		
		SimpleObject result = new SimpleObject(object.getAddress(), object.getRevisionNumber());
		
		if(object instanceof SimpleModel) {
			result.fields.putAll(((SimpleObject)object).fields);
		} else {
			for(XId xid : object) {
				result.addField(object.getField(xid));
			}
		}
		
		return result;
	}
	
}
