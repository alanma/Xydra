package org.xydra.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.util.DumpUtilsBase;
import org.xydra.core.model.delta.IFieldDiff;
import org.xydra.core.model.delta.IModelDiff;
import org.xydra.core.model.delta.IObjectDiff;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * @author xamde
 *
 */
public class DumpUtils extends DumpUtilsBase {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DumpUtils.class);

	public static StringBuilder changesToString(final IModelDiff changedModel) {
		final StringBuilder sb = new StringBuilder();
		final List<XReadableObject> addedList = new ArrayList<XReadableObject>(changedModel.getAdded());
		Collections.sort(addedList, XidComparator.INSTANCE);
		for (final XReadableObject addedObject : addedList) {
			sb.append("=== ADDED   Object '" + addedObject.getId() + "' ===<br/>\n");
			sb.append(DumpUtilsBase.toStringBuffer(addedObject).toString());
		}
		final List<XId> removedList = new ArrayList<XId>(changedModel.getRemoved());
		Collections.sort(removedList, XidComparator.INSTANCE);
		for (final XId removedObjectId : removedList) {
			sb.append("=== REMOVED Object '" + removedObjectId + "' ===<br/>\n");
		}
		final List<IObjectDiff> potentiallyChangedList = new ArrayList<IObjectDiff>(
				changedModel.getPotentiallyChanged());
		Collections.sort(potentiallyChangedList, XidComparator.INSTANCE);
		for (final IObjectDiff changedObject : potentiallyChangedList) {
			if (changedObject.hasChanges()) {
				sb.append("=== CHANGED Object '" + changedObject.getId() + "' === <br/>\n");
				sb.append(changesToString(changedObject).toString());
			}
		}
		return sb;
	}

	public static StringBuilder changesToString(final IObjectDiff changedObject) {
		final StringBuilder sb = new StringBuilder();
		final List<XReadableField> addedList = new ArrayList<XReadableField>(changedObject.getAdded());
		Collections.sort(addedList, XidComparator.INSTANCE);
		for (final XReadableField field : addedList) {
			sb.append("--- ADDED Field '" + field.getId() + "' ---<br/>\n");
			sb.append(DumpUtilsBase.toStringBuffer(field));
		}
		final List<XId> removedList = new ArrayList<XId>(changedObject.getRemoved());
		Collections.sort(removedList, XidComparator.INSTANCE);
		for (final XId objectId : changedObject.getRemoved()) {
			sb.append("--- REMOVED Field '" + objectId + "' ---<br/>\n");
		}
		final List<IFieldDiff> potentiallyChangedList = new ArrayList<IFieldDiff>(
				changedObject.getPotentiallyChanged());
		Collections.sort(potentiallyChangedList, XidComparator.INSTANCE);
		for (final IFieldDiff changedField : potentiallyChangedList) {
			if (changedField.isChanged()) {
				sb.append("--- CHANGED Field '" + changedField.getId() + "' ---<br/>\n");
				sb.append(changesToString(changedField).toString());
			}
		}
		return sb;
	}

	public static StringBuilder changesToString(final IFieldDiff changedField) {
		final StringBuilder sb = new StringBuilder();
		sb.append("'" + changedField.getInitialValue() + "' ==> '" + changedField.getValue()
				+ "' <br/>\n");
		return sb;
	}

}
