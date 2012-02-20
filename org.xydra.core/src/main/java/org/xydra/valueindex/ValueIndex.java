package org.xydra.valueindex;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.index.IMapSetIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;


/**
 * A data structure used for indexing the contents of an {@link XModel}
 * 
 * @author Kaidel
 * 
 */

public interface ValueIndex extends IMapSetIndex<String,ValueIndexEntry> {
	
	/**
	 * Associates the given {@link ValueIndexEntry} with the given key. The
	 * counter-variable of the ValueIndexEntry will be ignored, two
	 * ValueIndexEntry that only differ in the counter (i.e. have the same
	 * address and value) will be treated as equal entries.
	 * 
	 * If there already exists an {@link ValueIndexEntry} with the same address-
	 * and value-variables as the given entry in the set of entries associated
	 * with the given key, its counter will be incremented. If no such entry
	 * exists, the given entry will be added to the set of entries associated
	 * with the given key with its counter set to 1.
	 * 
	 * @param key The key.
	 * @param entry The ValueIndexEntry which is to be added under the specified
	 *            key (counter will be ignored)
	 */
	@Override
	public void index(String key, ValueIndexEntry entry);
	
	/**
	 * Associates the given pair of {@link XAddress} and {@link XValue} with the
	 * given key.
	 * 
	 * If there already exists a pair of {@link XAddress} and {@link XValue}
	 * equal to the given address and value in the set of entries associated
	 * with the given key, its counter will be incremented. If no such entry
	 * exists, the given entry will be added to the set of entries associated
	 * with the given key with its counter set to 1.
	 * 
	 * @param key The key.
	 * @param objectAddress the address of the {@link XObject} in which the
	 *            given value exists
	 * @param value the value which is to be associated with the given key
	 */
	public void index(String key, XAddress objectAddress, XValue value);
	
	/**
	 * Tries to remove the given {@link ValueIndexEntry} from the set of entries
	 * for the given key. The counter-variable of the ValueIndexEntry will be
	 * ignored, two ValueIndexEntry that only differ in the counter (i.e. have
	 * the same address and value) will be treated as equal entries.
	 * 
	 * If there already exists an {@link ValueIndexEntry} with the same address-
	 * and value-variables in the set of entries associated with the given key
	 * as the given entry, it's counter will be decremented. If the counter is
	 * set to 0 after if was decremented, the entry will be removed from the set
	 * of entries associated to the given key.
	 * 
	 * @param key The key.
	 * @param entry The ValueIndexEntry which is to be removed (counter will be
	 *            ignored)
	 */
	@Override
	public void deIndex(String key, ValueIndexEntry entry);
	
	/**
	 * Tries to remove the given pair of {@link XAddress} and {@link XValue}
	 * from the set of entries for the given key.
	 * 
	 * If there already exists a pair of {@link XAddress} and {@link XValue} in
	 * the set of entries associated with the given key equal to the given
	 * address and value, it's counter will be decremented. If the counter is
	 * set to 0 after if was decremented, the entry will be removed from the set
	 * of entries associated to the given key.
	 * 
	 * @param key The key.
	 * @param objectAddress the address of the {@link XObject} in which the
	 *            given value exists
	 * @param value the value which is to be removed from the set of values
	 *            associated with the given key
	 */
	public void deIndex(String key, XAddress objectAddress, XValue value);
	
	/**
	 * Returns an iterator over all {@link ValueIndexEntry ValueIndexEntries}
	 * associated with the key given in the constraint. Only
	 * {@link EqualsConstraint EqualsConstraints} are allowed.
	 * 
	 * Be aware that changes made to the {@link ValueIndexEntry
	 * ValueIndexEntries} returned by the iterator might not affect the stored
	 * {@link ValueIndexEntry ValueIndexEntries}, depending on the
	 * implementation. The safest way is to assume that changes made to the
	 * entries returned by the iterator do not propagate to the index.
	 * 
	 * @param c1
	 * @return an iterator that ranges over all entries indexes by keys, where
	 *         the keys match c1
	 * @throws UnsupportedOperationException if the given constraint is not an
	 *             instance of {@link EqualsConstraint}.
	 */
	@Override
	Iterator<ValueIndexEntry> constraintIterator(Constraint<String> c1);
	
}
