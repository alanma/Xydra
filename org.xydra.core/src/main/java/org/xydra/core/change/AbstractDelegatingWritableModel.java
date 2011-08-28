package org.xydra.core.change;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * A abstract helper class for the commonalities between {@link XWritableModel}
 * implementations that have a delegation strategy to an internal state.
 * 
 * 
 * @author xamde
 */
public abstract class AbstractDelegatingWritableModel implements XWritableModel {
	
	private static final Logger log = LoggerFactory
	        .getLogger(AbstractDelegatingWritableModel.class);
	
	public static final long UNDEFINED = -2;
	
	/**
	 * State-less wrapper pulling all state from the cache index or base model
	 * of the {@link AbstractDelegatingWritableModel}.
	 */
	class WrappedField implements XWritableField {
		
		private XID fieldId;
		private XID objectId;
		
		public WrappedField(XID objectId, XID fieldId) {
			assert objectId != null;
			assert fieldId != null;
			this.objectId = objectId;
			this.fieldId = fieldId;
		}
		
		@Override
		public XAddress getAddress() {
			XAddress xa = AbstractDelegatingWritableModel.this.getAddress();
			return XX.toAddress(xa.getRepository(), xa.getModel(), this.objectId, this.fieldId);
		}
		
		@Override
		public XID getID() {
			return this.fieldId;
		}
		
		@Override
		public long getRevisionNumber() {
			log.warn("Returning UNDEFINED as revision number");
			return UNDEFINED;
		}
		
		@Override
		public XType getType() {
			return XType.XFIELD;
		}
		
		@Override
		public XValue getValue() {
			return AbstractDelegatingWritableModel.this.field_getValue(this.objectId, this.fieldId);
		}
		
		@Override
		public boolean isEmpty() {
			return AbstractDelegatingWritableModel.this.field_isEmpty(this.objectId, this.fieldId);
		}
		
		@Override
		public boolean setValue(XValue value) {
			assert this.objectId != null;
			return AbstractDelegatingWritableModel.this.field_setValue(this.objectId, this.fieldId,
			        value);
		}
		
	}
	
	/**
	 * State-less wrapper pulling all state from the cache index or base model
	 * of the {@link AbstractDelegatingWritableModel}.
	 */
	class WrappedObject implements XWritableObject {
		
		private XID objectId;
		
		public WrappedObject(XID objectId) {
			assert objectId != null;
			this.objectId = objectId;
		}
		
		@Override
		public XWritableField createField(XID fieldId) {
			assert fieldId != null;
			return AbstractDelegatingWritableModel.this.object_createField(this.objectId, fieldId);
		}
		
		@Override
		public XAddress getAddress() {
			return XX.resolveObject(AbstractDelegatingWritableModel.this.getAddress(),
			        this.objectId);
		}
		
		@Override
		public XWritableField getField(XID fieldId) {
			return AbstractDelegatingWritableModel.this.object_getField(this.objectId, fieldId);
		}
		
		@Override
		public XID getID() {
			return this.objectId;
		}
		
		@Override
		public long getRevisionNumber() {
			log.warn("Returning UNDEFINED as revision number");
			return UNDEFINED;
		}
		
		@Override
		public XType getType() {
			return XType.XOBJECT;
		}
		
		@Override
		public boolean hasField(XID fieldId) {
			return AbstractDelegatingWritableModel.this.object_hasField(this.objectId, fieldId);
		}
		
		@Override
		public boolean isEmpty() {
			return AbstractDelegatingWritableModel.this.object_isEmpty(this.objectId);
		}
		
		@Override
		public Iterator<XID> iterator() {
			return AbstractDelegatingWritableModel.this.object_iterator(this.objectId);
		}
		
		@Override
		public boolean removeField(XID fieldId) {
			return AbstractDelegatingWritableModel.this.object_removeField(this.objectId, fieldId);
		}
		
		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("XObject '").append(this.getAddress().toString()).append("' <br/>\n");
			if(!this.isEmpty()) {
				for(XID fieldId : this) {
					buf.append("* '").append(fieldId.toString()).append("' = ");
					XWritableField field = this.getField(fieldId);
					if(field == null) {
						buf.append("NULL");
					} else {
						String value = field.getValue() == null ? "null" : field.getValue()
						        .toString();
						buf.append("'").append(value).append("'");
					}
					buf.append(" <br/>\n");
				}
			}
			return buf.toString();
		}
		
	}
	
	public abstract XWritableObject createObject(XID objectId);
	
	protected abstract XValue field_getValue(XID objectId, XID fieldId);
	
	protected boolean field_isEmpty(XID objectId, XID fieldId) {
		assert hasObject(objectId);
		assert getObject(objectId).hasField(fieldId);
		
		return getObject(objectId).getField(fieldId).isEmpty();
	}
	
	protected abstract boolean field_setValue(XID objectId, XID fieldId, XValue value);
	
	public abstract XAddress getAddress();
	
	public abstract XID getID();
	
	public XWritableObject getObject(XID objectId) {
		if(hasObject(objectId)) {
			return new WrappedObject(objectId);
		} else {
			return null;
		}
	}
	
	@Override
	public long getRevisionNumber() {
		log.warn("Returning UNDEFINED as revision number");
		return UNDEFINED;
	}
	
	@Override
	public XType getType() {
		return XType.XMODEL;
	}
	
	@Override
	public abstract boolean hasObject(XID objectId);
	
	public abstract Iterator<XID> iterator();
	
	protected abstract XWritableField object_createField(XID objectId, XID fieldId);
	
	protected XWritableField object_getField(XID objectId, XID fieldId) {
		assert objectId != null;
		assert fieldId != null;
		if(object_hasField(objectId, fieldId)) {
			return new WrappedField(objectId, fieldId);
		} else {
			return null;
		}
	}
	
	protected abstract boolean object_hasField(XID objectId, XID fieldId);
	
	protected abstract boolean object_isEmpty(XID objectId);
	
	protected abstract Iterator<XID> object_iterator(XID objectId);
	
	protected abstract boolean object_removeField(XID objectId, XID fieldId);
	
	public abstract boolean removeObject(XID objectId);
	
	protected XAddress resolveField(XID objectId, XID fieldId) {
		assert objectId != null;
		assert fieldId != null;
		return XX.toAddress(this.getAddress().getRepository(), this.getID(), objectId, fieldId);
	}
	
	protected XAddress resolveObject(XID objectId) {
		assert objectId != null;
		return XX.toAddress(this.getAddress().getRepository(), this.getID(), objectId, null);
	}
	
	@Override
	public String toString() {
		return this.getID() + " (" + this.getClass().getName() + ") " + this.hashCode();
	}
	
}
