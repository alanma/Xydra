package org.xydra.store.access.impl.delegate;

import java.util.List;

import org.xydra.base.XId;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.NamingUtils;
import org.xydra.store.access.XAuthenticationDatabase;
import org.xydra.store.access.XGroupDatabase;
import org.xydra.store.access.impl.memory.CachingOrMemoryAuthenticationDatabase;
import org.xydra.store.access.impl.memory.DelegatingAccessControlManager;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;
import org.xydra.store.access.impl.memory.MemoryGroupDatabase;
import org.xydra.store.rmof.impl.delegate.WritableModelOnPersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


/**
 * A minimal out of date overview on the implementation strategy of this class
 * is this:
 *
 * <pre>
 * AccessControlManagerOnPersistence
 * -> CachingOrMemoryAuthenticationDatabase
 *    -> AuthenticationDatabaseOnHalfWritableModel
 *       -> HalfWritableModelOnPersistence
 *          -> XydraPersistence
 * -> SyncingHookAuthorisationManagerAndDb
 *    -> MemoryAuthorisationManager (as container for groupDB)
 *       -> SyncingHookGroupDatabase
 *          -> MemoryGroupDatabase
 *          -> PartialGroupDatabaseOnHalfWritableModel
 *             -> HalfWritableModelOnPersistence
 *                -> XydraPersistence
 *          -> HalfWritableModelOnPersistence
 *             -> XydraPersistence
 *          [x] load rights
 *          [X] user changes -> persistence
 *          [x] persistence changed -> in memory
 *    -> MemoryAuthorisationManager (as authorisationDB)
 *    -> PartialAuthorisationDatabaseOnHalfWritableRepository
 *       -> HalfWritableRepositoryOnPersistence
 *    [x] load rights
 *    [x] user changes -> persistence
 *    [x] persistence changed -> in memory
 * </pre>
 *
 * @author xamde
 *
 */
public class AccessControlManagerOnPersistence extends DelegatingAccessControlManager {

	@SuppressWarnings("unused")
	private class SyncingHookAuthorisationManagerAndDb extends HookAuthorisationManagerAndDb {

		private final WritableRepositoryOnPersistence authorisationRepositoryOnPersistence;

		private final PartialAuthorisationDatabaseOnWritableRepository partialAuthorisationDb;

		public SyncingHookAuthorisationManagerAndDb(
		        final MemoryAuthorisationManager fastAuthorisationManager,
		        final WritableRepositoryOnPersistence authorisationRepositoryOnPersistence,
		        final PartialAuthorisationDatabaseOnWritableRepository partialAuthorisationDb) {
			super(fastAuthorisationManager);
			this.authorisationRepositoryOnPersistence = authorisationRepositoryOnPersistence;
			this.partialAuthorisationDb = partialAuthorisationDb;
		}

		@Override
		protected void beforeRead() {
			// FIXME check for events in persistence - unfinished impl!
			// List<XEvent> events =
			// this.authorisationRepositoryOnPersistence.getNewEvents();
			// if(events != null) {
			// /* don't let the user perform reads/write while we sync */
			// synchronized(this) {
			// /*
			// * stop listening to XGroupEvetns to avoid persisting events
			// * that are already persisted
			// */
			// this.partialAuthorisationDb.setEventListening(false);
			// /* apply */
			// PartialAuthorisationDatabaseOnWritableRepository.applyEventsTo(events,
			// this
			// .getBaseAuthorisationManager().getAuthorisationDatabase());
			// /* Restart event listening */
			// this.partialAuthorisationDb.setEventListening(true);
			// }
			// }
		}

	}

	/**
	 * Before each read in the fast in-memory index, we check in the underlying
	 * persistence if the data is still fresh and update as necessary.
	 *
	 * @author xamde
	 */
	private class SyncPersistenceToGroupDatabase implements IHookListener {

		private final MemoryGroupDatabase baseDatabase;
		private final WritableModelOnPersistence groupModelOnPersistence;
		private final PartialGroupDatabaseOnWritableModel partialGroupDb;

		public SyncPersistenceToGroupDatabase(final MemoryGroupDatabase fastDatabase,
		        final WritableModelOnPersistence groupModelOnPersistence,
		        final PartialGroupDatabaseOnWritableModel partialGroupDb) {
			this.baseDatabase = fastDatabase;
			this.groupModelOnPersistence = groupModelOnPersistence;
			this.partialGroupDb = partialGroupDb;
		}

