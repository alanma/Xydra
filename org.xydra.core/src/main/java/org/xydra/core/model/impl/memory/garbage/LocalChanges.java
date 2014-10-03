package org.xydra.core.model.impl.memory.garbage;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;


/**
 * @author xamde
 */
public class LocalChanges implements XLocalChanges {
	
	private List<LocalChange> list;
	
	public LocalChanges(List<LocalChange> list) {
		this.list = list;
	}
	
	@Override
	public List<LocalChange> getList() {
		return this.list;
	}
	
	@Override
	public void clear() {
		this.list.clear();
	}
	
	@Override
	public void append(XCommand command, XEvent event) {
		LocalChange lc = new LocalChange(command, event);
		this.list.add(lc);
	}
	
	public static LocalChanges create() {
		return new LocalChanges(new ArrayList<LocalChange>());
	}
	
	@Override
	public int countUnappliedLocalChanges() {
		return this.list.size();
	}
	
	@Override
	public void setSyncRevision(long syncRevision) {
		this.syncRevision = syncRevision;
	}
	
	private long syncRevision;
	
	@Override
	public long getSynchronizedRevision() {
		return this.syncRevision;
	}
	
}
