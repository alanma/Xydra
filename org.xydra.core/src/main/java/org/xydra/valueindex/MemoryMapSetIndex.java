package org.xydra.valueindex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.value.XValue;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.sharedutils.XyAssert;


/**
 * A simple in-memory implementation of {@link ValueIndex}, for testing
 * purposes.
 *
 * Warning: Some methods are not supported.
 *
 * @author kaidel
 *
 */

public class MemoryMapSetIndex implements ValueIndex {
	private final HashMap<String,HashSet<ValueIndexEntry>> map;

	public MemoryMapSetIndex() {
		this.map = new HashMap<String,HashSet<ValueIndexEntry>>();
	}

	@Override
	public Iterator<ValueIndexEntry> constraintIterator(final EqualsConstraint<String> c1) {
		final HashSet<ValueIndexEntry> result = new HashSet<ValueIndexEntry>();

		for(final String key : this.map.keySet()) {
			if(c1.matches(key)) {
				final HashSet<ValueIndexEntry> entries = this.map.get(key);

				result.addAll(entries);
			}
		}

		return result.iterator();
	}

	@Override
	public boolean contains(final EqualsConstraint<String> c1,
	        final EqualsConstraint<ValueIndexEntry> entryConstraint) {
		// TODO maybe implement...
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(final String key) {
		return this.map.containsKey(key);
	}

	@Override
	public void deIndex(final String key, final XAddress fieldAddress, final XValue value) {
		if(fieldAddress.getAddressedType() != XType.XFIELD) {
			throw new RuntimeException("The given fieldAddress was no address of a field, but an "
			        + fieldAddress.getAddressedType() + "type address!");
		}

		final HashSet<ValueIndexEntry> set = this.map.get(key);

		if(set == null) {
			return;
		}

		final Iterator<ValueIndexEntry> iterator = set.iterator();

		boolean found = false;
		while(!found && iterator.hasNext()) {
			final ValueIndexEntry entry = iterator.next();

			if(entry.equalAddressAndValue(fieldAddress, value)) {
				found = true;
				set.remove(entry);
			}
		}

		if(set.size() == 0) {
			deIndex(key);
		}

	}

	@Override
	public void deIndex(final String key) {
		this.map.remove(key);
	}

	@Override
	public void index(final String key, final XAddress fieldAddress, final XValue value) {
		if(fieldAddress.getAddressedType() != XType.XFIELD) {
			throw new RuntimeException("The given fieldAddress was no address of a field, but an "
			        + fieldAddress.getAddressedType() + "type address!");
		}

		if(!this.map.containsKey(key)) {
			this.map.put(key, new HashSet<ValueIndexEntry>());
		}

		final HashSet<ValueIndexEntry> set = this.map.get(key);
		XyAssert.xyAssert(set != null); assert set != null;

		final Iterator<ValueIndexEntry> iterator = set.iterator();

		boolean found = false;
		while(!found && iterator.hasNext()) {
			final ValueIndexEntry triple = iterator.next();

			if(triple.equalAddressAndValue(fieldAddress, value)) {
				found = true;
			}
		}

		if(!found) {
			// no entry found -> add one
			final ValueIndexEntry newEntry = new ValueIndexEntry(fieldAddress, value);
			set.add(newEntry);
		}
	}

	public Iterator<String> keyIterator() {
		return this.map.keySet().iterator();
	}

}
