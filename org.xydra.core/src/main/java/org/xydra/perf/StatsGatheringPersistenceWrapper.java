package org.xydra.perf;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.perf.Stats.Clock;
import org.xydra.store.RevisionState;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * A wrapper around a {@link XydraPersistence} that monitors statistics on call
 * number and time spent.
 * 
 * @author xamde
 */
public class StatsGatheringPersistenceWrapper implements XydraPersistence {
	
	private static final Logger log = LoggerFactory
	        .getLogger(StatsGatheringPersistenceWrapper.class);
	
	private XydraPersistence basePersistence;
	
	public static StatsGatheringPersistenceWrapper INSTANCE = null;
	
	public static boolean isEnabled() {
		return INSTANCE != null;
	}
	
	private Stats stats = new Stats();
	
	@Override
	public void clear() {
		Clock c = this.stats.startClock("clear");
		this.basePersistence.clear();
		c.stop();
	}
	
	@Override
	public RevisionState executeCommand(XID actorId, XCommand command) {
		Clock c = this.stats.startClock("executeCommand");
		RevisionState result = this.basePersistence.executeCommand(actorId, command);
		c.stop();
		return result;
	}
	
	@Override
	public List<XEvent> getEvents(XAddress address, long beginRevision, long endRevision) {
		Clock c = this.stats.startClock("getEvents");
		List<XEvent> result = this.basePersistence.getEvents(address, beginRevision, endRevision);
		c.stop();
		return result;
	}
	
	@Override
	public Set<XID> getModelIds() {
		Clock c = this.stats.startClock("getModelIds");
		Set<XID> result = this.basePersistence.getModelIds();
		c.stop();
		return result;
	}
	
	@Override
	public RevisionState getModelRevision(XAddress address) {
		Clock c = this.stats.startClock("getModelRevision");
		RevisionState result = this.basePersistence.getModelRevision(address);
		c.stop();
		return result;
	}
	
	@Override
	public XWritableModel getModelSnapshot(XAddress address) {
		Clock c = this.stats.startClock("getModelSnapshot");
		XWritableModel result = this.basePersistence.getModelSnapshot(address);
		c.stop();
		return result;
	}
	
	@Override
	public XWritableObject getObjectSnapshot(XAddress address) {
		Clock c = this.stats.startClock("getObjectSnapshot");
		XWritableObject result = this.basePersistence.getObjectSnapshot(address);
		c.stop();
		return result;
	}
	
	@Override
	public XID getRepositoryId() {
		Clock c = this.stats.startClock("getRepositoryId");
		XID result = this.basePersistence.getRepositoryId();
		c.stop();
		return result;
	}
	
	@Override
	public boolean hasModel(XID modelId) {
		Clock c = this.stats.startClock("hasModel");
		boolean result = this.basePersistence.hasModel(modelId);
		c.stop();
		return result;
	}
	
	public void dumpStats() {
		StringWriter sw = new StringWriter();
		try {
			writeStats(sw);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		log.info(sw.getBuffer().toString());
	}
	
	public void writeStats(Writer w) throws IOException {
		w.write("XydraPersistence stats ----------\n");
		this.stats.writeStats(w);
	}
	
	/**
	 * @param persistence a {@link XydraPersistence}
	 */
	public StatsGatheringPersistenceWrapper(XydraPersistence persistence) {
		super();
		this.basePersistence = persistence;
		// hack to have first instance also available statically for easy
		// dumping
		if(INSTANCE == null) {
			INSTANCE = this;
		} else {
			log.warn("Create new stats-instance that is not availalbe statically");
		}
	}
	
	public static void staticDumpStats() {
		if(INSTANCE == null) {
			log.warn("No StatsPersistence has been created");
		} else {
			INSTANCE.dumpStats();
		}
	}
	
	public XydraPersistence getBasePersistence() {
		return this.basePersistence;
	}
	
}
