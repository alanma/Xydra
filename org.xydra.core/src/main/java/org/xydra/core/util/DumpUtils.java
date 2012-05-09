package org.xydra.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.xydra.base.IHasXID;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.core.model.delta.DeltaUtils.IFieldDiff;
import org.xydra.core.model.delta.DeltaUtils.IObjectDiff;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * @author xamde
 * 
 */
public class DumpUtils {
	
	private static final Logger log = LoggerFactory.getLogger(DumpUtils.class);
	
	/**
	 * @param label to identify the log output
	 * @param repo to be dumped
	 * @return an empty string, so that this method can be used in <code>
	 * assert condition : DumpUtils.dump("mylabel", myentity);
	 * </code>
	 */
	public static String dump(String label, XReadableRepository repo) {
		XyAssert.xyAssert(repo != null);
		assert repo != null;
		XyAssert.xyAssert(repo.getAddress().getAddressedType() == XType.XREPOSITORY);
		log.info(label + " * Repo " + repo.getId() + " ...");
		for(XID modelId : repo) {
			XReadableModel model = repo.getModel(modelId);
			dump(label, model);
		}
		return "";
	}
	
	/**
	 * @param label to identify the log output
	 * @param model to be dumped
	 * @return an empty string, so that this method can be used in <code>
	 * assert condition : DumpUtils.dump("mylabel", myentity);
	 * </code>
	 */
	public static String dump(String label, XReadableModel model) {
		log.info(label + "\n" + toStringBuffer(model));
		return "";
	}
	
	/**
	 * @param model to be dumped to a String
	 * @return the model as a human-readable String
	 */
	public static StringBuffer toStringBuffer(XReadableModel model) {
		XyAssert.xyAssert(model != null);
		assert model != null;
		XyAssert.xyAssert(model.getAddress().getAddressedType() == XType.XMODEL);
		
		StringBuffer buf = new StringBuffer();
		buf.append("** Model   " + model.getAddress() + " [" + model.getRevisionNumber() + "]\n");
		List<XID> ids = toSortedList(model);
		for(XID objectId : ids) {
			XReadableObject object = model.getObject(objectId);
			buf.append(toStringBuffer(object));
		}
		return buf;
	}
	
	private static List<XID> toSortedList(Iterable<XID> iterable) {
		List<XID> list = new ArrayList<XID>();
		for(XID id : iterable) {
			list.add(id);
		}
		Collections.sort(list);
		return list;
	}
	
	/**
	 * @param label to identify the log output
	 * @param object to be dumped
	 * @return an empty string, so that this method can be used in <code>
	 * assert condition : DumpUtils.dump("mylabel", myentity);
	 * </code>
	 */
	public static String dump(String label, XReadableObject object) {
		log.info(label + "\n" + toStringBuffer(object));
		return "";
	}
	
	/**
	 * @param object to be dumped
	 * @return given object as human-readable string
	 */
	public static StringBuffer toStringBuffer(XReadableObject object) {
		XyAssert.xyAssert(object != null);
		assert object != null;
		XyAssert.xyAssert(object.getAddress().getAddressedType() == XType.XOBJECT);
		StringBuffer buf = new StringBuffer();
		buf.append("*** Object " + object.getAddress() + " [" + object.getRevisionNumber() + "]\n");
		List<XID> ids = toSortedList(object);
		for(XID fieldId : ids) {
			XReadableField field = object.getField(fieldId);
			buf.append(toStringBuffer(field));
		}
		return buf;
	}
	
	/**
	 * @param label to identify the log output
	 * @param field to be dumped
	 * @return an empty string, so that this method can be used in <code>
	 * assert condition : DumpUtils.dump("mylabel", myentity);
	 * </code>
	 */
	public static String dump(String label, XReadableField field) {
		log.info(label + "\n" + toStringBuffer(field));
		return "";
	}
	
