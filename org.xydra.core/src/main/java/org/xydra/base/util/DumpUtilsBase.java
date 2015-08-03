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
	public static String dump(final String label, final XReadableRepository repo) {
		XyAssert.xyAssert(repo != null);
		assert repo != null;
		XyAssert.xyAssert(repo.getAddress().getAddressedType() == XType.XREPOSITORY);
		log.info(label + " * Repo " + repo.getId() + " ...");
		for (final XId modelId : repo) {
			final XReadableModel model = repo.getModel(modelId);
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
	public static String dump(final String label, final XReadableModel model) {
		log.info(label + "\n" + toStringBuffer(model));
		return "";
	}

	/**
	 * @param model to be dumped to a String
	 * @return the model as a human-readable String
	 */
	public static StringBuffer toStringBuffer(final XStateReadableModel model) {
		XyAssert.xyAssert(model != null);
		assert model != null;
		XyAssert.xyAssert(model.getAddress().getAddressedType() == XType.XMODEL);

		final StringBuffer buf = new StringBuffer();
		buf.append("** Model   " + model.getAddress() + "\n");
		final List<XId> ids = toSortedList(model);
		for (final XId objectId : ids) {
			final XStateReadableObject object = model.getObject(objectId);
			buf.append(toStringBuffer(object));
		}
		return buf;
	}

	/**
	 * @param model to be dumped to a String
	 * @return the model as a human-readable String
	 */
	public static StringBuffer toStringBuffer(final XReadableModel model) {
		XyAssert.xyAssert(model != null);
		assert model != null;
		XyAssert.xyAssert(model.getAddress().getAddressedType() == XType.XMODEL);

		final StringBuffer buf = new StringBuffer();
		buf.append("** Model   " + model.getAddress() + " [" + model.getRevisionNumber() + "]\n");
		final List<XId> ids = toSortedList(model);
		for (final XId objectId : ids) {
			final XReadableObject object = model.getObject(objectId);
			buf.append(toStringBuffer(object));
		}
		return buf;
	}

	private static List<XId> toSortedList(final Iterable<XId> iterable) {
		final List<XId> list = new ArrayList<XId>();
		for (final XId id : iterable) {
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
	public static String dump(final String label, final XReadableObject object) {
		log.info(label + "\n" + toStringBuffer(object));
		return "";
	}

	/**
	 * @param object to be dumped
	 * @return given object as human-readable string
	 */
	public static StringBuffer toStringBuffer(final XReadableObject object) {
		XyAssert.xyAssert(object != null);
		assert object != null;
		XyAssert.xyAssert(object.getAddress().getAddressedType() == XType.XOBJECT);
		final StringBuffer buf = new StringBuffer();
		buf.append("*** Object " + object.getAddress() + " [" + object.getRevisionNumber() + "]\n");
		final List<XId> ids = toSortedList(object);
		for (final XId fieldId : ids) {
			final XReadableField field = object.getField(fieldId);
			buf.append(toStringBuffer(field));
		}
		return buf;
	}

	/**
	 * @param object to be dumped
	 * @return given object as human-readable string
	 */
	public static StringBuffer toStringBuffer(final XStateReadableObject object) {
		XyAssert.xyAssert(object != null);
		assert object != null;
		XyAssert.xyAssert(object.getAddress().getAddressedType() == XType.XOBJECT);
		final StringBuffer buf = new StringBuffer();
		buf.append("*** Object " + object.getAddress() + "\n");
		final List<XId> ids = toSortedList(object);
		for (final XId fieldId : ids) {
			final XStateReadableField field = object.getField(fieldId);
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
	public static String dump(final String label, final XReadableField field) {
		log.info(label + "\n" + toStringBuffer(field));
		return "";
	}

	/**
	 * @param field to be dumped
	 * @return the field as a human-readable String
	 */
	public static StringBuffer toStringBuffer(final XReadableField field) {
		XyAssert.xyAssert(field != null);
		assert field != null;
		XyAssert.xyAssert(field.getAddress().getAddressedType() == XType.XFIELD);
		final StringBuffer buf = new StringBuffer();
		buf.append("**** Field " + field.getAddress() + " = '" + field.getValue() + "' X-type=" +

		(field.getValue() == null ? "NoValue" : field.getValue().getType())

		+ " [" + field.getRevisionNumber() + "]\n");
		return buf;
	}

	/**
	 * @param field to be dumped
	 * @return the field as a human-readable String
	 */
	public static StringBuffer toStringBuffer(final XStateReadableField field) {
		XyAssert.xyAssert(field != null);
		assert field != null;
		XyAssert.xyAssert(field.getAddress().getAddressedType() == XType.XFIELD);
		final StringBuffer buf = new StringBuffer();
		buf.append("**** Field " + field.getAddress() + " = '" + field.getValue() + "' " + "\n");
		return buf;
	}

	public static class XidComparator implements Comparator<IHasXId> {

		@Override
		public int compare(final IHasXId a, final IHasXId b) {
			return a.getId().compareTo(b.getId());
		}

		public static XidComparator INSTANCE = new XidComparator();

	}

	public static void dump(final Map<?, ?> map) {
		// IMPROVE sort, if possible
		for (final Entry<?, ?> e : map.entrySet()) {
			log.info("Key '" + e.getKey() + "' = Value '" + e.getValue() + "'");
		}
	}

	public static StringBuilder toStringBuilder(final Map<?, ?> map) {
		final StringBuilder b = new StringBuilder();
		// IMPROVE sort, if possible
		for (final Entry<?, ?> e : map.entrySet()) {
			b.append("Key '");
			b.append(e.getKey());
			b.append("' = Value '");
			b.append(e.getValue());
			b.append("'\n");
		}
		return b;
	}

}
