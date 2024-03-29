package org.xydra.core.model.delta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;

/**
 * State: {@link Change}; Map: fieldId -> {@link SummaryField}
 *
 * @author xamde
 */
public class SummaryObject extends SummaryEntity {

	private final Map<XId, SummaryField> id2summaryField = new HashMap<XId, SummaryField>();

	public SummaryObject(final XId id) {
		super(id);
	}

	public SummaryField createOrGet(final XId fieldId) {
		SummaryField sf = this.id2summaryField.get(fieldId);
		if (sf == null) {
			sf = new SummaryField(fieldId);
			this.id2summaryField.put(fieldId, sf);
		}
		return sf;
	}

	/**
	 * @return
	 */
	public Iterator<Entry<XId, SummaryField>> getChildren() {
		return this.id2summaryField.entrySet().iterator();
	}

	/**
	 * @param xo @CanBeNull
	 * @param fieldId
	 * @param remove
	 * @return
	 */
	public XValue getFieldValue(final XReadableObject xo, final XId fieldId, final boolean remove) {
		final SummaryField f = this.id2summaryField.get(fieldId);
		if (f != null) {
			return remove ? f.getOldValue() : f.getNewValue();
		}

		if (xo == null) {
			return null;
		}

		final XReadableField xf = xo.getField(fieldId);
		if (xf == null) {
			return null;
		}

		return xf.getValue();
	}

	/**
	 * @param fieldId
	 * @return @CanBeNull
	 */
	public SummaryField getSummaryField(final XId fieldId) {
		return this.id2summaryField.get(fieldId);
	}

	/**
	 * @return @NeverNull
	 */
	public Set<Entry<XId, SummaryField>> getSummaryFields() {
		return this.id2summaryField.entrySet();
	}

	@Override
	public String toString() {
		return toString("").toString();
	}

	public StringBuilder toString(final String indent) {
		final StringBuilder b = new StringBuilder();
		b.append(indent + "Object." + this.change + " '" + getId() + "'\n");
		// IMPROVE sort, if possible
		for (final Entry<XId, SummaryField> e : this.id2summaryField.entrySet()) {
			b.append(e.getValue().toString(indent + "  ").toString());
		}
		b.append(indent + "Events: " + super.appliedEvents + "\n");
		return b;
	}

	public boolean isNoOp() {
		for (final SummaryField sf : this.id2summaryField.values()) {
			if (!sf.isNoOp()) {
				return false;
			}
		}
		return true;
	}

	public void removeNoOps() {
		final List<XId> toBeRemoved = new ArrayList<XId>();
		for (final SummaryField sf : this.id2summaryField.values()) {
			if (sf.isNoOp()) {
				toBeRemoved.add(sf.getId());
			}
		}
		for (final XId id : toBeRemoved) {
			this.id2summaryField.remove(id);
		}
	}

}