	/**
	 * @param field to be dumped
	 * @return the field as a human-readable String
	 */
	public static StringBuffer toStringBuffer(XReadableField field) {
		XyAssert.xyAssert(field != null);
		assert field != null;
		XyAssert.xyAssert(field.getAddress().getAddressedType() == XType.XFIELD);
		StringBuffer buf = new StringBuffer();
		buf.append("**** Field " + field.getAddress() + " = '" + field.getValue() + "' " + " ["
		        + field.getRevisionNumber() + "]\n");
		return buf;
	}
	
	public static class XidComparator implements Comparator<IHasXID> {
		
		@Override
		public int compare(IHasXID a, IHasXID b) {
			return a.getId().compareTo(b.getId());
		}
		
		public static XidComparator INSTANCE = new XidComparator();
		
	}
	
	public static StringBuilder changesToString(final DeltaUtils.IModelDiff changedModel) {
		StringBuilder sb = new StringBuilder();
		List<XReadableObject> addedList = new ArrayList<XReadableObject>(changedModel.getAdded());
		Collections.sort(addedList, XidComparator.INSTANCE);
		for(XReadableObject addedObject : addedList) {
			sb.append("=== ADDED   Object '" + addedObject.getId() + "' ===<br/>\n");
			sb.append(DumpUtils.toStringBuffer(addedObject));
		}
		List<XID> removedList = new ArrayList<XID>(changedModel.getRemoved());
		Collections.sort(removedList, XidComparator.INSTANCE);
		for(XID removedObjectId : removedList) {
			sb.append("=== REMOVED Object '" + removedObjectId + "' ===<br/>\n");
		}
		List<IObjectDiff> potentiallyChangedList = new ArrayList<IObjectDiff>(
		        changedModel.getPotentiallyChanged());
		Collections.sort(potentiallyChangedList, XidComparator.INSTANCE);
		for(IObjectDiff changedObject : potentiallyChangedList) {
			if(changedObject.hasChanges()) {
				sb.append("=== CHANGED Object '" + changedObject.getId() + "' === <br/>\n");
				sb.append(changesToString(changedObject));
			}
		}
		return sb;
	}
	
	public static StringBuilder changesToString(final DeltaUtils.IObjectDiff changedObject) {
		StringBuilder sb = new StringBuilder();
		List<XReadableField> addedList = new ArrayList<XReadableField>(changedObject.getAdded());
		Collections.sort(addedList, XidComparator.INSTANCE);
		for(XReadableField field : addedList) {
			sb.append("--- ADDED Field '" + field.getId() + "' ---<br/>\n");
			sb.append(DumpUtils.toStringBuffer(field));
		}
		List<XID> removedList = new ArrayList<XID>(changedObject.getRemoved());
		Collections.sort(removedList, XidComparator.INSTANCE);
		for(XID objectId : changedObject.getRemoved()) {
			sb.append("--- REMOVED Field '" + objectId + "' ---<br/>\n");
		}
		List<IFieldDiff> potentiallyChangedList = new ArrayList<IFieldDiff>(
		        changedObject.getPotentiallyChanged());
		Collections.sort(potentiallyChangedList, XidComparator.INSTANCE);
		for(IFieldDiff changedField : potentiallyChangedList) {
			if(changedField.isChanged()) {
				sb.append("--- CHANGED Field '" + changedField.getId() + "' ---<br/>\n");
				sb.append(changesToString(changedField));
			}
		}
		return sb;
	}
	
	public static StringBuilder changesToString(final DeltaUtils.IFieldDiff changedField) {
		StringBuilder sb = new StringBuilder();
		sb.append("'" + changedField.getInitialValue() + "' ==> '" + changedField.getValue()
		        + "' <br/>\n");
		return sb;
	}
	
	public static void dumpChangeLog(XydraPersistence pers, XAddress modelAddress) {
		List<XEvent> events = pers.getEvents(modelAddress, 0, Long.MAX_VALUE);
		for(XEvent event : events) {
			log.info("Event " + event.getRevisionNumber() + ": " + event);
		}
	}
}
