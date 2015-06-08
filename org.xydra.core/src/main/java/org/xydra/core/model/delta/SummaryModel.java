package org.xydra.core.model.delta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.core.util.DumpUtils;

public class SummaryModel extends SummaryEntity {

	private Map<XId, SummaryObject> map = new HashMap<XId, SummaryObject>();

	private XAddress modelAddress;

	public SummaryModel(XAddress modelAddress) {
		this.modelAddress = modelAddress;
	}

	public SummaryObject createOrGet(XId object) {
		SummaryObject so = this.map.get(object);
		if (so == null) {
			so = new SummaryObject();
			this.map.put(object, so);
		}
		return so;
	}

	/**
	 * @return all summaryObjects, including those that have only been
	 *         changed (i.e. have children that changed)
	 */
	public Iterator<Entry<XId, SummaryObject>> getChildren() {
		return this.map.entrySet().iterator();
	}

	public XAddress getAddress() {
		return this.modelAddress;
	}

	@Override
	public String toString() {
		return "M-" + this.change + "\n" + DumpUtils.toStringBuilder(this.map);
	}

}