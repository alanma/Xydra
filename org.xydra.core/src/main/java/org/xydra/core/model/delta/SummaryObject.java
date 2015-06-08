package org.xydra.core.model.delta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.xydra.base.XId;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XObject;
import org.xydra.core.util.DumpUtils;

public class SummaryObject extends SummaryEntity {
	private Map<XId, SummaryField> id2summaryField = new HashMap<XId, SummaryField>();

	public SummaryField createOrGet(XId field) {
		SummaryField sf = this.id2summaryField.get(field);
		if (sf == null) {
			sf = new SummaryField();
			this.id2summaryField.put(field, sf);
		}
		return sf;
	}

	Iterator<Entry<XId, SummaryField>> getChildren() {
		return this.id2summaryField.entrySet().iterator();
	}

	/**
	 * @param xo
	 * @param fieldId
	 * @param remove
	 * @return
	 */
	public XValue getFieldValue(XObject xo, XId fieldId, boolean remove) {
		SummaryField f = this.id2summaryField.get(fieldId);
		if (f != null && f.summaryValue != null) {
			return remove ? f.summaryValue.oldValue : f.summaryValue.newValue;
		}
		if (xo != null) {
			return xo.getFieldValue(fieldId);
		}
		return null;
	}

	@Override
	public String toString() {
		return "O-" + this.change + "\n" + DumpUtils.toStringBuilder(this.id2summaryField);
	}

	public AtomicChangeType getAtomichChangeType() {
		return this.change.getAtomicChangeType();
	}

	public SummaryField getSummaryField(XId fieldId) {
		return this.id2summaryField.get(fieldId);
	}

	public Set<Entry<XId, SummaryField>> getSummaryFields() {
		return this.id2summaryField.entrySet();
	}

}