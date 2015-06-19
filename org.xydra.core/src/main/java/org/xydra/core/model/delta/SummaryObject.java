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

	private Map<XId, SummaryField> id2summaryField = new HashMap<XId, SummaryField>();

	public SummaryObject(XId id) {
		super(id);
	}

	public SummaryField createOrGet(XId fieldId) {
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
	public XValue getFieldValue(XReadableObject xo, XId fieldId, boolean remove) {
		SummaryField f = this.id2summaryField.get(fieldId);
		if (f != null) {
			return remove ? f.getOldValue() : f.getNewValue();
		}

		if (xo == null)
			return null;

		XReadableField xf = xo.getField(fieldId);
		if (xf == null)
			return null;

		return xf.getValue();
	}

	/**
	 * @param fieldId
	 * @return @CanBeNull
	 */
	public SummaryField getSummaryField(XId fieldId) {
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

	public StringBuilder toString(String indent) {
		StringBuilder b = new StringBuilder();
		b.append(indent + "Object." + this.change + " '" + getId() + "'\n");
		// IMPROVE sort, if possible
		for (Entry<XId, SummaryField> e : this.id2summaryField.entrySet()) {
			b.append(e.getValue().toString(indent + "  ").toString());
		}
		b.append(indent + "Events: " + super.appliedEvents + "\n");
		return b;
	}

	public boolean isNoOp() {
		for (SummaryField sf : this.id2summaryField.values()) {
			if (!sf.isNoOp())
				return false;
		}
		return true;
	}

	public void removeNoOps() {
		List<XId> toBeRemoved = new ArrayList<XId>();
		for (SummaryField sf : this.id2summaryField.values()) {
			if (sf.isNoOp()) {
				toBeRemoved.add(sf.getId());
			}
		}
		for (XId id : toBeRemoved) {
			this.id2summaryField.remove(id);
		}
	}

}