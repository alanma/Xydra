package org.xydra.core.model.delta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.base.XAddress;
import org.xydra.base.XId;

/**
 * State: {@link Change}; Map: objectId -> {@link SummaryObject}
 *
 * @author xamde
 */
public class SummaryModel extends SummaryEntity {

	private final Map<XId, SummaryObject> id2summaryObject = new HashMap<XId, SummaryObject>();

	private final XAddress modelAddress;

	public SummaryModel(final XAddress modelAddress) {
		super(modelAddress.getModel());
		this.modelAddress = modelAddress;
	}

	public SummaryObject createOrGet(final XId objectId) {
		SummaryObject summaryObject = this.id2summaryObject.get(objectId);
		if (summaryObject == null) {
			summaryObject = new SummaryObject(objectId);
			this.id2summaryObject.put(objectId, summaryObject);
		}
		return summaryObject;
	}

	public XAddress getAddress() {
		return this.modelAddress;
	}

	/**
	 * @return all summaryObjects, including those that have only been changed
	 *         (i.e. have children that changed)
	 */
	public Iterator<Entry<XId, SummaryObject>> getChildren() {
		return this.id2summaryObject.entrySet().iterator();
	}

	@Override
	public String toString() {
		return toString("").toString();
	}

	public StringBuilder toString(final String indent) {
		final StringBuilder b = new StringBuilder();
		b.append(indent + "Model." + this.change + " '" + getId() + "'\n");
		// IMPROVE sort, if possible
		for (final Entry<XId, SummaryObject> e : this.id2summaryObject.entrySet()) {
			b.append(e.getValue().toString(indent + "  ").toString());
		}
		return b;
	}

	public void removeNoOps() {
		final List<XId> toBeRemoved = new ArrayList<XId>();
		for (final SummaryObject so : this.id2summaryObject.values()) {
			if (so.isNoOp()) {
				toBeRemoved.add(so.getId());
			} else {
				so.removeNoOps();
			}
		}
		for (final XId id : toBeRemoved) {
			this.id2summaryObject.remove(id);
		}
	}

}