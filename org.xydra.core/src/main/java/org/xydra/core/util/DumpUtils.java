package org.xydra.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.model.delta.ChangedField;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.ChangedObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


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
		assert repo != null;
		assert repo.getAddress().getAddressedType() == XType.XREPOSITORY;
		log.info(label + " * Repo " + repo.getID() + " ...");
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
		assert model != null;
		assert model.getAddress().getAddressedType() == XType.XMODEL;
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
		assert object != null;
		assert object.getAddress().getAddressedType() == XType.XOBJECT;
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
		assert field != null;
		assert field.getAddress().getAddressedType() == XType.XFIELD;
		StringBuffer buf = new StringBuffer();
		buf.append("**** Field " + field.getAddress() + " = '" + field.getValue() + "' " + " ["
		        + field.getRevisionNumber() + "]\n");
		return buf;
	}
	
	public static StringBuffer changesToString(final ChangedModel changedModel) {
		StringBuffer buf = new StringBuffer();
		for(SimpleObject object : changedModel.getNewObjects()) {
			buf.append("=== ADDED Object '" + object.getID() + "' ===<br/>\n");
			buf.append(DumpUtils.toStringBuffer(object));
		}
		for(XID objectId : changedModel.getRemovedObjects()) {
			buf.append("=== REMOVED Object '" + objectId + "' ===<br/>\n");
		}
		for(ChangedObject changedObject : changedModel.getChangedObjects()) {
			if(changedObject.hasChanges()) {
				buf.append("=== CHANGED Object '" + changedObject.getID() + "' === <br/>\n");
				buf.append(changesToString(changedObject));
			}
		}
		return buf;
	}
	
	public static StringBuffer changesToString(final ChangedObject changedObject) {
		StringBuffer buf = new StringBuffer();
		for(SimpleField field : changedObject.getNewFields()) {
			buf.append("--- ADDED Field '" + field.getID() + "' ---<br/>\n");
			buf.append(DumpUtils.toStringBuffer(field));
		}
		for(XID objectId : changedObject.getRemovedFields()) {
			buf.append("--- REMOVED Field '" + objectId + "' ---<br/>\n");
		}
		for(ChangedField changedField : changedObject.getChangedFields()) {
			if(changedField.isChanged()) {
				buf.append("--- CHANGED Field '" + changedField.getID() + "' ---<br/>\n");
				buf.append("'" + changedField.getOldValue() + "' ==> '" + changedField.getValue()
				        + "' <br/>\n");
			}
		}
		return buf;
	}
	
}
