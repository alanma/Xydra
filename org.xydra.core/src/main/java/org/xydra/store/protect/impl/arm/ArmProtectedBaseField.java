package org.xydra.store.protect.impl.arm;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.value.XValue;
import org.xydra.core.AccessException;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.access.XAuthorisationManager;


/**
 * An {@link XReadableField} that wraps an {@link XReadableField} for a specific
 * actor and checks all access against an {@link XAuthorisationManager}.
 *
 * @author dscharrer
 *
 */
public class ArmProtectedBaseField implements XReadableField {

	protected final XId actor;
	protected final XAuthorisationManager arm;
	private final XReadableField field;

	public ArmProtectedBaseField(final XReadableField field, final XAuthorisationManager arm, final XId actor) {
		this.field = field;
		this.arm = arm;
		this.actor = actor;

		XyAssert.xyAssert(field != null); assert field != null;
		XyAssert.xyAssert(arm != null); assert arm != null;
	}

	protected void checkReadAccess() throws AccessException {
		// IMPROVE cache this?
		if(!this.arm.canRead(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot read " + getAddress());
		}
	}

	public XId getActor() {
		return this.actor;
	}

	@Override
    public XAddress getAddress() {
		return this.field.getAddress();
	}

	@Override
    public XId getId() {
		return this.field.getId();
	}

	@Override
    public long getRevisionNumber() {

		checkReadAccess();

		return this.field.getRevisionNumber();
	}

	@Override
    public XValue getValue() {

		checkReadAccess();

		return this.field.getValue();
	}

	@Override
    public boolean isEmpty() {

		checkReadAccess();

		return this.field.isEmpty();
	}

	@Override
	public XType getType() {
		return XType.XFIELD;
	}

}
