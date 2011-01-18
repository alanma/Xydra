package org.xydra.core.model.state.impl.memory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XReadableField;
import org.xydra.base.XReadableModel;
import org.xydra.base.XReadableObject;
import org.xydra.base.XID;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;



public class XStateUtils {
	
	/**
	 * A fast, memory efficient comparison
	 * 
	 * @param <T>
	 * @param aIterator should be the smaller iterator of the two
	 * @param bIterator
	 * @return
	 */
	public static <T> boolean equals(Iterator<T> aIterator, Iterator<T> bIterator) {
		Collection<T> aCollection = new HashSet<T>();
		while(aIterator.hasNext()) {
			aCollection.add(aIterator.next());
		}
		
		int bCount = 0;
		while(bIterator.hasNext()) {
			T b = bIterator.next();
			if(!aCollection.contains(b)) {
				return false;
			} else {
				bCount++;
			}
		}
		if(bCount != aCollection.size()) {
			return false;
		}
		
		return true;
	}
	
	public static String toString(Iterator<XID> idIterator, String separator) {
		StringBuffer buf = new StringBuffer();
		while(idIterator.hasNext()) {
			XID xid = idIterator.next();
			buf.append(xid.toString());
			buf.append(separator);
		}
		return buf.toString();
	}

	/**
	 * Copies the source field's state to the target. Doesn't copy the change
	 * log.
	 */
	public static void copy(XReadableModel source, XModelState target) {
		target.setRevisionNumber(source.getRevisionNumber());
		if(!target.isEmpty()) {
			Set<XID> toRemove = new HashSet<XID>();
			for(XID objectId : target) {
				if(!source.hasObject(objectId)) {
					XObjectState os = target.getObjectState(objectId);
					os.delete(null);
					toRemove.add(objectId);
				}
			}
			for(XID objectId : toRemove) {
				target.removeObjectState(objectId);
			}
		}
		for(XID objectId : source) {
			XObjectState os = target.getObjectState(objectId);
			if(os == null) {
				os = target.createObjectState(objectId);
				target.addObjectState(os);
			}
			XStateUtils.copy(source.getObject(objectId), os);
		}
		target.save(null);
	}

	/**
	 * Copies the source field's state to the target. Doesn't copy the change
	 * log.
	 */
	public static void copy(XReadableObject source, XObjectState target) {
		target.setRevisionNumber(source.getRevisionNumber());
		if(!target.isEmpty()) {
			Set<XID> toRemove = new HashSet<XID>();
			for(XID fieldId : target) {
				if(!source.hasField(fieldId)) {
					XFieldState fs = target.getFieldState(fieldId);
					fs.delete(null);
					toRemove.add(fieldId);
				}
			}
			for(XID fieldId : toRemove) {
				target.removeFieldState(fieldId);
			}
		}
		for(XID fieldId : source) {
			XFieldState fs = target.getFieldState(fieldId);
			if(fs == null) {
				fs = target.createFieldState(fieldId);
				target.addFieldState(fs);
			}
			XStateUtils.copy(source.getField(fieldId), fs);
		}
		target.save(null);
	}

	/**
	 * Copies the source field's state to the target.
	 */
	public static void copy(XReadableField source, XFieldState target) {
		target.setRevisionNumber(source.getRevisionNumber());
		target.setValue(source.getValue());
		target.save(null);
	}
	
}
