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
import org.xydra.core.util.DumpUtils;
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
	
	/**
	 * State-less wrapper pulling all state from the cache index or base model
	 * of the {@link AbstractDelegatingWritableModel}.
	 */
	class WrappedField implements XWritableField {
		
		private final XID fieldId;
		private final XID objectId;
		
		public WrappedField(final XID objectId, final XID fieldId) {
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
			return AbstractDelegatingWritableModel.this.field_getRevisionNumber(this.objectId,
			        this.fieldId);
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
		public boolean setValue(final XValue value) {
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
		
		private final XID objectId;
		
		public WrappedObject(final XID objectId) {
			assert objectId != null;
			this.objectId = objectId;
		}
		
		@Override
		public XWritableField createField(final XID fieldId) {
			assert fieldId != null;
			return AbstractDelegatingWritableModel.this.object_createField(this.objectId, fieldId);
		}
		
		@Override
		public XAddress getAddress() {
			return XX.resolveObject(AbstractDelegatingWritableModel.this.getAddress(),
			        this.objectId);
		}
		
		@Override
		public XWritableField getField(final XID fieldId) {
			return AbstractDelegatingWritableModel.this.object_getField(this.objectId, fieldId);
		}
		
		@Override
		public XID getID() {
			return this.objectId;
		}
		
		@Override
		public long getRevisionNumber() {
			return object_getRevisionNumber(this.objectId);
		}
		
		@Override
		public XType getType() {
			return XType.XOBJECT;
		}
		
		@Override
		public boolean hasField(final XID fieldId) {
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
		public boolean removeField(final XID fieldId) {
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
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
	        .getLogger(AbstractDelegatingWritableModel.class);
	
	public static final long UNDEFINED = -2;
	
	@Override
	public abstract XWritableObject createObject(final XID objectId);
	
	protected abstract long field_getRevisionNumber(final XID objectId, final XID fieldId);
	
	protected abstract XValue field_getValue(final XID objectId, final XID fieldId);
	
	protected boolean field_isEmpty(final XID objectId, final XID fieldId) {
		assert hasObject(objectId);
		assert getObject(objectId).hasField(fieldId);
		
		return getObject(objectId).getField(fieldId).isEmpty();
	}
	
	protected abstract boolean field_setValue(final XID objectId, final XID fieldId,
	        final XValue value);
	
	@Override
	public abstract XAddress getAddress();
	
	@Override
	public abstract XID getID();
	
	@Override
	public XWritableObject getObject(final XID objectId) {
		if(hasObject(objectId)) {
			return new WrappedObject(objectId);
		} else {
			return null;
		}
	}
	
	@Override
	public XType getType() {
		return XType.XMODEL;
	}
	
	@Override
	public abstract boolean hasObject(final XID objectId);
	
	@Override
	public abstract Iterator<XID> iterator();
	
	protected abstract XWritableField object_createField(final XID objectId, final XID fieldId);
	
	protected XWritableField object_getField(final XID objectId, final XID fieldId) {
		assert objectId != null;
		assert fieldId != null;
		if(object_hasField(objectId, fieldId)) {
			return new WrappedField(objectId, fieldId);
		} else {
			return null;
		}
	}
	
	protected abstract long object_getRevisionNumber(final XID objectId);
	
	protected abstract boolean object_hasField(final XID objectId, final XID fieldId);
	
	protected abstract boolean object_isEmpty(final XID objectId);
	
	protected abstract Iterator<XID> object_iterator(final XID objectId);
	
	protected abstract boolean object_removeField(final XID objectId, final XID fieldId);
	
	@Override
	public abstract boolean removeObject(final XID objectId);
	
	protected XAddress resolveField(final XID objectId, final XID fieldId) {
		assert objectId != null;
		assert fieldId != null;
		return XX.toAddress(this.getAddress().getRepository(), this.getID(), objectId, fieldId);
	}
	
	protected XAddress resolveObject(final XID objectId) {
		assert objectId != null;
		return XX.toAddress(this.getAddress().getRepository(), this.getID(), objectId, null);
	}
	
	@Override
	public String toString() {
		return this.getID() + " (" + this.getClass().getName() + ") " + this.hashCode() + " "
		        + DumpUtils.toStringBuffer(this);
	}
	
}
