package org.xydra.perf;

import java.util.List;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.minio.MiniIOException;
import org.xydra.base.minio.MiniStringWriter;
import org.xydra.base.minio.MiniWriter;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.perf.Stats.Clock;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;


/**
 * A wrapper around a {@link XydraPersistence} that monitors statistics on call
 * number and time spent.
 * 
 * IMPROVE Profile also tentative methods
 * 
 * @author xamde
 */
@RunsInGWT(true)
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
	public long executeCommand(XId actorId, XCommand command) {
		Clock c = this.stats.startClock("executeCommand");
		long result = this.basePersistence.executeCommand(actorId, command);
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
	public Set<XId> getManagedModelIds() {
		Clock c = this.stats.startClock("getModelIds");
		Set<XId> result = this.basePersistence.getManagedModelIds();
		c.stop();
		return result;
	}
	
	@Override
	public ModelRevision getModelRevision(GetWithAddressRequest addressRequest) {
		Clock c = this.stats.startClock("getModelRevision");
		ModelRevision result = this.basePersistence.getModelRevision(addressRequest);
		c.stop();
		return result;
	}
	
	@Override
	public XWritableModel getModelSnapshot(GetWithAddressRequest addressRequest) {
		Clock c = this.stats.startClock("getModelSnapshot");
		XWritableModel result = this.basePersistence.getModelSnapshot(addressRequest);
		c.stop();
		return result;
	}
	
	@Override
	public XWritableObject getObjectSnapshot(GetWithAddressRequest addressRequest) {
		Clock c = this.stats.startClock("getObjectSnapshot");
		XWritableObject result = this.basePersistence.getObjectSnapshot(addressRequest);
		c.stop();
		return result;
	}
	
	@Override
	public XId getRepositoryId() {
		Clock c = this.stats.startClock("getRepositoryId");
		XId result = this.basePersistence.getRepositoryId();
		c.stop();
		return result;
	}
	
	@Override
	public boolean hasManagedModel(XId modelId) {
		Clock c = this.stats.startClock("hasModel");
		boolean result = this.basePersistence.hasManagedModel(modelId);
		c.stop();
		return result;
	}
	
	public void dumpStats() {
		MiniStringWriter sw = new MiniStringWriter();
		try {
			writeStats(sw);
		} catch(MiniIOException e) {
			throw new RuntimeException(e);
		}
		log.info(sw.toString());
	}
	
	public void writeStats(MiniWriter w) throws MiniIOException {
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
