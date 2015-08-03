package org.xydra.core.change;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.index.IndexUtils;
import org.xydra.index.impl.MapIndex;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyEntryTuple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

/**
 * A helper class to minimize the number and size of persistence accesses.
 *
 * Helps also to create transactions easily.
 *
 * Does not support revision numbers.
 *
 * An implementation of {@link XWritableObject} that works as a diff on top of a
 * base {@link XWritableObject}. Via {@link #toCommandList(boolean)} a minimal
 * list of commands that changes the base model into the current state can be
 * created. The base model is changed at no times.
 *
 * @author xamde
 */
public class DiffWritableObject extends AbstractDelegatingWritableObject implements XWritableObject {

	private static final Logger log = LoggerFactory.getLogger(DiffWritableObject.class);

	static final XValue NOVALUE = XV.toValue("__NoValue_DiffWritableObject");

	/*
	 * Each index has the structure (field, value) with the notion to represent
	 * added/removed content in this model within a given repository.
	 *
	 * An added/removed, empty field without values is represented as ( fieldId,
	 * NOVALUE).
	 *
	 * A added/removed value is represented as ( fieldId, value).
	 */
	MapIndex<XId, XValue> added, removed;

	private final XWritableObject base;

	/**
	 * @param base any model
	 */
	public DiffWritableObject(final XWritableObject base) {
		XyAssert.xyAssert(base != null);
		assert base != null;
		this.base = base;
		this.added = new MapIndex<XId, XValue>();
		this.removed = new MapIndex<XId, XValue>();
	}

	private XValue getFieldValueFromBase(final XId fieldId) {
		final XWritableField f = this.base.getField(fieldId);
		if (f == null) {
			return null;
		}
		return f.getValue();
	}

	@Override
	protected XValue field_getValue(final XId fieldId) {
		XValue value = this.added.lookup(fieldId);
		if (value != null) {
			if (value == NOVALUE) {
				return null;
			}
			return value;
		} else {
			value = this.removed.lookup(fieldId);
			if (value != null) {
				return null;
			} else {
				return this.base.getField(fieldId).getValue();
			}
		}
	}

	@Override
	protected boolean field_setValue(final XId fieldId, final XValue value) {
		XyAssert.xyAssert(fieldId != null);
		assert fieldId != null;
		XyAssert.xyAssert(this.removed.lookup(fieldId) == null);
		XyAssert.xyAssert(hasField(fieldId));

		final XValue v = field_getValue(fieldId);
		if (v == null && value == null || v != null && v.equals(value)) {
			return false;
		}

		this.removed.deIndex(fieldId);
		this.added.index(fieldId, value);
		return true;
	}

	@Override
	public XAddress getAddress() {
		return this.base.getAddress();
	}

	/**
	 * Allows to end a transaction and go back to using the base object.
	 *
	 * @return the base object that has been used to created this wrapped
	 *         {@link DiffWritableObject}.
	 */
	public XWritableObject getBase() {
		return this.base;
	}

	@Override
	public XId getId() {
		return this.base.getId();
	}

	protected Set<XId> idsAsSet() {
		final Set<XId> set = IndexUtils.diff(this.base.iterator(), this.added.keyIterator(),
				this.removed.keyIterator());
		return set;
	}

	@Override
	public boolean isEmpty() {
		return idsAsSet().isEmpty();
	}

	@Override
	public Iterator<XId> iterator() {
		return idsAsSet().iterator();
	}

	@Override
	public XWritableField createField(final XId fieldId) {
		XyAssert.xyAssert(fieldId != null);
		assert fieldId != null;
		if (!hasField(fieldId)) {
			this.removed.deIndex(fieldId);
			this.added.index(fieldId, NOVALUE);
		}
		return new WrappedField(fieldId);
	}

	@Override
	public boolean hasField(final XId fieldId) {
		XyAssert.xyAssert(fieldId != null);
		assert fieldId != null;
		if (this.added.tupleIterator(new EqualsConstraint<XId>(fieldId)).hasNext()) {
			return true;
		}
		if (this.removed.tupleIterator(new EqualsConstraint<XId>(fieldId)).hasNext()) {
			return false;
		}
		// else
		return this.base.hasField(fieldId);
	}

	@Override
	public boolean removeField(final XId fieldId) {
		XyAssert.xyAssert(fieldId != null);
		assert fieldId != null;
		if (this.added.containsKey(new EqualsConstraint<XId>(fieldId))) {
			this.added.deIndex(fieldId);
			return true;
		} else {
			this.removed.index(fieldId, NOVALUE);
			return true;
		}
	}

