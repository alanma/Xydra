package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Iterator;

import org.xydra.base.value.XListValue;
import org.xydra.base.value.XListValueIterator;
import org.xydra.index.XI;
import org.xydra.sharedutils.XyAssert;


/**
 * A generic implementation for most parts of a {@link XListValue}.
 *
 * @author dscharrer
 *
 * @param <E> collection base type, e.g. XInteger
 */
public abstract class MemoryListValue<E> implements XListValue<E>, Serializable {

	private static final long serialVersionUID = 7285839520276137162L;

	// empty constructor for GWT-Serializable
	protected MemoryListValue() {
	}

	@Override
	public boolean contains(final E elem) {
		final int s = size();
		for(int i = 0; i < s; i++) {
			if(XI.equals(get(i), elem)) {
				return true;
			}
		}
		return false;
	}

	protected void fillArray(final E[] array) {
		XyAssert.xyAssert(array.length == size());
		int i = 0;
		for(final E e : this) {
			array[i++] = e;
		}
	}

	@Override
	public int indexOf(final E elem) {
		final int s = size();
		for(int i = 0; i < s; i++) {
			if(XI.equals(get(i), elem)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public Iterator<E> iterator() {
		return new XListValueIterator<E>(this);
	}

	@Override
	public int lastIndexOf(final E elem) {
		for(int i = size(); i >= 0; i--) {
			if(XI.equals(get(i), elem)) {
				return i;
			}
		}
		return -1;
	}

}
