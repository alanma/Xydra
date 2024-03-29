package org.xydra.core.change;

import java.util.Iterator;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.util.DumpUtilsBase;
import org.xydra.base.value.XValue;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

/**
 * A abstract helper class for the commonalities between {@link XWritableObject}
 * implementations that have a delegation strategy to an internal state.
 *
 *
 * @author xamde
 */
public abstract class AbstractDelegatingWritableObject implements XWritableObject {

	/**
	 * State-less wrapper pulling all state from the cache index or base model
	 * of the {@link AbstractDelegatingWritableObject}.
	 */
	class WrappedField implements XWritableField {

		private final XId fieldId;

		public WrappedField(final XId fieldId) {
			XyAssert.xyAssert(fieldId != null);
			assert fieldId != null;
			this.fieldId = fieldId;
		}

		@Override
		public XAddress getAddress() {
			final XAddress xa = AbstractDelegatingWritableObject.this.getAddress();
			return Base.toAddress(xa.getRepository(), xa.getModel(), xa.getObject(), this.fieldId);
		}

		@Override
		public XId getId() {
			return this.fieldId;
		}

		@Override
		public long getRevisionNumber() {
			return field_getRevisionNumber(this.fieldId);
		}

		@Override
		public XType getType() {
			return XType.XFIELD;
		}

		@Override
		public XValue getValue() {
			return field_getValue(this.fieldId);
		}

		@Override
		public boolean isEmpty() {
			return field_isEmpty(this.fieldId);
		}

		@Override
		public boolean setValue(final XValue value) {
			return field_setValue(this.fieldId, value);
		}

	}

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(AbstractDelegatingWritableObject.class);

	public static final long UNDEFINED = -2;

	protected abstract boolean field_exists(XId fieldId);

	protected abstract boolean field_isEmpty(XId fieldId);

	protected abstract long field_getRevisionNumber(final XId fieldId);

	protected abstract XValue field_getValue(final XId fieldId);

	protected abstract boolean field_setValue(final XId fieldId, final XValue value);

	@Override
	public abstract XAddress getAddress();

	@Override
	public abstract XId getId();

	@Override
	public XType getType() {
		return XType.XOBJECT;
	}

	@Override
	public abstract Iterator<XId> iterator();

	@Override
	public XWritableField getField(final XId fieldId) {
		XyAssert.xyAssert(fieldId != null);
		assert fieldId != null;
		if (hasField(fieldId)) {
			return getField_internal(fieldId);
		} else {
			return null;
		}
	}

	protected XWritableField getField_internal(final XId fieldId) {
		return new WrappedField(fieldId);
	}

	/**
	 * @param objectId
	 * @return the revision number of the object with the given ID
	 */
	protected abstract long getRevisionNumber(final XId objectId);

	protected XAddress resolveField(final XId fieldId) {
		XyAssert.xyAssert(fieldId != null);
		assert fieldId != null;
		return Base.toAddress(getAddress().getRepository(), getAddress().getModel(),
				getId(), fieldId);
	}

	protected XAddress resolveObject(final XId objectId) {
		XyAssert.xyAssert(objectId != null);
		assert objectId != null;
		return Base.toAddress(getAddress().getRepository(), getAddress().getModel(),
				getId(), null);
	}

	@Override
	public String toString() {
		return getId() + " (" + this.getClass().getName() + ") " + hashCode() + " "
				+ DumpUtilsBase.toStringBuffer(this);
	}

}
