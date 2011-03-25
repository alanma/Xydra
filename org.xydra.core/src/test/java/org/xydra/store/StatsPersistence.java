package org.xydra.store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * A wrapper around a {@link XydraPersistence} that monitors statistics on call
 * number and time spent.
 * 
 * @author xamde
 */
public class StatsPersistence implements XydraPersistence {
	
	private static final Logger log = LoggerFactory.getLogger(StatsPersistence.class);
	
	private XydraPersistence persistence;
	
	public Map<String,Data> stats = new HashMap<String,StatsPersistence.Data>();
	
	public static StatsPersistence INSTANCE = null;
	
	private static class Data {
		public long count = 0;
		public long duration = 0;
	}
	
	private class Clock {
		private long start;
		private String name;
		
		public Clock(String name) {
			this.name = name;
			this.start = System.nanoTime();
		}
		
		public void stop() {
			long duration = System.nanoTime() - this.start;
			Data data = StatsPersistence.this.stats.get(this.name);
			if(data == null) {
				data = new Data();
				StatsPersistence.this.stats.put(this.name, data);
			}
			data.count++;
			data.duration += duration;
		}
	}
	
	public void clear() {
		Clock c = new Clock("clear");
		this.persistence.clear();
		c.stop();
	}
	
	public long executeCommand(XID actorId, XCommand command) {
		Clock c = new Clock("executeCommand");
		long result = this.persistence.executeCommand(actorId, command);
		c.stop();
		return result;
	}
	
	public List<XEvent> getEvents(XAddress address, long beginRevision, long endRevision) {
		Clock c = new Clock("getEvents");
		List<XEvent> result = this.persistence.getEvents(address, beginRevision, endRevision);
		c.stop();
		return result;
	}
	
	public Set<XID> getModelIds() {
		Clock c = new Clock("getModelIds");
		Set<XID> result = this.persistence.getModelIds();
		c.stop();
		return result;
	}
	
	public long getModelRevision(XAddress address) {
		Clock c = new Clock("getModelRevision");
		long result = this.persistence.getModelRevision(address);
		c.stop();
		return result;
	}
	
	public XWritableModel getModelSnapshot(XAddress address) {
		Clock c = new Clock("getModelSnapshot");
		XWritableModel result = this.persistence.getModelSnapshot(address);
		c.stop();
		return result;
	}
	
	public XWritableObject getObjectSnapshot(XAddress address) {
		Clock c = new Clock("getObjectSnapshot");
		XWritableObject result = this.persistence.getObjectSnapshot(address);
		c.stop();
		return result;
	}
	
	public XID getRepositoryId() {
		Clock c = new Clock("getRepositoryId");
		XID result = this.persistence.getRepositoryId();
		c.stop();
		return result;
	}
	
	public boolean hasModel(XID modelId) {
		Clock c = new Clock("hasModel");
		boolean result = this.persistence.hasModel(modelId);
		c.stop();
		return result;
	}
	
	public void dumpStats() {
		StringBuffer buf = new StringBuffer();
		buf.append("XydraPersistence stats ----------\n");
		for(String name : this.stats.keySet()) {
			Data d = this.stats.get(name);
			long nsPerCall = d.count > 0 ? d.duration / d.count : -1;
			buf.append("  " + name + " called " + d.count + " times. Total: " + (d.duration / 1000)
			        + " micros = " + (d.duration / 1000000) + " ms. Per call: "
			        + (nsPerCall / 1000) + " micros = " + (nsPerCall / 1000000) + " ms\n");
		}
		log.info(buf.toString());
	}
	
	/**
	 * @param persistence a {@link XydraPersistence}
	 */
	public StatsPersistence(XydraPersistence persistence) {
		super();
		this.persistence = persistence;
		// hack to have first instance available statically
		if(INSTANCE == null) {
			INSTANCE = this;
		}
	}
	
}
