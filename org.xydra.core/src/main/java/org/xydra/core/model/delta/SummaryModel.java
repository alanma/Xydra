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

	private Map<XId, SummaryObject> id2summaryObject = new HashMap<XId, SummaryObject>();

	private XAddress modelAddress;

	public SummaryModel(XAddress modelAddress) {
		super(modelAddress.getModel());
		this.modelAddress = modelAddress;
	}

	public SummaryObject createOrGet(XId objectId) {
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

	public StringBuilder toString(String indent) {
		StringBuilder b = new StringBuilder();
		b.append(indent + "Model." + this.change + " '" + getId() + "'\n");
		// IMPROVE sort, if possible
		for (Entry<XId, SummaryObject> e : this.id2summaryObject.entrySet()) {
			b.append(e.getValue().toString(indent + "  ").toString());
		}
		return b;
	}

	public void removeNoOps() {
		List<XId> toBeRemoved = new ArrayList<XId>();
		for (SummaryObject so : this.id2summaryObject.values()) {
			if (so.isNoOp()) {
				toBeRemoved.add(so.getId());
			} else {
				so.removeNoOps();
			}
		}
		for (XId id : toBeRemoved) {
			this.id2summaryObject.remove(id);
		}
	}

}