package org.xydra.base.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XStateReadableField;
import org.xydra.base.rmof.XStateReadableModel;
import org.xydra.base.rmof.XStateReadableObject;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

public class DumpUtilsBase {

	private static final Logger log = LoggerFactory.getLogger(DumpUtilsBase.class);

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
		for (XId modelId : repo) {
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
	public static StringBuffer toStringBuffer(XStateReadableModel model) {
		XyAssert.xyAssert(model != null);
		assert model != null;
		XyAssert.xyAssert(model.getAddress().getAddressedType() == XType.XMODEL);

		StringBuffer buf = new StringBuffer();
		buf.append("** Model   " + model.getAddress() + "\n");
		List<XId> ids = toSortedList(model);
		for (XId objectId : ids) {
			XStateReadableObject object = model.getObject(objectId);
			buf.append(toStringBuffer(object));
		}
		return buf;
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
		List<XId> ids = toSortedList(model);
		for (XId objectId : ids) {
			XReadableObject object = model.getObject(objectId);
			buf.append(toStringBuffer(object));
		}
		return buf;
	}

	private static List<XId> toSortedList(Iterable<XId> iterable) {
		List<XId> list = new ArrayList<XId>();
		for (XId id : iterable) {
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
		List<XId> ids = toSortedList(object);
		for (XId fieldId : ids) {
			XReadableField field = object.getField(fieldId);
			buf.append(toStringBuffer(field));
		}
		return buf;
	}

	/**
	 * @param object to be dumped
	 * @return given object as human-readable string
	 */
	public static StringBuffer toStringBuffer(XStateReadableObject object) {
		XyAssert.xyAssert(object != null);
		assert object != null;
		XyAssert.xyAssert(object.getAddress().getAddressedType() == XType.XOBJECT);
		StringBuffer buf = new StringBuffer();
		buf.append("*** Object " + object.getAddress() + "\n");
		List<XId> ids = toSortedList(object);
		for (XId fieldId : ids) {
			XStateReadableField field = object.getField(fieldId);
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
		buf.append("**** Field " + field.getAddress() + " = '" + field.getValue() + "' X-type=" +

		(field.getValue() == null ? "NoValue" : field.getValue().getType())

		+ " [" + field.getRevisionNumber() + "]\n");
		return buf;
	}

	/**
	 * @param field to be dumped
	 * @return the field as a human-readable String
	 */
	public static StringBuffer toStringBuffer(XStateReadableField field) {
		XyAssert.xyAssert(field != null);
		assert field != null;
		XyAssert.xyAssert(field.getAddress().getAddressedType() == XType.XFIELD);
		StringBuffer buf = new StringBuffer();
		buf.append("**** Field " + field.getAddress() + " = '" + field.getValue() + "' " + "\n");
		return buf;
	}

	public static class XidComparator implements Comparator<IHasXId> {

		@Override
		public int compare(IHasXId a, IHasXId b) {
			return a.getId().compareTo(b.getId());
		}

		public static XidComparator INSTANCE = new XidComparator();

	}

	public static void dump(Map<?, ?> map) {
		// IMPROVE sort, if possible
		for (Entry<?, ?> e : map.entrySet()) {
			log.info("Key '" + e.getKey() + "' = Value '" + e.getValue() + "'");
		}
	}

	public static StringBuilder toStringBuilder(Map<?, ?> map) {
		StringBuilder b = new StringBuilder();
		// IMPROVE sort, if possible
		for (Entry<?, ?> e : map.entrySet()) {
			b.append("Key '");
			b.append(e.getKey());
			b.append("' = Value '");
			b.append(e.getValue());
			b.append("'\n");
		}
		return b;
	}

}
