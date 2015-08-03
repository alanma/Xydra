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

	private final XydraPersistence basePersistence;

	public static StatsGatheringPersistenceWrapper INSTANCE = null;

	public static boolean isEnabled() {
		return INSTANCE != null;
	}

	private final Stats stats = new Stats();

	@Override
	public void clear() {
		final Clock c = this.stats.startClock("clear");
		this.basePersistence.clear();
		c.stop();
	}

	@Override
	public long executeCommand(final XId actorId, final XCommand command) {
		final Clock c = this.stats.startClock("executeCommand");
		final long result = this.basePersistence.executeCommand(actorId, command);
		c.stop();
		return result;
	}

	@Override
	public List<XEvent> getEvents(final XAddress address, final long beginRevision, final long endRevision) {
		final Clock c = this.stats.startClock("getEvents");
		final List<XEvent> result = this.basePersistence.getEvents(address, beginRevision, endRevision);
		c.stop();
		return result;
	}

	@Override
	public Set<XId> getManagedModelIds() {
		final Clock c = this.stats.startClock("getModelIds");
		final Set<XId> result = this.basePersistence.getManagedModelIds();
		c.stop();
		return result;
	}

	@Override
	public ModelRevision getModelRevision(final GetWithAddressRequest addressRequest) {
		final Clock c = this.stats.startClock("getModelRevision");
		final ModelRevision result = this.basePersistence.getModelRevision(addressRequest);
		c.stop();
		return result;
	}

	@Override
	public XWritableModel getModelSnapshot(final GetWithAddressRequest addressRequest) {
		final Clock c = this.stats.startClock("getModelSnapshot");
		final XWritableModel result = this.basePersistence.getModelSnapshot(addressRequest);
		c.stop();
		return result;
	}

	@Override
	public XWritableObject getObjectSnapshot(final GetWithAddressRequest addressRequest) {
		final Clock c = this.stats.startClock("getObjectSnapshot");
		final XWritableObject result = this.basePersistence.getObjectSnapshot(addressRequest);
		c.stop();
		return result;
	}

	@Override
	public XId getRepositoryId() {
		final Clock c = this.stats.startClock("getRepositoryId");
		final XId result = this.basePersistence.getRepositoryId();
		c.stop();
		return result;
	}

	@Override
	public boolean hasManagedModel(final XId modelId) {
		final Clock c = this.stats.startClock("hasModel");
		final boolean result = this.basePersistence.hasManagedModel(modelId);
		c.stop();
		return result;
	}

	public void dumpStats() {
		final MiniStringWriter sw = new MiniStringWriter();
		try {
			writeStats(sw);
		} catch(final MiniIOException e) {
			throw new RuntimeException(e);
		}
		log.info(sw.toString());
	}

	public void writeStats(final MiniWriter w) throws MiniIOException {
		w.write("XydraPersistence stats ----------\n");
		this.stats.writeStats(w);
	}

	/**
	 * @param persistence a {@link XydraPersistence}
	 */
	public StatsGatheringPersistenceWrapper(final XydraPersistence persistence) {
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
