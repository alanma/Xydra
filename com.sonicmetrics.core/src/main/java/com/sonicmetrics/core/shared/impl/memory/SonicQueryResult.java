package com.sonicmetrics.core.shared.impl.memory;

import java.util.Iterator;

import com.sonicmetrics.core.shared.ISonicEvent;
import com.sonicmetrics.core.shared.query.ISonicQueryResult;

public class SonicQueryResult implements ISonicQueryResult {

	public SonicQueryResult(Iterator<ISonicEvent> eventIterator) {
		super();
		this.eventIterator = eventIterator;
	}

	private Iterator<ISonicEvent> eventIterator;

	@Override
	public Iterator<ISonicEvent> iterator() {
		return this.eventIterator;
	}

}