	/**
	 * @param forced if true, create forced commands
	 *
	 *            TODO implement semantics of 'forced'
	 *
	 * @return a list of commands which transform the base model given at
	 *         creation time into this current model state
	 */
	public List<XAtomicCommand> toCommandList(final boolean forced) {
		final List<XAtomicCommand> list = new LinkedList<XAtomicCommand>();

		// remove
		Iterator<KeyEntryTuple<XId, XValue>> it = this.removed.tupleIterator(new Wildcard<XId>());
		while (it.hasNext()) {
			final KeyEntryTuple<XId, XValue> field_value = it.next();
			// remove empty field or field with value
			list.add(BaseRuntime.getCommandFactory().createForcedRemoveFieldCommand(
					resolveField(field_value.getKey())));
		}

		// add
		it = this.added.tupleIterator(new Wildcard<XId>());
		while (it.hasNext()) {
			final KeyEntryTuple<XId, XValue> e = it.next();
			if (e.getEntry().equals(NOVALUE)) {
				// add empty field
				list.add(BaseRuntime.getCommandFactory()
						.createForcedAddFieldCommand(getAddress(), e.getKey()));
			} else {
				// value
				final XValue currentValue = getFieldValueFromBase(e.getKey());
				if (currentValue == null) {
					// maybe still add field
					if (!this.base.hasField(e.getKey())) {
						list.add(BaseRuntime.getCommandFactory().createForcedAddFieldCommand(getAddress(),
								e.getKey()));
					}
					// add value
					list.add(BaseRuntime.getCommandFactory().createForcedAddValueCommand(
							resolveField(e.getKey()), e.getEntry()));
				} else {
					// change value
					list.add(BaseRuntime.getCommandFactory().createForcedChangeValueCommand(
							resolveField(e.getKey()), e.getEntry()));
				}
			}
		}

		/*
		 * Make sure model commands come before object commands; object commands
		 * come before field commands. This avoids e.g. deleting a field before
		 * deleting its object parent.
		 */
		Collections.sort(list, new Comparator<XAtomicCommand>() {
			@Override
			public int compare(final XAtomicCommand a, final XAtomicCommand b) {
				return b.getChangedEntity().getAddressedType()
						.compareTo(a.getChangedEntity().getAddressedType());
			}
		});

		return list;
	}

	/**
	 * TODO merge with impl in {@link DiffWritableModel}
	 *
	 * @return txn or null
	 */
	public XTransaction toTransaction() {
		final List<XAtomicCommand> list = toCommandList(true);
		final XTransactionBuilder builder = new XTransactionBuilder(getAddress());
		for (final XAtomicCommand command : list) {
			builder.addCommand(command);
		}
		if (builder.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("No command in txn for model '" + getId() + "'");
			}
			return null;
		}
		final XTransaction txn = builder.build();
		assert txn != null;
		if (log.isTraceEnabled()) {
			if (log.isDebugEnabled()) {
				log.debug("Commands in txn for model '" + getId() + "'");
			}
			for (final XAtomicCommand atomicCommand : txn) {
				if (log.isDebugEnabled()) {
					log.debug("  Command " + atomicCommand);
				}
			}
		}
		return txn;
	}

	public boolean hasChanges() {
		return !this.added.isEmpty() || !this.removed.isEmpty();
	}

	@Override
	public long getRevisionNumber() {
		throw new UnsupportedOperationException();
		// if(log.isDebugEnabled())
		// log.debug("Returning outdated base-revision number");
		// return this.base.getRevisionNumber();
	}

	@Override
	protected long getRevisionNumber(final XId objectId) {
		throw new UnsupportedOperationException();
		// XWritableObject object = this.base.getObject(objectId);
		// if(object == null) {
		// return UNDEFINED;
		// }
		// return object.getRevisionNumber();
	}

	@Override
	protected long field_getRevisionNumber(final XId fieldId) {
		throw new UnsupportedOperationException();
		// XWritableObject object = this.base.getObject(objectId);
		// if(object == null) {
		// return UNDEFINED;
		// }
		// XWritableField field = object.getField(fieldId);
		// if(field == null) {
		// return UNDEFINED;
		// }
		// return field.getRevisionNumber();
	}

	@Override
	protected boolean field_exists(final XId fieldId) {
		return hasField(fieldId);
	}

	@Override
	protected boolean field_isEmpty(final XId fieldId) {
		if (hasField(fieldId)) {
			return field_getValue(fieldId) == null;
		}
		return false;
	}

}