		@Override
		public void beforeRead() {
			// check for events in persistence
			final List<XEvent> events = this.groupModelOnPersistence.getNewEvents();
			if(events != null) {
				/* don't let the user perform reads/write while we sync */
				synchronized(this) {
					/*
					 * stop listening to XGroupEvetns to avoid persisting events
					 * that are already persisted
					 */
					this.partialGroupDb.setEventListening(false);
					/* apply */
					PartialGroupDatabaseOnWritableModel.applyEventsTo(events, this.baseDatabase);
					/* Restart event listening */
					this.partialGroupDb.setEventListening(true);
				}
			}
		}

		@Override
		public void beforeWrite() {
		}

	}

	public AccessControlManagerOnPersistence(final XydraPersistence persistence, final XId executingActorId) {
		super();

		final WritableRepositoryOnPersistence repoOnPersistence = new WritableRepositoryOnPersistence(
		        persistence, executingActorId);

		/* ------------ GROUPS -------------- */
		/* fast in-memory, initially empty */
		final MemoryGroupDatabase memoryGroupDatabase = new MemoryGroupDatabase();

		// wrap persistence into groupModel-like API
		repoOnPersistence.createModel(NamingUtils.ID_GROUPS_MODEL);
		final WritableModelOnPersistence groupModelOnPersistence = new WritableModelOnPersistence(
		        persistence, executingActorId, NamingUtils.ID_GROUPS_MODEL);
		final PartialGroupDatabaseOnWritableModel partialGroupDb = new PartialGroupDatabaseOnWritableModel(
		        groupModelOnPersistence);
		/*
		 * Add beforeRead-hook to in-memory DB for sync from persistence to
		 * in-memory DB
		 */
		final SyncPersistenceToGroupDatabase syncPersistenceToGroupDb = new SyncPersistenceToGroupDatabase(
		        memoryGroupDatabase, groupModelOnPersistence, partialGroupDb);
		memoryGroupDatabase.addHookListener(syncPersistenceToGroupDb);

		/* Load groups from persistence into memory */
		partialGroupDb.loadInto(memoryGroupDatabase);
		groupModelOnPersistence.ignoreAllEventsUntilNow();

		/* Listen to changes in groupDb from user and write to persistence */
		memoryGroupDatabase.addListener(partialGroupDb);

		/* ------------ AUTHORISATION -------------- */
		final MemoryAuthorisationManager memoryAuthorisationManager = new MemoryAuthorisationManager(
		        memoryGroupDatabase);
		// TODO this should not be necessary
		memoryAuthorisationManager.grantGroupAllAccessToRepository(XGroupDatabase.ADMINISTRATOR_GROUP_ID,
		        persistence.getRepositoryId());

		// wrap persistence into groupModel-like API
		final PartialAuthorisationDatabaseOnWritableRepository partialAuthorisationDb = new PartialAuthorisationDatabaseOnWritableRepository(
		        repoOnPersistence);
		/*
		 * Add beforeRead-hook to in-memory DB for sync from persistence to
		 * in-memory DB
		 */
		final HookAuthorisationManagerAndDb hookAuthorisationManagerAndDb = new SyncingHookAuthorisationManagerAndDb(
		        memoryAuthorisationManager, repoOnPersistence, partialAuthorisationDb);
		/* Load access right definitions from persistence into memory */
		partialAuthorisationDb.loadInto(hookAuthorisationManagerAndDb);
		// FIXME repoOnPersistence.ignoreAllEventsUntilNow();

		/*
		 * Listen to changes in authorisationDb from user and write to
		 * persistence
		 */
		hookAuthorisationManagerAndDb.addListener(partialAuthorisationDb);

		super.authorisationManager = hookAuthorisationManagerAndDb;

		/* ------------ AUTHENTICATION -------------- */
		/* wrap persistence to RMOF-like API */
		repoOnPersistence.createModel(NamingUtils.ID_AUTHENTICATION_MODEL);
		final XWritableModel authenticationModelOnPersistence = new WritableModelOnPersistence(
		        persistence, executingActorId, NamingUtils.ID_AUTHENTICATION_MODEL);
		/* wrap RMOF in authentication database API */
		final XAuthenticationDatabase authenticationDbOnPersistence = new AuthenticationDatabaseOnWritableModel(
		        authenticationModelOnPersistence);
		/* wrap with an in-memory cache */
		super.authenticationDb = new CachingOrMemoryAuthenticationDatabase(
		        authenticationDbOnPersistence);
	}

}
