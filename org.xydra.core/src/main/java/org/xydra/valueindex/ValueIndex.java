package org.xydra.valueindex;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.index.query.EqualsConstraint;


/**
 * A data structure used for indexing the contents of an {@link XModel}, i.e.
 * the {@link XValue XValues} contained in its {@link XField XFields}.
 * 
 * @author Kaidel
 * 
 */

public interface ValueIndex {
	
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
	 * @param fieldAddress the address of the {@link XReadableField} in which
	 *            the given value exists
	 * @param value the value which is to be associated with the given key
	 * @throws RuntimeException if the given {@link XAddress} is not a field
	 *             address.
	 */
	public void index(String key, XAddress fieldAddress, XValue value);
	
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
	 * @param fieldAddress the address of the {@link XReadableField} in which
	 *            the given value exists
	 * @param value the value which is to be removed from the set of values
	 *            associated with the given key
	 * @throws RuntimeException if the given {@link XAddress} is not a field
	 *             address.
	 */
	public void deIndex(String key, XAddress fieldAddress, XValue value);
	
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
	 */
	Iterator<ValueIndexEntry> constraintIterator(EqualsConstraint<String> c1);
	
	/**
	 * Deindexes the given key, i.e. removes the set of entries which are
	 * associated with this key. This does not completely remove these entries
	 * from the Index, since they might be associated with other keys.
	 * 
	 * @param key The key which is to be completely deindexed.
	 */
	public void deIndex(String key);
	
	/**
	 * Checks if there are entries associated with the given key.
	 * 
	 * @param key The key which is to be checked.
	 * @return true, if there are entries associated with the given key, false
	 *         otherwise.
	 */
	public boolean containsKey(String key);
	
	/**
	 * Checks if under the given entryConstraint there are entries associated
	 * with key under the given constraint.
	 * 
	 * The counter-variable of the {@link ValueIndexEntry} in the constraint
	 * will not be used for checking!
	 * 
	 * @param keyConstraint The constraint for the keys.
	 * @param entryConstraint The constraint for the entries
	 * @return true, if under the given entryConstraint there are entries
	 *         associated with key under the given constraint, false otherwise.
	 */
	public boolean contains(EqualsConstraint<String> keyConstraint,
	        EqualsConstraint<ValueIndexEntry> entryConstraint);
	
}
