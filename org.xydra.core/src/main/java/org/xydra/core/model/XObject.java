package org.xydra.core.model;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;

/**
 * An {@link XObject} is a collection of {@link XField XFields}. {@link XField
 * XFields} may be added, removed or changed dynamically to an XObject at
 * runtime. An XObject may be stored in an {@link XModel} or live independently.
 *
 * For example an XObject might be used to model a person if we'd write a
 * phonebook application.
 *
 * Implementations of XObject should use an {@link XRevWritableObject} for
 * storing and representing the inner state of the XObject to allow maximum
 * persistence management flexibility.
 *
 * @author xamde
 * @author kaidel
 *
 */
public interface XObject extends XLoggedObject, XWritableObject, XSynchronizesChanges {

	/**
	 * FORCED Creates a new {@link XField} and adds it to this {@link XObject}
	 * or returns the already existing {@link XField} if the given {@link XId}
	 * was already taken.
	 *
	 * @param fieldId The {@link XId} for the {@link XField} which is to be
	 *            created.
	 *
	 * @return the newly created {@link XField} or the already existing
	 *         {@link XField} with this {@link XId}
	 * @throws IllegalStateException if this object has already been removed
	 */
	@Override
	@ModificationOperation
	XField createField(XId fieldId);

	/**
	 * Executes the given {@link XObjectCommand} if possible.
	 *
	 * This method will fail if, the given {@link XObjectCommand} cannot be
	 * executed which may occur in the following cases:
	 * <ul>
	 * <li>Remove-type {@link XObjectCommand}: the specified {@link XField} does
	 * not exist and therefore cannot be removed
	 * <li>Add-type {@link XObjectCommand}: the given {@link XId} is already
	 * taken and therefore a new {@link XField} with this {@link XId} cannot be
	 * created
	 * <li>the object-{@link XId} in the {@link XObjectCommand} does not concur
	 * with the {@link XId} of this XObject
	 * </ul>
	 *
	 * @param command The {@link XObjectCommand} which is to be executed
	 *
	 * @return {@link XCommand#FAILED} if executing the {@link XObjectCommand}
	 *         failed, {@link XCommand#NOCHANGE} if executing the
	 *         {@link XObjectCommand} didn't change anything or if executing the
	 *         {@link XObjectCommand} succeeded the revision number of the
	 *         {@link XObjectEvent} caused by the {@link XObjectCommand}.
	 * @throws IllegalStateException if this object has already been removed
	 */
	@ModificationOperation
	long executeObjectCommand(XObjectCommand command);

	/**
	 * Returns the {@link XField} with the given {@link XId} contained in this
	 * object.
	 *
	 * @param fieldId The {@link XId} of the {@link XField} which is to be
	 *            returned
	 * @return The {@link XField} with the given {@link XId} or null, if no
	 *         corresponding {@link XField} exists
	 * @throws IllegalStateException if this object has already been removed
	 */
	@Override
	@ReadOperation
	XField getField(XId fieldId);

	/**
	 * FORCED Removes the {@link XField} with the given {@link XId} from this
	 * XObject
	 *
	 * @param fieldId The {@link XId} of the {@link XField} which is to be
	 *            removed
	 *
	 * @return true, if the specified {@link XField} did exist and could be
	 *         removed
	 * @throws IllegalStateException if this object has already been removed
	 *             itself
	 */
	@Override
	@ModificationOperation
	boolean removeField(XId fieldId);

	/**
	 * Create a consistent snapshot of this object and all contained fields.
	 *
	 * @return null if this object has been removed, a consistent snapshot
	 *         otherwise.
	 */
	XRevWritableObject createSnapshot();

	/**
	 * Shortcut for createField, setValue. If value is null, this command does a
	 * removeField.
	 *
	 * @param fieldId
	 * @param value @CanBeNull
	 * @return
	 */
	boolean setFieldValue(XId fieldId, XValue value);

	/**
	 * Shortcut for getField, and if not null, getValue.
	 *
	 * @param fieldId
	 * @return
	 */
	XValue getFieldValue(XId fieldId);

}
